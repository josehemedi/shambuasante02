package hospicloud.model;

import java.time.LocalDateTime;

/**
 * Entité traçant les opérations de restauration de la base de données.
 * Crucial pour l'audit de sécurité et la traçabilité des actions critiques.
 * * Développé par Siku Hemedi Jose - Projet Hospicloud.
 */
public class HistoriqueRestauration {

    private Integer idRestauration;
    private Integer idBackup;       // Le fichier source utilisé pour la restauration
    private Integer idUtilisateur;  // L'administrateur responsable
    private LocalDateTime dateRestauration;
    private String motifRestauration;
    private String statutRestauration; // SUCCES, ECHEC

    // Constructeur par défaut
    public HistoriqueRestauration() {
    }

    // Constructeur complet
    public HistoriqueRestauration(Integer idRestauration, Integer idBackup, Integer idUtilisateur, 
                                 LocalDateTime dateRestauration, String motifRestauration, 
                                 String statutRestauration) {
        this.idRestauration = idRestauration;
        this.idBackup = idBackup;
        this.idUtilisateur = idUtilisateur;
        this.dateRestauration = dateRestauration;
        this.motifRestauration = motifRestauration;
        this.statutRestauration = statutRestauration;
    }

    // Getters & Setters
    public Integer getIdRestauration() { return idRestauration; }
    public void setIdRestauration(Integer idRestauration) { this.idRestauration = idRestauration; }

    public Integer getIdBackup() { return idBackup; }
    public void setIdBackup(Integer idBackup) { this.idBackup = idBackup; }

    public Integer getIdUtilisateur() { return idUtilisateur; }
    public void setIdUtilisateur(Integer idUtilisateur) { this.idUtilisateur = idUtilisateur; }

    public LocalDateTime getDateRestauration() { return dateRestauration; }
    public void setDateRestauration(LocalDateTime dateRestauration) { this.dateRestauration = dateRestauration; }

    public String getMotifRestauration() { return motifRestauration; }
    public void setMotifRestauration(String motifRestauration) { this.motifRestauration = motifRestauration; }

    public String getStatutRestauration() { return statutRestauration; }
    public void setStatutRestauration(String statutRestauration) { this.statutRestauration = statutRestauration; }

    @Override
    public String toString() {
        return "Restauration{" +
                "id=" + idRestauration +
                ", backupId=" + idBackup +
                ", adminId=" + idUtilisateur +
                ", statut='" + statutRestauration + '\'' +
                '}';
    }
}