package hospicloud.model;

import java.time.LocalDateTime;

/**
 * Entité gérant les sessions de télémédecine par visioconférence.
 * Stocke les métadonnées techniques de l'appel WebRTC et le lien vers l'enregistrement.
 * * Développé par Siku Hemedi Jose - Projet Hospicloud.
 */
public class TeleConsultation {

    private Integer idTelecons;
    private Integer idConsultation; // Clé étrangère vers la table consultations
    private String roomId;          // Identifiant de la salle de conférence
    private LocalDateTime debutAppel;
    private LocalDateTime finAppel;
    private String statutAppel;     // EN_ATTENTE, EN_COURS, ECHOUÉ, TERMINE
    private String lienEnregistrementUrl;

    // Constructeur par défaut
    public TeleConsultation() {
    }

    // Constructeur complet
    public TeleConsultation(Integer idTelecons, Integer idConsultation, String roomId, 
                            LocalDateTime debutAppel, LocalDateTime finAppel, 
                            String statutAppel, String lienEnregistrementUrl) {
        this.idTelecons = idTelecons;
        this.idConsultation = idConsultation;
        this.roomId = roomId;
        this.debutAppel = debutAppel;
        this.finAppel = finAppel;
        this.statutAppel = statutAppel;
        this.lienEnregistrementUrl = lienEnregistrementUrl;
    }

    // Getters & Setters
    public Integer getIdTelecons() {
        return idTelecons;
    }

    public void setIdTelecons(Integer idTelecons) {
        this.idTelecons = idTelecons;
    }

    public Integer getIdConsultation() {
        return idConsultation;
    }

    public void setIdConsultation(Integer idConsultation) {
        this.idConsultation = idConsultation;
    }

    public String getRoomId() {
        return roomId;
    }

    public void setRoomId(String roomId) {
        this.roomId = roomId;
    }

    public LocalDateTime getDebutAppel() {
        return debutAppel;
    }

    public void setDebutAppel(LocalDateTime debutAppel) {
        this.debutAppel = debutAppel;
    }

    public LocalDateTime getFinAppel() {
        return finAppel;
    }

    public void setFinAppel(LocalDateTime finAppel) {
        this.finAppel = finAppel;
    }

    public String getStatutAppel() {
        return statutAppel;
    }

    public void setStatutAppel(String statutAppel) {
        this.statutAppel = statutAppel;
    }

    public String getLienEnregistrementUrl() {
        return lienEnregistrementUrl;
    }

    public void setLienEnregistrementUrl(String lienEnregistrementUrl) {
        this.lienEnregistrementUrl = lienEnregistrementUrl;
    }

    @Override
    public String toString() {
        return "Teleconsultation{" +
                "id=" + idTelecons +
                ", room='" + roomId + '\'' +
                ", statut='" + statutAppel + '\'' +
                '}';
    }
}