package hospicloud.model;

import java.time.LocalDateTime;

/**
 * Entité traçant l'exécution réelle de chaque sauvegarde.
 * Permet de vérifier l'intégrité (checksum) et de diagnostiquer les échecs.
 * * Développé par Siku Hemedi Jose - Projet Hospicloud.
 */
public class HistoriqueSauvegarde {

    private Integer idBackup;
    private Integer idPlan;           // Référence à la stratégie parente
    private String nomFichier;        // ex: backup_unikin_20260310.sql.gz
    private Long tailleFichierKb;
    private LocalDateTime dateDebut;
    private LocalDateTime dateFin;
    private String statutBackup;      // REUSSI, ECHOUE, EN_COURS
    private String urlStockage;       // Chemin local ou URL S3/Cloud
    private String checksum;          // Empreinte SHA-256 pour vérification
    private String erreurLog;         // Détails techniques en cas d'échec

    // Constructeur par défaut
    public HistoriqueSauvegarde() {
    }

    // Constructeur complet
    public HistoriqueSauvegarde(Integer idBackup, Integer idPlan, String nomFichier, 
                               Long tailleFichierKb, LocalDateTime dateDebut, 
                               LocalDateTime dateFin, String statutBackup, 
                               String urlStockage, String checksum, String erreurLog) {
        this.idBackup = idBackup;
        this.idPlan = idPlan;
        this.nomFichier = nomFichier;
        this.tailleFichierKb = tailleFichierKb;
        this.dateDebut = dateDebut;
        this.dateFin = dateFin;
        this.statutBackup = statutBackup;
        this.urlStockage = urlStockage;
        this.checksum = checksum;
        this.erreurLog = erreurLog;
    }

    // Getters & Setters
    public Integer getIdBackup() { return idBackup; }
    public void setIdBackup(Integer idBackup) { this.idBackup = idBackup; }

    public Integer getIdPlan() { return idPlan; }
    public void setIdPlan(Integer idPlan) { this.idPlan = idPlan; }

    public String getNomFichier() { return nomFichier; }
    public void setNomFichier(String nomFichier) { this.nomFichier = nomFichier; }

    public Long getTailleFichierKb() { return tailleFichierKb; }
    public void setTailleFichierKb(Long tailleFichierKb) { this.tailleFichierKb = tailleFichierKb; }

    public LocalDateTime getDateDebut() { return dateDebut; }
    public void setDateDebut(LocalDateTime dateDebut) { this.dateDebut = dateDebut; }

    public LocalDateTime getDateFin() { return dateFin; }
    public void setDateFin(LocalDateTime dateFin) { this.dateFin = dateFin; }

    public String getStatutBackup() { return statutBackup; }
    public void setStatutBackup(String statutBackup) { this.statutBackup = statutBackup; }

    public String getUrlStockage() { return urlStockage; }
    public void setUrlStockage(String urlStockage) { this.urlStockage = urlStockage; }

    public String getChecksum() { return checksum; }
    public void setChecksum(String checksum) { this.checksum = checksum; }

    public String getErreurLog() { return erreurLog; }
    public void setErreurLog(String erreurLog) { this.erreurLog = erreurLog; }

    @Override
    public String toString() {
        return "Backup{" +
                "id=" + idBackup +
                ", fichier='" + nomFichier + '\'' +
                ", statut='" + statutBackup + '\'' +
                ", taille=" + tailleFichierKb + " KB" +
                '}';
    }
}