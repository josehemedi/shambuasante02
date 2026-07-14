package hospicloud.model.archive;

import java.time.LocalDateTime;

public class ArchiveFichier {

    public static final String TYPE_DOSSIER_PATIENT = "DOSSIER_PATIENT";
    public static final String TYPE_ORDONNANCE_PREFIX = "ORDONNANCE_";
    public static final String TYPE_CONSULTATION_PREFIX = "CONSULTATION_";
    public static final String TYPE_BULLETIN_PREFIX = "BULLETIN_SORTIE_";
    public static final String TYPE_UPLOAD_PREFIX = "UPLOAD_";

    private Long id;
    private Integer hopitalId;
    private Long archiveId;
    private String typeFichier;
    private String nomFichier;
    private String cheminStockage;
    private String mimeType;
    private Long tailleOctets;
    private LocalDateTime genereAt;
    private Integer generePar;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Integer getHopitalId() { return hopitalId; }
    public void setHopitalId(Integer hopitalId) { this.hopitalId = hopitalId; }

    public Long getArchiveId() { return archiveId; }
    public void setArchiveId(Long archiveId) { this.archiveId = archiveId; }

    public String getTypeFichier() { return typeFichier; }
    public void setTypeFichier(String typeFichier) { this.typeFichier = typeFichier; }

    public String getNomFichier() { return nomFichier; }
    public void setNomFichier(String nomFichier) { this.nomFichier = nomFichier; }

    public String getCheminStockage() { return cheminStockage; }
    public void setCheminStockage(String cheminStockage) { this.cheminStockage = cheminStockage; }

    public String getMimeType() { return mimeType; }
    public void setMimeType(String mimeType) { this.mimeType = mimeType; }

    public Long getTailleOctets() { return tailleOctets; }
    public void setTailleOctets(Long tailleOctets) { this.tailleOctets = tailleOctets; }

    public LocalDateTime getGenereAt() { return genereAt; }
    public void setGenereAt(LocalDateTime genereAt) { this.genereAt = genereAt; }

    public Integer getGenerePar() { return generePar; }
    public void setGenerePar(Integer generePar) { this.generePar = generePar; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
