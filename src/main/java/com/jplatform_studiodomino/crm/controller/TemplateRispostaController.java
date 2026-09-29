package com.jplatform_studiodomino.crm.controller;

import com.jplatform_studiodomino.crm.entity.TemplateRisposta;
import com.jplatform_studiodomino.crm.service.TemplateRispostaService;
import com.jplatform_studiodomino.shared.config.Configurazione;
import com.jplatform_studiodomino.shared.service.ConfigurazioneService;
import com.jplatform_studiodomino.shared.util.ViewUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

/**
 * Controller admin per la gestione delle Risposte Standard CRM.
 * Conversione da GestioneTemplateRisposte.java (Struts) a Spring MVC.
 *
 * Mapping URL:
 *   GET  /admin/crm/risposte              → elenco
 *   GET  /admin/crm/risposte/new          → nuovo
 *   GET  /admin/crm/risposte/{id}         → apri
 *   POST /admin/crm/risposte/save         → salva
 *   POST /admin/crm/risposte/{id}/delete  → elimina (AJAX)
 *   GET  /admin/crm/risposte/{id}/duplica → duplica
 */
@Controller
@RequestMapping("/admin/crm/risposte")
@RequiredArgsConstructor
@Slf4j
public class TemplateRispostaController {

    private final ConfigurazioneService configurazioneService;
    private final TemplateRispostaService templateRispostaService;

    @GetMapping
    public String elenco(HttpServletRequest request, Model model) {
        HttpSession session = request.getSession();
        Configurazione config = configurazioneService.getConfig(session);
        if (!config.isLogged()) return "redirect:/login";

        try {
            model.addAttribute("elencoTemplateRisposte", templateRispostaService.findAll());
        } catch (Exception e) {
            log.error("Errore elenco TemplateRisposte", e);
            model.addAttribute("elencoTemplateRisposte", java.util.List.of());
        }
        model.addAttribute("config", config);

        return ViewUtils.resolveProtectedTemplate("crm/sezioni/elencoTemplateRisposta");
    }

    @GetMapping("/api")
    @ResponseBody
    public ResponseEntity<java.util.List<TemplateRisposta>> elencoJson(HttpServletRequest request) {
        HttpSession session = request.getSession();
        Configurazione config = configurazioneService.getConfig(session);
        if (!config.isLogged()) return ResponseEntity.status(401).build();

        try {
            return ResponseEntity.ok(templateRispostaService.findAll());
        } catch (Exception e) {
            log.error("Errore elencoJson TemplateRisposte", e);
            return ResponseEntity.ok(java.util.List.of());
        }
    }

    @GetMapping("/new")
    public String nuovo(HttpServletRequest request, Model model) {
        HttpSession session = request.getSession();
        Configurazione config = configurazioneService.getConfig(session);
        if (!config.isLogged()) return "redirect:/login";

        TemplateRisposta templateRisposta = new TemplateRisposta();
        templateRisposta.setId(-1L);

        model.addAttribute("templateRisposta", templateRisposta);
        model.addAttribute("config", config);

        return ViewUtils.resolveProtectedTemplate("crm/contenuti/dettaglioTemplateRisposta");
    }

    @GetMapping("/{id}")
    public String apri(@PathVariable Long id, HttpServletRequest request, Model model) {
        HttpSession session = request.getSession();
        Configurazione config = configurazioneService.getConfig(session);
        if (!config.isLogged()) return "redirect:/login";

        try {
            model.addAttribute("templateRisposta", templateRispostaService.findById(id));
        } catch (Exception e) {
            log.error("Errore apri TemplateRisposta id={}", id, e);
            return "redirect:/admin/crm/risposte";
        }
        model.addAttribute("config", config);

        return ViewUtils.resolveProtectedTemplate("crm/contenuti/dettaglioTemplateRisposta");
    }

    @PostMapping("/save")
    public String salva(@ModelAttribute TemplateRisposta templateRisposta,
                        HttpServletRequest request, Model model) {
        HttpSession session = request.getSession();
        Configurazione config = configurazioneService.getConfig(session);
        if (!config.isLogged()) return "redirect:/login";

        try {
            boolean isNew = templateRisposta.getId() == null || templateRisposta.getId() == -1;
            templateRisposta = isNew
                    ? templateRispostaService.crea(templateRisposta)
                    : templateRispostaService.salva(templateRisposta);

            model.addAttribute("templateRisposta", templateRisposta);
        } catch (Exception e) {
            log.error("Errore salva TemplateRisposta", e);
            model.addAttribute("error", "Errore nel salvataggio: " + e.getMessage());
            model.addAttribute("templateRisposta", templateRisposta);
        }
        model.addAttribute("config", config);

        return ViewUtils.resolveProtectedTemplate("crm/contenuti/dettaglioTemplateRisposta");
    }

    @PostMapping("/{id}/delete")
    @ResponseBody
    public ResponseEntity<String> elimina(@PathVariable Long id, HttpServletRequest request) {
        HttpSession session = request.getSession();
        Configurazione config = configurazioneService.getConfig(session);
        if (!config.isLogged()) return ResponseEntity.status(401).body("KO");

        try {
            templateRispostaService.elimina(id);
            return ResponseEntity.ok("OK");
        } catch (Exception e) {
            log.error("Errore elimina TemplateRisposta id={}", id, e);
            return ResponseEntity.ok("KO");
        }
    }

    @GetMapping("/{id}/duplica")
    public String duplica(@PathVariable Long id, HttpServletRequest request, Model model) {
        HttpSession session = request.getSession();
        Configurazione config = configurazioneService.getConfig(session);
        if (!config.isLogged()) return "redirect:/login";

        try {
            TemplateRisposta copia = templateRispostaService.prepareDuplica(id);
            model.addAttribute("templateRisposta", copia);
            model.addAttribute("config", config);
            return ViewUtils.resolveProtectedTemplate("crm/contenuti/dettaglioTemplateRisposta");
        } catch (Exception e) {
            log.error("Errore duplica TemplateRisposta id={}", id, e);
            return "redirect:/admin/crm/risposte";
        }
    }
}