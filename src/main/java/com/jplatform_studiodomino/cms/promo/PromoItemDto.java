package com.jplatform_studiodomino.cms.promo;

public class PromoItemDto {

    private String id;
    private String titolo;
    private String url;
    private String img;
    private String sito;

    public PromoItemDto() {
    }

    public PromoItemDto(String id, String titolo, String url, String img, String sito) {
        this.id = id;
        this.titolo = titolo;
        this.url = url;
        this.img = img;
        this.sito = sito;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitolo() {
        return titolo;
    }

    public void setTitolo(String titolo) {
        this.titolo = titolo;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getImg() {
        return img;
    }

    public void setImg(String img) {
        this.img = img;
    }

    public String getSito() {
        return sito;
    }

    public void setSito(String sito) {
        this.sito = sito;
    }
}