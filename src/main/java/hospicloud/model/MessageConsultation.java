package hospicloud.model;

import java.time.LocalDateTime;

/**
 * Entité représentant un message échangé dans le chat d'une téléconsultation.
 * Permet de conserver une trace écrite des échanges durant la visioconférence.
 * * Développé par Siku Hemedi Jose - Projet Hospicloud.
 */
public class MessageConsultation {

    private Integer idMessage;
    private Integer idTelecons;   // Lien vers la session de téléconsultation
    private Integer idEmetteur;   // Référence à l'utilisateur (Médecin ou Patient)
    private String contenuMessage;
    private LocalDateTime dateEnvoi;

    // Constructeur par défaut
    public MessageConsultation() {
    }

    // Constructeur complet
    public MessageConsultation(Integer idMessage, Integer idTelecons, Integer idEmetteur, 
                               String contenuMessage, LocalDateTime dateEnvoi) {
        this.idMessage = idMessage;
        this.idTelecons = idTelecons;
        this.idEmetteur = idEmetteur;
        this.contenuMessage = contenuMessage;
        this.dateEnvoi = dateEnvoi;
    }

    // Getters & Setters
    public Integer getIdMessage() {
        return idMessage;
    }

    public void setIdMessage(Integer idMessage) {
        this.idMessage = idMessage;
    }

    public Integer getIdTelecons() {
        return idTelecons;
    }

    public void setIdTelecons(Integer idTelecons) {
        this.idTelecons = idTelecons;
    }

    public Integer getIdEmetteur() {
        return idEmetteur;
    }

    public void setIdEmetteur(Integer idEmetteur) {
        this.idEmetteur = idEmetteur;
    }

    public String getContenuMessage() {
        return contenuMessage;
    }

    public void setContenuMessage(String contenuMessage) {
        this.contenuMessage = contenuMessage;
    }

    public LocalDateTime getDateEnvoi() {
        return dateEnvoi;
    }

    public void setDateEnvoi(LocalDateTime dateEnvoi) {
        this.dateEnvoi = dateEnvoi;
    }

    @Override
    public String toString() {
        return "MessageConsultation{" +
                "id=" + idMessage +
                ", teleconsId=" + idTelecons +
                ", emetteurId=" + idEmetteur +
                ", date=" + dateEnvoi +
                '}';
    }
}