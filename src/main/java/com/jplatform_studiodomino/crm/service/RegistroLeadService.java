package com.jplatform_studiodomino.crm.service;

import com.jplatform_studiodomino.cms.admin.service.EmailSenderService;
import com.jplatform_studiodomino.shared.entity.Account;
import com.jplatform_studiodomino.shared.repository.AccountRepository;
import com.jplatform_studiodomino.cms.entity.Commento;
import com.jplatform_studiodomino.cms.service.CommentoService;
import com.jplatform_studiodomino.crm.entity.RegistroLead;
import com.jplatform_studiodomino.crm.repository.AreaInteresseRepository;
import com.jplatform_studiodomino.crm.repository.RegistroLeadRepository;
import com.jplatform_studiodomino.shared.config.Configurazione;
import com.jplatform_studiodomino.shared.entity.Utente;
import com.jplatform_studiodomino.shared.entity.UtenteEsterno;
import com.jplatform_studiodomino.shared.repository.UtenteEsternoRepository;
import com.jplatform_studiodomino.shared.repository.UtenteRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Random;

/**
 * Service per la gestione del Registro LEAD CRM.
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class RegistroLeadService {

    private final RegistroLeadRepository registroLeadRepository;
    private final UtenteEsternoRepository utenteEsternoRepository;
    private final UtenteRepository utenteRepository;
    private final AnagraficaService anagraficaService;
    private final AreaInteresseRepository areaInteresseRepository;
    private final CommentoService commentoService;
    private final AccountRepository accountRepository;

    // Adatta questi due service ai nomi reali del tuo progetto
    private final EmailSenderService emailSenderService;
    private final SmsSenderService smsSenderService;

    // =====================================================================
    // FIND
    // =====================================================================

    private static final int REGISTRO_LEAD_PAGE_SIZE = 20;

    /**
     * Elenco paginato di Registro Lead per direzione/stato (usato da "Gestione Registro Lead").
     * Fix: il filtro per stato viene sempre applicato, anche per "Completato" (stato "4"),
     */
    public org.springframework.data.domain.Page<RegistroLead> findAllPaged(String direzione, String stato, int page) {
        org.springframework.data.domain.Pageable pageable =
                org.springframework.data.domain.PageRequest.of(Math.max(page, 0), REGISTRO_LEAD_PAGE_SIZE);
        return registroLeadRepository.findByDirezioneAndStatoOrderByIdDesc(direzione, stato, pageable);
    }

    public RegistroLead findById(Long id) {
        return registroLeadRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("RegistroLead non trovato: " + id));
    }

    public UtenteEsterno findUtenteBase(Integer idUtente) {
        if (idUtente == null || idUtente <= 0) return new UtenteEsterno();
        return utenteEsternoRepository.findById(idUtente).orElse(new UtenteEsterno());
    }

    /**
     * Tutte le attività/richieste (RegistroLead) collegate a questo contatto.
     * Equivalente al vecchio "Registro delle attività" nella scheda anagrafica.
     */
    public List<RegistroLead> findByUtente(Integer idUtente) {
        if (idUtente == null || idUtente <= 0) return List.of();
        return registroLeadRepository.findByIdutenteOrderByIdDesc(idUtente);
    }

    public Utente findAmministratoreById(String id) {
        if (id == null || id.isBlank()) return null;
        return utenteRepository.findById(Integer.parseInt(id)).orElse(null);
    }

    public List<Utente> searchAmministratori(String term) {
        if (term == null || term.isBlank()) return List.of();
        String t = term.trim().toLowerCase();
        return utenteRepository.findAll().stream()
                .filter(u -> (u.getNome() != null && u.getNome().toLowerCase().contains(t))
                        || (u.getCognome() != null && u.getCognome().toLowerCase().contains(t)))
                .limit(20)
                .toList();
    }

    public List<?> findAreeInteresse() {
        return areaInteresseRepository.findAllByOrderByDescrizioneAsc();
    }

    public Commento findCommentoById(Long idLeadStore) {
        return commentoService.findById(idLeadStore.intValue()).orElse(null);
    }

    public Integer findIdUtenteByEmail(String email) {
        return anagraficaService.getIdByEmail(email);
    }

    public Object findEmailStoreById(Integer idLeadStore) {
        log.warn("findEmailStoreById: EmailService non ancora disponibile");
        return null;
    }

    public List<RegistroLead> findAllByStore(String direzione, String stato, String store) {
        if ("4".equals(stato)) {
            return registroLeadRepository.findByDirezioneAndStoreOrderByIdDesc(direzione, store);
        }
        return registroLeadRepository.findByDirezioneAndStatoAndStoreOrderByIdDesc(direzione, stato, store);
    }

    // =====================================================================
    // CREA
    // =====================================================================

    @Transactional
    public RegistroLead crea(RegistroLead registroLead, Configurazione config) {
        log.info("Creazione registro lead store={}", registroLead.getStore());

        if (registroLead.getId() == null || registroLead.getId() <= 0) {
            registroLead.setId(null);
        }

        if ("diretto".equalsIgnoreCase(registroLead.getStore())) {

            // CASO 1: utente esistente selezionato dall'autocomplete
            if (registroLead.getIdutente() > 0) {
                UtenteEsterno utente = findUtenteBase(registroLead.getIdutente());
                registroLead.setUtente(utente);
            }
            // CASO 2: utente nuovo inserito a mano
            else if (registroLead.getUtente() != null) {
                UtenteEsterno utente = creaOAggiornaUtentePerLead(registroLead, config);
                registroLead.setUtente(utente);
                registroLead.setIdutente(utente.getId());
            }
        }

        RegistroLead saved = registroLeadRepository.save(registroLead);
        log.info("RegistroLead creato: id={}", saved.getId());

        aggiornaStatoSorgente(saved);
        return saved;
    }

    // =====================================================================
    // SALVA
    // =====================================================================

    @Transactional
    public RegistroLead salva(RegistroLead registroLead) {
        log.debug("Salvataggio registro lead: id={}", registroLead.getId());
        return registroLeadRepository.save(registroLead);
    }

    // =====================================================================
    // ELIMINA
    // =====================================================================

    @Transactional
    public void elimina(Long id) {
        registroLeadRepository.deleteById(id);
        log.info("RegistroLead eliminato: id={}", id);
    }

    // =====================================================================
    // INVIA SMS
    // =====================================================================

    public void inviaSms(RegistroLead registroLead, Configurazione config) {
        String telefono = estraiTelefonoDestinatario(registroLead);

        log.info("Invio SMS a: {}", telefono);

        if (telefono == null || telefono.isBlank()) {
            throw new RuntimeException("Numero destinatario assente");
        }

        String testo = registroLead.getNotalead() != null ? registroLead.getNotalead().trim() : "";
        if (testo.isBlank()) {
            throw new RuntimeException("Testo SMS assente");
        }

        String tipoMittente = registroLead.getL2() != null ? registroLead.getL2().trim() : "";

        smsSenderService.inviaSms(telefono, testo, tipoMittente, config);
    }

    // =====================================================================
    // INVIA EMAIL
    // =====================================================================

    public void inviaEmail(RegistroLead registroLead, Configurazione config) {
        String destinatario = estraiEmailDestinatario(registroLead);

        log.info("Invio Email a: {}", destinatario);

        if (destinatario == null || destinatario.isBlank()) {
            throw new RuntimeException("Destinatario email assente");
        }

        String oggetto = registroLead.getL3() != null ? registroLead.getL3().trim() : "";
        if (oggetto.isBlank()) {
            throw new RuntimeException("Oggetto email assente");
        }

        String testo = registroLead.getNotalead() != null ? registroLead.getNotalead() : "";
        if (testo.isBlank()) {
            throw new RuntimeException("Corpo email assente");
        }

        // Firma automatica
        if ("1".equals(registroLead.getL2())
                && config != null
                && config.getAmministratore() != null
                && config.getAmministratore().getExtra1() != null
                && !config.getAmministratore().getExtra1().isBlank()) {

            int pos = testo.indexOf("</body>");
            if (pos > 0) {
                testo = testo.substring(0, pos)
                        + config.getAmministratore().getExtra1()
                        + testo.substring(pos);
            } else {
                testo = testo + config.getAmministratore().getExtra1();
            }
        }

        // Account di posta scelto dall'operatore (campo l1) — se non trovato,
        // ripiega sull'account di default come prima
        Account account = null;
        if (registroLead.getL1() != null && !registroLead.getL1().isBlank()) {
            try {
                account = accountRepository.findById(Integer.parseInt(registroLead.getL1().trim())).orElse(null);
            } catch (NumberFormatException e) {
                log.warn("Id account email non valido: {}", registroLead.getL1());
            }
        }

        if (account != null) {
            emailSenderService.inviaEmailConAccount(account, destinatario, null, oggetto, testo, null);
        } else {
            emailSenderService.inviaEmail(destinatario, null, oggetto, testo);
        }
    }

    /** Account email selezionabili dall'operatore (campo utente.idaccountemail, CSV di id). */
    public List<Account> findAccountsEmail(String idAccountEmailCsv) {
        if (idAccountEmailCsv == null || idAccountEmailCsv.isBlank()) return List.of();
        return java.util.Arrays.stream(idAccountEmailCsv.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .map(s -> {
                    try {
                        return accountRepository.findById(Integer.parseInt(s)).orElse(null);
                    } catch (NumberFormatException e) {
                        return null;
                    }
                })
                .filter(java.util.Objects::nonNull)
                .toList();
    }

    // =====================================================================
    // UTILITY PRIVATI
    // =====================================================================

    private String estraiEmailDestinatario(RegistroLead registroLead) {
        if (registroLead == null) return null;

        if (registroLead.getUtente() != null
                && registroLead.getUtente().getEmail() != null
                && !registroLead.getUtente().getEmail().isBlank()) {
            return registroLead.getUtente().getEmail().trim();
        }

        if (registroLead.getIdutente() != 0 && registroLead.getIdutente() > 0) {
            UtenteEsterno utente = findUtenteBase(registroLead.getIdutente());
            if (utente.getEmail() != null && !utente.getEmail().isBlank()) {
                return utente.getEmail().trim();
            }
        }

        return null;
    }

    private String estraiTelefonoDestinatario(RegistroLead registroLead) {
        if (registroLead == null) return null;

        if (registroLead.getUtente() != null) {
            if (registroLead.getUtente().getTelefono2() != null
                    && !registroLead.getUtente().getTelefono2().isBlank()) {
                return registroLead.getUtente().getTelefono2().trim();
            }

            if (registroLead.getUtente().getTelefono() != null
                    && !registroLead.getUtente().getTelefono().isBlank()) {
                return registroLead.getUtente().getTelefono().trim();
            }
        }

        if (registroLead.getIdutente() != 0 && registroLead.getIdutente() > 0) {
            UtenteEsterno utente = findUtenteBase(registroLead.getIdutente());

            if (utente.getTelefono2() != null && !utente.getTelefono2().isBlank()) {
                return utente.getTelefono2().trim();
            }

            if (utente.getTelefono() != null && !utente.getTelefono().isBlank()) {
                return utente.getTelefono().trim();
            }
        }

        return null;
    }

    /**
     * Crea o aggiorna utente esterno per lead diretto.
     */
    private UtenteEsterno creaOAggiornaUtentePerLead(RegistroLead lead, Configurazione config) {
        UtenteEsterno datiUtente = lead.getUtente();
        if (datiUtente == null) {
            throw new RuntimeException("Utente esterno assente");
        }

        String email = datiUtente.getEmail() != null ? datiUtente.getEmail().trim() : "";

        Integer idEsistente = null;
        if (!email.isBlank()) {
            idEsistente = anagraficaService.getIdByEmail(email);
        }

        UtenteEsterno utente = idEsistente != null
                ? anagraficaService.findByIdSafe(idEsistente)
                : new UtenteEsterno();

        Random rnd = new Random(System.currentTimeMillis());
        int rnn = Math.abs(rnd.nextInt() / 1000);

        utente.setNome(datiUtente.getNome());
        utente.setCognome(datiUtente.getCognome());
        utente.setEmail(email);
        utente.setUsername(!email.isBlank() ? email : "utente" + rnn);
        utente.setPassword(Integer.toString(rnn));
        utente.setTelefono(datiUtente.getTelefono());
        utente.setTelefono2(datiUtente.getTelefono2());
        utente.setL1("1");
        utente.setL2("1");

        if (utente.getStatus() == null || utente.getStatus().isBlank()) {
            utente.setStatus("1");
        }

        if (config.getSito() != null && config.getSito().getLibero3() != null) {
            try {
                utente.setNazione(config.getSito().getLibero3());
            } catch (Exception e) {
                utente.setNazione("1");
            }
        } else if (utente.getNazione() == null || utente.getNazione().isBlank()) {
            utente.setNazione("1");
        }

        return idEsistente == null
                ? anagraficaService.crea(utente)
                : anagraficaService.salva(utente);
    }

    @Transactional
    private void aggiornaStatoSorgente(RegistroLead lead) {
        if ("commenti".equals(lead.getStore())) {
            commentoService.findById(lead.getIdleadstore()).ifPresent(commento -> {
                commento.setStato("2"); // "2" = già profilato in Lead
                commentoService.save(commento);
            });
        } else if ("emailstore".equals(lead.getStore())) {
            log.debug("TODO: aggiorna emailstore id={} stato=2", lead.getIdleadstore());
        }
    }
}