package com.example.backend.coins;

import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.r2dbc.core.Parameter;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

@Repository
public class CoinsUpsertDao {

    private final DatabaseClient db;

    public CoinsUpsertDao(DatabaseClient db) {
        this.db = db;
    }

    /**
     * Idempotent upsert for the coins table keyed by coin_gecko_id.
     * Updates symbol/name/repos when the row already exists.
     */
    public Mono<Void> upsert(
            String coinGeckoId,
            String symbol,
            String name,
            String[] repoUrls
    ) {
        final String sql = """
            INSERT INTO coins (
                coin_gecko_id, symbol, name, coin_gecko_repo_url_github, created_at, updated_at
            ) VALUES (
                :coin_gecko_id, :symbol, :name, :repo_urls, NOW(), NOW()
            )
            ON CONFLICT (coin_gecko_id) DO UPDATE SET
                symbol                    = EXCLUDED.symbol,
                name                      = EXCLUDED.name,
                coin_gecko_repo_url_github= EXCLUDED.coin_gecko_repo_url_github,
                updated_at                = NOW()
            """;

        return db.sql(sql)
                .bind("coin_gecko_id", coinGeckoId)
                .bind("symbol", Parameter.fromOrEmpty(symbol, String.class))
                .bind("name", Parameter.fromOrEmpty(name, String.class))
                // R2DBC postgres can bind String[] as text[] directly
                .bind("repo_urls", Parameter.fromOrEmpty(repoUrls, String[].class))
                .then()
                .then();
    }
}
