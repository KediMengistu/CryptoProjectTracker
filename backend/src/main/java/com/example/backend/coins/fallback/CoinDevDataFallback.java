package com.example.backend.coins.fallback;

import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.Instant;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table("coin_dev_data_fallback")
public class CoinDevDataFallback {

    @Id
    private String id;

    @Column("coin_gecko_id")
    private String coinGeckoId;

    @Column("snapshot_date")
    private LocalDate snapshotDate;

    private Integer forks;
    private Integer stars;
    private Integer subscribers;
    @Column("total_issues")
    private Integer totalIssues;
    @Column("closed_issues")
    private Integer closedIssues;
    @Column("pull_requests_merged")
    private Integer pullRequestsMerged;
    @Column("pull_request_contributors")
    private Integer pullRequestContributors;
    @Column("code_additions_4w")
    private Integer codeAdditions4w;
    @Column("code_deletions_4w")
    private Integer codeDeletions4w;
    @Column("commit_count_4w")
    private Integer commitCount4w;

    @CreatedDate
    @Column("created_at")
    private Instant createdAt;

    @LastModifiedDate
    @Column("updated_at")
    private Instant updatedAt;
}
