package com.example.backend.coins;

import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.ZoneOffset;

@Component
@EnableScheduling
@RequiredArgsConstructor
public class CoinSyncScheduler {

    private final CoinIngestionService ingestionService;

    @Scheduled(cron = "0 59 23 * * *", zone = "America/Toronto")
    public void runDailySync() {
        var todayUtc = LocalDate.now(ZoneOffset.UTC);
        ingestionService.syncAll(todayUtc)
                .doOnError(e -> System.err.println("Daily sync failed: " + e.getMessage()))
                .subscribe();
    }
}
