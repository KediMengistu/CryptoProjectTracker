package com.example.backend.coins.feed;

import com.example.backend.coins.*;
import com.example.backend.coins.fallback.*;
import com.example.backend.config.FeedCompositionProperties;
import com.example.backend.coins.dto.CoinGeckoDtos.TrendingResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.*;
import java.util.*;
import java.util.function.Predicate;

@Service
@RequiredArgsConstructor
public class FeedComposerService {

    private final WebClient coinGeckoWebClient;
    private final CoinRepository coinRepository;
    private final CoinDevDataRepository devRepo;

    private final CoinsFallbackRepository fallbackRepo;
    private final CoinDevDataFallbackRepository fallbackDevRepo;

    private final FeedCompositionProperties props;

    public Flux<ComposedCoinView> compose() {
        int target = Math.max(1, props.getTargetSize());
        return trendingAccepted()
                .collectList()
                .flatMapMany(accepted -> {
                    if (accepted.size() >= target) {
                        return Flux.fromIterable(accepted.subList(0, target));
                    }
                    Set<String> seen = new LinkedHashSet<>();
                    accepted.forEach(v -> seen.add(v.coinGeckoId));

                    int remaining = target - accepted.size();
                    return fallbackFill(seen, remaining)
                            .map(list -> {
                                List<ComposedCoinView> out = new ArrayList<>(accepted);
                                out.addAll(list);
                                return out;
                            })
                            .flatMapMany(Flux::fromIterable);
                });
    }

    /** Trending coins that are already accepted in DB (exist in coins & have dev snapshot). */
    private Flux<ComposedCoinView> trendingAccepted() {
        return fetchTrendingIds()
                .flatMapSequential(id ->
                                coinRepository.findByCoinGeckoId(id)
                                        .flatMap(coin ->
                                                devRepo.findByCoinGeckoId(id)
                                                        .map(dev -> toViewFromPrimary(coin, dev, Source.TRENDING, false))
                                        )
                        , 4);
    }

    /** Fallback fill uses the first N entries by position. */
    private Mono<List<ComposedCoinView>> fallbackFill(Set<String> seen, int remaining) {
        if (remaining <= 0) return Mono.just(List.of());

        var maxStale = Duration.ofHours(props.getMaxFallbackStalenessHours());
        Instant now = Instant.now();

        return fallbackRepo.findOrdered()
                .filter(entry -> !seen.contains(entry.getCoinGeckoId()))
                .flatMapSequential(entry ->
                                fallbackDevRepo.findByCoinGeckoId(entry.getCoinGeckoId())
                                        .map(dev -> {
                                            boolean stale = true;
                                            if (dev != null && dev.getSnapshotDate() != null) {
                                                var zdt = dev.getSnapshotDate().atStartOfDay(ZoneOffset.UTC);
                                                stale = Duration.between(zdt.toInstant(), now).compareTo(maxStale) > 0;
                                            }
                                            return toViewFromFallback(entry, dev, stale);
                                        })
                        , 2)
                .take(remaining)
                .collectList();
    }

    private Flux<String> fetchTrendingIds() {
        return coinGeckoWebClient.get()
                .uri(uri -> uri.path("/search/trending").build())
                .retrieve()
                .bodyToMono(TrendingResponse.class)
                .flatMapMany(resp -> resp != null && resp.coins != null
                        ? Flux.fromIterable(resp.coins)
                        : Flux.empty())
                .map(c -> c.item != null ? c.item.id : null)
                .filter(Objects::nonNull)
                .filter(Predicate.not(String::isBlank));
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

        v.source = src;
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
