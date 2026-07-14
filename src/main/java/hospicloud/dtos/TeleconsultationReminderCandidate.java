package hospicloud.dtos;

import java.time.LocalDateTime;

public class TeleconsultationReminderCandidate {

    private Integer idRdv;
    private Integer idHopital;
    private LocalDateTime dateHeureRdv;
    private String urlVisio;
    private String motifVisite;
    private String emailPatient;
    private String nomPatient;
    private String emailMedecin;
    private String nomMedecin;
    private String telephonePatient;
    private String telephoneMedecin;
    private String nomHopital;

    public Integer getIdRdv() {
        return idRdv;
    }

    public void setIdRdv(Integer idRdv) {
        this.idRdv = idRdv;
    }

    public Integer getIdHopital() {
        return idHopital;
    }

    public void setIdHopital(Integer idHopital) {
        this.idHopital = idHopital;
    }

    public LocalDateTime getDateHeureRdv() {
        return dateHeureRdv;
    }

    public void setDateHeureRdv(LocalDateTime dateHeureRdv) {
        this.dateHeureRdv = dateHeureRdv;
    }

    public String getUrlVisio() {
        return urlVisio;
    }

    public void setUrlVisio(String urlVisio) {
        this.urlVisio = urlVisio;
    }

    public String getMotifVisite() {
        return motifVisite;
    }

    public void setMotifVisite(String motifVisite) {
        this.motifVisite = motifVisite;
    }

    public String getEmailPatient() {
        return emailPatient;
    }

    public void setEmailPatient(String emailPatient) {
        this.emailPatient = emailPatient;
    }

    public String getNomPatient() {
        return nomPatient;
    }

    public void setNomPatient(String nomPatient) {
        this.nomPatient = nomPatient;
    }

    public String getEmailMedecin() {
        return emailMedecin;
    }

    public void setEmailMedecin(String emailMedecin) {
        this.emailMedecin = emailMedecin;
    }

    public String getNomMedecin() {
        return nomMedecin;
    }

    public void setNomMedecin(String nomMedecin) {
        this.nomMedecin = nomMedecin;
    }

    public String getTelephonePatient() {
        return telephonePatient;
    }

    public void setTelephonePatient(String telephonePatient) {
        this.telephonePatient = telephonePatient;
    }

    public String getTelephoneMedecin() {
        return telephoneMedecin;
    }

    public void setTelephoneMedecin(String telephoneMedecin) {
        this.telephoneMedecin = telephoneMedecin;
    }

    public String getNomHopital() {
        return nomHopital;
    }

    public void setNomHopital(String nomHopital) {
        this.nomHopital = nomHopital;
    }
}
