package com.example.backend.coins.fallback;

import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.r2dbc.core.Parameter;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

@Repository
public class CoinsFallbackUpsertDao {

    private final DatabaseClient db;

    public CoinsFallbackUpsertDao(DatabaseClient db) {
        this.db = db;
    }

    /**
     * Idempotent upsert into coins_fallback keyed by coin_gecko_id.
     * Updates symbol/name/repo_urls/position when the row already exists.
     */
    public Mono<Void> upsert(
            String coinGeckoId,
            String symbol,
            String name,
            String[] repoUrls,
            Integer position
    ) {
        final String sql = """
            INSERT INTO coins_fallback (
                coin_gecko_id, symbol, name, repo_urls, position, created_at, updated_at
            ) VALUES (
                :coin_gecko_id, :symbol, :name, :repo_urls, :position, NOW(), NOW()
            )
            ON CONFLICT (coin_gecko_id) DO UPDATE SET
                symbol     = EXCLUDED.symbol,
                name       = EXCLUDED.name,
                repo_urls  = EXCLUDED.repo_urls,
                position   = EXCLUDED.position,
                updated_at = NOW()
            """;

        return db.sql(sql)
                .bind("coin_gecko_id", coinGeckoId)
                .bind("symbol", Parameter.fromOrEmpty(symbol, String.class))
                .bind("name", Parameter.fromOrEmpty(name, String.class))
                .bind("repo_urls", Parameter.fromOrEmpty(repoUrls, String[].class))
                .bind("position", position)
                .then()
                .then();
    }
}
