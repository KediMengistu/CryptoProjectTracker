package com.example.backend.coins.fallback;

import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;

public interface CoinsFallbackRepository extends ReactiveCrudRepository<CoinsFallback, String> {

    @Query("""
           SELECT * FROM coins_fallback
           ORDER BY position ASC, coin_gecko_id ASC
           """)
    Flux<CoinsFallback> findOrdered();
}
