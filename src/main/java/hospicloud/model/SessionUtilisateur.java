package hospicloud.model;

import java.time.LocalDateTime;

/**
 * Entité représentant une session active d'un utilisateur sur la plateforme.
 * Permet le suivi des connexions et la gestion de la sécurité des tokens.
 * * Développé par Siku Hemedi Jose - Projet Hospicloud.
 */
public class SessionUtilisateur {

    private Integer idSession;
    private Integer idUtilisateur; // Clé étrangère vers la table utilisateurs
    private String tokenSession;   // Le JWT ou token opaque
    private LocalDateTime dateConnexion;
    private LocalDateTime dateExpiration;
    private String adresseIp;

    // Constructeur par défaut
    public SessionUtilisateur() {
    }

    // Constructeur complet
    public SessionUtilisateur(Integer idSession, Integer idUtilisateur, String tokenSession, 
                              LocalDateTime dateConnexion, LocalDateTime dateExpiration, String adresseIp) {
        this.idSession = idSession;
        this.idUtilisateur = idUtilisateur;
        this.tokenSession = tokenSession;
        this.dateConnexion = dateConnexion;
        this.dateExpiration = dateExpiration;
        this.adresseIp = adresseIp;
    }

    // Getters & Setters
    public Integer getIdSession() {
        return idSession;
    }

    public void setIdSession(Integer idSession) {
        this.idSession = idSession;
    }

    public Integer getIdUtilisateur() {
        return idUtilisateur;
    }

    public void setIdUtilisateur(Integer idUtilisateur) {
        this.idUtilisateur = idUtilisateur;
    }

    public String getTokenSession() {
        return tokenSession;
    }

    public void setTokenSession(String tokenSession) {
        this.tokenSession = tokenSession;
    }

    public LocalDateTime getDateConnexion() {
        return dateConnexion;
    }

    public void setDateConnexion(LocalDateTime dateConnexion) {
        this.dateConnexion = dateConnexion;
    }

    public LocalDateTime getDateExpiration() {
        return dateExpiration;
    }

    public void setDateExpiration(LocalDateTime dateExpiration) {
        this.dateExpiration = dateExpiration;
    }

    public String getAdresseIp() {
        return adresseIp;
    }

    public void setAdresseIp(String adresseIp) {
        this.adresseIp = adresseIp;
    }

    @Override
    public String toString() {
        return "SessionUtilisateur{" +
                "id=" + idSession +
                ", utilisateurId=" + idUtilisateur +
                ", ip='" + adresseIp + '\'' +
                ", expireLe=" + dateExpiration +
                '}';
    }
}