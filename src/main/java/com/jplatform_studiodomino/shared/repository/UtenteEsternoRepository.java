package com.jplatform_studiodomino.shared.repository;

import com.jplatform_studiodomino.shared.entity.UtenteEsterno;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UtenteEsternoRepository extends JpaRepository<UtenteEsterno, Integer> {


    @Query("SELECT u FROM UtenteEsterno u WHERE LOWER(u.username) = LOWER(:username)")
    Optional<UtenteEsterno> findByUsernameIgnoreCase(@Param("username") String username);

    @Query("SELECT u FROM UtenteEsterno u WHERE LOWER(u.email) = LOWER(:email)")
    Optional<UtenteEsterno> findByEmailIgnoreCase(@Param("email") String email);

    Optional<UtenteEsterno> findBySocialId(String socialId);
    Optional<UtenteEsterno> findByCodicefiscale(String codicefiscale);
    Optional<UtenteEsterno> findByPartitaiva(String partitaiva);

    boolean existsByEmailIgnoreCase(String email);
    boolean existsByUsernameIgnoreCase(String username);


    @Query("SELECT u FROM UtenteEsterno u WHERE u.idGruppo LIKE %:gruppo%")
    List<UtenteEsterno> findByGruppo(@Param("gruppo") String gruppo);

    @Query("SELECT u FROM UtenteEsterno u WHERE u.status = '1' ORDER BY u.cognome, u.nome")
    List<UtenteEsterno> findUtentiAttivi();

    List<UtenteEsterno> findByStatusOrderByCognomeAscNomeAsc(String status);

    @Query("SELECT u FROM UtenteEsterno u " +
            "WHERE LOWER(u.nome) LIKE LOWER(CONCAT('%', :search, '%')) " +
            "OR LOWER(u.cognome) LIKE LOWER(CONCAT('%', :search, '%')) " +
            "ORDER BY u.cognome, u.nome")
    List<UtenteEsterno> searchByNomeCognome(@Param("search") String search);

    @Query(value = "SELECT * FROM utenteesterno WHERE 1=1 :sqlCondition", nativeQuery = true)
    List<UtenteEsterno> findWithCustomSql(@Param("sqlCondition") String sqlCondition);


    @Query("SELECT COUNT(u) FROM UtenteEsterno u WHERE u.idGruppo LIKE %:gruppo%")
    long countByGruppo(@Param("gruppo") String gruppo);

    @Query("SELECT COUNT(u) FROM UtenteEsterno u WHERE u.status = '1'")
    long countUtentiAttivi();

    List<UtenteEsterno> findByComuneOrderByCognomeAscNomeAsc(String comune);
    List<UtenteEsterno> findByProvinciaOrderByCognomeAscNomeAsc(String provincia);


    @Modifying
    @Query("UPDATE UtenteEsterno u SET u.navigationProfile = :profile WHERE u.id = :id")
    void updateNavigationProfile(@Param("id") Integer id, @Param("profile") String profile);

    @Modifying
    @Query("UPDATE UtenteEsterno u SET u.password = :password WHERE u.id = :id")
    void updatePassword(@Param("id") Integer id, @Param("password") String password);

    @Modifying
    @Query("UPDATE UtenteEsterno u SET u.status = :status WHERE u.id = :id")
    void updateStatus(@Param("id") Integer id, @Param("status") String status);

    @Modifying
    @Query("UPDATE UtenteEsterno u SET u.dataultimoaccesso = :data WHERE u.id = :id")
    void updateUltimoAccesso(@Param("id") Integer id, @Param("data") String data);

    @Modifying
    @Query("UPDATE UtenteEsterno u SET u.profileImage = :profileImage WHERE u.id = :id")
    void updateProfileImage(@Param("id") Integer id, @Param("profileImage") Integer profileImage);

    @Modifying
    @Query(value = "UPDATE utenteesterno SET :fieldName = :fieldValue WHERE :condition",
            nativeQuery = true)
    void updateCampoGenerico(
            @Param("fieldName") String fieldName,
            @Param("fieldValue") String fieldValue,
            @Param("condition") String condition
    );

    // ===== AGGIUNTE PER CRM (nuove) =====

    /**
     * Ultimi 10 utenti per dashboard CRM.
     * Vecchio: DAO.getElencoUtenteEsterno("order by id desc limit 0,10")
     */
    List<UtenteEsterno> findTop10ByOrderByIdDesc();
    List<UtenteEsterno> findTop10ByStatusOrderByIdDesc(String status);

    /**
     * Utenti destinatari newsletter senza filtro gruppo.
     * Vecchio: "where email!='' and l1='1' and status!=0 and testMx=1"
     */
    @Query("SELECT u FROM UtenteEsterno u " +
            "WHERE u.email != '' AND u.l1 = '1' AND u.status != '0' AND u.testMx = 1")
    List<UtenteEsterno> findUtentiNewsletter();

    /**
     * Utenti destinatari newsletter filtrati per gruppo.
     * Vecchio: "where email!='' and l1='1' and status!=0 and testMx=1 and id_gruppo like '%(X);%'"
     */
    @Query("SELECT u FROM UtenteEsterno u " +
            "WHERE u.email != '' AND u.l1 = '1' AND u.status != '0' AND u.testMx = 1 " +
            "AND u.idGruppo LIKE CONCAT('%(', :gruppo, ');%')")
    List<UtenteEsterno> findUtentiNewsletterByGruppo(@Param("gruppo") String gruppo);

    /**
     * Conta utenti disattivi per dashboard.
     * Vecchio: "select count(*) from utenteesterno where status='0'"
     */
    @Query("SELECT COUNT(u) FROM UtenteEsterno u WHERE u.status = '0'")
    long countUtentiDisattivi();
}