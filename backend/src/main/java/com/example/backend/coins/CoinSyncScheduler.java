// src/main/java/com/example/backend/coins/CoinSyncScheduler.java
package com.example.backend.coins;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@Profile("!prod") // <-- disables this bean when SPRING_PROFILES_ACTIVE=prod
@RequiredArgsConstructor
public class CoinSyncScheduler {

    private final CoinIngestionService ingestionService;

    // Run hourly, 5 minutes after the hour, Toronto time
    @Scheduled(cron = "0 5 * * * *", zone = "America/Toronto")
    public void runHourlyTrendingSync() {
        var todayUtc = java.time.LocalDate.now(java.time.ZoneOffset.UTC);
        ingestionService.syncAll(todayUtc)
                .doOnError(e -> System.err.println("Hourly trending sync failed: " + e.getMessage()))
                .subscribe();
    }
}
