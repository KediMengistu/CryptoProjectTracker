package com.example.backend.coins.feed;

import com.example.backend.coins.*;
import com.example.backend.coins.fallback.*;
import com.example.backend.config.FeedCompositionProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.*;
import java.util.*;

@Service
@RequiredArgsConstructor
public class FeedComposerService {

    // 🔄 Removed WebClient/CoinGecko usage entirely
    private final CoinRepository coinRepository;
    private final CoinDevDataRepository devRepo;

    private final CoinsFallbackRepository fallbackRepo;
    private final CoinDevDataFallbackRepository fallbackDevRepo;

    private final FeedCompositionProperties props;

    public Flux<ComposedCoinView> compose() {
        final int target = Math.max(1, props.getTargetSize());

        // 1) Take up to N primaries that already exist in DB (latest first)
        return acceptedPrimaries(target)
                .collectList()
                .flatMapMany(accepted -> {
                    if (accepted.size() >= target) {
                        return Flux.fromIterable(accepted);
                    }
                    // 2) Fill the remainder from curated fallback, strictly from DB
                    final Set<String> seen = new LinkedHashSet<>();
                    for (ComposedCoinView v : accepted) seen.add(v.coinGeckoId);

                    final int remaining = target - accepted.size();
                    return fallbackFill(seen, remaining)
                            .map(fill -> {
                                List<ComposedCoinView> out = new ArrayList<>(accepted);
                                out.addAll(fill);
                                return out;
                            })
                            .flatMapMany(Flux::fromIterable);
                });
    }

    /**
     * Primary “accepted” coins come from coin_dev_data (latest snapshots first),
     * joined with coins for symbol/name/repos. No external calls here.
     */
    private Flux<ComposedCoinView> acceptedPrimaries(int limit) {
        return devRepo.findAll()
                // in-memory sort is fine here (we keep this table ≤ 15 via the cron)
                .sort((a, b) -> {
                    // Prefer newest snapshot_date, then newest updated_at
                    final LocalDate da = a.getSnapshotDate() != null ? a.getSnapshotDate() : LocalDate.MIN;
                    final LocalDate db = b.getSnapshotDate() != null ? b.getSnapshotDate() : LocalDate.MIN;
                    int byDay = db.compareTo(da);
                    if (byDay != 0) return byDay;

                    final Instant ua = a.getUpdatedAt();
                    final Instant ub = b.getUpdatedAt();
                    if (ua != null && ub != null) return ub.compareTo(ua);
                    if (ub != null) return 1;
                    if (ua != null) return -1;
                    return 0;
                })
                .flatMapSequential(dev ->
                                coinRepository.findByCoinGeckoId(dev.getCoinGeckoId())
                                        .map(coin -> toViewFromPrimary(coin, dev, Source.TRENDING, false)),
                        4
                )
                .take(limit);
    }

    /** Fallback fill uses the first N entries by position — from DB only. */
    private Mono<List<ComposedCoinView>> fallbackFill(Set<String> seen, int remaining) {
        if (remaining <= 0) return Mono.just(List.of());

        final Duration maxStale = Duration.ofHours(props.getMaxFallbackStalenessHours());
        final Instant now = Instant.now();

        return fallbackRepo.findOrdered()
                .filter(entry -> !seen.contains(entry.getCoinGeckoId()))
                .flatMapSequential(entry ->
                                fallbackDevRepo.findByCoinGeckoId(entry.getCoinGeckoId())
                                        // ensure we still emit a row even if there is no dev snapshot yet
                                        .defaultIfEmpty(null)
                                        .map(dev -> {
                                            boolean stale = true;
                                            if (dev != null && dev.getSnapshotDate() != null) {
                                                var zdt = dev.getSnapshotDate().atStartOfDay(ZoneOffset.UTC);
                                                stale = Duration.between(zdt.toInstant(), now).compareTo(maxStale) > 0;
                                            }
                                            return toViewFromFallback(entry, dev, stale);
                                        }),
                        2
                )
                .take(remaining)
                .collectList();
    }

    private static ComposedCoinView toViewFromPrimary(Coin coin, CoinDevData dev, Source src, boolean stale) {
        ComposedCoinView v = new ComposedCoinView();
        v.coinGeckoId = coin.getCoinGeckoId();
        v.symbol = coin.getSymbol();
        v.name = coin.getName();
        v.repoUrls = coin.getCoinGeckoRepoUrlGithub();

        v.forks = dev.getForks();
        v.stars = dev.getStars();
        v.subscribers = dev.getSubscribers();
        v.totalIssues = dev.getTotalIssues();
        v.closedIssues = dev.getClosedIssues();
        v.pullRequestsMerged = dev.getPullRequestsMerged();
        v.pullRequestContributors = dev.getPullRequestContributors();
        v.codeAdditions4w = dev.getCodeAdditions4w();
        v.codeDeletions4w = dev.getCodeDeletions4w();
        v.commitCount4w = dev.getCommitCount4w();
        v.snapshotDate = dev.getSnapshotDate();

        v.source = src;     // keep using TRENDING to denote “primary”
        v.stale = stale;
        return v;
    }

    private static ComposedCoinView toViewFromFallback(CoinsFallback coin, CoinDevDataFallback dev, boolean stale) {
        ComposedCoinView v = new ComposedCoinView();
        v.coinGeckoId = coin.getCoinGeckoId();
        v.symbol = coin.getSymbol();
        v.name = coin.getName();
        v.repoUrls = coin.getRepoUrls();

        if (dev != null) {
            v.forks = dev.getForks();
            v.stars = dev.getStars();
            v.subscribers = dev.getSubscribers();
            v.totalIssues = dev.getTotalIssues();
            v.closedIssues = dev.getClosedIssues();
            v.pullRequestsMerged = dev.getPullRequestsMerged();
            v.pullRequestContributors = dev.getPullRequestContributors();
            v.codeAdditions4w = dev.getCodeAdditions4w();
            v.codeDeletions4w = dev.getCodeDeletions4w();
            v.commitCount4w = dev.getCommitCount4w();
            v.snapshotDate = dev.getSnapshotDate();
        } else {
            v.stale = true; // no data yet
        }

        v.source = Source.FALLBACK;
        v.stale = stale || v.stale;
        return v;
    }
}
