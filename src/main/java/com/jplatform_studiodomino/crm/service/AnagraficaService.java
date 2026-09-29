package com.jplatform_studiodomino.crm.service;

import com.jplatform_studiodomino.shared.entity.UtenteEsterno;
import com.jplatform_studiodomino.shared.repository.UtenteEsternoRepository;
import com.jplatform_studiodomino.shared.util.CryptBean;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Service per la gestione dell'anagrafica utenti CRM.
 * Conversione da DAOUtenteEsterno (JDBC raw) a Spring Service + JPA.
 * Usa UtenteEsternoRepository esistente in shared/repository.
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class AnagraficaService {

    private final UtenteEsternoRepository utenteEsternoRepository;

    // =====================================================================
    // FIND
    // Vecchio: getAnagraficabyId(id)
    // =====================================================================

    public UtenteEsterno findById(Integer id) {
        return utenteEsternoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Utente non trovato: " + id));
    }

    public UtenteEsterno findByIdSafe(Integer id) {
        if (id == null || id <= 0) return new UtenteEsterno();
        return utenteEsternoRepository.findById(id).orElse(new UtenteEsterno());
    }

    // Vecchio: getIdUtenteByEmail
    public Integer getIdByEmail(String email) {
        return utenteEsternoRepository.findByEmailIgnoreCase(email)
                .map(UtenteEsterno::getId)
                .orElse(null);
    }

    public List<UtenteEsterno> findAll() {
        return utenteEsternoRepository.findUtentiAttivi();
    }

    // Vecchio: verificaUtenteEmail
    public boolean esistePerEmail(String email) {
        return utenteEsternoRepository.existsByEmailIgnoreCase(email);
    }

    // =====================================================================
    // RICERCA DINAMICA
    // Vecchio: getElencoAnagraficaMinimalSql(sql, filePath)
    // Usa i metodi del repository esistente, con fallback filtro in memoria
    // per combinazioni multi-campo.
    // =====================================================================

    public List<UtenteEsterno> cerca(UtenteEsterno filtro) {
        log.debug("Ricerca anagrafica con filtri");

        // Se c'è solo nome/cognome usa la query ottimizzata del repository
        boolean soloNomeCognome = isBlank(filtro.getEmail())
                && isBlank(filtro.getCodicefiscale())
                && isBlank(filtro.getTelefono())
                && isBlank(filtro.getIndirizzo())
                && isBlank(filtro.getComune())
                && isBlank(filtro.getPec());

        String termineNomeCognome = nonBlank(filtro.getNome(), filtro.getCognome());

        if (soloNomeCognome && termineNomeCognome != null) {
            return utenteEsternoRepository.searchByNomeCognome(termineNomeCognome);
        }

        // Nessun filtro → non mostrare nulla (l'utente deve inserire almeno un criterio)
        if (soloNomeCognome) {
            return java.util.List.of();
        }

        // Filtri multipli: carica tutti gli attivi e filtra in memoria.
        // (Alternativa futura: implementare JPA Specification o query @Query dedicata)
        return utenteEsternoRepository.findUtentiAttivi().stream()
                .filter(u -> matchesIfSet(filtro.getEmail(), u.getEmail()))
                .filter(u -> matchesIfSet(filtro.getNome(), u.getNome()))
                .filter(u -> matchesIfSet(filtro.getCognome(), u.getCognome()))
                .filter(u -> matchesIfSet(filtro.getCodicefiscale(), u.getCodicefiscale()))
                .filter(u -> matchesIfSet(filtro.getTelefono(),
                        u.getTelefono() + " " + u.getTelefono2()))
                .filter(u -> matchesIfSet(filtro.getIndirizzo(), u.getIndirizzo()))
                .filter(u -> matchesIfSet(filtro.getComune(), u.getComune()))
                .filter(u -> matchesIfSet(filtro.getPec(), u.getPec()))
                .toList();
    }

    // =====================================================================
    // CREA
    // Vecchio: CreaAnagrafica(utente)
    // =====================================================================

    @Transactional
    public UtenteEsterno crea(UtenteEsterno utente) {
        log.debug("Creazione utente: {}", utente.getEmail());
        String passwordCriptata = CryptBean.cryptString(
                utente.getPassword() != null ? utente.getPassword().toUpperCase() : "");
        utente.setPassword(passwordCriptata);
        UtenteEsterno saved = utenteEsternoRepository.save(utente);
        log.info("Utente creato: id={}", saved.getId());
        return saved;
    }

    // =====================================================================
    // SALVA
    // Vecchio: SalvaAnagrafica(utente)
    // =====================================================================

    @Transactional
    public UtenteEsterno salva(UtenteEsterno utente) {
        log.debug("Salvataggio utente: id={}", utente.getId());
        // Mantieni la password attuale — si cambia solo con cambiaPassword()
        UtenteEsterno esistente = findById(utente.getId());
        utente.setPassword(esistente.getPassword());
        return utenteEsternoRepository.save(utente);
    }

    // =====================================================================
    // ELIMINA
    // Vecchio: deleteAnagrafica(id)
    // =====================================================================

    @Transactional
    public void elimina(Integer id) {
        utenteEsternoRepository.deleteById(id);
        log.info("Utente eliminato: id={}", id);
    }

    @Transactional
    public int eliminaPerEmail(List<String> emails) {
        int eliminati = 0;
        for (String email : emails) {
            if (email == null || email.isBlank()) continue;
            var utente = utenteEsternoRepository.findByEmailIgnoreCase(email.trim());
            if (utente.isPresent()) {
                utenteEsternoRepository.deleteById(utente.get().getId());
                eliminati++;
            }
        }
        log.info("Eliminati {} contatti da CSV", eliminati);
        return eliminati;
    }

    // =====================================================================
    // CAMBIA PASSWORD
    // Vecchio: CambiaPasswordAmministratore(id, password)
    // =====================================================================

    @Transactional
    public String cambiaPassword(Integer id, String nuovaPassword) {
        String passwordCriptata = CryptBean.cryptString(nuovaPassword);
        utenteEsternoRepository.updatePassword(id, passwordCriptata);
        log.info("Password aggiornata per utente id={}", id);
        return passwordCriptata;
    }

    // =====================================================================
    // LOGIN
    // Vecchio: loginAnagrafica(username, password)
    // =====================================================================

    public UtenteEsterno login(String username, String password) {
        return utenteEsternoRepository.findByUsernameIgnoreCase(username)
                .map(utente -> {
                    String p1 = CryptBean.cryptString(password);
                    String p2 = CryptBean.cryptString(password.toUpperCase());
                    if (utente.getPassword().equals(p1) || utente.getPassword().equals(p2)) {
                        return utente;
                    }
                    UtenteEsterno ko = new UtenteEsterno();
                    ko.setNazione("-2"); // password errata
                    return ko;
                })
                .orElseGet(() -> {
                    UtenteEsterno ko = new UtenteEsterno();
                    ko.setNazione("-1"); // non trovato
                    return ko;
                });
    }

    // =====================================================================
    // UTILITY PRIVATI
    // =====================================================================

    private boolean isBlank(String s) {
        return s == null || s.isBlank();
    }

    /** Restituisce il primo valore non blank tra quelli passati, null se tutti blank. */
    private String nonBlank(String... values) {
        for (String v : values) {
            if (!isBlank(v)) return v;
        }
        return null;
    }

    /** True se il filtro è blank (non applicare) oppure il valore contiene il filtro (case-insensitive). */
    private boolean matchesIfSet(String filtro, String valore) {
        if (isBlank(filtro)) return true;
        if (valore == null) return false;
        return valore.toLowerCase().contains(filtro.toLowerCase());
    }

    // Aggiungi questo metodo nel tuo AnagraficaService
    public List<UtenteEsterno> ricercaRapida(String term) {
        log.debug("Ricerca rapida CRM per: {}", term);
        // Utilizza direttamente la query ottimizzata che cerca in Nome O Cognome
        return utenteEsternoRepository.searchByNomeCognome(term);
    }
}