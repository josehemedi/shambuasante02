package hospicloud.model;

/**
 * Entité représentant une catégorie de médicaments (ex: Antibiotiques, Analgésiques).
 * Permet l'organisation du catalogue pharmaceutique propre à chaque hôpital.
 * * Développé par Siku Hemedi Jose - Projet Hospicloud.
 */
public class CategorieMedicament {

    private Integer idCategorie;
    private Integer idHopital; // Isolation SaaS pour le catalogue de l'hôpital
    private String nomCategorie;
    private String description;

    // Constructeur par défaut
    public CategorieMedicament() {
    }

    // Constructeur complet
    public CategorieMedicament(Integer idCategorie, Integer idHopital, String nomCategorie, String description) {
        this.idCategorie = idCategorie;
        this.idHopital = idHopital;
        this.nomCategorie = nomCategorie;
        this.description = description;
    }

    // Getters & Setters
    public Integer getIdCategorie() {
        return idCategorie;
    }

    public void setIdCategorie(Integer idCategorie) {
        this.idCategorie = idCategorie;
    }

    public Integer getIdHopital() {
        return idHopital;
    }

    public void setIdHopital(Integer idHopital) {
        this.idHopital = idHopital;
    }

    public String getNomCategorie() {
        return nomCategorie;
    }

    public void setNomCategorie(String nomCategorie) {
        this.nomCategorie = nomCategorie;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public String toString() {
        return "CategorieMedicament{" +
                "id=" + idCategorie +
                ", nom='" + nomCategorie + '\'' +
                ", hopitalId=" + idHopital +
                '}';
    }
}