package hospicloud.model;

import java.time.LocalDateTime;

/**
 * Entité gérant le cycle de vie d'un examen de laboratoire.
 * Suit l'état de l'analyse, du prélèvement jusqu'à la disponibilité des résultats.
 * * Développé par Siku Hemedi Jose - Projet Hospicloud.
 */
public class AnalyseLaboratoire {

    private Integer idAnalyse;
    private Integer idPatient;
    private Integer idMedecin;      // Prescripteur
    private Integer idLaborantin;   // Technicien traitant
    private Integer idTypeAnalyse;  // Référence au catalogue (ex: NFS)
    private Integer idConsultation; // Lien optionnel
    private LocalDateTime dateDemande;
    private LocalDateTime datePrelevement;
    private LocalDateTime dateResultat;
    private String statut;          // EN_ATTENTE, PRELEVE, EN_COURS, TERMINE, ANNULE
    private String urgence;         // NORMALE, HAUTE, VITALE
    private String observationsMedecin;
    private String resultatTexte;
    private String interpretation;
    private String valeursReference;

    // Constructeur par défaut
    public AnalyseLaboratoire() {
    }

    // Constructeur complet
    public AnalyseLaboratoire(Integer idAnalyse, Integer idPatient, Integer idMedecin, 
                             Integer idLaborantin, Integer idTypeAnalyse, Integer idConsultation, 
                             LocalDateTime dateDemande, LocalDateTime datePrelevement, 
                             LocalDateTime dateResultat, String statut, String urgence, 
                             String observationsMedecin) {
        this.idAnalyse = idAnalyse;
        this.idPatient = idPatient;
        this.idMedecin = idMedecin;
        this.idLaborantin = idLaborantin;
        this.idTypeAnalyse = idTypeAnalyse;
        this.idConsultation = idConsultation;
        this.dateDemande = dateDemande;
        this.datePrelevement = datePrelevement;
        this.dateResultat = dateResultat;
        this.statut = statut;
        this.urgence = urgence;
        this.observationsMedecin = observationsMedecin;
    }

    // Getters & Setters
    public Integer getIdAnalyse() { return idAnalyse; }
    public void setIdAnalyse(Integer idAnalyse) { this.idAnalyse = idAnalyse; }

    public Integer getIdPatient() { return idPatient; }
    public void setIdPatient(Integer idPatient) { this.idPatient = idPatient; }

    public Integer getIdMedecin() { return idMedecin; }
    public void setIdMedecin(Integer idMedecin) { this.idMedecin = idMedecin; }

    public Integer getIdLaborantin() { return idLaborantin; }
    public void setIdLaborantin(Integer idLaborantin) { this.idLaborantin = idLaborantin; }

    public Integer getIdTypeAnalyse() { return idTypeAnalyse; }
    public void setIdTypeAnalyse(Integer idTypeAnalyse) { this.idTypeAnalyse = idTypeAnalyse; }

    public Integer getIdConsultation() { return idConsultation; }
    public void setIdConsultation(Integer idConsultation) { this.idConsultation = idConsultation; }

    public LocalDateTime getDateDemande() { return dateDemande; }
    public void setDateDemande(LocalDateTime dateDemande) { this.dateDemande = dateDemande; }

    public LocalDateTime getDatePrelevement() { return datePrelevement; }
    public void setDatePrelevement(LocalDateTime datePrelevement) { this.datePrelevement = datePrelevement; }

    public LocalDateTime getDateResultat() { return dateResultat; }
    public void setDateResultat(LocalDateTime dateResultat) { this.dateResultat = dateResultat; }

    public String getStatut() { return statut; }
    public void setStatut(String statut) { this.statut = statut; }

    public String getUrgence() { return urgence; }
    public void setUrgence(String urgence) { this.urgence = urgence; }

    public String getObservationsMedecin() { return observationsMedecin; }
    public void setObservationsMedecin(String observationsMedecin) { this.observationsMedecin = observationsMedecin; }

    public String getResultatTexte() { return resultatTexte; }
    public void setResultatTexte(String resultatTexte) { this.resultatTexte = resultatTexte; }

    public String getInterpretation() { return interpretation; }
    public void setInterpretation(String interpretation) { this.interpretation = interpretation; }

    public String getValeursReference() { return valeursReference; }
    public void setValeursReference(String valeursReference) { this.valeursReference = valeursReference; }

    @Override
    public String toString() {
        return "AnalyseLaboratoire{" +
                "id=" + idAnalyse +
                ", statut='" + statut + '\'' +
                ", urgence='" + urgence + '\'' +
                '}';
    }
}