package hospicloud.dtos.archive;

/** Déplacer un dossier virtuel vers un autre parent (null = racine). */
public class MoveDossierVirtuelRequest {
    private Long parentId;

    public Long getParentId() { return parentId; }
    public void setParentId(Long parentId) { this.parentId = parentId; }
}
