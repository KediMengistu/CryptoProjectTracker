package com.example.backend.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.List;

@ConfigurationProperties(prefix = "coins.fallback")
public class FallbackRosterProperties {

    /**
     * Ordered list of fallback CoinGecko IDs (exactly what you put in application config).
     * Example:
     * coins.fallback.ids=bitcoin,ethereum,solana,avalanche-2,cardano,dogecoin,polkadot,chainlink,cosmos,litecoin,internet-computer,near,arbitrum,aptos,sui
     */
    private List<String> ids = new ArrayList<>();

    /**
     * If true, reconcile will remove any rows from coins_fallback that are not in {@link #ids}.
     */
    private boolean pruneExtraneous = true;

    public List<String> getIds() {
        return ids;
    }
    public void setIds(List<String> ids) {
        this.ids = ids;
    }

    public boolean isPruneExtraneous() {
        return pruneExtraneous;
    }
    public void setPruneExtraneous(boolean pruneExtraneous) {
        this.pruneExtraneous = pruneExtraneous;
    }
}
