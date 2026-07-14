package hospicloud.dtos.sortie;

import hospicloud.dtos.BonSortieResponseDto;

public class AutoriserSortieResponseDTO {

    private BonSortieResponseDto bonSortie;
    private Long idOrdonnance;
    private String statutPatient;
    private String statutAdmission;
    private String message;
    private Long idArchiveDossier;
    private boolean dossierEnvoyeArchiviste;

    public BonSortieResponseDto getBonSortie() { return bonSortie; }
    public void setBonSortie(BonSortieResponseDto bonSortie) { this.bonSortie = bonSortie; }

    public Long getIdOrdonnance() { return idOrdonnance; }
    public void setIdOrdonnance(Long idOrdonnance) { this.idOrdonnance = idOrdonnance; }

    public String getStatutPatient() { return statutPatient; }
    public void setStatutPatient(String statutPatient) { this.statutPatient = statutPatient; }

    public String getStatutAdmission() { return statutAdmission; }
    public void setStatutAdmission(String statutAdmission) { this.statutAdmission = statutAdmission; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public Long getIdArchiveDossier() { return idArchiveDossier; }
    public void setIdArchiveDossier(Long idArchiveDossier) { this.idArchiveDossier = idArchiveDossier; }

    public boolean isDossierEnvoyeArchiviste() { return dossierEnvoyeArchiviste; }
    public void setDossierEnvoyeArchiviste(boolean dossierEnvoyeArchiviste) {
        this.dossierEnvoyeArchiviste = dossierEnvoyeArchiviste;
    }
}
