package com.example.backend.coins;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/coins")
@RequiredArgsConstructor
public class CoinController {

    private final CoinReadService readService;
    private final CoinIngestionService ingestionService; // optional manual sync

    // List all tracked coins
    @GetMapping
    public Flux<Coin> listCoins() {
        return readService.listCoins();
    }

    // Get one coin by CoinGecko id (e.g., "bitcoin")
    @GetMapping("/{coinGeckoId}")
    public Mono<ResponseEntity<Coin>> getCoin(@PathVariable String coinGeckoId) {
        return readService.getCoinByGeckoId(coinGeckoId)
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    // Latest-only dev data for a coin
    @GetMapping("/{coinGeckoId}/dev-data")
    public Mono<ResponseEntity<CoinDevData>> getLatestDevData(@PathVariable String coinGeckoId) {
        return readService.getLatestDevData(coinGeckoId)
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    // OPTIONAL: manual sync trigger
    @PostMapping("/sync")
    public Mono<ResponseEntity<Void>> syncAllNow() {
        return ingestionService
                .syncAll(java.time.LocalDate.now(java.time.ZoneOffset.UTC))
                .thenReturn(ResponseEntity.accepted().build());
    }
}
