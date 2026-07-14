package hospicloud.model;

import java.time.LocalTime;

/**
 * Entité gérant la politique de sauvegarde des données (Backups).
 * Assure la résilience et la sécurité des informations médicales par hôpital.
 * * Développé par Siku Hemedi Jose - Projet Hospicloud.
 */
public class PlanSauvegarde {

    private Integer idPlan;
    private Integer idHopital;
    private String frequence;      // HORAIRE, QUOTIDIEN, HEBDOMADAIRE
    private LocalTime heureExecution;
    private String typeSauvegarde; // COMPLETE, INCREMENTALE
    private String destinationStockage; // LOCAL, CLOUD_S3, FTP
    private boolean estActif;

    // Constructeur par défaut
    public PlanSauvegarde() {
    }

    // Constructeur complet
    public PlanSauvegarde(Integer idPlan, Integer idHopital, String frequence, 
                          LocalTime heureExecution, String typeSauvegarde, 
                          String destinationStockage, boolean estActif) {
        this.idPlan = idPlan;
        this.idHopital = idHopital;
        this.frequence = frequence;
        this.heureExecution = heureExecution;
        this.typeSauvegarde = typeSauvegarde;
        this.destinationStockage = destinationStockage;
        this.estActif = estActif;
    }

    // Getters & Setters
    public Integer getIdPlan() { return idPlan; }
    public void setIdPlan(Integer idPlan) { this.idPlan = idPlan; }

    public Integer getIdHopital() { return idHopital; }
    public void setIdHopital(Integer idHopital) { this.idHopital = idHopital; }

    public String getFrequence() { return frequence; }
    public void setFrequence(String frequence) { this.frequence = frequence; }

    public LocalTime getHeureExecution() { return heureExecution; }
    public void setHeureExecution(LocalTime heureExecution) { this.heureExecution = heureExecution; }

    public String getTypeSauvegarde() { return typeSauvegarde; }
    public void setTypeSauvegarde(String typeSauvegarde) { this.typeSauvegarde = typeSauvegarde; }

    public String getDestinationStockage() { return destinationStockage; }
    public void setDestinationStockage(String destinationStockage) { this.destinationStockage = destinationStockage; }

    public boolean isEstActif() { return estActif; }
    public void setEstActif(boolean estActif) { this.estActif = estActif; }

    @Override
    public String toString() {
        return "PlanSauvegarde{" +
                "id=" + idPlan +
                ", hopitalId=" + idHopital +
                ", frequence='" + frequence + '\'' +
                ", actif=" + estActif +
                '}';
    }
}