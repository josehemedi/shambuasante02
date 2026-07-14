package hospicloud.dtos.archive;

import java.time.LocalDateTime;

public class ArchiveFichierDto {

    private Long id;
    private Long archiveId;
    private String typeFichier;
    private String nomFichier;
    private String mimeType;
    private Long tailleOctets;
    private LocalDateTime genereAt;
    private String downloadUrl;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getArchiveId() { return archiveId; }
    public void setArchiveId(Long archiveId) { this.archiveId = archiveId; }

    public String getTypeFichier() { return typeFichier; }
    public void setTypeFichier(String typeFichier) { this.typeFichier = typeFichier; }

    public String getNomFichier() { return nomFichier; }
    public void setNomFichier(String nomFichier) { this.nomFichier = nomFichier; }

    public String getMimeType() { return mimeType; }
    public void setMimeType(String mimeType) { this.mimeType = mimeType; }

    public Long getTailleOctets() { return tailleOctets; }
    public void setTailleOctets(Long tailleOctets) { this.tailleOctets = tailleOctets; }

    public LocalDateTime getGenereAt() { return genereAt; }
    public void setGenereAt(LocalDateTime genereAt) { this.genereAt = genereAt; }

    public String getDownloadUrl() { return downloadUrl; }
    public void setDownloadUrl(String downloadUrl) { this.downloadUrl = downloadUrl; }
}
