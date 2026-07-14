package hospicloud.model;

import java.time.LocalDateTime;

/**
 * Entité gérant les notifications système et les alertes techniques.
 * Permet une surveillance proactive de l'état de santé de l'infrastructure Hospicloud.
 * * Développé par Siku Hemedi Jose - Projet Hospicloud.
 */
public class AlerteMaintenance {

    private Integer idAlerte;
    private Integer idHopital;
    private String typeAlerte;    // ex: "Espace disque", "Echec Sauvegarde", "Connexion DB"
    private String priorite;      // Basse, Moyenne, Haute, Critique
    private String messageAlerte;
    private boolean estResolu;
    private LocalDateTime dateCreation;

    // Constructeur par défaut
    public AlerteMaintenance() {
    }

    // Constructeur complet
    public AlerteMaintenance(Integer idAlerte, Integer idHopital, String typeAlerte, 
                            String priorite, String messageAlerte, boolean estResolu, 
                            LocalDateTime dateCreation) {
        this.idAlerte = idAlerte;
        this.idHopital = idHopital;
        this.typeAlerte = typeAlerte;
        this.priorite = priorite;
        this.messageAlerte = messageAlerte;
        this.estResolu = estResolu;
        this.dateCreation = dateCreation;
    }

    // Getters & Setters
    public Integer getIdAlerte() { return idAlerte; }
    public void setIdAlerte(Integer idAlerte) { this.idAlerte = idAlerte; }

    public Integer getIdHopital() { return idHopital; }
    public void setIdHopital(Integer idHopital) { this.idHopital = idHopital; }

    public String getTypeAlerte() { return typeAlerte; }
    public void setTypeAlerte(String typeAlerte) { this.typeAlerte = typeAlerte; }

    public String getPriorite() { return priorite; }
    public void setPriorite(String priorite) { this.priorite = priorite; }

    public String getMessageAlerte() { return messageAlerte; }
    public void setMessageAlerte(String messageAlerte) { this.messageAlerte = messageAlerte; }

    public boolean isEstResolu() { return estResolu; }
    public void setEstResolu(boolean estResolu) { this.estResolu = estResolu; }

    public LocalDateTime getDateCreation() { return dateCreation; }
    public void setDateCreation(LocalDateTime dateCreation) { this.dateCreation = dateCreation; }

    @Override
    public String toString() {
        return "Alerte{" +
                "id=" + idAlerte +
                ", type='" + typeAlerte + '\'' +
                ", priorite='" + priorite + '\'' +
                ", resolu=" + estResolu +
                '}';
    }
}