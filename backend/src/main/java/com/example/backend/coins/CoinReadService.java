package com.example.backend.coins;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface CoinReadService {
    Flux<Coin> listCoins();
    Mono<Coin> getCoinByGeckoId(String coinGeckoId);
    Flux<CoinDevData> getDevDataLastDays(String coinGeckoId, int days); // days: 1..7
}
