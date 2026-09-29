package com.jplatform_studiodomino.crm.controller;

import com.jplatform_studiodomino.cms.entity.Commento;
import com.jplatform_studiodomino.cms.repository.CommentoRepository;
import com.jplatform_studiodomino.cms.repository.ContentRepository;
import com.jplatform_studiodomino.crm.entity.RegistroLead;
import com.jplatform_studiodomino.crm.repository.RegistroLeadRepository;
import com.jplatform_studiodomino.crm.service.RegistroLeadService;
import com.jplatform_studiodomino.shared.config.Configurazione;
import com.jplatform_studiodomino.shared.repository.UtenteEsternoRepository;
import com.jplatform_studiodomino.shared.service.ConfigurazioneService;
import com.jplatform_studiodomino.shared.util.ViewUtils;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

/**
 * Controller per la dashboard principale del CRM admin.
 * Conversione da MainAdmin.java (Struts) a Spring MVC.
 *
 * Mapping URL:
 *   GET /admin/crm            → dashboard
 *   GET /admin/crm/logout     → logoutAmministratore
 */
@Controller
@RequestMapping("/admin/crm/dashboard")
@RequiredArgsConstructor
@Slf4j
public class MainAdminController {

    private final ConfigurazioneService configurazioneService;
    private final UtenteEsternoRepository utenteEsternoRepository;
    private final RegistroLeadRepository registroLeadRepository;
    private final RegistroLeadService registroLeadService;
    private final CommentoRepository commentoRepository;
    private final ContentRepository contentRepository;

    // =====================================================================
    // DASHBOARD
    // Vecchio: dashboard() → forward "successDashboard"
    // =====================================================================

    @GetMapping
    public String dashboard(
            @RequestParam(value = "all", required = false) String all,
            HttpServletRequest request, Model model) {

        HttpSession session = request.getSession();
        Configurazione config = configurazioneService.getConfig(session);
        if (!config.isLogged()) return "redirect:/login";

        try {
            String idAmministratore = String.valueOf(config.getAmministratore().getId());

            // Ultimi 10 utenti esterni (i più recenti)
            model.addAttribute("dashUltimiUtentiEsterni",
                    utenteEsternoRepository.findTop10ByStatusOrderByIdDesc("1"));

            // Todo lead aperti (stato != 4 e != 5)
            // Come nel vecchio: di default solo i lead assegnati a chi è loggato;
            // "Tutti i lead" (?all=all) mostra tutti i lead aperti, per QUALSIASI operatore
            List<RegistroLead> leads;

            if (all != null) {
                leads = registroLeadRepository.findByStatoNotInOrderByIdDesc(List.of("4", "5"));
            } else {
                leads = registroLeadRepository.findByIdamministratoreAndStatoNotInOrderByIdDesc(
                        Integer.parseInt(idAmministratore), List.of("4", "5"));
            }

// Carica utente e amministratore assegnato per ogni lead
            leads.forEach(lead -> {
                if (lead.getIdutente() > 0) {
                    lead.setUtente(registroLeadService.findUtenteBase(lead.getIdutente()));
                }
                if (lead.getIdamministratore() > 0) {
                    lead.setAmministratore(registroLeadService.findAmministratoreById(
                            String.valueOf(lead.getIdamministratore())));
                }
            });

            model.addAttribute("todoLead", leads);

            // Contatore "Messaggi ricevuti": messaggi dal form contatti (tipologia "web")
            // ancora da gestire (stato "0"), stessa logica di elencoCommentiUtente()
            model.addAttribute("dashCommenti", commentoRepository.countByTipologiaAndStato("web", "0"));

            List<Commento> ultimiCommenti = commentoRepository
                    .findByTipologiaAndStatoOrderByIdDesc("web", "0")
                    .stream().limit(10).toList();
            ultimiCommenti.forEach(c -> {
                if (c.getIdoggetto() != null) {
                    try {
                        int idoggetto = Integer.parseInt(c.getIdoggetto().trim());
                        contentRepository.findById(idoggetto).ifPresent(c::setContenuto);
                    } catch (NumberFormatException ignored) {
                        // idoggetto non numerico: nessun contenuto collegato da mostrare
                    }
                }
            });
            model.addAttribute("dashUltimiCommenti", ultimiCommenti);

            model.addAttribute("areeInteresse", List.of());

            model.addAttribute("config", config);

        } catch (Exception e) {
            log.error("Errore dashboard CRM", e);
        }

        return ViewUtils.resolveProtectedTemplate("crm/sezioni/dashboard");
    }

    // =====================================================================
    // LOGOUT
    // Vecchio: logoutAmministratore() → forward "successExit"
    // =====================================================================

    @GetMapping("/logout")
    public String logout(HttpServletRequest request, HttpServletResponse response) {

        HttpSession session = request.getSession();
        Configurazione config = configurazioneService.getConfig(session);

        try {
            if (config.getSito() != null) {
                String cookieName = "JPlatformEdit" + config.getSito().getCheck();
                Cookie cookie = new Cookie(cookieName, "");
                cookie.setMaxAge(0);
                cookie.setPath("/");
                response.addCookie(cookie);
            }
            config.setAmministratore(null);
            session.setAttribute("configCore", config);
        } catch (Exception e) {
            log.error("Errore logout CRM", e);
        }

        return "redirect:/login";
    }
}