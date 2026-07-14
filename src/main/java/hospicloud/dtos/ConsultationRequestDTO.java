package hospicloud.dtos;

import java.math.BigDecimal;

public class ConsultationRequestDTO {
    // Les IDs techniques de structure
    /** Ignoré côté API — l'id_hopital est toujours résolu depuis le JWT (TenantContext). */
    private Integer idHopital;
    private Integer idMedecin;
    private Integer idPatient;
    private Integer idRdv; // Optionnel
    
    private String motifVisite;
    
    // Constantes vitales (Saisies par l'infirmerie ou le médecin)
    private BigDecimal poids;
    private Integer taille;
    private String tensionArterielle;
    private BigDecimal temperature;
    private Integer frequenceCardiaque;
    
    // Données cliniques (Saisies par le médecin)
    private String observations;
    private String diagnostic;

    // Constructeur vide
    public ConsultationRequestDTO() {}

    // Getters et Setters
    public Integer getIdHopital() { return idHopital; }
    public void setIdHopital(Integer idHopital) { this.idHopital = idHopital; }

    public Integer getIdMedecin() { return idMedecin; }
    public void setIdMedecin(Integer idMedecin) { this.idMedecin = idMedecin; }

    public Integer getIdPatient() { return idPatient; }
    public void setIdPatient(Integer idPatient) { this.idPatient = idPatient; }

    public Integer getIdRdv() { return idRdv; }
    public void setIdRdv(Integer idRdv) { this.idRdv = idRdv; }

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
}