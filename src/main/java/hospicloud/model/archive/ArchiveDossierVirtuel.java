package hospicloud.model.archive;

import java.time.LocalDateTime;

public class ArchiveDossierVirtuel {

    private Long id;
    private Integer hopitalId;
    private Long parentId;
    private String nom;
    private Integer createdBy;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private int enfantsCount;
    private int dossiersCount;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Integer getHopitalId() { return hopitalId; }
    public void setHopitalId(Integer hopitalId) { this.hopitalId = hopitalId; }

    public Long getParentId() { return parentId; }
    public void setParentId(Long parentId) { this.parentId = parentId; }

    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }

    public Integer getCreatedBy() { return createdBy; }
    public void setCreatedBy(Integer createdBy) { this.createdBy = createdBy; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public int getEnfantsCount() { return enfantsCount; }
    public void setEnfantsCount(int enfantsCount) { this.enfantsCount = enfantsCount; }

    public int getDossiersCount() { return dossiersCount; }
    public void setDossiersCount(int dossiersCount) { this.dossiersCount = dossiersCount; }
}
