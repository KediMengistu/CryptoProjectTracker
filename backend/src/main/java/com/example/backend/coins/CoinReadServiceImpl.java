package com.example.backend.coins;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Comparator;

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
    public Flux<CoinDevData> getDevDataLastDays(String coinGeckoId, int days) {
        int n = Math.max(1, Math.min(7, days));
        // We already have a “top 7 desc” finder; trim to n and return ascending for charts.
        return devRepo.findTop7ByCoinGeckoIdOrderBySnapshotDateDesc(coinGeckoId)
                .take(n)
                .sort(Comparator.comparing(CoinDevData::getSnapshotDate)); // ascending
    }
}
