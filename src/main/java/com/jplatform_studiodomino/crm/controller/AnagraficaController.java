package com.jplatform_studiodomino.crm.controller;

import com.jplatform_studiodomino.crm.service.AnagraficaService;
import com.jplatform_studiodomino.crm.service.RegistroLeadService;
import com.jplatform_studiodomino.shared.config.Configurazione;
import com.jplatform_studiodomino.shared.entity.UtenteEsterno;
import com.jplatform_studiodomino.shared.service.ConfigurazioneService;
import com.jplatform_studiodomino.shared.service.UtenteEsternoService;
import com.jplatform_studiodomino.shared.util.ViewUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.Random;

@Controller
@RequestMapping("/admin/crm/utenti")
@RequiredArgsConstructor
@Slf4j
public class AnagraficaController {

    private final ConfigurazioneService configurazioneService;
    private final AnagraficaService anagraficaService;
    private final UtenteEsternoService utenteEsternoService;
    private final RegistroLeadService registroLeadService;

    private static final DateTimeFormatter DF = DateTimeFormatter.ofPattern("dd/MM/yyyy - HH:mm");

    @GetMapping
    public String elencoAnagrafico(HttpServletRequest request, Model model) {
        HttpSession session = request.getSession();
        Configurazione config = configurazioneService.getConfig(session);
        if (!config.isLogged()) return "redirect:/login";

        config.setGruppi(configurazioneService.getAllGruppi(String.valueOf(config.getIdSito())));

        try {
            model.addAttribute("elencoAnagrafico", java.util.List.of());
        } catch (Exception e) {
            log.error("Errore elencoAnagrafico", e);
            model.addAttribute("elencoAnagrafico", List.of());
        }
        model.addAttribute("ricerca", new UtenteEsterno());
        model.addAttribute("config", config);

        return ViewUtils.resolveProtectedTemplate("crm/sezioni/elencoAnagrafica");
    }

    @GetMapping("/new")
    public String newAnagrafica(
            @RequestParam(value = "email", defaultValue = "") String email,
            HttpServletRequest request, Model model) {

        HttpSession session = request.getSession();
        Configurazione config = configurazioneService.getConfig(session);
        if (!config.isLogged()) return "redirect:/login";

        config.setGruppi(configurazioneService.getAllGruppi(String.valueOf(config.getIdSito())));

        UtenteEsterno utente = new UtenteEsterno();
        utente.setId(null);
        utente.setStatus("1");
        utente.setNazione("");
        utente.setDatanascita("");
        utente.setCodicefiscale("");
        utente.setPartitaiva("");
        utente.setSocieta("");
        utente.setComune("");
        utente.setCap("");
        utente.setProvincia("");
        utente.setIndirizzo("");
        utente.setIndirizzospedizione("");
        utente.setTelefono("");
        utente.setTelefono2("");
        utente.setPec("");
        if (!email.isEmpty()) utente.setEmail(email);

        model.addAttribute("utente", utente);
        model.addAttribute("elencoAnagrafico", List.of(utente));
        model.addAttribute("config", config);

        return ViewUtils.resolveProtectedTemplate("crm/contenuti/dettaglioAnagrafica");
    }

    @GetMapping("/{id}")
    public String openAnagrafica(@PathVariable Integer id,
                                 HttpServletRequest request, Model model) {

        HttpSession session = request.getSession();
        Configurazione config = configurazioneService.getConfig(session);
        if (!config.isLogged()) return "redirect:/login";

        config.setGruppi(configurazioneService.getAllGruppi(String.valueOf(config.getIdSito())));

        try {
            UtenteEsterno utente = anagraficaService.findById(id);
            model.addAttribute("utente", utente);
            model.addAttribute("elencoAnagrafico", List.of(utente));
            model.addAttribute("registroAttivita", registroLeadService.findByUtente(id));
            model.addAttribute("config", config);
        } catch (Exception e) {
            log.error("Errore openAnagrafica id={}", id, e);
            return "redirect:/admin/crm/utenti";
        }

        return ViewUtils.resolveProtectedTemplate("crm/contenuti/dettaglioAnagrafica");
    }

    @PostMapping("/save")
    public String saveAnagrafica(
            @RequestParam(value = "id", required = false) Integer id,
            @ModelAttribute UtenteEsterno form,
            @RequestParam(value = "newpassword", defaultValue = "") String newPassword,
            @RequestParam(value = "newpasswordretype", defaultValue = "") String newPasswordRetype,
            @RequestParam(value = "avatarChanged", defaultValue = "0") String avatarChanged,
            @RequestParam(value = "avatarNum", required = false) Integer avatarNum,
            HttpServletRequest request, Model model) {

        HttpSession session = request.getSession();
        Configurazione config = configurazioneService.getConfig(session);
        if (!config.isLogged()) return "redirect:/login";

        config.setGruppi(configurazioneService.getAllGruppi(String.valueOf(config.getIdSito())));

        UtenteEsterno utente = form;

        try {
            String now = LocalDateTime.now().format(DF);
            boolean isNew = (id == null || id <= 0);

            if (isNew) {

                if (form.getEmail() != null && !form.getEmail().isBlank()) {
                    Integer esistente = anagraficaService.getIdByEmail(form.getEmail().trim());
                    if (esistente != null) {
                        model.addAttribute("errorMessage", "L'email " + form.getEmail() + " è già presente in anagrafica.");
                        model.addAttribute("utente", form);
                        model.addAttribute("elencoAnagrafico", List.of(form));
                        model.addAttribute("config", config);
                        return ViewUtils.resolveProtectedTemplate("crm/contenuti/dettaglioAnagrafica");
                    }
                }
                utente.setId(null);
                Random rnd = new Random(System.currentTimeMillis());
                int rnn = Math.abs(rnd.nextInt() / 1000);
                String username = (utente.getNome() != null && !utente.getNome().isEmpty()
                        ? utente.getNome() : "user") + "@" + rnn;
                utente.setUsername(username);
                utente.setPassword(username);
                utente.setDatacreazione(now);
                utente.setDataultimoaccesso(now);

                utente = anagraficaService.crea(utente);
                log.info("Utente creato: id={}", utente.getId());

            } else {
                // Carica dal DB per preservare i campi non presenti nel form
                UtenteEsterno db = anagraficaService.findById(id);

                // Merge solo i campi modificabili dal form
                db.setNome(form.getNome());
                db.setCognome(form.getCognome());
                db.setEmail(form.getEmail());
                db.setPec(form.getPec());
                db.setGenere(form.getGenere());
                db.setTelefono(form.getTelefono());
                db.setTelefono2(form.getTelefono2());
                db.setDatanascita(form.getDatanascita());
                db.setIndirizzo(form.getIndirizzo());
                db.setCap(form.getCap());
                db.setProvincia(form.getProvincia());
                db.setComune(form.getComune());
                db.setNazione(form.getNazione());
                db.setIndirizzospedizione(form.getIndirizzospedizione());
                db.setCodicefiscale(form.getCodicefiscale());
                db.setPartitaiva(form.getPartitaiva());
                db.setSocieta(form.getSocieta());
                db.setPersonagiuridica(form.getPersonagiuridica());
                db.setStatus(form.getStatus());
                db.setIdGruppo(form.getIdGruppo());
                db.setExtra1(form.getExtra1());
                db.setExtra2(form.getExtra2());
                db.setExtra3(form.getExtra3());
                db.setExtra4(form.getExtra4());
                db.setExtra5(form.getExtra5());
                db.setSottoscrizioni(form.getSottoscrizioni());
                db.setS2(form.getS2()); db.setS3(form.getS3());
                db.setS4(form.getS4()); db.setS5(form.getS5()); db.setS6(form.getS6());
                db.setS7(form.getS7()); db.setS8(form.getS8()); db.setS9(form.getS9());
                db.setS10(form.getS10());

                // Gestione avatar/immagine profilo
                if (avatarNum != null && avatarNum > 0) {
                    // Utente ha scelto un avatar predefinito
                    db.setS1(String.valueOf(avatarNum));
                    db.setProfileImage(0);
                } else {
                    Integer piForm = form.getProfileImage();
                    if (piForm != null && piForm == 1) {
                        // Foto custom già salvata via AJAX: aggiorna solo il flag,
                        // NON toccare db.image che è già persistito correttamente
                        db.setProfileImage(1);
                        db.setImage("/imageProfile/pfImage" + id + ".jpg");
                    }
                    // Se piForm è null o 0 non tocchiamo né profileImage né image:
                    // restano quelli già presenti nel DB

                    // Aggiorna S1 solo se è un valore intenzionale (non vuoto e non "0")
                    if (form.getS1() != null && !form.getS1().equals("0") && !form.getS1().isEmpty()) {
                        db.setS1(form.getS1());
                    }
                }

                if (!newPassword.isEmpty() && newPassword.equals(newPasswordRetype)) {
                    anagraficaService.cambiaPassword(id, newPassword);
                    log.info("Password cambiata per utente id={}", id);
                }

                utente = anagraficaService.salva(db);
                log.info("Utente salvato: id={}, profileImage={}, hasImage={}",
                        utente.getId(), utente.getProfileImage(), (utente.getImage() != null));
            }

            model.addAttribute("utente", utente);
            model.addAttribute("elencoAnagrafico", List.of(utente));
            model.addAttribute("config", config);

        } catch (Exception e) {
            log.error("Errore saveAnagrafica", e);
            model.addAttribute("error", "Errore nel salvataggio: " + e.getMessage());
            model.addAttribute("utente", utente);
            model.addAttribute("elencoAnagrafico", List.of(utente));
            model.addAttribute("config", config);
        }

        return ViewUtils.resolveProtectedTemplate("crm/contenuti/dettaglioAnagrafica");
    }

    @PostMapping("/cerca")
    public String ricercaAnagrafica(
            @ModelAttribute UtenteEsterno ricerca,
            HttpServletRequest request, Model model) {

        HttpSession session = request.getSession();
        Configurazione config = configurazioneService.getConfig(session);
        if (!config.isLogged()) return "redirect:/login";

        config.setGruppi(configurazioneService.getAllGruppi(String.valueOf(config.getIdSito())));

        try {
            List<UtenteEsterno> risultati = anagraficaService.cerca(ricerca);
            model.addAttribute("elencoAnagrafico", risultati);
            model.addAttribute("ricerca", ricerca);
            model.addAttribute("config", config);
        } catch (Exception e) {
            log.error("Errore ricercaAnagrafica", e);
            model.addAttribute("elencoAnagrafico", List.of());
            model.addAttribute("ricerca", ricerca);
            model.addAttribute("config", config);
        }

        return ViewUtils.resolveProtectedTemplate("crm/sezioni/elencoAnagrafica");
    }

    @PostMapping("/{id}/delete")
    @ResponseBody
    public ResponseEntity<String> deleteAnagrafica(
            @PathVariable Integer id,
            HttpServletRequest request) {

        HttpSession session = request.getSession();
        Configurazione config = configurazioneService.getConfig(session);
        if (!config.isLogged()) return ResponseEntity.status(401).body("KO");

        try {
            anagraficaService.elimina(id);
            log.info("Utente eliminato: id={}", id);
            return ResponseEntity.ok("OK");
        } catch (Exception e) {
            log.error("Errore deleteAnagrafica id={}", id, e);
            return ResponseEntity.ok("KO");
        }
    }

    @PostMapping("/delete-multiplo")
    @ResponseBody
    public String deleteMultiplo(
            @RequestParam(value = "ids[]", required = false) Integer[] ids,
            HttpServletRequest request) {

        HttpSession session = request.getSession();
        Configurazione config = configurazioneService.getConfig(session);
        if (!config.isLogged()) return "KO";
        if (ids == null || ids.length == 0) return "KO";

        try {
            for (Integer id : ids) anagraficaService.elimina(id);
            log.info("Delete multiplo utenti: {}", java.util.Arrays.toString(ids));
            return "OK";
        } catch (Exception e) {
            log.error("Errore delete multiplo", e);
            return "KO";
        }
    }

    @GetMapping("/{id}/duplicate")
    public String duplicaAnagrafica(
            @PathVariable Integer id,
            HttpServletRequest request, Model model) {

        HttpSession session = request.getSession();
        Configurazione config = configurazioneService.getConfig(session);
        if (!config.isLogged()) return "redirect:/login";

        config.setGruppi(configurazioneService.getAllGruppi(String.valueOf(config.getIdSito())));

        try {
            UtenteEsterno original = anagraficaService.findById(id);
            original.setId(null);
            original.setCognome(original.getCognome() + " (copia)");
            original.setEmail("");
            original.setUsername("");

            model.addAttribute("utente", original);
            model.addAttribute("elencoAnagrafico", List.of(original));
            model.addAttribute("config", config);

        } catch (Exception e) {
            log.error("Errore duplicaAnagrafica id={}", id, e);
            return "redirect:/admin/crm/utenti";
        }

        return ViewUtils.resolveProtectedTemplate("crm/contenuti/dettaglioAnagrafica");
    }

    @PostMapping("/{id}/password")
    @ResponseBody
    public ResponseEntity<String> modificaPassword(
            @PathVariable Integer id,
            @RequestParam String password,
            HttpServletRequest request) {

        HttpSession session = request.getSession();
        Configurazione config = configurazioneService.getConfig(session);
        if (!config.isLogged()) return ResponseEntity.status(401).body("KO");

        try {
            String nuovaPassword = anagraficaService.cambiaPassword(id, password);
            log.info("Password modificata per utente id={}", id);
            return ResponseEntity.ok(nuovaPassword);
        } catch (Exception e) {
            log.error("Errore modificaPassword id={}", id, e);
            return ResponseEntity.ok("KO");
        }
    }

    // In AnagraficaController, sostituisci il metodo searchJson

    @GetMapping("/search")
    @ResponseBody
    public List<Map<String, Object>> searchJson(@RequestParam(value="term", required=false) String term) {
        if (term == null || term.trim().length() < 2) return List.of();

        try {
            List<UtenteEsterno> utenti = anagraficaService.ricercaRapida(term);
            log.info("Ricerca rapida CRM per: {}", term);

            return utenti.stream().map(u -> {
                Map<String, Object> map = new java.util.LinkedHashMap<>();
                map.put("id",       u.getId());
                map.put("nome",     u.getNome());
                map.put("cognome",  u.getCognome());
                map.put("email",    u.getEmail());
                map.put("telefono", u.getTelefono());
                map.put("telefono2", u.getTelefono2());
                map.put("societa",  u.getSocieta());
                map.put("indirizzo", u.getIndirizzo());
                map.put("cap",      u.getCap());
                map.put("comune",   u.getComune());
                map.put("provincia", u.getProvincia());
                return map;
            }).toList();

        } catch (Exception e) {
            log.error("Errore searchJson per term: {}", term, e);
            return List.of();
        }
    }

    @PostMapping("/{id}/avatar/save")
    @ResponseBody
    public ResponseEntity<String> saveAvatar(
            @PathVariable Integer id,
            @RequestBody Map<String, String> body,
            HttpServletRequest request) {

        HttpSession session = request.getSession();
        Configurazione config = configurazioneService.getConfig(session);
        if (!config.isLogged()) return ResponseEntity.status(401).body("KO");

        try {
            String base64 = body.get("image");
            UtenteEsterno utente = anagraficaService.findById(id);
            if (base64 == null || base64.isEmpty()) {
                utente.setProfileImage(0);
            } else {
                utente.setImage(base64);
                utente.setProfileImage(1);
            }
            anagraficaService.salva(utente);
            return ResponseEntity.ok("OK");
        } catch (Exception e) {
            log.error("Errore saveAvatar CRM id={}", id, e);
            return ResponseEntity.ok("KO");
        }
    }

    @PostMapping("/import-csv")
    public String importaCsv(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "tipo", defaultValue = "esteso") String tipo,
            @RequestParam(value = "idGruppo", required = false, defaultValue = "") String idGruppo,
            @RequestParam(value = "status", required = false, defaultValue = "1") String status,
            @RequestParam(value = "newsletter", required = false, defaultValue = "") String newsletter,
            @RequestParam(value = "sms", required = false, defaultValue = "") String sms,
            HttpServletRequest request,
            RedirectAttributes redirectAttributes) {

        HttpSession session = request.getSession();
        Configurazione config = configurazioneService.getConfig(session);
        if (!config.isLogged()) return "redirect:/login";

        try {
            if (file.isEmpty()) {
                redirectAttributes.addFlashAttribute("csvErrore", "Seleziona un file CSV.");
                return "redirect:/admin/crm/utenti";
            }

            int importati;
            try (java.io.BufferedReader reader = new java.io.BufferedReader(
                    new java.io.InputStreamReader(file.getInputStream(), java.nio.charset.StandardCharsets.UTF_8))) {

                if ("esteso".equals(tipo)) {
                    List<String[]> righe = reader.lines()
                            .filter(riga -> !riga.isBlank())
                            .map(riga -> riga.split(","))
                            .toList();
                    importati = utenteEsternoService.importaCsvEsteso(righe, idGruppo, status, newsletter, sms);
                } else if ("semplificato".equals(tipo)) {
                    List<String[]> righe = reader.lines()
                            .filter(riga -> !riga.isBlank())
                            .map(riga -> riga.split(","))
                            .toList();
                    importati = utenteEsternoService.importaCsvSemplificato(righe, idGruppo, status, newsletter, sms);
                } else {
                    List<String> righe = reader.lines()
                            .map(String::trim)
                            .filter(riga -> !riga.isBlank())
                            .toList();
                    importati = utenteEsternoService.importaCsvSemplice(righe, idGruppo);
                }
            }

            redirectAttributes.addFlashAttribute("csvOk", "Importati " + importati + " contatti.");
        } catch (Exception e) {
            log.error("Errore import CSV contatti", e);
            redirectAttributes.addFlashAttribute("csvErrore", "Errore durante l'importazione del file.");
        }

        return "redirect:/admin/crm/utenti";
    }

    @PostMapping("/rimuovi-csv")
    public String rimuoviCsv(
            @RequestParam("file") MultipartFile file,
            HttpServletRequest request,
            RedirectAttributes redirectAttributes) {

        HttpSession session = request.getSession();
        Configurazione config = configurazioneService.getConfig(session);
        if (!config.isLogged()) return "redirect:/login";

        try {
            if (file.isEmpty()) {
                redirectAttributes.addFlashAttribute("csvErrore", "Seleziona un file CSV.");
                return "redirect:/admin/crm/utenti";
            }

            List<String> emails;
            try (java.io.BufferedReader reader = new java.io.BufferedReader(
                    new java.io.InputStreamReader(file.getInputStream(), java.nio.charset.StandardCharsets.UTF_8))) {
                emails = reader.lines()
                        .map(String::trim)
                        .filter(riga -> !riga.isBlank())
                        .toList();
            }

            int eliminati = anagraficaService.eliminaPerEmail(emails);
            redirectAttributes.addFlashAttribute("csvOk", "Rimossi " + eliminati + " contatti.");
        } catch (Exception e) {
            log.error("Errore rimozione CSV contatti", e);
            redirectAttributes.addFlashAttribute("csvErrore", "Errore durante la rimozione.");
        }

        return "redirect:/admin/crm/utenti";
    }
}