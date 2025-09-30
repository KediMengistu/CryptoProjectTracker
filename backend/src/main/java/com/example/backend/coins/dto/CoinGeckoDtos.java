package com.example.backend.coins.dto;

import java.util.List;

public class CoinGeckoDtos {

    // Still available if you need it elsewhere
    public record CoinListItem(String id, String symbol, String name) {}

    public static class CoinDetail {
        // NEW: allow refreshing fallback symbol/name from the /coins/{id} payload
        public String symbol;
        public String name;

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
                public Integer deletions;
            }
        }
    }

    // /search/trending response (we only map what's needed)
    public static class TrendingResponse {
        public List<Coin> coins;

        public static class Coin {
            public Item item;
            public static class Item {
                public String id;
                public String symbol;
                public String name;
            }
        }
    }
}
