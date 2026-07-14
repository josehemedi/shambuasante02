package hospicloud.model;

/**
 * Entité représentant la valeur mesurée pour un paramètre spécifique d'une analyse.
 * Permet de stocker le résultat final et de signaler les anomalies cliniques.
 * * Développé par Siku Hemedi Jose - Projet Hospicloud.
 */
public class ResultatAnalyse {

    private Integer idResultat;
    private Integer idAnalyse;    // Lien vers l'examen global
    private Integer idParametre;  // Lien vers la définition du paramètre (ex: Hémoglobine)
    private String valeurTrouvee; // Stocké en String pour gérer le qualitatif et le quantitatif
    private boolean estAnormal;   // Flag pour alerter le médecin
    private String commentaireLaborantin;

    // Constructeur par défaut
    public ResultatAnalyse() {
    }

    // Constructeur complet
    public ResultatAnalyse(Integer idResultat, Integer idAnalyse, Integer idParametre, 
                           String valeurTrouvee, boolean estAnormal, String commentaireLaborantin) {
        this.idResultat = idResultat;
        this.idAnalyse = idAnalyse;
        this.idParametre = idParametre;
        this.valeurTrouvee = valeurTrouvee;
        this.estAnormal = estAnormal;
        this.commentaireLaborantin = commentaireLaborantin;
    }

    // Getters & Setters
    public Integer getIdResultat() {
        return idResultat;
    }

    public void setIdResultat(Integer idResultat) {
        this.idResultat = idResultat;
    }

    public Integer getIdAnalyse() {
        return idAnalyse;
    }

    public void setIdAnalyse(Integer idAnalyse) {
        this.idAnalyse = idAnalyse;
    }

    public Integer getIdParametre() {
        return idParametre;
    }

    public void setIdParametre(Integer idParametre) {
        this.idParametre = idParametre;
    }

    public String getValeurTrouvee() {
        return valeurTrouvee;
    }

    public void setValeurTrouvee(String valeurTrouvee) {
        this.valeurTrouvee = valeurTrouvee;
    }

    public boolean isEstAnormal() {
        return estAnormal;
    }

    public void setEstAnormal(boolean estAnormal) {
        this.estAnormal = estAnormal;
    }

    public String getCommentaireLaborantin() {
        return commentaireLaborantin;
    }

    public void setCommentaireLaborantin(String commentaireLaborantin) {
        this.commentaireLaborantin = commentaireLaborantin;
    }

    @Override
    public String toString() {
        return "ResultatAnalyse{" +
                "id=" + idResultat +
                ", analyseId=" + idAnalyse +
                ", valeur='" + valeurTrouvee + '\'' +
                ", anormal=" + estAnormal +
                '}';
    }
}