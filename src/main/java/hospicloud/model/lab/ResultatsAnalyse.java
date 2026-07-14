package hospicloud.model.lab;

import java.math.BigDecimal;

/**
 * Entité pour les résultats d'analyses.
 * Correspond à la table resultats_analyses.
 */
public class ResultatsAnalyse {
    private String id;
    private String idCommandeAnalyse;
    private String idLocataire;
    private String nomParametre;
    private BigDecimal valeurMesuree;
    private String unite;
    private BigDecimal seuilMin;
    private BigDecimal seuilMax;
    private Boolean estCritique;
    private Boolean estAcquitte;

    // Getters & Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getIdCommandeAnalyse() { return idCommandeAnalyse; }
    public void setIdCommandeAnalyse(String idCommandeAnalyse) { this.idCommandeAnalyse = idCommandeAnalyse; }

    public String getIdLocataire() { return idLocataire; }
    public void setIdLocataire(String idLocataire) { this.idLocataire = idLocataire; }

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

    public Boolean getEstCritique() { return estCritique; }
    public void setEstCritique(Boolean estCritique) { this.estCritique = estCritique; }

    public Boolean getEstAcquitte() { return estAcquitte; }
    public void setEstAcquitte(Boolean estAcquitte) { this.estAcquitte = estAcquitte; }
}
