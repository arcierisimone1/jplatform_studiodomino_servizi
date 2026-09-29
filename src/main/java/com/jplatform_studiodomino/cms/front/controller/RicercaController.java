package com.jplatform_studiodomino.cms.front.controller;

import com.jplatform_studiodomino.cms.entity.DatiBase;
import com.jplatform_studiodomino.cms.service.ContentService;
import com.jplatform_studiodomino.shared.config.Configurazione;
import com.jplatform_studiodomino.shared.service.ConfigurazioneService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.ArrayList;
import java.util.List;

@Controller
@RequiredArgsConstructor
@Slf4j
public class RicercaController {

    private final ConfigurazioneService configurazioneService;
    private final ContentService contentService;

    // URL pulito: /ricerca/il-centro  (usato dal form di ricerca)
    @GetMapping("/ricerca/{campo1:.+}")
    public String ricercaPulita(
            @PathVariable String campo1,
            @RequestParam(required = false) String site,
            Model model,
            HttpServletRequest request
    ) {
        return doRicerca(campo1.replace('-', ' '), site, model, request);
    }

    // Retro-compatibilità: /ricerca?campo1=il+centro
    @GetMapping("/ricerca")
    public String ricerca(
            @RequestParam(required = false) String campo1,
            @RequestParam(required = false) String site,
            Model model,
            HttpServletRequest request
    ) {
        return doRicerca(campo1, site, model, request);
    }

    private String doRicerca(String campo1, String site, Model model, HttpServletRequest request) {

        Configurazione config = configurazioneService.getOrCreateConfiguration(request);
        model.addAttribute("config", config);

        String siteId = (site != null && !site.isBlank())
                ? site.trim()
                : String.valueOf(config.getSito().getId());

        String q = (campo1 != null) ? campo1.trim() : "";

        List<DatiBase> results = new ArrayList<>();
        if (!q.isEmpty()) {
            results = contentService.searchFullText(siteId, q);
            if (results != null && results.size() > 50) {
                results = results.subList(0, 50);
            }
        }

        model.addAttribute("campo1", q);
        model.addAttribute("results", results != null ? results : new ArrayList<>());
        model.addAttribute("resultsCount", results != null ? results.size() : 0);

        return config.getPublicTemplateFolder() + "/front/ricerca";
    }
}