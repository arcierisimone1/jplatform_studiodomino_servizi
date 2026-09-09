package com.jplatform_studiodomino.cms.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDate;

/**
 * Entity JPA completa per la tabella contents
 * ATTENZIONE: Questa entity contiene TUTTI i campi del DB legacy
 */
@Entity
@Table(name = "contents")
@Data
public class Content {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    // ========================================
    // IDENTIFICAZIONE BASE
    // ========================================

    @Column(name = "id_site")
    private String idSite;

    @Column(name = "id_root")
    private Integer idRoot = -1;

    @Column(name = "id_type")
    private Integer idType;

    @Column(name = "idparent")
    private String idParent = "0";

    @Column(name = "label")
    private String label = "";

    // ========================================
    // CONTENUTO BASE
    // ========================================

    @Column(name = "titolo")
    private String titolo;

    @Column(name = "riassunto", columnDefinition = "TEXT")
    private String riassunto;

    @Column(name = "testo", columnDefinition = "LONGTEXT")
    private String testo;

    // Versioni EN
    @Column(name = "titoloEN", columnDefinition = "TEXT")
    private String titoloEN;

    @Column(name = "riassuntoEN", columnDefinition = "TEXT")
    private String riassuntoEN;

    @Column(name = "testoEN", columnDefinition = "TEXT")
    private String testoEN;

    // ========================================
    // STATO E VISIBILITÀ
    // ========================================

    @Column(name = "stato")
    private String stato = "0";

    @Column(name = "privato")
    private String privato = "0";

    @Column(name = "idgruppo")
    private String idGruppo = "";

    @Column(name = "visualizza")
    private String visualizza = "1";

    @Column(name = "`check`")  // Keyword SQL, escaped
    private Integer check;

    // ========================================
    // POSIZIONAMENTO
    // ========================================

    @Column(name = "position")
    private Integer position = 0;

    @Column(name = "first_page")
    private String firstPage = "0";

    @Column(name = "click", nullable = false)
    private Integer click = 0;

    // ========================================
    // MENU (1-5)
    // ========================================

    @Column(name = "menu1")
    private String menu1 = "0";

    @Column(name = "menu2")
    private String menu2 = "0";

    @Column(name = "menu3")
    private String menu3 = "0";

    @Column(name = "menu4")
    private String menu4 = "0";

    @Column(name = "menu5")
    private String menu5 = "0";

    // ========================================
    // MEDIA E ALLEGATI
    // ========================================

    @Column(name = "gallery", columnDefinition = "LONGTEXT")
    private String galleryString;

    @Column(name = "id_allegato")
    private String idAllegato;

    // ========================================
    // DATE
    // ========================================

    @Column(name = "data")
    private String data;

    @Column(name = "dataVisualizzata")
    private String dataVisualizzata;

    @Column(name = "datasql")
    private LocalDate datasql;

    @Column(name = "creato")
    private String creato;

    @Column(name = "creatoda")
    private String creatoda;

    @Column(name = "modificato")
    private String modificato;

    @Column(name = "modificatoda")
    private String modificatoda;

    @Column(name = "apertoda")
    private String apertoda;

    // ========================================
    // TAG E RATING
    // ========================================

    @Column(name = "tag", columnDefinition = "TEXT")
    private String tag;

    @Column(name = "rating")
    private Integer rating = 0;

    @Column(name = "anno")
    private Integer anno = 2012;

    @Column(name = "mese")
    private String mese = "gennaio";

    // ========================================
    // CAMPI S (1-10)
    // ========================================

    @Column(name = "s1")
    private String s1;

    @Column(name = "s2")
    private String s2;

    @Column(name = "s3")
    private String s3;

    @Column(name = "s4")
    private String s4;

    @Column(name = "s5")
    private String s5;

    @Column(name = "s6")
    private String s6;

    @Column(name = "s7")
    private String s7;

    @Column(name = "s8")
    private String s8;

    @Column(name = "s9")
    private String s9;

    @Column(name = "s10")
    private String s10;

    // ========================================
    // CAMPI L (1-15)
    // ========================================

    @Column(name = "l1", columnDefinition = "TEXT")
    private String l1;

    @Column(name = "l2", columnDefinition = "TEXT")
    private String l2;

    @Column(name = "l3", columnDefinition = "TEXT")
    private String l3;

    @Column(name = "l4", columnDefinition = "TEXT")
    private String l4;

    @Column(name = "l5", columnDefinition = "TEXT")
    private String l5;

    @Column(name = "l6", columnDefinition = "TEXT")
    private String l6;

    @Column(name = "l7", columnDefinition = "TEXT")
    private String l7;

    @Column(name = "l8", columnDefinition = "TEXT")
    private String l8;

    @Column(name = "l9", columnDefinition = "TEXT")
    private String l9;

    @Column(name = "l10", columnDefinition = "TEXT")
    private String l10;

    @Column(name = "l11", columnDefinition = "TEXT")
    private String l11;

    @Column(name = "l12", columnDefinition = "TEXT")
    private String l12;

    @Column(name = "l13", columnDefinition = "TEXT")
    private String l13;

    @Column(name = "l14", columnDefinition = "TEXT")
    private String l14;

    @Column(name = "l15", columnDefinition = "TEXT")
    private String l15;

    // ========================================
    // CAMPI INFO (1-5)
    // ========================================

    @Column(name = "info1")
    private String info1;

    @Column(name = "info2", columnDefinition = "TEXT")
    private String info2;

    @Column(name = "info3")
    private String info3;

    @Column(name = "info4")
    private String info4;

    @Column(name = "info5")
    private String info5;

    // ========================================
    // SONDAGGI
    // ========================================

    @Column(name = "sondaggio")
    private String sondaggio = "0";

    // ========================================
    // NEWSLETTER E SMS (1-5)
    // ========================================

    @Column(name = "newsletter1")
    private String newsletter1 = "0";

    @Column(name = "newsletter2")
    private String newsletter2 = "0";

    @Column(name = "newsletter3")
    private String newsletter3 = "0";

    @Column(name = "newsletter4")
    private String newsletter4 = "0";

    @Column(name = "newsletter5")
    private String newsletter5 = "0";

    @Column(name = "sms1")
    private String sms1 = "0";

    @Column(name = "sms2")
    private String sms2 = "0";

    @Column(name = "sms3")
    private String sms3 = "0";

    @Column(name = "sms4")
    private String sms4 = "0";

    @Column(name = "sms5")
    private String sms5 = "0";

    // ========================================
    // UTENTI ASSOCIATI
    // ========================================

    @Column(name = "utentiAssociati", columnDefinition = "TEXT")
    private String utentiAssociati;

    // ========================================
    // CAMPI TEXT (1-10)
    // ========================================

    @Column(name = "text1", columnDefinition = "TEXT")
    private String text1;

    @Column(name = "text2", columnDefinition = "TEXT")
    private String text2;

    @Column(name = "text3", columnDefinition = "TEXT")
    private String text3;

    @Column(name = "text4", columnDefinition = "TEXT")
    private String text4;

    @Column(name = "text5", columnDefinition = "TEXT")
    private String text5;

    @Column(name = "text6", columnDefinition = "TEXT")
    private String text6;

    @Column(name = "text7", columnDefinition = "TEXT")
    private String text7;

    @Column(name = "text8", columnDefinition = "TEXT")
    private String text8;

    @Column(name = "text9", columnDefinition = "TEXT")
    private String text9;

    @Column(name = "text10", columnDefinition = "TEXT")
    private String text10;

    // ========================================
    // CAMPI DATA (1-10)
    // ========================================

    @Column(name = "data1", columnDefinition = "TEXT")
    private String data1;  // TEXT nel DB

    @Column(name = "data2")
    private LocalDate data2;

    @Column(name = "data3")
    private LocalDate data3;

    @Column(name = "data4")
    private LocalDate data4;

    @Column(name = "data5")
    private LocalDate data5;

    @Column(name = "data6")
    private LocalDate data6;

    @Column(name = "data7")
    private LocalDate data7;

    @Column(name = "data8")
    private LocalDate data8;

    @Column(name = "data9")
    private LocalDate data9;

    @Column(name = "data10")
    private LocalDate data10;

    // ========================================
    // CAMPI NUMBER (1-10)
    // ========================================

    @Column(name = "number1")
    private Double number1 = 0.0;

    @Column(name = "number2")
    private Double number2 = 0.0;

    @Column(name = "number3")
    private Double number3 = 0.0;

    @Column(name = "number4")
    private Double number4 = 0.0;

    @Column(name = "number5")
    private Double number5 = 0.0;

    @Column(name = "number6")
    private Double number6 = 0.0;

    @Column(name = "number7")
    private Double number7 = 0.0;

    @Column(name = "number8")
    private Double number8 = 0.0;

    @Column(name = "number9")
    private Double number9 = 0.0;

    @Column(name = "number10")
    private Double number10 = 0.0;

    // ========================================
    // CAMPI VARCHAR (1-10)
    // ========================================

    @Column(name = "varchar1", columnDefinition = "TEXT")
    private String varchar1;

    @Column(name = "varchar2", columnDefinition = "TEXT")
    private String varchar2;

    @Column(name = "varchar3", columnDefinition = "TEXT")
    private String varchar3;

    @Column(name = "varchar4", columnDefinition = "TEXT")
    private String varchar4;

    @Column(name = "varchar5", columnDefinition = "TEXT")
    private String varchar5;

    @Column(name = "varchar6", columnDefinition = "TEXT")
    private String varchar6;

    @Column(name = "varchar7", columnDefinition = "TEXT")
    private String varchar7;

    @Column(name = "varchar8", columnDefinition = "TEXT")
    private String varchar8;

    @Column(name = "varchar9", columnDefinition = "TEXT")
    private String varchar9;

    @Column(name = "varchar10", columnDefinition = "TEXT")
    private String varchar10;

    // ========================================
    // CAMPI ARRAY (1-5) - TEXT nel DB
    // ========================================

    @Column(name = "array1", columnDefinition = "TEXT")
    private String array1;

    @Column(name = "array2", columnDefinition = "TEXT")
    private String array2;

    @Column(name = "array3", columnDefinition = "TEXT")
    private String array3;

    @Column(name = "array4", columnDefinition = "TEXT")
    private String array4;

    @Column(name = "array5", columnDefinition = "TEXT")
    private String array5;

    // ========================================
    // CAMPI LOG (1-3)
    // ========================================

    @Column(name = "log1", columnDefinition = "TEXT")
    private String log1;

    @Column(name = "log2", columnDefinition = "TEXT")
    private String log2;

    @Column(name = "log3", columnDefinition = "TEXT")
    private String log3;

    // ========================================
    // NUMERATORI (1-5) - ZEROFILL
    // ========================================

    @Column(name = "numeratore1")
    private Integer numeratore1 = 0;

    @Column(name = "numeratore2")
    private Integer numeratore2 = 0;

    @Column(name = "numeratore3")
    private Integer numeratore3 = 0;

    @Column(name = "numeratore4")
    private Integer numeratore4 = 0;

    @Column(name = "numeratore5")
    private Integer numeratore5 = 0;

    // ========================================
    // EXTRA TAG (1-10)
    // ========================================

    @Column(name = "extratag1", columnDefinition = "TEXT")
    private String extratag1;

    @Column(name = "extratag2", columnDefinition = "TEXT")
    private String extratag2;

    @Column(name = "extratag3", columnDefinition = "TEXT")
    private String extratag3;

    @Column(name = "extratag4", columnDefinition = "TEXT")
    private String extratag4;

    @Column(name = "extratag5", columnDefinition = "TEXT")
    private String extratag5;

    @Column(name = "extratag6", columnDefinition = "TEXT")
    private String extratag6;

    @Column(name = "extratag7", columnDefinition = "TEXT")
    private String extratag7;

    @Column(name = "extratag8", columnDefinition = "TEXT")
    private String extratag8;

    @Column(name = "extratag9", columnDefinition = "TEXT")
    private String extratag9;

    @Column(name = "extratag10", columnDefinition = "TEXT")
    private String extratag10;

    // ========================================
    // EXTRA TAG REF (1-10)
    // ========================================

    @Column(name = "extratagref1")
    private String extratagref1;

    @Column(name = "extratagref2")
    private String extratagref2;

    @Column(name = "extratagref3")
    private String extratagref3;

    @Column(name = "extratagref4")
    private String extratagref4;

    @Column(name = "extratagref5")
    private String extratagref5;

    @Column(name = "extratagref6")
    private String extratagref6;

    @Column(name = "extratagref7")
    private String extratagref7;

    @Column(name = "extratagref8")
    private String extratagref8;

    @Column(name = "extratagref9")
    private String extratagref9;

    @Column(name = "extratagref10")
    private String extratagref10;

    // ========================================
    // ORDINAMENTO EXTRA TAG
    // ========================================

    @Column(name = "ordineextratag")
    private String ordineextratag;

    @Column(name = "maxextratag")
    private String maxextratag;

    @Column(name = "regolaextratag1")
    private String regolaextratag1;

    @Column(name = "regolaextratag2")
    private String regolaextratag2;

    /**
     * Logo principale (transient - non salvato nel DB)
     */
    @Transient
    private String logo;

    @Transient
    private String logo2;

    @Transient
    private String logo3;

    @Transient
    private String temp1;

    @Transient
    private String temp2;

    @Transient
    private String temp3;

    @Transient
    private String temp4;

    @Transient
    private String temp5;

    @Transient
    private String repo;

    @Transient
    private String repoId;

    @Transient
    private String repoName;

    @Transient
    private SectionType sectionType;


    // ========================================
    // METODI HELPER
    // ========================================

    /**
     * Verifica se è una sezione (idRoot = -1)
     */
    public boolean isSection() {
        return idRoot != null && idRoot == -1;
    }

    /**
     * Verifica se è un contenuto (idRoot != -1)
     */
    public boolean isContent() {
        return idRoot != null && idRoot != -1;
    }

    /**
     * Getter generico per campi S (S1-S10)
     * S1 = Data inizio pubblicazione programmata
     * S2 = Data fine pubblicazione programmata
     * S3 = Flag pubblicazione programmata (0=no, 1=si)
     */
    public String getS(int numero) {
        if (numero < 1 || numero > 10) {
            return "";
        }

        switch (numero) {
            case 1: return s1 != null ? s1 : "";
            case 2: return s2 != null ? s2 : "";
            case 3: return s3 != null ? s3 : "";
            case 4: return s4 != null ? s4 : "";
            case 5: return s5 != null ? s5 : "";
            case 6: return s6 != null ? s6 : "";
            case 7: return s7 != null ? s7 : "";
            case 8: return s8 != null ? s8 : "";
            case 9: return s9 != null ? s9 : "";
            case 10: return s10 != null ? s10 : "";
            default: return "";
        }
    }

    /**
     * Setter generico per campi S (S1-S10)
     */
    public void setS(int numero, String valore) {
        if (numero < 1 || numero > 10) {
            return;
        }

        switch (numero) {
            case 1: s1 = valore; break;
            case 2: s2 = valore; break;
            case 3: s3 = valore; break;
            case 4: s4 = valore; break;
            case 5: s5 = valore; break;
            case 6: s6 = valore; break;
            case 7: s7 = valore; break;
            case 8: s8 = valore; break;
            case 9: s9 = valore; break;
            case 10: s10 = valore; break;
        }
    }
}