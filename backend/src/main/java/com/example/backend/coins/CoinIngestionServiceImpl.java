package com.example.backend.coins;

import com.example.backend.coins.dto.CoinGeckoDtos.CoinDetail;
import com.example.backend.coins.dto.CoinGeckoDtos.CoinListItem;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CoinIngestionServiceImpl implements CoinIngestionService {

    private final WebClient coinGeckoWebClient;
    private final CoinRepository coinRepository;
    private final CoinDevDataRepository devRepo;

    @Override
    public Mono<Void> syncAll(LocalDate snapshotDate) {
        LocalDate day = snapshotDate != null ? snapshotDate : LocalDate.now(ZoneOffset.UTC);

        // 1) pull the lightweight /coins/list (id, symbol, name)
        return fetchCoinList()
                .flatMapSequential(item -> upsertCoinFromListItem(item)) // upsert coins
                // 2) for each tracked coin, fetch details + dev data and upsert that day's snapshot
                .thenMany(coinRepository.findAll())
                .flatMapSequential(coin ->
                                syncOne(coin.getCoinGeckoId(), day)
                                        // small delay to be polite to the API (adjust as needed)
                                        .then(Mono.delay(java.time.Duration.ofMillis(120)).then())
                        , /*concurrency*/ 3) // throttle
                .then();
    }

    @Override
    public Mono<Void> syncOne(String coinGeckoId, LocalDate snapshotDate) {
        LocalDate day = snapshotDate != null ? snapshotDate : LocalDate.now(ZoneOffset.UTC);
        return coinRepository.findByCoinGeckoId(coinGeckoId)
                .switchIfEmpty(Mono.defer(() -> Mono.error(new IllegalArgumentException(
                        "Coin must exist before syncing dev data: " + coinGeckoId))))
                .flatMap(coin ->
                        fetchCoinDetail(coinGeckoId)
                                .flatMap(detail -> {
                                    // Update GitHub repo URLs if present
                                    List<String> gh = detail.links != null && detail.links.repos_url != null
                                            ? detail.links.repos_url.github : null;
                                    if (gh != null) {
                                        coin.setCoinGeckoRepoUrlGithub(gh.stream()
                                                .filter(Objects::nonNull)
                                                .map(String::trim)
                                                .filter(s -> !s.isBlank())
                                                .distinct()
                                                .toArray(String[]::new));
                                    }
                                    return coinRepository.save(coin)
                                            .then(upsertDevSnapshot(coinGeckoId, day, detail));
                                })
                )
                .then(pruneToLast7Days(coinGeckoId));
    }

    // ---------- helpers ----------

    private Flux<CoinListItem> fetchCoinList() {
        return coinGeckoWebClient.get()
                .uri(uri -> uri.path("/coins/list")
                        .queryParam("include_platform", false) // keep small
                        .build())
                .retrieve()
                .bodyToFlux(CoinListItem.class);
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

    private Mono<Coin> upsertCoinFromListItem(CoinListItem item) {
        return coinRepository.findByCoinGeckoId(item.id())
                .flatMap(existing -> {
                    boolean changed = false;
                    if (!Objects.equals(existing.getSymbol(), item.symbol())) { existing.setSymbol(item.symbol()); changed = true; }
                    if (!Objects.equals(existing.getName(), item.name())) { existing.setName(item.name()); changed = true; }
                    return changed ? coinRepository.save(existing) : Mono.just(existing);
                })
                .switchIfEmpty(Mono.defer(() -> {
                    var coin = Coin.builder()
                            .id(UUID.randomUUID().toString())          // IDs set in service (your choice)
                            .coinGeckoId(item.id())
                            .symbol(item.symbol())
                            .name(item.name())
                            .build();
                    return coinRepository.save(coin);
                }));
    }

    private Mono<Void> upsertDevSnapshot(String coinGeckoId, LocalDate day, CoinDetail detail) {
        var d = detail.developer_data;
        if (d == null) return Mono.empty();

        return devRepo.findByCoinGeckoIdAndSnapshotDate(coinGeckoId, day)
                .flatMap(existing -> {
                    existing.setForks(d.forks);
                    existing.setStars(d.stars);
                    existing.setSubscribers(d.subscribers);
                    existing.setTotalIssues(d.total_issues);
                    existing.setClosedIssues(d.closed_issues);
                    existing.setPullRequestsMerged(d.pull_requests_merged);
                    existing.setPullRequestContributors(d.pull_request_contributors);
                    if (d.code_additions_deletions_4_weeks != null) {
                        existing.setCodeAdditions4w(d.code_additions_deletions_4_weeks.additions);
                        existing.setCodeDeletions4w(d.code_additions_deletions_4_weeks.deletions);
                    }
                    existing.setCommitCount4w(d.commit_count_4_weeks);
                    return devRepo.save(existing).then();
                })
                .switchIfEmpty(Mono.defer(() -> {
                    var row = CoinDevData.builder()
                            .id(UUID.randomUUID().toString())
                            .coinGeckoId(coinGeckoId)
                            .snapshotDate(day)
                            .forks(d.forks)
                            .stars(d.stars)
                            .subscribers(d.subscribers)
                            .totalIssues(d.total_issues)
                            .closedIssues(d.closed_issues)
                            .pullRequestsMerged(d.pull_requests_merged)
                            .pullRequestContributors(d.pull_request_contributors)
                            .codeAdditions4w(d.code_additions_deletions_4_weeks == null ? null : d.code_additions_deletions_4_weeks.additions)
                            .codeDeletions4w(d.code_additions_deletions_4_weeks == null ? null : d.code_additions_deletions_4_weeks.deletions)
                            .commitCount4w(d.commit_count_4_weeks)
                            .build();
                    return devRepo.save(row).then();
                }));
    }

    /** Keep only the most recent 7 snapshot_date rows for this coin. */
    private Mono<Void> pruneToLast7Days(String coinGeckoId) {
        // Simpler approach: fetch top 7 to learn the cutoff, then delete older.
        return devRepo.findTop7ByCoinGeckoIdOrderBySnapshotDateDesc(coinGeckoId)
                .collectList()
                .flatMap(list -> {
                    if (list.isEmpty()) return Mono.empty();
                    var minKept = list.get(list.size() - 1).getSnapshotDate(); // the 7th newest
                    // delete anything older than minKept
                    // With ReactiveCrudRepository we don’t have a built-in deleteWhere; do a custom query in a small repo extension
                    return deleteOlderThan(coinGeckoId, minKept);
                });
    }

    private Mono<Void> deleteOlderThan(String coinGeckoId, java.time.LocalDate minKeptInclusive) {
        // Easiest: load those older and deleteAll (small data). If you expect large data, add a @Query delete.
        return devRepo.findAll()
                .filter(row -> row.getCoinGeckoId().equals(coinGeckoId))
                .filter(row -> row.getSnapshotDate().isBefore(minKeptInclusive))
                .collectList()
                .flatMap(devRepo::deleteAll);
    }
}
