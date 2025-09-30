package com.example.backend.coins;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class CoinReadServiceImpl implements CoinReadService {

    private final CoinRepository coinRepository;
    private final CoinDevDataRepository devRepo;

    @Override
    public Flux<Coin> listCoins() {
        return coinRepository.findAll();
    }

    @Override
    public Mono<Coin> getCoinByGeckoId(String coinGeckoId) {
        return coinRepository.findByCoinGeckoId(coinGeckoId);
    }

    @Override
    public Mono<CoinDevData> getLatestDevData(String coinGeckoId) {
        // In the latest-only model, there is at most one row per coin_gecko_id.
        return devRepo.findByCoinGeckoId(coinGeckoId);
    }
}
