package com.jplatform_studiodomino.crm.repository;

import com.jplatform_studiodomino.crm.entity.TemplateRisposta;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository JPA per la tabella 'templates'.
 * Conversione da DAOCrm (metodi TemplateRisposte, JDBC raw) a Spring Data JPA.
 */
@Repository
public interface TemplateRispostaRepository extends JpaRepository<TemplateRisposta, Long> {

    List<TemplateRisposta> findAllByOrderByIdAsc();
}