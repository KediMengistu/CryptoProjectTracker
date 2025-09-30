package com.example.backend.coins.fallback;

import com.example.backend.coins.dto.CoinGeckoDtos.CoinDetail;
import com.example.backend.config.FallbackRosterProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.*;
import java.util.function.Predicate;

@Service
@RequiredArgsConstructor
public class FallbackRosterService {

    private final CoinsFallbackRepository rosterRepo;      // keep for reads & prune
    private final CoinsFallbackUpsertDao coinsUpsertDao;   // <-- use DAO for idempotent writes
    private final FallbackDevDataUpsertDao devUpsertDao;   // already idempotent for snapshots
    private final WebClient coinGeckoWebClient;
    private final FallbackRosterProperties props;

    public Mono<Void> reconcileRoster() {
        final List<String> ids = props.getIds() == null ? List.of() : props.getIds();
        if (ids.isEmpty()) {
            // Nothing configured: optionally skip prune to avoid wiping table on misconfig
            return Mono.empty();
        }

        final Map<String, Integer> positionById = new LinkedHashMap<>();
        for (int i = 0; i < ids.size(); i++) positionById.put(ids.get(i), i + 1);

        // 1) Upsert every configured id with latest symbol/name/repos (no UPDATE-by-id races)
        Mono<Void> upserts = Flux.fromIterable(ids)
                .flatMapSequential(id ->
                        fetchCoinDetail(id).flatMap(detail -> {
                            String[] repos = extractRepos(detail);
                            if (repos == null || repos.length == 0) {
                                // Don’t store a fallback coin without repos
                                return Mono.empty();
                            }
                            return coinsUpsertDao.upsert(
                                    id,
                                    detail.symbol,
                                    detail.name,
                                    repos,
                                    positionById.get(id)
                            );
                        }), 3)
                .then();

        // 2) Optionally prune anything not in the configured set (after upserts complete)
        Mono<Void> prune = Mono.empty();
        if (props.isPruneExtraneous()) {
            Set<String> idSet = new HashSet<>(ids);
            prune = rosterRepo.findAll()
                    .filter(row -> !idSet.contains(row.getCoinGeckoId()))
                    .flatMap(rosterRepo::delete)
                    .then();
        }

        return upserts.then(prune);
    }

    public Mono<Void> refreshSnapshots(LocalDate snapshotDate) {
        LocalDate day = snapshotDate != null ? snapshotDate : LocalDate.now(ZoneOffset.UTC);
        return rosterRepo.findOrdered()
                .flatMapSequential(entry ->
                                fetchCoinDetail(entry.getCoinGeckoId())
                                        .flatMap(detail -> upsertFallbackDev(entry.getCoinGeckoId(), day, detail))
                        , 2)
                .then();
    }

    private Mono<CoinDetail> fetchCoinDetail(String coinGeckoId) {
        return coinGeckoWebClient.get()
                .uri(uri -> uri.path("/coins/{id}")
                        .queryParam("localization", false)
                        .queryParam("tickers", false)
                        .queryParam("market_data", false)
                        .queryParam("community_data", false)
                        .queryParam("developer_data", true)
                        .queryParam("sparkline", false)
                        .build(coinGeckoId))
                .retrieve()
                .bodyToMono(CoinDetail.class);
    }

    private static String[] extractRepos(CoinDetail detail) {
        if (detail == null || detail.links == null || detail.links.repos_url == null) return null;
        var gh = detail.links.repos_url.github;
        if (gh == null) return null;
        return gh.stream()
                .filter(Objects::nonNull)
                .map(String::trim)
                .filter(Predicate.not(String::isBlank))
                .distinct()
                .toArray(String[]::new);
    }

    private Mono<Void> upsertFallbackDev(String coinGeckoId, LocalDate day, CoinDetail detail) {
        var d = detail.developer_data;
        if (d == null) return Mono.empty();

        Integer additions = (d.code_additions_deletions_4_weeks == null) ? null : d.code_additions_deletions_4_weeks.additions;
        Integer deletions = (d.code_additions_deletions_4_weeks == null) ? null : d.code_additions_deletions_4_weeks.deletions;

        return devUpsertDao.upsertLatest(
                coinGeckoId,
                day,
                d.forks,
                d.stars,
                d.subscribers,
                d.total_issues,
                d.closed_issues,
                d.pull_requests_merged,
                d.pull_request_contributors,
                additions,
                deletions,
                d.commit_count_4_weeks
        );
    }
}
