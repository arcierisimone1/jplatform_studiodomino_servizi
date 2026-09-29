package com.jplatform_studiodomino.crm.controller;

import com.jplatform_studiodomino.cms.entity.Commento;
import com.jplatform_studiodomino.cms.repository.ContentRepository;
import com.jplatform_studiodomino.cms.service.CommentoService;
import com.jplatform_studiodomino.crm.entity.RegistroLead;
import com.jplatform_studiodomino.crm.service.RegistroLeadService;
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

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

/**
 * Controller admin per la gestione del Registro LEAD CRM.
 * Conversione da GestioneRegistroLead.java (Struts) a Spring MVC.
 *
 * Mapping URL:
 *   GET  /admin/crm/lead/new/{store}            → nuovoLead (entrata diretta)
 *   GET  /admin/crm/lead/new/uscita/{tipologia} → nuovoLeadUscita (uscita diretta)
 *   GET  /admin/crm/lead/{id}                   → openRegistroLead (entrata esistente)
 *   GET  /admin/crm/lead/{id}/uscita            → openRegistroLeadUscita (uscita esistente)
 *   POST /admin/crm/lead/save                   → saveRegistroLead
 *   POST /admin/crm/lead/{id}/delete            → deleteRegistroLead (AJAX)
 *   POST /admin/crm/lead/{id}/inviaSms          → inviaSmsRegistroLead
 *   POST /admin/crm/lead/{id}/inviaEmail        → inviaEmailRegistroLead
 */
@Controller
@RequestMapping("/admin/crm/lead")
@RequiredArgsConstructor
@Slf4j
public class RegistroLeadController {

    private final ConfigurazioneService configurazioneService;
    private final RegistroLeadService registroLeadService;
    private final CommentoService commentoService;
    private final ContentRepository contentRepository;

    private static final DateTimeFormatter DF  = DateTimeFormatter.ofPattern("dd/MM/yyyy - HH:mm");
    private static final DateTimeFormatter DTA = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter ORA = DateTimeFormatter.ofPattern("HH:mm");

    private static final String[] STATO_LEAD = {
            "Non gestito", "Da richiamare", "Inviare email",
            "In lavorazione", "Completato", "Annullato"
    };

    // =====================================================================
    // ELENCO
    // GET /admin/crm/lead
    // =====================================================================

    // =====================================================================
    // ELENCO CON URL PULITI
    // GET /admin/crm/lead/filtro/{direzione}/{stato}
    // =====================================================================

    @GetMapping({"", "/filtro/{direzione}/{stato}"})
    public String elencoCommentiUtente(
            @PathVariable(required = false) String direzione,
            @PathVariable(required = false) String stato,
            @RequestParam(value = "page", defaultValue = "0") int page,
            HttpServletRequest request, Model model) {

        HttpSession session = request.getSession();
        Configurazione config = configurazioneService.getConfig(session);
        if (!config.isLogged()) return "redirect:/login";

        // path "4" (bottone "Completati") -> messaggi già profilati in Lead (stato commento "2")
        // qualsiasi altro valore, o assente (bottone "Da iniziare") -> messaggi da gestire (stato "0")
        String statoCommento = "4".equals(stato) ? "2" : "0";

        try {
            var risultato = commentoService.findByTipologiaEStatoPaged("web", statoCommento, page);

            model.addAttribute("elencoCommentiUtente", risultato.getContent());
            model.addAttribute("paginaCorrente",       risultato.getNumber());
            model.addAttribute("totalePagine",         risultato.getTotalPages());
            model.addAttribute("statoSelezionato",     stato != null ? stato : "0");
            model.addAttribute("config",               config);
        } catch (Exception e) {
            log.error("Errore elencoCommentiUtente", e);
        }

        return ViewUtils.resolveProtectedTemplate("crm/sezioni/elencoCommentiUtente");
    }

    // =====================================================================
    // DETTAGLIO MESSAGGIO WEB
    // GET /admin/crm/lead/messaggio/{id}
    // =====================================================================

    @GetMapping("/messaggio/{id}")
    public String apriMessaggio(@PathVariable Integer id, HttpServletRequest request, Model model) {
        HttpSession session = request.getSession();
        Configurazione config = configurazioneService.getConfig(session);
        if (!config.isLogged()) return "redirect:/login";

        try {
            Commento messaggio = commentoService.findById(id).orElse(null);
            if (messaggio == null) return "redirect:/admin/crm/lead";

            if (messaggio.getIdoggetto() != null) {
                try {
                    int idoggetto = Integer.parseInt(messaggio.getIdoggetto().trim());
                    contentRepository.findById(idoggetto).ifPresent(messaggio::setContenuto);
                } catch (NumberFormatException ignored) {
                    // idoggetto non numerico: nessun contenuto collegato da mostrare
                }
            }

            model.addAttribute("messaggio", messaggio);
            model.addAttribute("config", config);
        } catch (Exception e) {
            log.error("Errore apriMessaggio id={}", id, e);
            return "redirect:/admin/crm/lead";
        }

        return ViewUtils.resolveProtectedTemplate("crm/contenuti/dettaglioMessaggioWeb");
    }

    // =====================================================================
    // SALVA MESSAGGIO WEB
    // POST /admin/crm/lead/messaggio/{id}/save
    // =====================================================================

    @PostMapping("/messaggio/{id}/save")
    public String salvaMessaggio(
            @PathVariable Integer id,
            @ModelAttribute Commento form,
            HttpServletRequest request, Model model) {

        HttpSession session = request.getSession();
        Configurazione config = configurazioneService.getConfig(session);
        if (!config.isLogged()) return "redirect:/login";

        try {
            Commento messaggio = commentoService.findById(id).orElseThrow();
            messaggio.setNome(form.getNome());
            messaggio.setCognome(form.getCognome());
            messaggio.setEmail(form.getEmail());
            messaggio.setStato(form.getStato());
            messaggio.setL1(form.getL1());
            messaggio.setMessaggio(form.getMessaggio());
            commentoService.save(messaggio);
        } catch (Exception e) {
            log.error("Errore salvaMessaggio id={}", id, e);
        }

        return "redirect:/admin/crm/lead/messaggio/" + id;
    }

    // =====================================================================
    // ELIMINA MESSAGGIO WEB (AJAX)
    // POST /admin/crm/lead/messaggio/{id}/delete
    // =====================================================================

    @PostMapping("/messaggio/{id}/delete")
    @ResponseBody
    public ResponseEntity<String> eliminaMessaggio(@PathVariable Integer id, HttpServletRequest request) {
        HttpSession session = request.getSession();
        Configurazione config = configurazioneService.getConfig(session);
        if (!config.isLogged()) return ResponseEntity.status(401).body("KO");

        try {
            commentoService.eliminaCommento(id);
            return ResponseEntity.ok("OK");
        } catch (Exception e) {
            log.error("Errore eliminaMessaggio id={}", id, e);
            return ResponseEntity.ok("KO");
        }
    }

    // =====================================================================
    // PROFILA LEAD PER UTENTE (crea un nuovo Lead a partire da un messaggio)
    // GET /admin/crm/lead/new/commenti/{idCommento}
    // =====================================================================

    @GetMapping("/new/commenti/{idCommento}")
    public String nuovoLeadDaCommento(
            @PathVariable Integer idCommento,
            HttpServletRequest request, Model model) {

        HttpSession session = request.getSession();
        Configurazione config = configurazioneService.getConfig(session);
        if (!config.isLogged()) return "redirect:/login";

        try {
            Commento messaggio = commentoService.findById(idCommento).orElse(null);
            if (messaggio == null) return "redirect:/admin/crm/lead";

            RegistroLead registroLead = new RegistroLead();
            registroLead.setId(-1L);
            registroLead.setDirezione("e");
            registroLead.setData(LocalDate.now().format(DTA));
            registroLead.setOra(LocalTime.now().format(ORA));
            registroLead.setIdamministratore(config.getAmministratore().getId());
            registroLead.setAmministratore(config.getAmministratore());
            registroLead.setIdleadstore(idCommento);
            registroLead.setStore("commenti");
            registroLead.setStato("0");
            registroLead.setNotalead(messaggio.getMessaggio());

            Integer idUtenteDaCommento = null;
            String iduserStr = messaggio.getIduser();
            if (iduserStr != null && !iduserStr.isBlank() && !"0".equals(iduserStr) && !"-1".equals(iduserStr)) {
                try {
                    idUtenteDaCommento = Integer.parseInt(iduserStr.trim());
                } catch (NumberFormatException e) {
                    log.warn("iduser non numerico sul commento id={}: {}", messaggio.getId(), iduserStr);
                }
            }

            Integer idUtente = (idUtenteDaCommento != null && idUtenteDaCommento > 0)
                    ? idUtenteDaCommento
                    : registroLeadService.findIdUtenteByEmail(messaggio.getEmail());
            registroLead.setIdutente(idUtente != null ? idUtente : 0);
            if (idUtente != null && idUtente > 0) {
                registroLead.setUtente(registroLeadService.findUtenteBase(idUtente));
            }

            model.addAttribute("areeInteresse", registroLeadService.findAreeInteresse());
            model.addAttribute("statoLead",     STATO_LEAD);
            model.addAttribute("registroLead",  registroLead);
            model.addAttribute("config",        config);

        } catch (Exception e) {
            log.error("Errore nuovoLeadDaCommento idCommento={}", idCommento, e);
        }

        return ViewUtils.resolveProtectedTemplate("crm/contenuti/dettaglioRegistroLead");
    }

    // =====================================================================
// ELENCO REGISTRO LEAD (BOZZA)
// GET /admin/crm/registrolead
// GET /admin/crm/registrolead/filtro/{direzione}/{stato}
// =====================================================================

    @GetMapping({"/registrolead", "/registrolead/filtro/{direzione}/{stato}"})
    public String elencoRegistroLead(
            @PathVariable(required = false) String direzione,
            @PathVariable(required = false) String stato,
            @RequestParam(value = "page", defaultValue = "0") int page,
            HttpServletRequest request, Model model) {

        HttpSession session = request.getSession();
        Configurazione config = configurazioneService.getConfig(session);
        if (!config.isLogged()) return "redirect:/login";

        String dirFinal = (direzione != null) ? direzione : "e";
        String statoFinal = (stato != null) ? stato : "0";

        try {
            var risultato = registroLeadService.findAllPaged(dirFinal, statoFinal, page);
            risultato.getContent().forEach(lead -> {
                if (lead.getIdutente() > 0) {
                    lead.setUtente(registroLeadService.findUtenteBase(lead.getIdutente()));
                }
                if (lead.getIdamministratore() > 0) {
                    lead.setAmministratore(registroLeadService.findAmministratoreById(
                            String.valueOf(lead.getIdamministratore())));
                }
            });

            model.addAttribute("elencoRegistroLead",    risultato.getContent());
            model.addAttribute("paginaCorrente",        risultato.getNumber());
            model.addAttribute("totalePagine",          risultato.getTotalPages());
            model.addAttribute("statoLead",             STATO_LEAD);
            model.addAttribute("statoSelezionato",      statoFinal);
            model.addAttribute("direzioneSelezionata",  dirFinal);
            model.addAttribute("config",                config);
        } catch (Exception e) {
            log.error("Errore elencoRegistroLead", e);
        }

        return ViewUtils.resolveProtectedTemplate("crm/sezioni/elencoRegistroLead");
    }

    // =====================================================================
    // NUOVO LEAD ENTRATA — URL pulito
    // GET /admin/crm/lead/new/{store}
    // Es: /admin/crm/lead/new/diretto
    // =====================================================================

    @GetMapping("/new/{store}")
    public String nuovoLead(
            @PathVariable String store,
            @RequestParam(value = "idutente", required = false, defaultValue = "0") Integer idutente,
            HttpServletRequest request, Model model) {

        HttpSession session = request.getSession();
        Configurazione config = configurazioneService.getConfig(session);
        if (!config.isLogged()) return "redirect:/login";

        try {
            RegistroLead registroLead = new RegistroLead();
            registroLead.setId(-1L);
            registroLead.setDirezione("e");
            registroLead.setData(LocalDate.now().format(DTA));
            registroLead.setOra(LocalTime.now().format(ORA));
            registroLead.setIdamministratore(config.getAmministratore().getId());
            registroLead.setAmministratore(config.getAmministratore());
            registroLead.setIdleadstore(0);
            registroLead.setIdutente(idutente);
            registroLead.setStore(store);

            if (idutente != null && idutente > 0) {
                registroLead.setUtente(registroLeadService.findUtenteBase(idutente));
            }

            model.addAttribute("areeInteresse", registroLeadService.findAreeInteresse());
            model.addAttribute("statoLead",     STATO_LEAD);
            model.addAttribute("registroLead",  registroLead);
            model.addAttribute("config",        config);

        } catch (Exception e) {
            log.error("Errore nuovoLead store={}", store, e);
        }

        return ViewUtils.resolveProtectedTemplate("crm/contenuti/dettaglioRegistroLead");
    }

    // =====================================================================
    // NUOVO LEAD USCITA — URL pulito
    // GET /admin/crm/lead/new/uscita/{tipologia}
    // Es: /admin/crm/lead/new/uscita/Email
    //     /admin/crm/lead/new/uscita/Sms
    // =====================================================================

    @GetMapping("/new/uscita/{tipologia}")
    public String nuovoLeadUscita(
            @PathVariable String tipologia,
            HttpServletRequest request, Model model) {

        HttpSession session = request.getSession();
        Configurazione config = configurazioneService.getConfig(session);
        if (!config.isLogged()) return "redirect:/login";

        try {
            RegistroLead registroLead = new RegistroLead();
            registroLead.setId(-1L);
            registroLead.setDirezione("u");
            registroLead.setData(LocalDate.now().format(DTA));
            registroLead.setOra(LocalTime.now().format(ORA));
            registroLead.setIdamministratore(config.getAmministratore().getId());
            registroLead.setAmministratore(config.getAmministratore());
            registroLead.setIdleadstore(-1);
            registroLead.setIdutente(0);
            registroLead.setStore("diretto");

            model.addAttribute("tipologia",    tipologia);
            model.addAttribute("statoLead",    STATO_LEAD);
            model.addAttribute("registroLead", registroLead);
            model.addAttribute("config",       config);
            model.addAttribute("accountsEmail",
                    registroLeadService.findAccountsEmail(config.getAmministratore().getIdaccountemail()));

        } catch (Exception e) {
            log.error("Errore nuovoLeadUscita tipologia={}", tipologia, e);
        }

        return ViewUtils.resolveProtectedTemplate(
                "crm/contenuti/dettaglioRegistroLeadUscita" + tipologia);
    }

    // =====================================================================
    // OPEN LEAD ESISTENTE (entrata)
    // GET /admin/crm/lead/{id}
    // =====================================================================

    @GetMapping("/{id}")
    public String openRegistroLead(
            @PathVariable Long id,
            HttpServletRequest request, Model model) {

        HttpSession session = request.getSession();
        Configurazione config = configurazioneService.getConfig(session);
        if (!config.isLogged()) return "redirect:/login";

        try {
            RegistroLead registroLead = registroLeadService.findById(id);
            arricchisciConStore(registroLead);
            if (registroLead.getIdamministratore() > 0) {
                registroLead.setAmministratore(
                        registroLeadService.findAmministratoreById(
                                String.valueOf(registroLead.getIdamministratore())));
            }

            model.addAttribute("areeInteresse", registroLeadService.findAreeInteresse());
            model.addAttribute("statoLead",     STATO_LEAD);
            model.addAttribute("registroLead",  registroLead);
            model.addAttribute("config",        config);

        } catch (Exception e) {
            log.error("Errore openRegistroLead id={}", id, e);
        }

        return ViewUtils.resolveProtectedTemplate("crm/contenuti/dettaglioRegistroLead");
    }

    // =====================================================================
    // OPEN LEAD ESISTENTE USCITA
    // GET /admin/crm/lead/{id}/uscita/{tipologia}
    // =====================================================================

    @GetMapping("/{id}/uscita/{tipologia}")
    public String openRegistroLeadUscita(
            @PathVariable Long id,
            @PathVariable String tipologia,
            HttpServletRequest request, Model model) {

        HttpSession session = request.getSession();
        Configurazione config = configurazioneService.getConfig(session);
        if (!config.isLogged()) return "redirect:/login";

        try {
            RegistroLead registroLead = registroLeadService.findById(id);

            model.addAttribute("tipologia",    tipologia);
            model.addAttribute("statoLead",    STATO_LEAD);
            model.addAttribute("registroLead", registroLead);
            model.addAttribute("config",       config);
            model.addAttribute("accountsEmail",
                    registroLeadService.findAccountsEmail(config.getAmministratore().getIdaccountemail()));

        } catch (Exception e) {
            log.error("Errore openRegistroLeadUscita id={} tipologia={}", id, tipologia, e);
        }

        return ViewUtils.resolveProtectedTemplate(
                "crm/contenuti/dettaglioRegistroLeadUscita" + tipologia);
    }

    // =====================================================================
    // RICERCA OPERATORI (per assegnazione lead)
    // GET /admin/crm/lead/operatori/search
    // =====================================================================

    @GetMapping("/operatori/search")
    @ResponseBody
    public List<Map<String, Object>> searchOperatori(
            @RequestParam(value = "term", required = false) String term) {

        if (term == null || term.trim().length() < 2) return List.of();

        return registroLeadService.searchAmministratori(term).stream().map(u -> {
            Map<String, Object> map = new java.util.LinkedHashMap<>();
            map.put("id",      u.getId());
            map.put("nome",    u.getNome());
            map.put("cognome", u.getCognome());
            map.put("email",   u.getEmail());
            return map;
        }).toList();
    }

    // =====================================================================
    // SAVE
    // POST /admin/crm/lead/save
    // =====================================================================

    @PostMapping("/save")
    public String saveRegistroLead(
            @ModelAttribute RegistroLead registroLead,
            HttpServletRequest request, Model model) {

        HttpSession session = request.getSession();
        Configurazione config = configurazioneService.getConfig(session);
        if (!config.isLogged()) return "redirect:/login";

        try {
            log.info("idutente ricevuto dal form: {}", registroLead.getIdutente());
            String now      = LocalDateTime.now().format(DF);
            String operatore = config.getAmministratore().getNome()
                    + " " + config.getAmministratore().getCognome();

            registroLead.setAmministratore(
                    registroLeadService.findAmministratoreById(
                            String.valueOf(registroLead.getIdamministratore())));

            boolean isNew = registroLead.getId() == null || registroLead.getId() == -1;

            if (isNew) {
                if (registroLead.getIdutente() > 0) {
                    registroLead.setUtente(
                            registroLeadService.findUtenteBase(registroLead.getIdutente()));
                }
                String statoText = STATO_LEAD[Integer.parseInt(registroLead.getStato())];
                registroLead.setLog(buildLog(now, operatore, registroLead,
                        statoText, "Inserimento nuovo lead da"));
                registroLead = registroLeadService.crea(registroLead, config);
            } else {
                registroLead.setUtente(
                        registroLeadService.findUtenteBase(registroLead.getIdutente()));
                String statoText = STATO_LEAD[Integer.parseInt(registroLead.getStato())];
                String logEntry  = buildLog(now, operatore, registroLead,
                        statoText, "Modificato da");
                registroLead.setLog(registroLead.getLog() + logEntry);
                registroLead = registroLeadService.salva(registroLead);
            }

            arricchisciConStore(registroLead);

            model.addAttribute("areeInteresse", registroLeadService.findAreeInteresse());
            model.addAttribute("statoLead",     STATO_LEAD);
            model.addAttribute("registroLead",  registroLead);
            model.addAttribute("config",        config);

        } catch (Exception e) {
            log.error("Errore saveRegistroLead", e);
            model.addAttribute("error",        "Errore nel salvataggio: " + e.getMessage());
            model.addAttribute("registroLead", registroLead);
            model.addAttribute("config",       config);
        }

        return ViewUtils.resolveProtectedTemplate("crm/contenuti/dettaglioRegistroLead");
    }

    // =====================================================================
    // DELETE (AJAX)
    // POST /admin/crm/lead/{id}/delete
    // =====================================================================

    @PostMapping("/{id}/delete")
    @ResponseBody
    public ResponseEntity<String> deleteRegistroLead(
            @PathVariable Long id,
            HttpServletRequest request) {

        HttpSession session = request.getSession();
        Configurazione config = configurazioneService.getConfig(session);
        if (!config.isLogged()) return ResponseEntity.status(401).body("KO");

        try {
            registroLeadService.elimina(id);
            return ResponseEntity.ok("OK");
        } catch (Exception e) {
            log.error("Errore deleteRegistroLead id={}", id, e);
            return ResponseEntity.ok("KO");
        }
    }

    // =====================================================================
    // INVIA SMS
    // POST /admin/crm/lead/{id}/inviaSms
    // =====================================================================

    @PostMapping("/{id}/inviaSms")
    public String inviaSmsRegistroLead(
            @PathVariable Long id,
            @ModelAttribute RegistroLead registroLead,
            HttpServletRequest request, Model model) {

        HttpSession session = request.getSession();
        Configurazione config = configurazioneService.getConfig(session);
        if (!config.isLogged()) return "redirect:/login";

        try {
            if (registroLead.getIdutente() != 0 && registroLead.getIdutente() > 0) {
                registroLead.setUtente(
                        registroLeadService.findUtenteBase(registroLead.getIdutente()));
            } else if (registroLead.getUtente() != null) {
                registroLead.setIdutente(
                        registroLead.getUtente().getId() != null ? registroLead.getUtente().getId() : 0);
            }

            log.info("idutente ricevuto: {}", registroLead.getIdutente());
            log.info("utente ricevuto: {}", registroLead.getUtente() != null ? registroLead.getUtente().toString() : "null");
            log.info("telefono destinatario: {}", registroLead.getUtente() != null ? registroLead.getUtente().getTelefono() : "null");
            log.info("telefono2 destinatario: {}", registroLead.getUtente() != null ? registroLead.getUtente().getTelefono2() : "null");

            registroLeadService.inviaSms(registroLead, config);

            String now = LocalDateTime.now().format(DF);
            String operatore = config.getAmministratore().getNome()
                    + " " + config.getAmministratore().getCognome();

            registroLead.setId(null); // FONDAMENTALE
            registroLead.setDirezione("u");
            registroLead.setStore("Sms");
            registroLead.setLog(buildLogSemplice(now, operatore, "Creazione nuovo lead da"));

            registroLead = registroLeadService.crea(registroLead, config);

            model.addAttribute("registroLead", registroLead);
            model.addAttribute("config", config);

        } catch (Exception e) {
            log.error("Errore inviaSmsRegistroLead id={}", id, e);
            model.addAttribute("registroLead", registroLead);
            model.addAttribute("config", config);
        }

        return ViewUtils.resolveProtectedTemplate("crm/contenuti/dettaglioRegistroLeadUscitaSms");
    }

    // =====================================================================
    // INVIA EMAIL
    // POST /admin/crm/lead/{id}/inviaEmail
    // =====================================================================

    @PostMapping("/{id}/inviaEmail")
    public String inviaEmailRegistroLead(
            @PathVariable Long id,
            @ModelAttribute RegistroLead registroLead,
            HttpServletRequest request, Model model) {

        HttpSession session = request.getSession();
        Configurazione config = configurazioneService.getConfig(session);
        if (!config.isLogged()) return "redirect:/login";

        try {
            if ((registroLead.getIdutente() != 0) && (registroLead.getIdutente() > 0)) {
                registroLead.setUtente(
                        registroLeadService.findUtenteBase(registroLead.getIdutente()));
            } else if (registroLead.getUtente() != null) {
                registroLead.setIdutente(
                        registroLead.getUtente().getId() != null ? registroLead.getUtente().getId() : 0);
            }

            log.info("idutente ricevuto: {}", registroLead.getIdutente());
            log.info("utente ricevuto: {}", registroLead.getUtente() != null ? registroLead.getUtente().toString() : "null");
            log.info("email destinatario: {}", registroLead.getUtente() != null ? registroLead.getUtente().getEmail() : "null");

            registroLeadService.inviaEmail(registroLead, config);

            String now = LocalDateTime.now().format(DF);
            String operatore = config.getAmministratore().getNome()
                    + " " + config.getAmministratore().getCognome();

            registroLead.setId(null); // FONDAMENTALE
            registroLead.setDirezione("u");
            registroLead.setStore("Email");
            registroLead.setLog(buildLogSemplice(now, operatore, "Creazione nuovo lead da"));

            registroLead = registroLeadService.crea(registroLead, config);

            model.addAttribute("registroLead", registroLead);
            model.addAttribute("config", config);

        } catch (Exception e) {
            log.error("Errore inviaEmailRegistroLead id={}", id, e);
            model.addAttribute("registroLead", registroLead);
            model.addAttribute("config", config);
        }

        return ViewUtils.resolveProtectedTemplate("crm/contenuti/dettaglioRegistroLeadUscitaEmail");
    }

    // =====================================================================
    // UTILITY PRIVATI
    // =====================================================================

    private void arricchisciConStore(RegistroLead registroLead) {
        try {
            if ("commenti".equals(registroLead.getStore())) {
                var commento = registroLeadService.findCommentoById(
                        (long) registroLead.getIdleadstore());
                registroLead.setCommento(commento);

            } else if ("emailstore".equals(registroLead.getStore())
                    && registroLead.getIdutente() > 0) {
                var email = registroLeadService.findEmailStoreById(
                        registroLead.getIdleadstore());
                registroLead.setMessaggioEmail(email);
                registroLead.setNotalead("Nuovo lead da email ricevuta");
            }
        } catch (Exception e) {
            log.warn("Errore arricchimento store lead id={}: {}",
                    registroLead.getId(), e.getMessage());
        }
    }

    private String buildLog(String now, String operatore, RegistroLead rl,
                            String statoText, String azione) {
        return "<code>" + now + "</code>" +
                "<blockquote class='success'>" +
                "<p>" + azione + ": " + operatore + "</p>" +
                "<p>Data: " + rl.getData() + " " + rl.getOra() + "</p>" +
                "<p>Assegnatario: " + (rl.getAmministratore() != null
                ? rl.getAmministratore().getNome() + " " + rl.getAmministratore().getCognome()
                : "") + "</p>" +
                "<p>Stato: " + statoText + "</p>" +
                "<p>Testo: " + rl.getNotalead() + "</p>" +
                "</blockquote>";
    }

    private String buildLogSemplice(String now, String operatore, String azione) {
        return "<code>" + now + "</code>" +
                "<blockquote class='success'>" +
                "<p>" + azione + ": " + operatore + "</p>" +
                "</blockquote>";
    }
}