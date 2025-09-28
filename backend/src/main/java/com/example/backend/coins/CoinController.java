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
    private final CoinIngestionService ingestionService; // optional – for a manual sync endpoint

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

    // Get up to the last N (<=7) dev snapshots for a coin (ascending by date)
    @GetMapping("/{coinGeckoId}/dev-data")
    public Flux<CoinDevData> getDevData(
            @PathVariable String coinGeckoId,
            @RequestParam(name = "days", defaultValue = "7") int days) {
        return readService.getDevDataLastDays(coinGeckoId, days);
    }

    // OPTIONAL: manual sync trigger (use carefully; consider auth/rate-limits)
    @PostMapping("/sync")
    public Mono<ResponseEntity<Void>> syncAllNow() {
        return ingestionService
                .syncAll(java.time.LocalDate.now(java.time.ZoneOffset.UTC))
                .thenReturn(ResponseEntity.accepted().build());
    }
}
