package com.example.backend.coins;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDate;

public interface CoinDevDataRepository extends ReactiveCrudRepository<CoinDevData, String> {

    Mono<CoinDevData> findByCoinGeckoIdAndSnapshotDate(String coinGeckoId, LocalDate snapshotDate);

    Flux<CoinDevData> findTop7ByCoinGeckoIdOrderBySnapshotDateDesc(String coinGeckoId);

    // For pruning, you’ll typically write a custom query in a service (select all dates > top 7 and delete).
}
