package com.jplatform_studiodomino.cms.promo;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class PromoController {

    private final PromoSourceService localSourceService;
    private final PromoAggregationService aggregationService;

    public PromoController(PromoSourceService localSourceService, PromoAggregationService aggregationService) {
        this.localSourceService = localSourceService;
        this.aggregationService = aggregationService;
    }

    // Chiamato dagli ALTRI due siti per pescare le sezioni di QUESTO sito
    @GetMapping("/api/promo/items")
    public List<PromoItemDto> getLocalItems() {
        return localSourceService.getLocalItems();
    }

    // Chiamato dal frontend di QUESTO sito per mostrare il widget
    @GetMapping("/api/promo/all")
    public List<PromoItemDto> getAggregatedItems() {
        return aggregationService.getAggregatedItems();
    }
}