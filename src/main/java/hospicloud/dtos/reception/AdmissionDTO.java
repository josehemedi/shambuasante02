package hospicloud.dtos.reception;

import java.time.LocalDateTime;

public class AdmissionDTO {
    private Integer idAdmission;
    private Integer idPatient;
    private String nomCompletPatient;
    private String nomMedecin;
    private Integer niveauPriorite;
    private LocalDateTime tempsArrivee;
    private long tempsAttenteMinutes; // Calculé dynamiquement
    private String statut;

    public AdmissionDTO() {}

    public Integer getIdAdmission() { return idAdmission; }
    public void setIdAdmission(Integer idAdmission) { this.idAdmission = idAdmission; }

    public Integer getIdPatient() { return idPatient; }
    public void setIdPatient(Integer idPatient) { this.idPatient = idPatient; }

    public String getNomCompletPatient() { return nomCompletPatient; }
    public void setNomCompletPatient(String nomCompletPatient) { this.nomCompletPatient = nomCompletPatient; }

    public String getNomMedecin() { return nomMedecin; }
    public void setNomMedecin(String nomMedecin) { this.nomMedecin = nomMedecin; }

    public Integer getNiveauPriorite() { return niveauPriorite; }
    public void setNiveauPriorite(Integer niveauPriorite) { this.niveauPriorite = niveauPriorite; }

    public LocalDateTime getTempsArrivee() { return tempsArrivee; }
    public void setTempsArrivee(LocalDateTime tempsArrivee) { this.tempsArrivee = tempsArrivee; }

    public long getTempsAttenteMinutes() { return tempsAttenteMinutes; }
    public void setTempsAttenteMinutes(long tempsAttenteMinutes) { this.tempsAttenteMinutes = tempsAttenteMinutes; }

    public String getStatut() { return statut; }
    public void setStatut(String statut) { this.statut = statut; }
}
