package com.example.backend.coins;

import reactor.core.publisher.Mono;

import java.time.LocalDate;

public interface CoinIngestionService {

    /**
     * Upsert coins from /coins/list, then for each tracked coin:
     * - fetch /coins/{id} (developer_data + repos)
     * - upsert today's dev snapshot
     * - prune to last 7 days per coin
     */
    Mono<Void> syncAll(LocalDate snapshotDate);

    /** Upsert a single coin’s snapshot (by CoinGecko id) for provided date. */
    Mono<Void> syncOne(String coinGeckoId, LocalDate snapshotDate);
}
