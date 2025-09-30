package com.example.backend.coins;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface CoinReadService {
    Flux<Coin> listCoins();
    Mono<Coin> getCoinByGeckoId(String coinGeckoId);

    // Latest-only dev data for a coin
    Mono<CoinDevData> getLatestDevData(String coinGeckoId);
}
