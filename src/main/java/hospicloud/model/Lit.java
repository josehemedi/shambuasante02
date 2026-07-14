package hospicloud.model;

/**
 * Entité représentant un lit individuel au sein d'une chambre.
 * C'est l'unité de base pour l'admission d'un patient en hospitalisation.
 * * Développé par Siku Hemedi Jose - Projet Hospicloud.
 */
public class Lit {

    private Integer idLit;
    private Integer idHopital;
    private Integer idChambre; // Clé étrangère vers la table chambres
    private String codeLit;    // Ex: "LIT-A", "01"
    private boolean estOccupe;

    // Constructeur par défaut
    public Lit() {
    }

    // Constructeur complet
    public Lit(Integer idLit, Integer idHopital, Integer idChambre, String codeLit, boolean estOccupe) {
        this.idLit = idLit;
        this.idHopital = idHopital;
        this.idChambre = idChambre;
        this.codeLit = codeLit;
        this.estOccupe = estOccupe;
    }

    // Getters & Setters
    public Integer getIdLit() {
        return idLit;
    }

    public void setIdLit(Integer idLit) {
        this.idLit = idLit;
    }

    public Integer getIdHopital() {
        return idHopital;
    }

    public void setIdHopital(Integer idHopital) {
        this.idHopital = idHopital;
    }

    public Integer getIdChambre() {
        return idChambre;
    }

    public void setIdChambre(Integer idChambre) {
        this.idChambre = idChambre;
    }

    public String getCodeLit() {
        return codeLit;
    }

    public void setCodeLit(String codeLit) {
        this.codeLit = codeLit;
    }

    public boolean isEstOccupe() {
        return estOccupe;
    }

    public void setEstOccupe(boolean estOccupe) {
        this.estOccupe = estOccupe;
    }

    @Override
    public String toString() {
        return "Lit{" +
                "id=" + idLit +
                ", chambre=" + idChambre +
                ", code='" + codeLit + '\'' +
                ", occupe=" + estOccupe +
                '}';
    }
}