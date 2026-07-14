package hospicloud.dtos.archive;

import java.util.ArrayList;
import java.util.List;

/** Contenu d'un dossier virtuel (explorateur type Windows). */
public class ArchiveExplorerContentDto {

    private Long currentFolderId;
    private String currentFolderName;
    private List<BreadcrumbItem> breadcrumb = new ArrayList<>();
    private List<ArchiveDossierVirtuelDto> folders = new ArrayList<>();
    private List<ArchiveDossierResponseDto> files = new ArrayList<>();

    public Long getCurrentFolderId() { return currentFolderId; }
    public void setCurrentFolderId(Long currentFolderId) { this.currentFolderId = currentFolderId; }

    public String getCurrentFolderName() { return currentFolderName; }
    public void setCurrentFolderName(String currentFolderName) { this.currentFolderName = currentFolderName; }

    public List<BreadcrumbItem> getBreadcrumb() { return breadcrumb; }
    public void setBreadcrumb(List<BreadcrumbItem> breadcrumb) { this.breadcrumb = breadcrumb; }

    public List<ArchiveDossierVirtuelDto> getFolders() { return folders; }
    public void setFolders(List<ArchiveDossierVirtuelDto> folders) { this.folders = folders; }

    public List<ArchiveDossierResponseDto> getFiles() { return files; }
    public void setFiles(List<ArchiveDossierResponseDto> files) { this.files = files; }

    public static class BreadcrumbItem {
        private Long id;
        private String nom;

        public BreadcrumbItem() {}

        public BreadcrumbItem(Long id, String nom) {
            this.id = id;
            this.nom = nom;
        }

        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }

        public String getNom() { return nom; }
        public void setNom(String nom) { this.nom = nom; }
    }
}
