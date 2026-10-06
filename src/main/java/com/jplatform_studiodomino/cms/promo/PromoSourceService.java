package com.jplatform_studiodomino.cms.promo;

import com.jplatform_studiodomino.cms.entity.DatiBase;
import com.jplatform_studiodomino.cms.entity.Section;
import com.jplatform_studiodomino.cms.service.ContentService;
import com.jplatform_studiodomino.shared.entity.Images;
import com.jplatform_studiodomino.shared.entity.Site;
import com.jplatform_studiodomino.shared.service.ImagesService;
import com.jplatform_studiodomino.shared.service.SiteService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class PromoSourceService {

    // 393 = PEC, 719 = SPID, 718 = Firma Digitale, 319 = Assistenza, 318 = Sviluppo
    private static final List<Integer> SECTION_IDS = List.of(393, 719, 718, 319, 318);

    private static final String ID_SITE = "1";

    private final ContentService contentService;
    private final SiteService siteService;
    private final ImagesService imagesService;

    @Value("${promo.public-base-url:http://localhost:8088}")
    private String publicBaseUrl;

    public PromoSourceService(ContentService contentService, SiteService siteService, ImagesService imagesService) {
        this.contentService = contentService;
        this.siteService = siteService;
        this.imagesService = imagesService;
    }

    public List<PromoItemDto> getLocalItems() {
        List<PromoItemDto> items = new ArrayList<>();
        Site site = siteService.findById(Integer.valueOf(ID_SITE));
        String imagesRepositoryWeb = (site != null)
                ? site.getDescrizione() + site.getPathWeb() + "/images/"
                : "";

        for (Integer id : SECTION_IDS) {
            contentService.findContentById(id, ID_SITE).ifPresentOrElse(dati -> {
                String logo = resolveLogo(dati.getGalleryString(), dati.getLogo());
                items.add(buildItem(id, dati.getTitolo(), dati.getUrl(), logo, publicBaseUrl, imagesRepositoryWeb));
            }, () -> {
                contentService.findSectionById(id, ID_SITE).ifPresent(sezione -> {
                    String logo = resolveLogo(sezione.getGalleryString(), sezione.getLogo());
                    items.add(buildItem(id, sezione.getTitolo(), sezione.getUrl(), logo, publicBaseUrl, imagesRepositoryWeb));
                });
            });
        }
        return items;
    }

    /**
     * Il mapper Content -> Section/DatiBase non popola mai la collezione "gallery",
     * quindi getLogo() ritorna sempre il placeholder "nofoto.jpg". Il campo
     * "galleryString" (lista di id immagine separati da virgola/punto e virgola)
     * e' pero' popolato correttamente: lo risolviamo qui nello stesso modo in cui
     * lo fa (privatamente) ContentService.popolaGallerySezioneMenu/popolaGalleryContenuti.
     */
    private String resolveLogo(String galleryString, String fallbackLogo) {
        if (galleryString == null || galleryString.isEmpty()) {
            return fallbackLogo;
        }
        for (String part : galleryString.split("[,;]")) {
            part = part.trim().replace("(", "").replace(")", "");
            if (part.isEmpty()) continue;
            try {
                Integer imgId = Integer.parseInt(part);
                Images img = imagesService.findById(imgId).orElse(null);
                if (img != null && img.getFullpath() != null && !img.getFullpath().isEmpty()) {
                    return img.getFullpath();
                }
            } catch (NumberFormatException ignored) {
                // id non numerico nella galleryString, si salta
            }
        }
        return fallbackLogo;
    }

    private PromoItemDto buildItem(Integer id, String titolo, String relativeUrl, String logo,
                                   String publicBaseUrl, String imagesRepositoryWeb) {
        String url = publicBaseUrl + "/" + relativeUrl;
        String img = (logo != null && !logo.isEmpty() && !"nofoto.jpg".equals(logo))
                ? imagesRepositoryWeb + logo
                : "";
        return new PromoItemDto("servizi-" + id, titolo, url, img, "servizi");
    }
}