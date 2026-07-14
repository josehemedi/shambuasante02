package hospicloud.dtos.events;

import java.time.LocalDateTime;

/**
 * DTO événement publié quand un RendezVous est créé
 */
public class RendezVousCreatedEvent {
    private Integer idRdv;
    private Integer idHopital;
    private Integer idPatient;
    private Integer idMedecin;
    private LocalDateTime dateHeureRdv;
    private String motifVisite;
    private String statutRdv;

    public RendezVousCreatedEvent() {}

    public RendezVousCreatedEvent(Integer idRdv, Integer idHopital, Integer idPatient, Integer idMedecin,
                                   LocalDateTime dateHeureRdv, String motifVisite, String statutRdv) {
        this.idRdv = idRdv;
        this.idHopital = idHopital;
        this.idPatient = idPatient;
        this.idMedecin = idMedecin;
        this.dateHeureRdv = dateHeureRdv;
        this.motifVisite = motifVisite;
        this.statutRdv = statutRdv;
    }

    // Getters & Setters
    public Integer getIdRdv() { return idRdv; }
    public void setIdRdv(Integer idRdv) { this.idRdv = idRdv; }
    public Integer getIdHopital() { return idHopital; }
    public void setIdHopital(Integer idHopital) { this.idHopital = idHopital; }
    public Integer getIdPatient() { return idPatient; }
    public void setIdPatient(Integer idPatient) { this.idPatient = idPatient; }
    public Integer getIdMedecin() { return idMedecin; }
    public void setIdMedecin(Integer idMedecin) { this.idMedecin = idMedecin; }
    public LocalDateTime getDateHeureRdv() { return dateHeureRdv; }
    public void setDateHeureRdv(LocalDateTime dateHeureRdv) { this.dateHeureRdv = dateHeureRdv; }
    public String getMotifVisite() { return motifVisite; }
    public void setMotifVisite(String motifVisite) { this.motifVisite = motifVisite; }
    public String getStatutRdv() { return statutRdv; }
    public void setStatutRdv(String statutRdv) { this.statutRdv = statutRdv; }
}
