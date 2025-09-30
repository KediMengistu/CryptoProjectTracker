package com.example.backend.coins.fallback;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Mono;

public interface CoinDevDataFallbackRepository extends ReactiveCrudRepository<CoinDevDataFallback, String> {

    Mono<CoinDevDataFallback> findByCoinGeckoId(String coinGeckoId);
}
