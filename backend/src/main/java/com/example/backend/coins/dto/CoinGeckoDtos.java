package com.example.backend.coins.dto;

import java.util.List;
import java.util.Map;

public class CoinGeckoDtos {

    // /coins/list (id, symbol, name)
    public record CoinListItem(String id, String symbol, String name) {}

    // /coins/{id}?developer_data=true&...  (we only map what we need)
    public static class CoinDetail {
        public Links links;
        public DeveloperData developer_data;

        public static class Links {
            public ReposUrl repos_url;
        }
        public static class ReposUrl {
            public List<String> github;
        }

        public static class DeveloperData {
            public Integer forks;
            public Integer stars;
            public Integer subscribers;
            public Integer total_issues;
            public Integer closed_issues;
            public Integer pull_requests_merged;
            public Integer pull_request_contributors;
            public CodeDelta code_additions_deletions_4_weeks;
            public Integer commit_count_4_weeks;

            public static class CodeDelta {
                public Integer additions;
                public Integer deletions; // note: CoinGecko returns negative deletions; we’ll store as-is or abs(...)
            }
        }
    }
}
