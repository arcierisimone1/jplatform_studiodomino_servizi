package com.jplatform_studiodomino.shared.service;

import com.jplatform_studiodomino.shared.entity.Activity;
import com.jplatform_studiodomino.shared.entity.MessaggioUtente;
import com.jplatform_studiodomino.shared.entity.UtenteEsterno;
import com.jplatform_studiodomino.shared.repository.UtenteEsternoRepository;
import com.jplatform_studiodomino.shared.util.PasswordService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class UtenteEsternoService {

    private final UtenteEsternoRepository utenteRepository;
    private final MessaggioUtenteService messaggioService;
    private final ActivityService activityService;
    private final PasswordService passwordService;

    private static final SimpleDateFormat DATE_FORMAT =
            new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");

    // ===== RICERCHE BASE =====

    /**
     * Trova utente per ID
     */
    public Optional<UtenteEsterno> findById(Integer id) {
        return utenteRepository.findById(id);
    }

    /**
     * Trova ID utente da email
     * Equivalente a getIdUtenteByEmail()
     */
    public Optional<Integer> getIdByEmail(String email) {
        return utenteRepository.findByEmailIgnoreCase(email)
                .map(UtenteEsterno::getId);
    }

    /**
     * Carica anagrafica completa con messaggi e activity
     * Equivalente a getAnagraficabyId()
     */
    public Optional<UtenteEsterno> getAnagraficaCompleta(String idUtente) {
        Optional<UtenteEsterno> utenteOpt = utenteRepository.findById(Integer.parseInt(idUtente));

        utenteOpt.ifPresent(utente -> {
            // Carica messaggi non letti
            List<MessaggioUtente> messaggi = messaggioService
                    .getMessaggiRicevutiConThread(idUtente, "utenteesterno");
            utente.setMessaggi(messaggi);

            // Carica activity
            List<Activity> activity = activityService.getAttivitaUtente(Integer.parseInt(idUtente));
            utente.setActivity(activity);
        });

        return utenteOpt;
    }

    /**
     * Carica anagrafica con path immagini
     */
    public Optional<UtenteEsterno> getAnagraficaCompleta(String idUtente, String filePath) {
        Optional<UtenteEsterno> utenteOpt = getAnagraficaCompleta(idUtente);
        utenteOpt.ifPresent(utente -> utente.setFilePath(filePath));
        return utenteOpt;
    }

    /**
     * Verifica esistenza email
     */
    public boolean verificaEmail(String email) {
        return utenteRepository.existsByEmailIgnoreCase(email);
    }

    /**
     * Verifica email e ritorna ID
     * Equivalente a verificaUtenteEmailReturnId()
     */
    public String verificaEmailReturnId(String email) {
        return utenteRepository.findByEmailIgnoreCase(email)
                .map(u -> u.getId().toString())
                .orElse("-1");
    }

    // ===== LOGIN =====

    /**
     * Login con username e password
     * Equivalente a loginAnagrafica()
     */
    public LoginResult login(String username, String password) {
        Optional<UtenteEsterno> utenteOpt = utenteRepository.findByUsernameIgnoreCase(username);

        if (utenteOpt.isEmpty()) {
            return LoginResult.notFound();
        }

        UtenteEsterno utente = utenteOpt.get();

        // Verifica password
        if (!passwordService.verificaPassword(password, utente.getPassword())) {
            return LoginResult.wrongPassword();
        }

        // Carica messaggi
        List<MessaggioUtente> messaggi = messaggioService
                .getMessaggiRicevutiConThread(utente.getId().toString(), "utenteesterno");
        utente.setMessaggi(messaggi);

        // Aggiorna ultimo accesso
        aggiornaUltimoAccesso(utente.getId());

        return LoginResult.success(utente);
    }

    /**
     * Login diretto da ID (sessione esistente)
     * Equivalente a loginAnagraficabyId()
     */
    public LoginResult loginById(String id) {
        Optional<UtenteEsterno> utenteOpt = utenteRepository.findById(Integer.parseInt(id));

        if (utenteOpt.isEmpty()) {
            return LoginResult.notFound();
        }

        UtenteEsterno utente = utenteOpt.get();

        // Carica messaggi
        List<MessaggioUtente> messaggi = messaggioService
                .getMessaggiRicevutiConThread(id, "utenteesterno");
        utente.setMessaggi(messaggi);

        return LoginResult.success(utente);
    }

    // ===== CRUD OPERATIONS =====

    /**
     * Crea nuovo utente
     * Equivalente a CreaAnagrafica()
     */
    public UtenteEsterno creaUtente(UtenteEsterno utente) {
        try {
            // Cripta password
            String passwordCriptata = passwordService.cryptPassword(utente.getPassword().toUpperCase());
            utente.setPassword(passwordCriptata);

            // Imposta date creazione
            String now = DATE_FORMAT.format(new Date());
            utente.setDatacreazione(now);
            utente.setDataultimoaccesso(now);

            // Salva
            UtenteEsterno saved = utenteRepository.save(utente);
            saved.setStatoAsInt(1); // Success

            log.info("Utente creato: id={}, username={}", saved.getId(), saved.getUsername());

            return saved;

        } catch (Exception e) {
            log.error("Errore creazione utente", e);
            utente.setStatoAsInt(-1); // Error
            return utente;
        }
    }

    /**
     * Aggiorna utente esistente
     * Equivalente a SalvaAnagrafica()
     */
    public UtenteEsterno aggiornaUtente(UtenteEsterno utente) {
        try {
            UtenteEsterno saved = utenteRepository.save(utente);
            log.info("Utente aggiornato: id={}, username={}", saved.getId(), saved.getUsername());
            return saved;

        } catch (Exception e) {
            log.error("Errore aggiornamento utente", e);
            utente.setStatoAsInt(-1);
            return utente;
        }
    }

    /**
     * Elimina utente
     * Equivalente a deleteAnagrafica()
     */
    public boolean eliminaUtente(Integer id) {
        try {
            utenteRepository.deleteById(id);
            log.info("Utente eliminato: id={}", id);
            return true;
        } catch (Exception e) {
            log.error("Errore eliminazione utente", e);
            return false;
        }
    }

    // ===== PASSWORD MANAGEMENT =====

    /**
     * Cambia password (amministratore)
     * Equivalente a CambiaPasswordAmministratore()
     */
    public boolean cambiaPasswordAdmin(Integer id, String nuovaPassword) {
        try {
            String passwordCriptata = passwordService.cryptPassword(nuovaPassword);
            utenteRepository.updatePassword(id, passwordCriptata);
            log.info("Password cambiata per utente: id={}", id);
            return true;
        } catch (Exception e) {
            log.error("Errore cambio password", e);
            return false;
        }
    }

    /**
     * Cambia password (utente stesso)
     */
    public boolean cambiaPassword(Integer id, String vecchiaPassword, String nuovaPassword) {
        Optional<UtenteEsterno> utenteOpt = utenteRepository.findById(id);

        if (utenteOpt.isEmpty()) {
            return false;
        }

        UtenteEsterno utente = utenteOpt.get();

        // Verifica vecchia password
        if (!passwordService.verificaPassword(vecchiaPassword, utente.getPassword())) {
            log.warn("Vecchia password errata per utente: id={}", id);
            return false;
        }

        // Cambia password
        return cambiaPasswordAdmin(id, nuovaPassword);
    }

    /**
     * Reset password (genera nuova casuale)
     */
    public String resetPassword(Integer id) {
        String nuovaPassword = passwordService.generaPasswordCasuale(10);

        if (cambiaPasswordAdmin(id, nuovaPassword)) {
            log.info("Password resettata per utente: id={}", id);
            return nuovaPassword;
        }

        return null;
    }

    // ===== UPDATE FIELDS =====

    /**
     * Aggiorna campo generico
     * Equivalente a cambiaValoreCampo()
     */
    public boolean aggiornaCampo(String nomeCampo, String valore, Integer id) {
        try {
            String condition = "WHERE id = " + id;
            utenteRepository.updateCampoGenerico(nomeCampo, valore, condition);
            return true;
        } catch (Exception e) {
            log.error("Errore aggiornamento campo generico", e);
            return false;
        }
    }

    /**
     * Aggiorna navigation profile
     */
    public void aggiornaNavigationProfile(Integer id, String profile) {
        utenteRepository.updateNavigationProfile(id, profile);
    }

    /**
     * Aggiorna ultimo accesso
     */
    public void aggiornaUltimoAccesso(Integer id) {
        String now = DATE_FORMAT.format(new Date());
        utenteRepository.updateUltimoAccesso(id, now);
    }

    /**
     * Aggiorna stato
     */
    public void aggiornaStato(Integer id, String stato) {
        utenteRepository.updateStatus(id, stato);
    }

    /**
     * Aggiorna profile image
     */
    public void aggiornaProfileImage(Integer id, Integer profileImage) {
        utenteRepository.updateProfileImage(id, profileImage);
    }

    // ===== QUERY AVANZATE =====

    /**
     * Elenco utenti con SQL custom (compatibilità legacy)
     * Equivalente a getElencoAnagraficaMinimalSql()
     */
    public List<UtenteEsterno> getElencoCustomSql(String sqlCondition) {
        return utenteRepository.findWithCustomSql(sqlCondition);
    }

    /**
     * Ricerca utenti per nome/cognome
     */
    public List<UtenteEsterno> ricercaUtenti(String searchTerm) {
        return utenteRepository.searchByNomeCognome(searchTerm);
    }

    /**
     * Utenti attivi
     */
    public List<UtenteEsterno> getUtentiAttivi() {
        return utenteRepository.findUtentiAttivi();
    }

    /**
     * Utenti per gruppo
     */
    public List<UtenteEsterno> getUtentiPerGruppo(String idGruppo) {
        String pattern = "(" + idGruppo + ");";
        return utenteRepository.findByGruppo(pattern);
    }

    /**
     * Utenti per comune
     */
    public List<UtenteEsterno> getUtentiPerComune(String comune) {
        return utenteRepository.findByComuneOrderByCognomeAscNomeAsc(comune);
    }

    // ===== IMPORT CSV =====

    /**
     * Importa utenti da CSV semplice
     * Equivalente a caricaCsv()
     */
    public int importaCsvSemplice(List<String> contatti, String idGruppo) {
        int totale = 0;

        for (String contatto : contatti) {
            try {
                UtenteEsterno utente = UtenteEsterno.builder()
                        .nome(contatto)
                        .cognome(contatto)
                        .username(contatto)
                        .password(passwordService.cryptPassword(contatto))
                        .idGruppo("(" + idGruppo + ");")
                        .status("1")
                        .email(contatto + "@temp.com")
                        .datacreazione(DATE_FORMAT.format(new Date()))
                        .build();

                utenteRepository.save(utente);
                totale++;

            } catch (Exception e) {
                log.error("Errore import CSV contatto: {}", contatto, e);
            }
        }

        log.info("Importati {} utenti da CSV", totale);
        return totale;
    }

    /**
     * Importa utenti da CSV semplificato (4 colonne: nome, cognome, email, telefono2)
     * Equivalente a caricaCsvSemplificato()
     */
    public int importaCsvSemplificato(List<String[]> contatti, String idGruppo, String status, String newsletter, String sms) {
        int totale = 0;

        for (String[] row : contatti) {
            try {
                if (row.length < 4) {
                    log.warn("Riga CSV semplificato incompleta, skip");
                    continue;
                }

                String email = pulisci(row[2]);

                if (verificaEmail(email)) {
                    log.debug("Email già esistente: {}, skip", email);
                    continue;
                }

                UtenteEsterno utente = UtenteEsterno.builder()
                        .nome(pulisci(row[0]))
                        .cognome(pulisci(row[1]))
                        .email(email)
                        .telefono2(pulisci(row[3]))
                        .username(email)
                        .password(passwordService.cryptPassword(email))
                        .idGruppo("(" + idGruppo + ");")
                        .status(status != null && !status.isBlank() ? status : "1")
                        .l1(newsletter != null ? newsletter : "")
                        .l2(sms != null ? sms : "")
                        .datacreazione(DATE_FORMAT.format(new Date()))
                        .build();

                utenteRepository.save(utente);
                totale++;

            } catch (Exception e) {
                log.error("Errore import CSV semplificato riga", e);
            }
        }

        log.info("Importati {} utenti da CSV semplificato", totale);
        return totale;
    }

    /**
     * Importa utenti da CSV esteso
     * Equivalente a caricaCsvEsteso()
     */
    public int importaCsvEsteso(List<String[]> contatti, String idGruppo, String status, String newsletter, String sms) {
        int totale = 0;

        for (String[] row : contatti) {
            try {
                if (row.length < 14) {
                    log.warn("Riga CSV incompleta, skip");
                    continue;
                }

                String email = pulisci(row[2]);

                // Skip se email già esistente
                if (verificaEmail(email)) {
                    log.debug("Email già esistente: {}, skip", email);
                    continue;
                }

                UtenteEsterno utente = UtenteEsterno.builder()
                        .nome(pulisci(row[0]))
                        .cognome(pulisci(row[1]))
                        .email(email)
                        .telefono2(pulisci(row[3]))
                        .telefono(pulisci(row[4]))
                        .indirizzo(pulisci(row[5]))
                        .comune(pulisci(row[6]))
                        .cap(pulisci(row[7]))
                        .provincia(pulisci(row[8]))
                        .nazione(pulisci(row[9]))
                        .pec(pulisci(row[10]))
                        .codicefiscale(pulisci(row[11]))
                        .datanascita(pulisci(row[12]))
                        .genere(pulisci(row[13]))
                        .username(email)
                        .password(passwordService.cryptPassword(email))
                        .idGruppo("(" + idGruppo + ");")
                        .status(status != null && !status.isBlank() ? status : "1")
                        .l1(newsletter != null ? newsletter : "")
                        .l2(sms != null ? sms : "")
                        .datacreazione(DATE_FORMAT.format(new Date()))
                        .build();

                utenteRepository.save(utente);
                totale++;

            } catch (Exception e) {
                log.error("Errore import CSV esteso riga", e);
            }
        }

        log.info("Importati {} utenti da CSV esteso", totale);
        return totale;
    }

    /**
     * Pulisce stringa CSV (rimuove virgolette)
     */
    private String pulisci(String value) {
        if (value == null) return "";
        return value.replaceAll("\"", "").trim();
    }

    // ===== DTO RESULT =====

    /**
     * Risultato login
     */
    public record LoginResult(
            int stato,
            UtenteEsterno utente,
            String messaggio
    ) {
        public static LoginResult success(UtenteEsterno utente) {
            return new LoginResult(0, utente, "Login effettuato");
        }

        public static LoginResult notFound() {
            return new LoginResult(-1, null, "Utente non trovato");
        }

        public static LoginResult wrongPassword() {
            return new LoginResult(-2, null, "Password errata");
        }

        public boolean isSuccess() {
            return stato == 0;
        }
    }
}