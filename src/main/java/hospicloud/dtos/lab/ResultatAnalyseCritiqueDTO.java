package hospicloud.dtos.lab;

import java.math.BigDecimal;

public class ResultatAnalyseCritiqueDTO {
    private String id;
    private String idCommandeAnalyse;
    private String nomParametre;
    private BigDecimal valeurMesuree;
    private String unite;
    private BigDecimal seuilMin;
    private BigDecimal seuilMax;
    private Boolean estAcquitte;

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getIdCommandeAnalyse() { return idCommandeAnalyse; }
    public void setIdCommandeAnalyse(String idCommandeAnalyse) { this.idCommandeAnalyse = idCommandeAnalyse; }

    public String getNomParametre() { return nomParametre; }
    public void setNomParametre(String nomParametre) { this.nomParametre = nomParametre; }

    public BigDecimal getValeurMesuree() { return valeurMesuree; }
    public void setValeurMesuree(BigDecimal valeurMesuree) { this.valeurMesuree = valeurMesuree; }

    public String getUnite() { return unite; }
    public void setUnite(String unite) { this.unite = unite; }

    public BigDecimal getSeuilMin() { return seuilMin; }
    public void setSeuilMin(BigDecimal seuilMin) { this.seuilMin = seuilMin; }

    public BigDecimal getSeuilMax() { return seuilMax; }
    public void setSeuilMax(BigDecimal seuilMax) { this.seuilMax = seuilMax; }

    public Boolean getEstAcquitte() { return estAcquitte; }
    public void setEstAcquitte(Boolean estAcquitte) { this.estAcquitte = estAcquitte; }
}
