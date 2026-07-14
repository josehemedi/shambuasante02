package hospicloud.dtos.sortie;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDateTime;

public class PretSortieDTO {

    private Integer idBonSortie;
    private String numeroBon;
    private Integer idPatient;
    private String nomPatient;
    private String diagnosticFinal;
    private String etatSortie;
    private String autorisePar;
    private Boolean statutPaiementFinal;
    private String statutWorkflow;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime dateSortie;

    private String recommandations;

    public String getRecommandations() { return recommandations; }
    public void setRecommandations(String recommandations) { this.recommandations = recommandations; }

    public Integer getIdBonSortie() { return idBonSortie; }
    public void setIdBonSortie(Integer idBonSortie) { this.idBonSortie = idBonSortie; }

    public String getNumeroBon() { return numeroBon; }
    public void setNumeroBon(String numeroBon) { this.numeroBon = numeroBon; }

    public Integer getIdPatient() { return idPatient; }
    public void setIdPatient(Integer idPatient) { this.idPatient = idPatient; }

    public String getNomPatient() { return nomPatient; }
    public void setNomPatient(String nomPatient) { this.nomPatient = nomPatient; }

    public String getDiagnosticFinal() { return diagnosticFinal; }
    public void setDiagnosticFinal(String diagnosticFinal) { this.diagnosticFinal = diagnosticFinal; }

    public String getEtatSortie() { return etatSortie; }
    public void setEtatSortie(String etatSortie) { this.etatSortie = etatSortie; }

    public String getAutorisePar() { return autorisePar; }
    public void setAutorisePar(String autorisePar) { this.autorisePar = autorisePar; }

    public Boolean getStatutPaiementFinal() { return statutPaiementFinal; }
    public void setStatutPaiementFinal(Boolean statutPaiementFinal) { this.statutPaiementFinal = statutPaiementFinal; }

    public String getStatutWorkflow() { return statutWorkflow; }
    public void setStatutWorkflow(String statutWorkflow) { this.statutWorkflow = statutWorkflow; }

    public LocalDateTime getDateSortie() { return dateSortie; }
    public void setDateSortie(LocalDateTime dateSortie) { this.dateSortie = dateSortie; }
}
