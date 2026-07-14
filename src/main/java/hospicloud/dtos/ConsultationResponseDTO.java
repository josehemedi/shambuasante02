package hospicloud.dtos;

import java.math.BigDecimal;

public class ConsultationResponseDTO {
    private Long idConsultation;
    private Integer idHopital;
    private Integer idMedecin;
    private Integer idPatient;
    private Integer idRdv;
    private String dateConsultation; // Formatée en String (ex: "dd/MM/yyyy HH:mm") pour le Front
    private String motifVisite;
    
    // Constantes vitales
    private BigDecimal poids;
    private Integer taille;
    private String tensionArterielle;
    private BigDecimal temperature;
    private Integer frequenceCardiaque;
    
    // Données cliniques
    private String observations;
    private String diagnostic;
    private java.util.List<AnalyseConsultationDTO> analyses;
    
    private String nomHopital;
    private String nomMedecin;
    private String nomPatient;
    private String statut;
    private String dateSignature;
    private String referenceSignature;
    private String hashAbrege;
    private String numeroOrdreMedecin;
    // Constructeur vide
    public ConsultationResponseDTO() {}

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

    public String getDateConsultation() { return dateConsultation; }
    public void setDateConsultation(String dateConsultation) { this.dateConsultation = dateConsultation; }

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

	public String getNomHopital() {
		return nomHopital;
	}

	public void setNomHopital(String nomHopital) {
		this.nomHopital = nomHopital;
	}

	public String getNomMedecin() {
		return nomMedecin;
	}

	public void setNomMedecin(String nomMedecin) {
		this.nomMedecin = nomMedecin;
	}

	public String getNomPatient() {
		return nomPatient;
	}

	public void setNomPatient(String nomPatient) {
		this.nomPatient = nomPatient;
	}

    public String getStatut() { return statut; }
    public void setStatut(String statut) { this.statut = statut; }

    public String getDateSignature() { return dateSignature; }
    public void setDateSignature(String dateSignature) { this.dateSignature = dateSignature; }

    public String getReferenceSignature() { return referenceSignature; }
    public void setReferenceSignature(String referenceSignature) { this.referenceSignature = referenceSignature; }

    public String getHashAbrege() { return hashAbrege; }
    public void setHashAbrege(String hashAbrege) { this.hashAbrege = hashAbrege; }

    public String getNumeroOrdreMedecin() { return numeroOrdreMedecin; }
    public void setNumeroOrdreMedecin(String numeroOrdreMedecin) { this.numeroOrdreMedecin = numeroOrdreMedecin; }

    public java.util.List<AnalyseConsultationDTO> getAnalyses() {
        return analyses;
    }

    public void setAnalyses(java.util.List<AnalyseConsultationDTO> analyses) {
        this.analyses = analyses;
    }
    
    
}