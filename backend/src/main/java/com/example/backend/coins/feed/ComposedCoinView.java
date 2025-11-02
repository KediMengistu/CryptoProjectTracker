package com.example.backend.coins.feed;

import com.example.backend.coins.feed.Source;
import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDate;
import java.util.List;

/**
 * Lightweight DTO returned by /api/coins/feed.
 *
 * Note the @JsonAutoDetect configuration: we assign directly to fields in the service,
 * so we expose fields to Jackson without requiring getters/setters.
 */
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ComposedCoinView {

    // Identity & metadata
    String coinGeckoId;
    String symbol;
    String name;
    List<String> repoUrls;

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

    // Snapshot date (serialized as ISO-8601 string, e.g. "2025-11-02")
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    LocalDate snapshotDate;

    // Where this row came from in the feed composition
    Source source; // TRENDING (primary) or FALLBACK

    // Whether the data is considered stale (e.g., fallback older than allowed window)
    boolean stale;
}
