package com.example.backend.coins;

import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.r2dbc.core.Parameter;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

import java.time.LocalDate;

@Repository
public class DevDataUpsertDao {

    private final DatabaseClient db;

    public DevDataUpsertDao(DatabaseClient db) {
        this.db = db;
    }

    public Mono<Void> upsertLatest(
            String coinGeckoId,
            LocalDate snapshotDate,
            Integer forks,
            Integer stars,
            Integer subscribers,
            Integer totalIssues,
            Integer closedIssues,
            Integer pullRequestsMerged,
            Integer pullRequestContributors,
            Integer codeAdditions4w,
            Integer codeDeletions4w,
            Integer commitCount4w
    ) {
        final String sql = """
            INSERT INTO coin_dev_data (
                coin_gecko_id, snapshot_date,
                forks, stars, subscribers, total_issues, closed_issues,
                pull_requests_merged, pull_request_contributors,
                code_additions_4w, code_deletions_4w, commit_count_4w,
                created_at, updated_at
            ) VALUES (
                :coin_gecko_id, :snapshot_date,
                :forks, :stars, :subscribers, :total_issues, :closed_issues,
                :pull_requests_merged, :pull_request_contributors,
                :code_additions_4w, :code_deletions_4w, :commit_count_4w,
                NOW(), NOW()
            )
            ON CONFLICT (coin_gecko_id) DO UPDATE SET
                snapshot_date = EXCLUDED.snapshot_date,
                forks = EXCLUDED.forks,
                stars = EXCLUDED.stars,
                subscribers = EXCLUDED.subscribers,
                total_issues = EXCLUDED.total_issues,
                closed_issues = EXCLUDED.closed_issues,
                pull_requests_merged = EXCLUDED.pull_requests_merged,
                pull_request_contributors = EXCLUDED.pull_request_contributors,
                code_additions_4w = EXCLUDED.code_additions_4w,
                code_deletions_4w = EXCLUDED.code_deletions_4w,
                commit_count_4w = EXCLUDED.commit_count_4w,
                updated_at = NOW()
            """;

        return db.sql(sql)
                .bind("coin_gecko_id", coinGeckoId)
                .bind("snapshot_date", snapshotDate)
                .bind("forks", Parameter.fromOrEmpty(forks, Integer.class))
                .bind("stars", Parameter.fromOrEmpty(stars, Integer.class))
                .bind("subscribers", Parameter.fromOrEmpty(subscribers, Integer.class))
                .bind("total_issues", Parameter.fromOrEmpty(totalIssues, Integer.class))
                .bind("closed_issues", Parameter.fromOrEmpty(closedIssues, Integer.class))
                .bind("pull_requests_merged", Parameter.fromOrEmpty(pullRequestsMerged, Integer.class))
                .bind("pull_request_contributors", Parameter.fromOrEmpty(pullRequestContributors, Integer.class))
                .bind("code_additions_4w", Parameter.fromOrEmpty(codeAdditions4w, Integer.class))
                .bind("code_deletions_4w", Parameter.fromOrEmpty(codeDeletions4w, Integer.class))
                .bind("commit_count_4w", Parameter.fromOrEmpty(commitCount4w, Integer.class))
                .then()
                .then();
    }
}
