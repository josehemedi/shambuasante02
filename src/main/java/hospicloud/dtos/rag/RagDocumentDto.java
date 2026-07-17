package hospicloud.dtos.rag;

import java.time.LocalDateTime;

public class RagDocumentDto {
    private Long id;
    private Integer hopitalId;
    private String categorie;
    private String titre;
    private String contenu;
    private String versionLabel;
    private String statut;
    private String audience;
    private String tags;
    private LocalDateTime expireAt;
    private LocalDateTime updatedAt;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Integer getHopitalId() { return hopitalId; }
    public void setHopitalId(Integer hopitalId) { this.hopitalId = hopitalId; }
    public String getCategorie() { return categorie; }
    public void setCategorie(String categorie) { this.categorie = categorie; }
    public String getTitre() { return titre; }
    public void setTitre(String titre) { this.titre = titre; }
    public String getContenu() { return contenu; }
    public void setContenu(String contenu) { this.contenu = contenu; }
    public String getVersionLabel() { return versionLabel; }
    public void setVersionLabel(String versionLabel) { this.versionLabel = versionLabel; }
    public String getStatut() { return statut; }
    public void setStatut(String statut) { this.statut = statut; }
    public String getAudience() { return audience; }
    public void setAudience(String audience) { this.audience = audience; }
    public String getTags() { return tags; }
    public void setTags(String tags) { this.tags = tags; }
    public LocalDateTime getExpireAt() { return expireAt; }
    public void setExpireAt(LocalDateTime expireAt) { this.expireAt = expireAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
