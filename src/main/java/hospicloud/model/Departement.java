package hospicloud.model;

/**
 * Entité représentant un service ou département au sein d'un hôpital (ex: Pédiatrie, Cardiologie).
 * Cette classe gère la relation hiérarchique avec l'entité Hopital.
 * * Développé par Siku Hemedi Jose - Projet Hospicloud.
 */
public class Departement {

    private Integer idDepartement;
    private Integer idHopital; // Clé étrangère vers la table hopitaux
    private String nomDepartement;
    private String description;

    // Constructeur par défaut
    public Departement() {
    }

    // Constructeur complet
    public Departement(Integer idDepartement, Integer idHopital, String nomDepartement, String description) {
        this.idDepartement = idDepartement;
        this.idHopital = idHopital;
        this.nomDepartement = nomDepartement;
        this.description = description;
    }

    // Getters & Setters
    public Integer getIdDepartement() {
        return idDepartement;
    }

    public void setIdDepartement(Integer idDepartement) {
        this.idDepartement = idDepartement;
    }

    public Integer getIdHopital() {
        return idHopital;
    }

    public void setIdHopital(Integer idHopital) {
        this.idHopital = idHopital;
    }

    public String getNomDepartement() {
        return nomDepartement;
    }

    public void setNomDepartement(String nomDepartement) {
        this.nomDepartement = nomDepartement;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public String toString() {
        return "Departement{" +
                "id=" + idDepartement +
                ", idHopital=" + idHopital +
                ", nom='" + nomDepartement + '\'' +
                '}';
    }
}