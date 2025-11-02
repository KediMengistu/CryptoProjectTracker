package com.example.backend.coins.feed;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.LocalDate;

/**
 * Lightweight DTO returned by /api/coins/feed.
 * Fields are public to Jackson via @JsonAutoDetect.
 */
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ComposedCoinView {

    // Identity & metadata
    String coinGeckoId;
    String symbol;
    String name;
    String[] repoUrls;   // <-- changed to array to match DB entities

    // Developer metrics (nullable = not available)
    Integer forks;
    Integer stars;
    Integer subscribers;
    Integer totalIssues;
    Integer closedIssues;
    Integer pullRequestsMerged;
    Integer pullRequestContributors;
    Integer codeAdditions4w;
    Integer codeDeletions4w;
    Integer commitCount4w;

    // Snapshot date (ISO-8601, e.g. "2025-11-02")
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    LocalDate snapshotDate;

    // Where this row came from in the feed composition
    Source source; // TRENDING (primary) or FALLBACK

    // Whether the data is considered stale
    boolean stale;
}
