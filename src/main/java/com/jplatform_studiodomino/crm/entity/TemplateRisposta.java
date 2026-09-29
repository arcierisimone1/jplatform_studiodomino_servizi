package com.jplatform_studiodomino.crm.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Entity JPA per la tabella 'templates'.
 * Conversione da TemplateRisposte.java (Struts ActionForm) a Spring Boot JPA Entity.
 */
@Entity
@Table(name = "templates")
@Getter
@Setter
@NoArgsConstructor
public class TemplateRisposta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "selettore", length = 255)
    private String selettore = "";

    @Column(name = "titolo", length = 255)
    private String titolo = "";

    @Column(name = "descrizione", length = 255)
    private String descrizione = "";

    @Column(name = "contenuto", columnDefinition = "TEXT")
    private String contenuto = "";

    @Column(name = "url", length = 255)
    private String url = "";

    @Column(name = "l1", length = 255)  private String l1 = "";
    @Column(name = "l2", length = 255)  private String l2 = "";
    @Column(name = "l3", length = 255)  private String l3 = "";
    @Column(name = "l4", length = 255)  private String l4 = "";
    @Column(name = "l5", length = 255)  private String l5 = "";
    @Column(name = "l6", length = 255)  private String l6 = "";
    @Column(name = "l7", length = 255)  private String l7 = "";
    @Column(name = "l8", length = 255)  private String l8 = "";
    @Column(name = "l9", length = 255)  private String l9 = "";
    @Column(name = "l10", length = 255) private String l10 = "";
}