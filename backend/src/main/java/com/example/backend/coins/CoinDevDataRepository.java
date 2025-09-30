package com.example.backend.coins;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDate;

public interface CoinDevDataRepository extends ReactiveCrudRepository<CoinDevData, String> {

    // Kept for compatibility (unused in latest-only flow)
    Mono<CoinDevData> findByCoinGeckoIdAndSnapshotDate(String coinGeckoId, LocalDate snapshotDate);

    // Kept for compatibility (unused in latest-only flow)
    Flux<CoinDevData> findTop7ByCoinGeckoIdOrderBySnapshotDateDesc(String coinGeckoId);

    // NEW: latest-only model uses a single row per coin
    Mono<CoinDevData> findByCoinGeckoId(String coinGeckoId);
}
