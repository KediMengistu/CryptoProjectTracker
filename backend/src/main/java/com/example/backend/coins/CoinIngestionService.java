package com.example.backend.coins;

import reactor.core.publisher.Mono;

import java.time.LocalDate;

public interface CoinIngestionService {

    /**
     * Ingest/refresh the tracked set, then for each tracked coin:
     * - fetch /coins/{id} (developer_data + repos)
     * - upsert the latest dev snapshot (single row per coin; snapshot_date = "as-of" date)
     */
    Mono<Void> syncAll(LocalDate snapshotDate);

    /** Upsert a single coin’s latest snapshot (by CoinGecko id) for provided date. */
    Mono<Void> syncOne(String coinGeckoId, LocalDate snapshotDate);
}
