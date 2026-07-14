package hospicloud.model;

import java.time.LocalDateTime;

/**
 * Entité traçant chaque flux (entrée, sortie, perte) de médicaments.
 * Assure l'audit et la transparence de la gestion de la pharmacie.
 * * Développé par Siku Hemedi Jose - Projet Hospicloud.
 */
public class MouvementStock {

    private Integer idMouvement;
    private Integer idStock;       // Référence au stock spécifique (Hôpital + Médicament)
    private String typeMouvement;  // ENTREE, SORTIE, RETOUR, PERIME
    private Integer quantite;
    private String motif;          // ex: "Livraison fournisseur", "Vente ordonnance"
    private LocalDateTime dateMouvement;
    private Integer idUtilisateur; // Le pharmacien ou l'agent ayant validé l'action

    // Constructeur par défaut
    public MouvementStock() {
    }

    // Constructeur complet
    public MouvementStock(Integer idMouvement, Integer idStock, String typeMouvement, 
                          Integer quantite, String motif, LocalDateTime dateMouvement, 
                          Integer idUtilisateur) {
        this.idMouvement = idMouvement;
        this.idStock = idStock;
        this.typeMouvement = typeMouvement;
        this.quantite = quantite;
        this.motif = motif;
        this.dateMouvement = dateMouvement;
        this.idUtilisateur = idUtilisateur;
    }

    // Getters & Setters
    public Integer getIdMouvement() { return idMouvement; }
    public void setIdMouvement(Integer idMouvement) { this.idMouvement = idMouvement; }

    public Integer getIdStock() { return idStock; }
    public void setIdStock(Integer idStock) { this.idStock = idStock; }

    public String getTypeMouvement() { return typeMouvement; }
    public void setTypeMouvement(String typeMouvement) { this.typeMouvement = typeMouvement; }

    public Integer getQuantite() { return quantite; }
    public void setQuantite(Integer quantite) { this.quantite = quantite; }

    public String getMotif() { return motif; }
    public void setMotif(String motif) { this.motif = motif; }

    public LocalDateTime getDateMouvement() { return dateMouvement; }
    public void setDateMouvement(LocalDateTime dateMouvement) { this.dateMouvement = dateMouvement; }

    public Integer getIdUtilisateur() { return idUtilisateur; }
    public void setIdUtilisateur(Integer idUtilisateur) { this.idUtilisateur = idUtilisateur; }

    @Override
    public String toString() {
        return "Mouvement{" +
                "id=" + idMouvement +
                ", type='" + typeMouvement + '\'' +
                ", qte=" + quantite +
                ", date=" + dateMouvement +
                '}';
    }
}