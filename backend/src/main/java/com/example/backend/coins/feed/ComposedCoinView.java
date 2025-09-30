package com.example.backend.coins.feed;

import java.time.LocalDate;

public class ComposedCoinView {
    public String   coinGeckoId;
    public String   symbol;
    public String   name;
    public String[] repoUrls;

    // Developer metrics (nullable if truly missing, but for our feed they’ll be present)
    public Integer forks;
    public Integer stars;
    public Integer subscribers;
    public Integer totalIssues;
    public Integer closedIssues;
    public Integer pullRequestsMerged;
    public Integer pullRequestContributors;
    public Integer codeAdditions4w;
    public Integer codeDeletions4w;
    public Integer commitCount4w;

    public LocalDate snapshotDate;

    public Source source;   // TRENDING or FALLBACK
    public boolean stale;   // true if fallback snapshot is older than configured staleness

    public ComposedCoinView() {}
}
