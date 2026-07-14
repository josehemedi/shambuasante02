package hospicloud.dtos;

import java.math.BigDecimal;
import java.util.List;

public class ConsultationFicheDTO {

    private BigDecimal poids;
    private Integer taille;
    private String tensionArterielle;
    private BigDecimal temperature;
    private Integer frequenceCardiaque;
    private String observations;
    private String diagnostic;
    private List<AnalyseConsultationDTO> analyses;
    /** Si true, la fiche est visible dans les dossiers médicaux */
    private Boolean finaliser;

    public ConsultationFicheDTO() {}

    public BigDecimal getPoids() {
        return poids;
    }

    public void setPoids(BigDecimal poids) {
        this.poids = poids;
    }

    public Integer getTaille() {
        return taille;
    }

    public void setTaille(Integer taille) {
        this.taille = taille;
    }

    public String getTensionArterielle() {
        return tensionArterielle;
    }

    public void setTensionArterielle(String tensionArterielle) {
        this.tensionArterielle = tensionArterielle;
    }

    public BigDecimal getTemperature() {
        return temperature;
    }

    public void setTemperature(BigDecimal temperature) {
        this.temperature = temperature;
    }

    public Integer getFrequenceCardiaque() {
        return frequenceCardiaque;
    }

    public void setFrequenceCardiaque(Integer frequenceCardiaque) {
        this.frequenceCardiaque = frequenceCardiaque;
    }

    public String getObservations() {
        return observations;
    }

    public void setObservations(String observations) {
        this.observations = observations;
    }

    public String getDiagnostic() {
        return diagnostic;
    }

    public void setDiagnostic(String diagnostic) {
        this.diagnostic = diagnostic;
    }

    public List<AnalyseConsultationDTO> getAnalyses() {
        return analyses;
    }

    public void setAnalyses(List<AnalyseConsultationDTO> analyses) {
        this.analyses = analyses;
    }

    public Boolean getFinaliser() {
        return finaliser;
    }

    public void setFinaliser(Boolean finaliser) {
        this.finaliser = finaliser;
    }
}
