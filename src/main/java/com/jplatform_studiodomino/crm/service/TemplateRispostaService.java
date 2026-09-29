package com.jplatform_studiodomino.crm.service;

import com.jplatform_studiodomino.crm.entity.TemplateRisposta;
import com.jplatform_studiodomino.crm.repository.TemplateRispostaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Service per la gestione delle Risposte Standard (templates) CRM.
 * Conversione da GestioneTemplateRisposte.java (Struts) a Spring Boot.
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class TemplateRispostaService {

    private final TemplateRispostaRepository templateRispostaRepository;

    public List<TemplateRisposta> findAll() {
        return templateRispostaRepository.findAllByOrderByIdAsc();
    }

    public TemplateRisposta findById(Long id) {
        return templateRispostaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("TemplateRisposta non trovato: " + id));
    }

    @Transactional
    public TemplateRisposta crea(TemplateRisposta templateRisposta) {
        sanitizzaContenuto(templateRisposta);
        templateRisposta.setId(null);
        return templateRispostaRepository.save(templateRisposta);
    }

    @Transactional
    public TemplateRisposta salva(TemplateRisposta templateRisposta) {
        sanitizzaContenuto(templateRisposta);
        return templateRispostaRepository.save(templateRisposta);
    }

    @Transactional
    public void elimina(Long id) {
        templateRispostaRepository.deleteById(id);
    }

    /**
     * Prepara una copia SENZA salvarla —
     * solo in sessione finché l'operatore non premeva "Salva".
     */
    public TemplateRisposta prepareDuplica(Long id) {
        TemplateRisposta originale = findById(id);
        TemplateRisposta copia = new TemplateRisposta();
        copia.setId(-1L);
        copia.setSelettore(originale.getSelettore());
        copia.setTitolo(originale.getTitolo() + "(2)");
        copia.setDescrizione(originale.getDescrizione());
        copia.setContenuto(originale.getContenuto());
        copia.setUrl(originale.getUrl());
        copia.setL1(originale.getL1());
        copia.setL2(originale.getL2());
        copia.setL3(originale.getL3());
        copia.setL4(originale.getL4());
        copia.setL5(originale.getL5());
        copia.setL6(originale.getL6());
        copia.setL7(originale.getL7());
        copia.setL8(originale.getL8());
        copia.setL9(originale.getL9());
        copia.setL10(originale.getL10());
        return copia;
    }

    // Vecchio: contenuto.replaceAll("\r\n","").replaceAll("\"","'")
    private void sanitizzaContenuto(TemplateRisposta t) {
        if (t.getContenuto() != null) {
            t.setContenuto(t.getContenuto().replace("\r\n", "").replace("\"", "'"));
        }
    }
}