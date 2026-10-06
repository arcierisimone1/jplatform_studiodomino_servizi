package com.jplatform_studiodomino.cms.promo;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@ConfigurationProperties(prefix = "promo")
public class PromoRemoteSiteConfig {

    private List<RemoteSite> remoteSites = new ArrayList<>();
    private long cacheRefreshMs = 300000;

    public List<RemoteSite> getRemoteSites() {
        return remoteSites;
    }

    public void setRemoteSites(List<RemoteSite> remoteSites) {
        this.remoteSites = remoteSites;
    }

    public long getCacheRefreshMs() {
        return cacheRefreshMs;
    }

    public void setCacheRefreshMs(long cacheRefreshMs) {
        this.cacheRefreshMs = cacheRefreshMs;
    }

    public static class RemoteSite {
        private String name;
        private String baseUrl;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getBaseUrl() {
            return baseUrl;
        }

        public void setBaseUrl(String baseUrl) {
            this.baseUrl = baseUrl;
        }
    }
}