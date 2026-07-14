package hospicloud.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Entité représentant les indicateurs de performance (KPI) de l'hôpital.
 * Permet le suivi statistique pour l'aide à la décision des gestionnaires.
 * * Développé par Siku Hemedi Jose - Projet Hospicloud.
 */
public class IndicateurKpi {

    private Integer idKpi;
    private Integer idHopital;
    private String nomIndicateur;   // ex: "Taux d'occupation", "CA du jour", "Délai moyen Labo"
    private BigDecimal valeurActuelle;
    private LocalDateTime dateMiseAJour;

    // Constructeur par défaut
    public IndicateurKpi() {
    }

    // Constructeur complet
    public IndicateurKpi(Integer idKpi, Integer idHopital, String nomIndicateur, 
                         BigDecimal valeurActuelle, LocalDateTime dateMiseAJour) {
        this.idKpi = idKpi;
        this.idHopital = idHopital;
        this.nomIndicateur = nomIndicateur;
        this.valeurActuelle = valeurActuelle;
        this.dateMiseAJour = dateMiseAJour;
    }

    // Getters & Setters
    public Integer getIdKpi() { return idKpi; }
    public void setIdKpi(Integer idKpi) { this.idKpi = idKpi; }

    public Integer getIdHopital() { return idHopital; }
    public void setIdHopital(Integer idHopital) { this.idHopital = idHopital; }

    public String getNomIndicateur() { return nomIndicateur; }
    public void setNomIndicateur(String nomIndicateur) { this.nomIndicateur = nomIndicateur; }

    public BigDecimal getValeurActuelle() { return valeurActuelle; }
    public void setValeurActuelle(BigDecimal valeurActuelle) { this.valeurActuelle = valeurActuelle; }

    public LocalDateTime getDateMiseAJour() { return dateMiseAJour; }
    public void setDateMiseAJour(LocalDateTime dateMiseAJour) { this.dateMiseAJour = dateMiseAJour; }

    @Override
    public String toString() {
        return "KPI{" +
                "nom='" + nomIndicateur + '\'' +
                ", valeur=" + valeurActuelle +
                ", maj=" + dateMiseAJour +
                '}';
    }
}