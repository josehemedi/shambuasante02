package hospicloud.dtos.reception;

public class WalkInRegistrationResponseDTO {

    private Integer idPatient;
    private String codePatient;
    private String nomPatient;
    private Integer idMedecin;
    private String nomMedecin;
    private String specialiteMedecin;
    private Integer idAdmission;
    private Integer idRendezVous;
    private Integer niveauPriorite;
    private String motifConsultation;
    private String serviceDemande;
    private String message;
    private Integer numeroPassage;
    private String statut;

    public Integer getIdPatient() { return idPatient; }
    public void setIdPatient(Integer idPatient) { this.idPatient = idPatient; }

    public String getCodePatient() { return codePatient; }
    public void setCodePatient(String codePatient) { this.codePatient = codePatient; }

    public String getNomPatient() { return nomPatient; }
    public void setNomPatient(String nomPatient) { this.nomPatient = nomPatient; }

    public Integer getIdMedecin() { return idMedecin; }
    public void setIdMedecin(Integer idMedecin) { this.idMedecin = idMedecin; }

    public String getNomMedecin() { return nomMedecin; }
    public void setNomMedecin(String nomMedecin) { this.nomMedecin = nomMedecin; }

    public String getSpecialiteMedecin() { return specialiteMedecin; }
    public void setSpecialiteMedecin(String specialiteMedecin) { this.specialiteMedecin = specialiteMedecin; }

    public Integer getIdAdmission() { return idAdmission; }
    public void setIdAdmission(Integer idAdmission) { this.idAdmission = idAdmission; }

    public Integer getIdRendezVous() { return idRendezVous; }
    public void setIdRendezVous(Integer idRendezVous) { this.idRendezVous = idRendezVous; }

    public Integer getNiveauPriorite() { return niveauPriorite; }
    public void setNiveauPriorite(Integer niveauPriorite) { this.niveauPriorite = niveauPriorite; }

    public String getMotifConsultation() { return motifConsultation; }
    public void setMotifConsultation(String motifConsultation) { this.motifConsultation = motifConsultation; }

    public String getServiceDemande() { return serviceDemande; }
    public void setServiceDemande(String serviceDemande) { this.serviceDemande = serviceDemande; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public Integer getNumeroPassage() { return numeroPassage; }
    public void setNumeroPassage(Integer numeroPassage) { this.numeroPassage = numeroPassage; }

    public String getStatut() { return statut; }
    public void setStatut(String statut) { this.statut = statut; }
}
