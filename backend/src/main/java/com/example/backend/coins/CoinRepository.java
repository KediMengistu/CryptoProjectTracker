package com.example.backend.coins;

import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
// import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface CoinRepository extends ReactiveCrudRepository<Coin, String> {
    Mono<Coin> findByCoinGeckoId(String coinGeckoId);

    // case-insensitive symbol lookup using the functional index (you’ll call with lower(...) in @Query if needed)
    // For simple equality:
     @Query("SELECT * FROM coins WHERE LOWER(symbol) = LOWER(:symbol)")
     Mono<Coin> findBySymbolIgnoreCase(String symbol);
}
