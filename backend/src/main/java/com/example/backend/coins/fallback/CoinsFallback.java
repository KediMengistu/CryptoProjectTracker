package com.example.backend.coins.fallback;

import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table("coins_fallback")
public class CoinsFallback {

    @Id
    @Column("coin_gecko_id")
    private String coinGeckoId; // PK

    private String symbol;
    private String name;

    @Column("repo_urls")
    private String[] repoUrls;

    /** 1-based position in your configured list (used to pick "first N"). */
    private Integer position;

    @CreatedDate
    @Column("created_at")
    private Instant createdAt;

    @LastModifiedDate
    @Column("updated_at")
    private Instant updatedAt;
}
