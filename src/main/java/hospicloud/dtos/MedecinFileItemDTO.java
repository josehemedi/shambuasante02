package hospicloud.dtos;

import java.time.LocalDateTime;

public class MedecinFileItemDTO {
    private Integer idAdmission;
    private Integer idRendezVous;
    private Integer idPatient;
    private String patientName;
    private String waited;
    private String priority;
    private String salle;
    private Integer numeroPassage;
    private String statut;
    private LocalDateTime tempsArrivee;
    private boolean canCall;
    private boolean canStart;

    public Integer getIdAdmission() { return idAdmission; }
    public void setIdAdmission(Integer idAdmission) { this.idAdmission = idAdmission; }

    public Integer getIdRendezVous() { return idRendezVous; }
    public void setIdRendezVous(Integer idRendezVous) { this.idRendezVous = idRendezVous; }

    public Integer getIdPatient() { return idPatient; }
    public void setIdPatient(Integer idPatient) { this.idPatient = idPatient; }

    public String getPatientName() { return patientName; }
    public void setPatientName(String patientName) { this.patientName = patientName; }

    public String getWaited() { return waited; }
    public void setWaited(String waited) { this.waited = waited; }

    public String getPriority() { return priority; }
    public void setPriority(String priority) { this.priority = priority; }

    public String getSalle() { return salle; }
    public void setSalle(String salle) { this.salle = salle; }

    public Integer getNumeroPassage() { return numeroPassage; }
    public void setNumeroPassage(Integer numeroPassage) { this.numeroPassage = numeroPassage; }

    public String getStatut() { return statut; }
    public void setStatut(String statut) { this.statut = statut; }

    public LocalDateTime getTempsArrivee() { return tempsArrivee; }
    public void setTempsArrivee(LocalDateTime tempsArrivee) { this.tempsArrivee = tempsArrivee; }

    public boolean isCanCall() { return canCall; }
    public void setCanCall(boolean canCall) { this.canCall = canCall; }

    public boolean isCanStart() { return canStart; }
    public void setCanStart(boolean canStart) { this.canStart = canStart; }

    /** Compat dashboard médecin existant (filePatients[].id). */
    public Integer getId() {
        return idAdmission != null ? idAdmission : idRendezVous;
    }

    public String getRoom() {
        return salle;
    }

    public LocalDateTime getAppointmentTime() {
        return tempsArrivee;
    }
}
