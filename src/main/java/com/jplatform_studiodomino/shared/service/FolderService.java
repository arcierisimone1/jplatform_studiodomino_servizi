package com.jplatform_studiodomino.shared.service;

import com.jplatform_studiodomino.cms.entity.Allegato;
import com.jplatform_studiodomino.cms.service.AllegatoService;
import com.jplatform_studiodomino.shared.entity.Folder;
import com.jplatform_studiodomino.shared.entity.Images;
import com.jplatform_studiodomino.shared.repository.FolderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Service per gestione Folder
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class FolderService {

    private final FolderRepository folderRepository;
    private final ImagesService imagesService;
    private final AllegatoService allegatoService;

    /**
     * Trova folder per ID
     */
    public Optional<Folder> findById(Integer id) {
        return folderRepository.findById(id);
    }

    /**
     * Trova folder per nome
     */
    public Optional<Folder> findByNome(String nome) {
        return folderRepository.findByNome(nome);
    }

    /**
     * Salva folder
     */
    @Transactional
    public Folder save(Folder folder) {
        if (folder.getDatacreazione() == null) {
            folder.setDatacreazione(LocalDate.now());
        }
        return folderRepository.save(folder);
    }

    /**
     * Crea nuovo folder
     */
    @Transactional
    public Folder createFolder(String nome, String idfolderParent, String idgruppo) {
        Folder folder = new Folder();
        folder.setNome(nome);
        folder.setIdfolder(idfolderParent != null ? idfolderParent : "0");
        folder.setIdgruppo(idgruppo != null ? idgruppo : "");
        folder.setDatacreazione(LocalDate.now());

        log.debug("Creazione folder: {} parent: {}", nome, idfolderParent);
        return folderRepository.save(folder);
    }

    /**
     * Ottieni tutti i subfolder di un parent
     */
    public List<Folder> getSubfolders(String idfolderParent) {
        return folderRepository.findByIdfolderOrderByNomeAsc(idfolderParent);
    }

    /**
     * Ottieni subfolder con conteggi
     */
    public List<Folder> getSubfoldersWithCounts(String idfolderParent) {
        List<Folder> folders = folderRepository.findByIdfolderOrderByNomeAsc(idfolderParent);

        for (Folder folder : folders) {
            // Conta subfolder
            Long subfolderCount = folderRepository.countSubfolders(folder.getId().toString());
            folder.setNumeroFolder(subfolderCount.toString());
            folder.setSubfolders(subfolderCount > 0 ? "1" : "0");

            // TODO: Aggiungere conteggio immagini e allegati quando le entity saranno disponibili
        }

        return folders;
    }

    /**
     * Ottiene l'albero delle cartelle partendo da un ID (Ricorsivo)
     */
    public List<Folder> getFolderTree(String idfolderParent, int depth) {
        if (depth > 3) return new ArrayList<>();

        // Recuperiamo i figli
        List<Folder> folders = folderRepository.findByIdfolderOrderByNomeAsc(idfolderParent);

        // FILTRO: Rimuoviamo dalla lista cartelle che si chiamano "ROOT"
        // o che hanno lo stesso ID del parent (per evitare loop)
        return folders.stream()
                .filter(f -> !f.getNome().equalsIgnoreCase("ROOT"))
                .filter(f -> !f.getId().toString().equals(idfolderParent))
                .peek(f -> {
                    List<Folder> children = getFolderTree(f.getId().toString(), depth + 1);
                    f.setSubfolder(children);
                    f.setSubfolders(!children.isEmpty() ? "1" : "0");
                })
                .collect(Collectors.toList());
    }

    @Transactional
    public void moveFolder(String folderId, String targetFolderId) {
        Folder folder = folderRepository.findById(Integer.parseInt(folderId))
                .orElseThrow(() -> new RuntimeException("Cartella non trovata"));

        // Cambiamo il riferimento al padre
        folder.setIdfolder(targetFolderId);
        folderRepository.save(folder);
    }

    /**
     * Ottieni folder root
     */
    public List<Folder> getRootFolders() {
        return folderRepository.findRootFolders();
    }

    /**
     * Ottieni folder per gruppo
     */
    public List<Folder> getFoldersByGruppo(String idgruppo) {
        return folderRepository.findByIdgruppoOrderByNomeAsc(idgruppo);
    }

    /**
     * Cerca folder per nome
     */
    public List<Folder> searchByNome(String nome) {
        return folderRepository.findByNomeContainingIgnoreCaseOrderByNomeAsc(nome);
    }

    /**
     * Elimina folder
     */
    @Transactional
    public void deleteFolder(Integer id) {
        Optional<Folder> folder = folderRepository.findById(id);
        if (folder.isPresent()) {
            // Verifica se ha subfolder
            boolean hasSubfolders = folderRepository.existsByIdfolder(id.toString());
            if (hasSubfolders) {
                throw new IllegalStateException("Impossibile eliminare folder con subfolder");
            }

            // TODO: Verificare anche immagini e allegati

            folderRepository.deleteById(id);
            log.info("Folder eliminato: {}", id);
        }
    }

    /**
     * Elimina folder ricorsivamente: subfolder, immagini e allegati contenuti, poi il folder stesso.
     */
    @Transactional
    public void deleteFolderRecursive(Integer id) throws java.io.IOException {
        Optional<Folder> folder = folderRepository.findById(id);
        if (folder.isPresent()) {
            // 1. Elimina ricorsivamente tutti i subfolder (e il loro contenuto)
            List<Folder> subfolders = folderRepository.findByIdfolderOrderByNomeAsc(id.toString());
            for (Folder subfolder : subfolders) {
                deleteFolderRecursive(subfolder.getId());
            }

            // 2. Elimina tutte le immagini contenute in questo folder (file fisico + record DB)
            List<Images> immagini = imagesService.findByFolder(id.toString());
            for (Images img : immagini) {
                imagesService.delete(img.getId());
            }

            // 3. Elimina tutti gli allegati contenuti in questo folder
            List<Allegato> allegati = allegatoService.findByFolder(id);
            for (Allegato all : allegati) {
                allegatoService.deleteAllegato(all.getId());
            }

            // 4. Elimina il folder stesso
            folderRepository.deleteById(id);
            log.info("Folder eliminato ricorsivamente (con immagini/allegati): {}", id);
        }
    }

    /**
     * Sposta folder in altro parent
     */
    @Transactional
    public Folder moveFolder(Integer id, String newParentId) {
        Optional<Folder> folder = folderRepository.findById(id);
        if (folder.isPresent()) {
            Folder f = folder.get();

            // Verifica che non si stia spostando in un suo subfolder
            if (isDescendant(id, newParentId)) {
                throw new IllegalArgumentException("Impossibile spostare folder in un suo subfolder");
            }

            f.setIdfolder(newParentId);
            log.info("Folder {} spostato in parent {}", id, newParentId);
            return folderRepository.save(f);
        }
        throw new IllegalArgumentException("Folder non trovato: " + id);
    }

    /**
     * Verifica se targetId è discendente di folderId
     */
    private boolean isDescendant(Integer folderId, String targetId) {
        try {
            Integer targetIdInt = Integer.parseInt(targetId);
            if (folderId.equals(targetIdInt)) {
                return true;
            }

            Optional<Folder> target = folderRepository.findById(targetIdInt);
            if (target.isPresent() && target.get().getIdfolder() != null) {
                return isDescendant(folderId, target.get().getIdfolder());
            }
        } catch (NumberFormatException e) {
            return false;
        }
        return false;
    }

    /**
     * Ottieni path completo del folder (breadcrumb)
     */
    public List<Folder> getFolderPath(Integer id) {
        List<Folder> path = new ArrayList<>();
        if (id == null || id <= 1) return path; // Root non appare nel breadcrumb

        Optional<Folder> current = folderRepository.findById(id);
        int maxDepth = 10;
        int depth = 0;
        java.util.Set<Integer> visited = new java.util.HashSet<>();

        while (current.isPresent() && depth < maxDepth) {
            Folder folder = current.get();

            if (visited.contains(folder.getId())) break; // anti-loop
            visited.add(folder.getId());

            path.add(0, folder);

            String parentId = folder.getIdfolder();
            if (parentId == null || parentId.isBlank()
                    || parentId.equals("0") || parentId.equals("1")
                    || parentId.equals("-1")) break;

            try {
                Integer parentIdInt = Integer.parseInt(parentId);
                current = folderRepository.findById(parentIdInt);
            } catch (NumberFormatException e) {
                break;
            }
            depth++;
        }
        return path;
    }

    /**
     * Rinomina folder
     */
    @Transactional
    public Folder renameFolder(Integer id, String nuovoNome) {
        Optional<Folder> folder = folderRepository.findById(id);
        if (folder.isPresent()) {
            Folder f = folder.get();
            f.setNome(nuovoNome);
            log.info("Folder {} rinominato in: {}", id, nuovoNome);
            return folderRepository.save(f);
        }
        throw new IllegalArgumentException("Folder non trovato: " + id);
    }

    /**
     * Conta tutti i folder
     */
    public long countAll() {
        return folderRepository.count();
    }

    /**
     * Verifica se folder esiste
     */
    public boolean existsById(Integer id) {
        return folderRepository.existsById(id);
    }

    /**
     * Ottieni tutti i folder (per select nei form)
     */
    public List<Folder> getAllFolders() {
        return folderRepository.findAllByOrderByNomeAsc();
    }
}