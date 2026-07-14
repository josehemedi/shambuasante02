package hospicloud.dtos;

import java.time.LocalDateTime;

public class WaitingRoomCallEventDTO {
    private String type = "PATIENT_CALLED";
    private Integer idHopital;
    private Integer idAdmission;
    private Integer idPatient;
    private Integer idMedecin;
    private String patientNom;
    private String medecinNom;
    private String salle;
    private Integer numeroPassage;
    private LocalDateTime appeleAt;
    /** true = le médecin rappelle un patient déjà appelé (alerte réception). */
    private boolean rappel;

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public Integer getIdHopital() { return idHopital; }
    public void setIdHopital(Integer idHopital) { this.idHopital = idHopital; }

    public Integer getIdAdmission() { return idAdmission; }
    public void setIdAdmission(Integer idAdmission) { this.idAdmission = idAdmission; }

    public Integer getIdPatient() { return idPatient; }
    public void setIdPatient(Integer idPatient) { this.idPatient = idPatient; }

    public Integer getIdMedecin() { return idMedecin; }
    public void setIdMedecin(Integer idMedecin) { this.idMedecin = idMedecin; }

    public String getPatientNom() { return patientNom; }
    public void setPatientNom(String patientNom) { this.patientNom = patientNom; }

    public String getMedecinNom() { return medecinNom; }
    public void setMedecinNom(String medecinNom) { this.medecinNom = medecinNom; }

    public String getSalle() { return salle; }
    public void setSalle(String salle) { this.salle = salle; }

    public Integer getNumeroPassage() { return numeroPassage; }
    public void setNumeroPassage(Integer numeroPassage) { this.numeroPassage = numeroPassage; }

    public LocalDateTime getAppeleAt() { return appeleAt; }
    public void setAppeleAt(LocalDateTime appeleAt) { this.appeleAt = appeleAt; }

    public boolean isRappel() { return rappel; }
    public void setRappel(boolean rappel) { this.rappel = rappel; }
}
