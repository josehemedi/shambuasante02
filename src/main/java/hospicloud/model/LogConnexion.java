package hospicloud.model;

import java.time.LocalDateTime;

/**
 * Entité de journalisation des accès au système.
 * Utilisée pour l'audit de sécurité et la traçabilité des actions des utilisateurs.
 * * Développé par Siku Hemedi Jose - Projet Hospicloud.
 */
public class LogConnexion {

    private Integer idLog;
    private Integer idUtilisateur;
    private String actionEffectuee; // ex: CONNEXION, DECONNEXION, ECHEC_AUTH
    private LocalDateTime dateLog;
    private String navigateurInfo; // User-Agent du navigateur

    // Constructeur par défaut
    public LogConnexion() {
    }

    // Constructeur complet
    public LogConnexion(Integer idLog, Integer idUtilisateur, String actionEffectuee, 
                        LocalDateTime dateLog, String navigateurInfo) {
        this.idLog = idLog;
        this.idUtilisateur = idUtilisateur;
        this.actionEffectuee = actionEffectuee;
        this.dateLog = dateLog;
        this.navigateurInfo = navigateurInfo;
    }

    // Getters & Setters
    public Integer getIdLog() {
        return idLog;
    }

    public void setIdLog(Integer idLog) {
        this.idLog = idLog;
    }

    public Integer getIdUtilisateur() {
        return idUtilisateur;
    }

    public void setIdUtilisateur(Integer idUtilisateur) {
        this.idUtilisateur = idUtilisateur;
    }

    public String getActionEffectuee() {
        return actionEffectuee;
    }

    public void setActionEffectuee(String actionEffectuee) {
        this.actionEffectuee = actionEffectuee;
    }

    public LocalDateTime getDateLog() {
        return dateLog;
    }

    public void setDateLog(LocalDateTime dateLog) {
        this.dateLog = dateLog;
    }

    public String getNavigateurInfo() {
        return navigateurInfo;
    }

    public void setNavigateurInfo(String navigateurInfo) {
        this.navigateurInfo = navigateurInfo;
    }

    @Override
    public String toString() {
        return "LogConnexion{" +
                "id=" + idLog +
                ", utilisateurId=" + idUtilisateur +
                ", action='" + actionEffectuee + '\'' +
                ", date=" + dateLog +
                '}';
    }
}