package hospicloud.model;

/**
 * Entité représentant chaque ligne de médicament délivrée lors d'une dispensation.
 * Détaille la quantité précise et les instructions de prise pour chaque produit.
 * * Développé par Siku Hemedi Jose - Projet Hospicloud.
 */
public class DispensationDetail {

    private Integer idDetails;
    private Integer idDispensation; // Référence à l'acte de dispensation global
    private Integer idMedicament;   // Référence au médicament du catalogue
    private Integer quantiteDelivree;
    private String posologieDonnee; // Instructions spécifiques (ex: "3 fois par jour après repas")

    // Constructeur par défaut
    public DispensationDetail() {
    }

    // Constructeur complet
    public DispensationDetail(Integer idDetails, Integer idDispensation, Integer idMedicament, 
                              Integer quantiteDelivree, String posologieDonnee) {
        this.idDetails = idDetails;
        this.idDispensation = idDispensation;
        this.idMedicament = idMedicament;
        this.quantiteDelivree = quantiteDelivree;
        this.posologieDonnee = posologieDonnee;
    }

    // Getters & Setters
    public Integer getIdDetails() {
        return idDetails;
    }

    public void setIdDetails(Integer idDetails) {
        this.idDetails = idDetails;
    }

    public Integer getIdDispensation() {
        return idDispensation;
    }

    public void setIdDispensation(Integer idDispensation) {
        this.idDispensation = idDispensation;
    }

    public Integer getIdMedicament() {
        return idMedicament;
    }

    public void setIdMedicament(Integer idMedicament) {
        this.idMedicament = idMedicament;
    }

    public Integer getQuantiteDelivree() {
        return quantiteDelivree;
    }

    public void setQuantiteDelivree(Integer quantiteDelivree) {
        this.quantiteDelivree = quantiteDelivree;
    }

    public String getPosologieDonnee() {
        return posologieDonnee;
    }

    public void setPosologieDonnee(String posologieDonnee) {
        this.posologieDonnee = posologieDonnee;
    }

    @Override
    public String toString() {
        return "DispensationDetail{" +
                "id=" + idDetails +
                ", medicamentId=" + idMedicament +
                ", qte=" + quantiteDelivree +
                '}';
    }
}