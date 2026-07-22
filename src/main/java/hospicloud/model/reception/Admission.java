package hospicloud.model.reception;

import java.time.LocalDateTime;

public class Admission {
    private Integer idAdmission;
    private Integer idHopital; // Multi-tenant ID
    private Integer idPatient;
    private Integer idMedecin;      // Optional si assigné à un service
    private Integer idRendezVous;   // Lien avec le rendez-vous si pertinent
    private Integer niveauPriorite; // 1 = Urgence, 2 = Haute, 3 = Normale
    private LocalDateTime tempsArrivee;
    private String statut;          // ATTENTE_TRIAGE, EN_ATTENTE, ORIENTE, ENREGISTRE, APPELE, EN_CONSULTATION, TERMINE, ABSENT
    private Integer creePar;
    private Integer checkInPar;
    private Integer numeroPassage;
    private String salle;
    private LocalDateTime appeleAt;
    private String typeVisite;
    private String motifGeneral;
    private String serviceDemande;
    private String observationsAdmin;
    private String modePaiement;

    // Getters and Setters
    public Integer getIdAdmission() { return idAdmission; }
    public void setIdAdmission(Integer idAdmission) { this.idAdmission = idAdmission; }

    public Integer getIdHopital() { return idHopital; }
    public void setIdHopital(Integer idHopital) { this.idHopital = idHopital; }

    public Integer getIdPatient() { return idPatient; }
    public void setIdPatient(Integer idPatient) { this.idPatient = idPatient; }

    public Integer getIdMedecin() { return idMedecin; }
    public void setIdMedecin(Integer idMedecin) { this.idMedecin = idMedecin; }

    public Integer getIdRendezVous() { return idRendezVous; }
    public void setIdRendezVous(Integer idRendezVous) { this.idRendezVous = idRendezVous; }

    public Integer getNiveauPriorite() { return niveauPriorite; }
    public void setNiveauPriorite(Integer niveauPriorite) { this.niveauPriorite = niveauPriorite; }

    public LocalDateTime getTempsArrivee() { return tempsArrivee; }
    public void setTempsArrivee(LocalDateTime tempsArrivee) { this.tempsArrivee = tempsArrivee; }

    public String getStatut() { return statut; }
    public void setStatut(String statut) { this.statut = statut; }

    public Integer getCreePar() { return creePar; }
    public void setCreePar(Integer creePar) { this.creePar = creePar; }

    public Integer getCheckInPar() { return checkInPar; }
    public void setCheckInPar(Integer checkInPar) { this.checkInPar = checkInPar; }

    public Integer getNumeroPassage() { return numeroPassage; }
    public void setNumeroPassage(Integer numeroPassage) { this.numeroPassage = numeroPassage; }

    public String getSalle() { return salle; }
    public void setSalle(String salle) { this.salle = salle; }

    public LocalDateTime getAppeleAt() { return appeleAt; }
    public void setAppeleAt(LocalDateTime appeleAt) { this.appeleAt = appeleAt; }

    public String getTypeVisite() { return typeVisite; }
    public void setTypeVisite(String typeVisite) { this.typeVisite = typeVisite; }

    public String getMotifGeneral() { return motifGeneral; }
    public void setMotifGeneral(String motifGeneral) { this.motifGeneral = motifGeneral; }

    public String getServiceDemande() { return serviceDemande; }
    public void setServiceDemande(String serviceDemande) { this.serviceDemande = serviceDemande; }

    public String getObservationsAdmin() { return observationsAdmin; }
    public void setObservationsAdmin(String observationsAdmin) { this.observationsAdmin = observationsAdmin; }

    public String getModePaiement() { return modePaiement; }
    public void setModePaiement(String modePaiement) { this.modePaiement = modePaiement; }
}
