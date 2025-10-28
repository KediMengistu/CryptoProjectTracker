package com.example.backend.coins;

import com.example.backend.coins.dto.CoinGeckoDtos.CoinDetail;
import com.example.backend.coins.dto.CoinGeckoDtos.TrendingResponse;
import com.example.backend.coins.fallback.FallbackRosterService;
import com.example.backend.config.FeedCompositionProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DuplicateKeyException;
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
public class CoinIngestionServiceImpl implements CoinIngestionService {

    private final WebClient coinGeckoWebClient;
    private final CoinRepository coinRepository;
    private final DevDataUpsertDao devUpsertDao;
    private final CoinsUpsertDao coinsUpsertDao;

    private final FallbackRosterService fallbackRosterService;
    private final FeedCompositionProperties feedProps; // 👈 use targetSize here

    @Override
    public Mono<Void> syncAll(LocalDate snapshotDate) {
        final LocalDate day = snapshotDate != null ? snapshotDate : LocalDate.now(ZoneOffset.UTC);
        final int target = Math.max(1, feedProps.getTargetSize());

        // 1) Ingest ONLY the first `target` accepted trending coins
        Mono<List<String>> acceptedTrendingIds =
                fetchTrending()
                        .concatMap(item -> {
                            String id = item.item != null ? item.item.id : null;
                            if (id == null || id.isBlank()) return Mono.empty();

                            return fetchCoinDetail(id)
                                    .filter(detail -> hasAcceptableRepos(detail) && hasMeaningfulDevData(detail))
                                    .flatMap(detail -> {
                                        String[] repos = extractRepos(detail);
                                        String symbol = detail.symbol;
                                        String name   = detail.name;

                                        // Upsert the coin, then its latest dev snapshot, then return the id we kept
                                        return coinsUpsertDao.upsert(id, symbol, name, repos)
                                                .onErrorResume(DuplicateKeyException.class, e -> Mono.empty())
                                                .then(upsertLatestDevSnapshot(id, day, detail))
                                                .thenReturn(id);
                                    });
                        }, 3)
                        .distinct()
                        .take(target)        // 👈 hard cap at 15 (or configured)
                        .collectList();

        // 2) After we’ve recorded the `target` accepted coins, prune everything else
        Mono<Void> prunePrimaryToAccepted =
                acceptedTrendingIds.flatMap(keepIds ->
                        coinRepository.findAll()
                                .filter(c -> !keepIds.contains(c.getCoinGeckoId()))
                                .flatMap(coinRepository::delete) // dev rows cascade-delete via FK
                                .then()
                );

        // 3) Maintain curated fallback tables (this calls CoinGecko but it’s part of the cron/sync path)
        Mono<Void> reconcileFallback = fallbackRosterService.reconcileRoster();
        Mono<Void> refreshFallback   = fallbackRosterService.refreshSnapshots(day);

        return acceptedTrendingIds
                .then(prunePrimaryToAccepted)
                .then(reconcileFallback)
                .then(refreshFallback);
    }

    @Override
    public Mono<Void> syncOne(String coinGeckoId, LocalDate snapshotDate) {
        final LocalDate day = snapshotDate != null ? snapshotDate : LocalDate.now(ZoneOffset.UTC);

        return fetchCoinDetail(coinGeckoId)
                .flatMap(detail -> {
                    if (!hasAcceptableRepos(detail) || !hasMeaningfulDevData(detail)) {
                        return Mono.empty();
                    }
                    String[] repos = extractRepos(detail);
                    String symbol  = detail.symbol;
                    String name    = detail.name;

                    return coinsUpsertDao.upsert(coinGeckoId, symbol, name, repos)
                            .onErrorResume(DuplicateKeyException.class, e -> Mono.empty())
                            .then(upsertLatestDevSnapshot(coinGeckoId, day, detail));
                })
                .then();
    }

    private Flux<TrendingResponse.Coin> fetchTrending() {
        return coinGeckoWebClient.get()
                .uri(uri -> uri.path("/search/trending").build())
                .retrieve()
                .bodyToMono(TrendingResponse.class)
                .flatMapMany(resp -> (resp != null && resp.coins != null)
                        ? Flux.fromIterable(resp.coins)
                        : Flux.empty());
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

    private Mono<Void> upsertLatestDevSnapshot(String coinGeckoId, LocalDate day, CoinDetail detail) {
        var d = detail.developer_data;
        if (d == null) return Mono.empty();

        Integer additions = (d.code_additions_deletions_4_weeks == null)
                ? null : d.code_additions_deletions_4_weeks.additions;
        Integer deletions = (d.code_additions_deletions_4_weeks == null)
                ? null : d.code_additions_deletions_4_weeks.deletions;

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

    private static boolean hasAcceptableRepos(CoinDetail detail) {
        if (detail == null || detail.links == null || detail.links.repos_url == null) return false;
        var gh = detail.links.repos_url.github;
        if (gh == null || gh.isEmpty()) return false;
        return gh.stream()
                .filter(Objects::nonNull)
                .map(String::trim)
                .anyMatch(Predicate.not(String::isBlank));
    }

    private static boolean hasMeaningfulDevData(CoinDetail detail) {
        if (detail == null || detail.developer_data == null) return false;
        var d = detail.developer_data;
        Integer[] ints = new Integer[]{
                d.forks, d.stars, d.subscribers, d.total_issues, d.closed_issues,
                d.pull_requests_merged, d.pull_request_contributors, d.commit_count_4_weeks
        };
        for (Integer i : ints) {
            if (i != null && i > 0) return true;
        }
        if (d.code_additions_deletions_4_weeks != null) {
            Integer add = d.code_additions_deletions_4_weeks.additions;
            Integer del = d.code_additions_deletions_4_weeks.deletions;
            if ((add != null && add != 0) || (del != null && del != 0)) return true;
        }
        return false;
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
}
