package com.example.backend.coins;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import java.time.LocalDate;
import java.time.ZoneOffset;
import org.springframework.scheduling.annotation.Scheduled;

@Component
@RequiredArgsConstructor
public class CoinSyncScheduler {

    private final CoinIngestionService ingestionService;

    // Run hourly, 5 minutes after the hour, Toronto time
    @Scheduled(cron = "0 5 * * * *", zone = "America/Toronto")
    public void runHourlyTrendingSync() {
        var todayUtc = LocalDate.now(ZoneOffset.UTC);
        ingestionService.syncAll(todayUtc)
                .doOnError(e -> System.err.println("Hourly trending sync failed: " + e.getMessage()))
                .subscribe();
    }
}
