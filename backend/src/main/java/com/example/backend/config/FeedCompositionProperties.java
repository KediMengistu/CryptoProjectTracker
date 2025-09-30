package com.example.backend.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "coins.feed")
public class FeedCompositionProperties {
    private int targetSize = 15;                 // default to 15
    private int maxFallbackStalenessHours = 168; // 7 days; mark stale=true beyond this

    public int getTargetSize() { return targetSize; }
    public void setTargetSize(int targetSize) { this.targetSize = targetSize; }

    public int getMaxFallbackStalenessHours() { return maxFallbackStalenessHours; }
    public void setMaxFallbackStalenessHours(int maxFallbackStalenessHours) {
        this.maxFallbackStalenessHours = maxFallbackStalenessHours;
    }
}
