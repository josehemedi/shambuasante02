package hospicloud.model;

import java.math.BigDecimal;

/**
 * Entité définissant les composantes mesurables d'une analyse de laboratoire.
 * Gère les unités et les seuils de normalité selon le sexe.
 * * Développé par Siku Hemedi Jose - Projet Hospicloud.
 */
public class ParametreAnalyse {

    private Integer idParametre;
    private Integer idTypeAnalyse; // Lien vers l'analyse parente (ex: NFS)
    private String nomParametre;   // ex: Glycémie à jeun, Taux de CRP
    private String uniteMesure;    // ex: g/dL, mg/L, %
    private BigDecimal valeurMinReference;
    private BigDecimal valeurMaxReference;
    private String sexeConcerne;   // M, F, TOUS

    // Constructeur par défaut
    public ParametreAnalyse() {
    }

    // Constructeur complet
    public ParametreAnalyse(Integer idParametre, Integer idTypeAnalyse, String nomParametre, 
                            String uniteMesure, BigDecimal valeurMinReference, 
                            BigDecimal valeurMaxReference, String sexeConcerne) {
        this.idParametre = idParametre;
        this.idTypeAnalyse = idTypeAnalyse;
        this.nomParametre = nomParametre;
        this.uniteMesure = uniteMesure;
        this.valeurMinReference = valeurMinReference;
        this.valeurMaxReference = valeurMaxReference;
        this.sexeConcerne = sexeConcerne;
    }

    // Getters & Setters
    public Integer getIdParametre() { return idParametre; }
    public void setIdParametre(Integer idParametre) { this.idParametre = idParametre; }

    public Integer getIdTypeAnalyse() { return idTypeAnalyse; }
    public void setIdTypeAnalyse(Integer idTypeAnalyse) { this.idTypeAnalyse = idTypeAnalyse; }

    public String getNomParametre() { return nomParametre; }
    public void setNomParametre(String nomParametre) { this.nomParametre = nomParametre; }

    public String getUniteMesure() { return uniteMesure; }
    public void setUniteMesure(String uniteMesure) { this.uniteMesure = uniteMesure; }

    public BigDecimal getValeurMinReference() { return valeurMinReference; }
    public void setValeurMinReference(BigDecimal valeurMinReference) { this.valeurMinReference = valeurMinReference; }

    public BigDecimal getValeurMaxReference() { return valeurMaxReference; }
    public void setValeurMaxReference(BigDecimal valeurMaxReference) { this.valeurMaxReference = valeurMaxReference; }

    public String getSexeConcerne() { return sexeConcerne; }
    public void setSexeConcerne(String sexeConcerne) { this.sexeConcerne = sexeConcerne; }

    @Override
    public String toString() {
        return "ParametreAnalyse{" +
                "id=" + idParametre +
                ", nom='" + nomParametre + '\'' +
                ", unite='" + uniteMesure + '\'' +
                '}';
    }
}