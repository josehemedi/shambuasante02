package hospicloud.messaging;

import java.time.LocalDateTime;

public class RendezVousModifieEvent {

    private Integer idRdv;
    private Integer idMedecin;
    private Integer idPatient;
    private Integer idHopital; // 🧠 IMPORTANT SaaS

    private LocalDateTime ancienneDate;
    private LocalDateTime nouvelleDate;

    private LocalDateTime eventTime; // 🧠 traçabilité
    private String eventType = "RDV_MODIFIE";

    public RendezVousModifieEvent() {
        this.eventTime = LocalDateTime.now();
    }

    public RendezVousModifieEvent(Integer idRdv,
                                  Integer idMedecin,
                                  Integer idPatient,
                                  Integer idHopital,
                                  LocalDateTime ancienneDate,
                                  LocalDateTime nouvelleDate) {
        this.idRdv = idRdv;
        this.idMedecin = idMedecin;
        this.idPatient = idPatient;
        this.idHopital = idHopital;
        this.ancienneDate = ancienneDate;
        this.nouvelleDate = nouvelleDate;
        this.eventTime = LocalDateTime.now();
    }

    // ================= GETTERS & SETTERS =================

    public Integer getIdRdv() { return idRdv; }
    public void setIdRdv(Integer idRdv) { this.idRdv = idRdv; }

    public Integer getIdMedecin() { return idMedecin; }
    public void setIdMedecin(Integer idMedecin) { this.idMedecin = idMedecin; }

    public Integer getIdPatient() { return idPatient; }
    public void setIdPatient(Integer idPatient) { this.idPatient = idPatient; }

    public Integer getIdHopital() { return idHopital; }
    public void setIdHopital(Integer idHopital) { this.idHopital = idHopital; }

    public LocalDateTime getAncienneDate() { return ancienneDate; }
    public void setAncienneDate(LocalDateTime ancienneDate) { this.ancienneDate = ancienneDate; }

    public LocalDateTime getNouvelleDate() { return nouvelleDate; }
    public void setNouvelleDate(LocalDateTime nouvelleDate) { this.nouvelleDate = nouvelleDate; }

    public LocalDateTime getEventTime() { return eventTime; }
    public void setEventTime(LocalDateTime eventTime) { this.eventTime = eventTime; }

    public String getEventType() { return eventType; }
    public void setEventType(String eventType) { this.eventType = eventType; }
}