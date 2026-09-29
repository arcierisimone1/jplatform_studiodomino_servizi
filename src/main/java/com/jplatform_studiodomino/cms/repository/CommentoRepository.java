package com.jplatform_studiodomino.cms.repository;

import com.jplatform_studiodomino.cms.entity.Commento;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CommentoRepository extends JpaRepository<Commento, Integer> {

    // ===== RICERCHE PER OGGETTO =====

    /**
     * Trova commenti per oggetto (contenuto)
     */
    List<Commento> findByIdoggettoOrderByIdDesc(String idoggetto);

    /**
     * Trova commenti approvati per oggetto
     */
    @Query("SELECT c FROM Commento c WHERE c.idoggetto = :idoggetto AND c.stato = '1' ORDER BY c.id ASC")
    List<Commento> findCommentiApprovatiByOggetto(@Param("idoggetto") String idoggetto);

    /**
     * Trova commenti principali (no reply) per oggetto
     */
    @Query("SELECT c FROM Commento c WHERE c.idoggetto = :idoggetto AND c.idparent = '0' ORDER BY c.id DESC")
    List<Commento> findCommentiPrincipaliByOggetto(@Param("idoggetto") String idoggetto);

    /**
     * Trova commenti principali approvati per oggetto
     */
    @Query("SELECT c FROM Commento c WHERE c.idoggetto = :idoggetto AND c.idparent = '0' AND c.stato = '1' ORDER BY c.id ASC")
    List<Commento> findCommentiPrincipaliApprovatiByOggetto(@Param("idoggetto") String idoggetto);

    // ===== RICERCHE PER THREADING =====

    /**
     * Trova risposte a commento (thread)
     */
    @Query("SELECT c FROM Commento c WHERE c.idparent = :idParent ORDER BY c.id ASC")
    List<Commento> findRisposteCommento(@Param("idParent") String idParent);

    /**
     * Trova risposte approvate a commento
     */
    @Query("SELECT c FROM Commento c WHERE c.idparent = :idParent AND c.stato = '1' ORDER BY c.id ASC")
    List<Commento> findRisposteApprovateCommento(@Param("idParent") String idParent);

    // ===== RICERCHE PER UTENTE =====

    /**
     * Trova commenti per utente
     */
    List<Commento> findByIduserOrderByIdDesc(String iduser);

    /**
     * Trova commenti per email
     */
    List<Commento> findByEmailOrderByIdDesc(String email);

    // ===== RICERCHE PER MODERAZIONE =====

    /**
     * Trova commenti per stato
     */
    List<Commento> findByStatoOrderByIdDesc(String stato);

    /**
     * Trova commenti in attesa moderazione
     */
    @Query("SELECT c FROM Commento c WHERE c.stato = '0' ORDER BY c.id DESC")
    List<Commento> findCommentiInAttesa();

    /**
     * Conta commenti in attesa per oggetto
     */
    @Query("SELECT COUNT(c) FROM Commento c WHERE c.idoggetto = :idoggetto AND c.stato = '0'")
    long countCommentiInAttesaByOggetto(@Param("idoggetto") String idoggetto);

    /**
     * Conta commenti approvati per oggetto
     */
    @Query("SELECT COUNT(c) FROM Commento c WHERE c.idoggetto = :idoggetto AND c.stato = '1'")
    long countCommentiApprovatiByOggetto(@Param("idoggetto") String idoggetto);

    // ===== RICERCHE PER TIPOLOGIA =====

    /**
     * Trova commenti per tipologia
     */
    List<Commento> findByTipologiaOrderByIdDesc(String tipologia);

    /**
     * Trova commenti per oggetto e tipologia
     */
    @Query("SELECT c FROM Commento c WHERE c.idoggetto = :idoggetto AND c.tipologia = :tipologia ORDER BY c.id DESC")
    List<Commento> findByOggettoAndTipologia(
            @Param("idoggetto") String idoggetto,
            @Param("tipologia") String tipologia
    );

    /**
     * Trova messaggi web per tipologia e stato (Gestione Messaggi WEB)
     */
    List<Commento> findByTipologiaAndStatoOrderByIdDesc(String tipologia, String stato);

    /**
     * Stessa query, paginata — usata dall'elenco "Gestione Messaggi WEB".
     */
    Page<Commento> findByTipologiaAndStatoOrderByIdDesc(
            String tipologia, String stato, org.springframework.data.domain.Pageable pageable);

    /**
     * Conta messaggi per tipologia e stato (usato dal contatore "Messaggi ricevuti" in dashboard CRM)
     */
    long countByTipologiaAndStato(String tipologia, String stato);

    // ===== STATISTICHE =====

    /**
     * Conta totale commenti per oggetto
     */
    long countByIdoggetto(String idoggetto);

    /**
     * Conta commenti per utente
     */
    long countByIduser(String iduser);
}