package com.jplatform_studiodomino.crm.repository;

import com.jplatform_studiodomino.crm.entity.RegistroLead;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository JPA per la tabella 'registrolead'.
 * Conversione da DAORegistroLead (JDBC raw) a Spring Data JPA.
 */
@Repository
public interface RegistroLeadRepository extends JpaRepository<RegistroLead, Long> {

    // ===== ELENCO =====

    /**
     * Tutti i lead per direzione.
     * Vecchio: getRegistroLead(direzione, "4") — stato 4 = mostra tutti
     */
    List<RegistroLead> findByDirezioneOrderByIdDesc(String direzione);

    /**
     * Lead per direzione e stato.
     * Vecchio: getRegistroLead(direzione, stato)
     */
    List<RegistroLead> findByDirezioneAndStatoOrderByIdDesc(String direzione, String stato);

    /**
     * Stessa query, paginata — usata dall'elenco "Gestione Registro Lead"
     * per non caricare in un colpo solo tutte le righe.
     */
    Page<RegistroLead> findByDirezioneAndStatoOrderByIdDesc(String direzione, String stato, Pageable pageable);

    List<RegistroLead> findByDirezioneAndStatoAndStoreOrderByIdDesc(String direzione, String stato, String store);

    List<RegistroLead> findByDirezioneAndStoreOrderByIdDesc(String direzione, String store);
    /**
     * Todo lead aperti — tutti gli amministratori.
     * Vecchio: getRegistroLeadUtenteSQL("stato !=4 AND stato!=5")
     */
    @Query("SELECT r FROM RegistroLead r WHERE r.stato NOT IN :stati ORDER BY r.id DESC")
    List<RegistroLead> findByStatoNotInOrderByIdDesc(@Param("stati") List<String> stati);

    /**
     * Todo lead aperti — solo per un amministratore specifico.
     * Vecchio: getRegistroLeadUtenteSQL("stato !=4 AND stato!=5 and idamministratore=X")
     */
    @Query("SELECT r FROM RegistroLead r " +
            "WHERE r.stato NOT IN :stati AND r.idamministratore = :idAmministratore " +
            "ORDER BY r.id DESC")
    List<RegistroLead> findByIdamministratoreAndStatoNotInOrderByIdDesc(
            @Param("idAmministratore") Integer idAmministratore,
            @Param("stati") List<String> stati);

    /**
     * Lead per utente specifico.
     */
    List<RegistroLead> findByIdutenteOrderByIdDesc(Integer idUtente);

    /**
     * Lead per amministratore assegnatario.
     */
    List<RegistroLead> findByIdamministratoreOrderByIdDesc(Integer idAmministratore);

    // ===== STATISTICHE =====

    long countByStato(String stato);

    @Query("SELECT COUNT(r) FROM RegistroLead r WHERE r.stato NOT IN ('4', '5')")
    long countLeadAperti();

    @Query("SELECT COUNT(r) FROM RegistroLead r " +
            "WHERE r.idamministratore = :idAmministratore AND r.stato NOT IN ('4', '5')")
    long countLeadApertiByAmministratore(@Param("idAmministratore") Integer idAmministratore);

    // ===== UPDATE =====

    @Modifying
    @Query("UPDATE RegistroLead r SET r.stato = :stato WHERE r.id = :id")
    void updateStato(@Param("id") Long id, @Param("stato") String stato);

    @Modifying
    @Query("UPDATE RegistroLead r SET r.log = :log WHERE r.id = :id")
    void updateLog(@Param("id") Long id, @Param("log") String log);
}