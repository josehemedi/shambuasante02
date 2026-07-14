package hospicloud.model;

import java.math.BigDecimal;

/**
 * Entité définissant les types d'examens de laboratoire disponibles.
 * Gère la tarification et les délais de réalisation pour chaque hôpital.
 * * Développé par Siku Hemedi Jose - Projet Hospicloud.
 */
public class TypeAnalyse {

    private Integer idTypeAnalyse;
    private Integer idHopital; // Isolation SaaS pour le catalogue labo
    private String nomAnalyse; // ex: NFS, Glycémie, VIH, Paludisme (GE)
    private String description;
    private BigDecimal prixAnalyse;
    private Integer delaiMoyenHeures;

    // Constructeur par défaut
    public TypeAnalyse() {
    }

    // Constructeur complet
    public TypeAnalyse(Integer idTypeAnalyse, Integer idHopital, String nomAnalyse, 
                       String description, BigDecimal prixAnalyse, Integer delaiMoyenHeures) {
        this.idTypeAnalyse = idTypeAnalyse;
        this.idHopital = idHopital;
        this.nomAnalyse = nomAnalyse;
        this.description = description;
        this.prixAnalyse = prixAnalyse;
        this.delaiMoyenHeures = delaiMoyenHeures;
    }

    // Getters & Setters
    public Integer getIdTypeAnalyse() {
        return idTypeAnalyse;
    }

    public void setIdTypeAnalyse(Integer idTypeAnalyse) {
        this.idTypeAnalyse = idTypeAnalyse;
    }

    public Integer getIdHopital() {
        return idHopital;
    }

    public void setIdHopital(Integer idHopital) {
        this.idHopital = idHopital;
    }

    public String getNomAnalyse() {
        return nomAnalyse;
    }

    public void setNomAnalyse(String nomAnalyse) {
        this.nomAnalyse = nomAnalyse;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public BigDecimal getPrixAnalyse() {
        return prixAnalyse;
    }

    public void setPrixAnalyse(BigDecimal prixAnalyse) {
        this.prixAnalyse = prixAnalyse;
    }

    public Integer getDelaiMoyenHeures() {
        return delaiMoyenHeures;
    }

    public void setDelaiMoyenHeures(Integer delaiMoyenHeures) {
        this.delaiMoyenHeures = delaiMoyenHeures;
    }

    @Override
    public String toString() {
        return "TypeAnalyse{" +
                "id=" + idTypeAnalyse +
                ", nom='" + nomAnalyse + '\'' +
                ", prix=" + prixAnalyse +
                ", delai=" + delaiMoyenHeures + "h" +
                '}';
    }
}