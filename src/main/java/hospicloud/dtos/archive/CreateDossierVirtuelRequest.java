package hospicloud.dtos.archive;

public class CreateDossierVirtuelRequest {
    private String nom;
    private Long parentId;

    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }

    public Long getParentId() { return parentId; }
    public void setParentId(Long parentId) { this.parentId = parentId; }
}
