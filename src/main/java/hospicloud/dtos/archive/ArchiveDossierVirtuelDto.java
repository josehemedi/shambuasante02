package hospicloud.dtos.archive;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class ArchiveDossierVirtuelDto {

    private Long id;
    private Long parentId;
    private String nom;
    private Integer createdBy;
    private LocalDateTime createdAt;
    private int enfantsCount;
    private int dossiersCount;
    private List<ArchiveDossierVirtuelDto> children = new ArrayList<>();

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getParentId() { return parentId; }
    public void setParentId(Long parentId) { this.parentId = parentId; }

    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }

    public Integer getCreatedBy() { return createdBy; }
    public void setCreatedBy(Integer createdBy) { this.createdBy = createdBy; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public int getEnfantsCount() { return enfantsCount; }
    public void setEnfantsCount(int enfantsCount) { this.enfantsCount = enfantsCount; }

    public int getDossiersCount() { return dossiersCount; }
    public void setDossiersCount(int dossiersCount) { this.dossiersCount = dossiersCount; }

    public List<ArchiveDossierVirtuelDto> getChildren() { return children; }
    public void setChildren(List<ArchiveDossierVirtuelDto> children) { this.children = children; }
}
