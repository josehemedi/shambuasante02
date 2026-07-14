package hospicloud.dtos.patient;

import java.time.LocalDateTime;

public class UpcomingAppointmentDTO {
    private Integer idRdv;
    private LocalDateTime dateHeureRdv;
    private String motifVisite;
    private String nomMedecin;
    private String canal;
    private String statutRdv;

    public UpcomingAppointmentDTO() {}

    public UpcomingAppointmentDTO(Integer idRdv, LocalDateTime dateHeureRdv, String motifVisite, String nomMedecin, String canal, String statutRdv) {
        this.idRdv = idRdv;
        this.dateHeureRdv = dateHeureRdv;
        this.motifVisite = motifVisite;
        this.nomMedecin = nomMedecin;
        this.canal = canal;
        this.statutRdv = statutRdv;
    }

    // Getters and Setters
    public Integer getIdRdv() { return idRdv; }
    public void setIdRdv(Integer idRdv) { this.idRdv = idRdv; }

    public LocalDateTime getDateHeureRdv() { return dateHeureRdv; }
    public void setDateHeureRdv(LocalDateTime dateHeureRdv) { this.dateHeureRdv = dateHeureRdv; }

    public String getMotifVisite() { return motifVisite; }
    public void setMotifVisite(String motifVisite) { this.motifVisite = motifVisite; }

    public String getNomMedecin() { return nomMedecin; }
    public void setNomMedecin(String nomMedecin) { this.nomMedecin = nomMedecin; }

    public String getCanal() { return canal; }
    public void setCanal(String canal) { this.canal = canal; }

    public String getStatutRdv() { return statutRdv; }
    public void setStatutRdv(String statutRdv) { this.statutRdv = statutRdv; }
}
