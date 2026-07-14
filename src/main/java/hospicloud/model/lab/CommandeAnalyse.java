package hospicloud.model.lab;

import java.time.LocalDateTime;

/**
 * Entité pour les commandes d'analyses (Laboratoire).
 * Correspond à la table commandes_analyses.
 */
public class CommandeAnalyse {
    private String id;
    private String idLocataire;
    private LocalDateTime dateCommande;
    private String statut;
    private String urgence;

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getIdLocataire() { return idLocataire; }
    public void setIdLocataire(String idLocataire) { this.idLocataire = idLocataire; }

    public LocalDateTime getDateCommande() { return dateCommande; }
    public void setDateCommande(LocalDateTime dateCommande) { this.dateCommande = dateCommande; }

    public String getStatut() { return statut; }
    public void setStatut(String statut) { this.statut = statut; }

    public String getUrgence() { return urgence; }
    public void setUrgence(String urgence) { this.urgence = urgence; }
}
