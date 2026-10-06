package com.jplatform_studiodomino.cms.promo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

@Service
public class PromoAggregationService {

    private final PromoSourceService localSourceService;
    private final PromoRemoteSiteConfig remoteSiteConfig;
    private final RestClient restClient = RestClient.create();

    private final AtomicReference<List<PromoItemDto>> cachedItems = new AtomicReference<>(Collections.emptyList());

    @Autowired
    public PromoAggregationService(PromoSourceService localSourceService, PromoRemoteSiteConfig remoteSiteConfig) {
        this.localSourceService = localSourceService;
        this.remoteSiteConfig = remoteSiteConfig;
        refreshCache();
    }

    public List<PromoItemDto> getAggregatedItems() {
        return cachedItems.get();
    }

    @Scheduled(fixedDelayString = "${promo.cache-refresh-ms:300000}")
    public void refreshCache() {
        List<PromoItemDto> combined = new ArrayList<>(localSourceService.getLocalItems());

        for (PromoRemoteSiteConfig.RemoteSite remoteSite : remoteSiteConfig.getRemoteSites()) {
            try {
                List<PromoItemDto> remoteItems = restClient.get()
                        .uri(remoteSite.getBaseUrl() + "/api/promo/items")
                        .retrieve()
                        .body(new org.springframework.core.ParameterizedTypeReference<List<PromoItemDto>>() {
                        });
                if (remoteItems != null) {
                    combined.addAll(remoteItems);
                }
            } catch (Exception e) {
                // Sito remoto non raggiungibile: lo saltiamo senza bloccare gli altri
                System.err.println("Promo widget: impossibile contattare " + remoteSite.getName() + " - " + e.getMessage());
            }
        }

        cachedItems.set(combined);
    }
}
