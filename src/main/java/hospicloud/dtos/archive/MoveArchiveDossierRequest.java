package hospicloud.dtos.archive;

/** Déplacer un dossier médical (archive) vers un dossier virtuel (null = racine). */
public class MoveArchiveDossierRequest {
    private Long dossierVirtuelId;

    public Long getDossierVirtuelId() { return dossierVirtuelId; }
    public void setDossierVirtuelId(Long dossierVirtuelId) { this.dossierVirtuelId = dossierVirtuelId; }
}
