package hospicloud.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import hospicloud.model.enums.ConsultationStatut;

public class ConsultationMedicale {
    private Long idConsultation;
    private Integer idHopital;
    private Integer idMedecin;
    private Integer idPatient;
    private Integer idRdv;
    private LocalDateTime dateConsultation;
    private String motifVisite;
    
    // Constantes vitales (Infirmerie)
    private BigDecimal poids;
    private Integer taille;
    private String tensionArterielle;
    private BigDecimal temperature;
    private Integer frequenceCardiaque;
    
    // Données cliniques (Médecin)
    private String observations;
    private String diagnostic;
    /** JSON sérialisé : liste des analyses/examens réalisés ou prescrits */
    private String analysesPrescrites;

    /** Fiche enregistrée explicitement par le médecin (visible dans les dossiers médicaux) */
    private Boolean ficheFinalisee;

    private ConsultationStatut statut;
    private LocalDateTime dateSignature;

    /** Champs enrichis (jointures SQL, non persistés) */
    private String nomMedecin;
    private String nomPatient;
    private String nomHopital;

    // Constructeur vide
    public ConsultationMedicale() {}

    // Getters et Setters
    public Long getIdConsultation() { return idConsultation; }
    public void setIdConsultation(Long idConsultation) { this.idConsultation = idConsultation; }

    public Integer getIdHopital() { return idHopital; }
    public void setIdHopital(Integer idHopital) { this.idHopital = idHopital; }

    public Integer getIdMedecin() { return idMedecin; }
    public void setIdMedecin(Integer idMedecin) { this.idMedecin = idMedecin; }

    public Integer getIdPatient() { return idPatient; }
    public void setIdPatient(Integer idPatient) { this.idPatient = idPatient; }

    public Integer getIdRdv() { return idRdv; }
    public void setIdRdv(Integer idRdv) { this.idRdv = idRdv; }

    public LocalDateTime getDateConsultation() { return dateConsultation; }
    public void setDateConsultation(LocalDateTime dateConsultation) { this.dateConsultation = dateConsultation; }

    public String getMotifVisite() { return motifVisite; }
    public void setMotifVisite(String motifVisite) { this.motifVisite = motifVisite; }

    public BigDecimal getPoids() { return poids; }
    public void setPoids(BigDecimal poids) { this.poids = poids; }

    public Integer getTaille() { return taille; }
    public void setTaille(Integer taille) { this.taille = taille; }

    public String getTensionArterielle() { return tensionArterielle; }
    public void setTensionArterielle(String tensionArterielle) { this.tensionArterielle = tensionArterielle; }

    public BigDecimal getTemperature() { return temperature; }
    public void setTemperature(BigDecimal temperature) { this.temperature = temperature; }

    public Integer getFrequenceCardiaque() { return frequenceCardiaque; }
    public void setFrequenceCardiaque(Integer frequenceCardiaque) { this.frequenceCardiaque = frequenceCardiaque; }

    public String getObservations() { return observations; }
    public void setObservations(String observations) { this.observations = observations; }

    public String getDiagnostic() { return diagnostic; }
    public void setDiagnostic(String diagnostic) { this.diagnostic = diagnostic; }

    public String getAnalysesPrescrites() { return analysesPrescrites; }
    public void setAnalysesPrescrites(String analysesPrescrites) { this.analysesPrescrites = analysesPrescrites; }

    public Boolean getFicheFinalisee() { return ficheFinalisee; }
    public void setFicheFinalisee(Boolean ficheFinalisee) { this.ficheFinalisee = ficheFinalisee; }

    public ConsultationStatut getStatut() { return statut; }
    public void setStatut(ConsultationStatut statut) { this.statut = statut; }

    public LocalDateTime getDateSignature() { return dateSignature; }
    public void setDateSignature(LocalDateTime dateSignature) { this.dateSignature = dateSignature; }

    public boolean isSignee() {
        return statut == ConsultationStatut.SIGNEE;
    }

    public String getNomMedecin() { return nomMedecin; }
    public void setNomMedecin(String nomMedecin) { this.nomMedecin = nomMedecin; }

    public String getNomPatient() { return nomPatient; }
    public void setNomPatient(String nomPatient) { this.nomPatient = nomPatient; }

    public String getNomHopital() { return nomHopital; }
    public void setNomHopital(String nomHopital) { this.nomHopital = nomHopital; }
}