package hospicloud.dtos;

import java.time.LocalDateTime;

public class RendezVousJourDTO {
    private Integer idRdv;
    private String nomPatient;
    private String motifVisite;
    private LocalDateTime dateHeureRdv;
    private Integer dureeEstimee;
    private String statutRdv;
    private String canal;

    public RendezVousJourDTO() {}

    public RendezVousJourDTO(Integer idRdv, String nomPatient, String motifVisite, 
                             LocalDateTime dateHeureRdv, Integer dureeEstimee, 
                             String statutRdv, String canal) {
        this.idRdv = idRdv;
        this.nomPatient = nomPatient;
        this.motifVisite = motifVisite;
        this.dateHeureRdv = dateHeureRdv;
        this.dureeEstimee = dureeEstimee;
        this.statutRdv = statutRdv;
        this.canal = canal;
    }

    public Integer getIdRdv() { return idRdv; }
    public void setIdRdv(Integer idRdv) { this.idRdv = idRdv; }

    public String getNomPatient() { return nomPatient; }
    public void setNomPatient(String nomPatient) { this.nomPatient = nomPatient; }

    public String getMotifVisite() { return motifVisite; }
    public void setMotifVisite(String motifVisite) { this.motifVisite = motifVisite; }

    public LocalDateTime getDateHeureRdv() { return dateHeureRdv; }
    public void setDateHeureRdv(LocalDateTime dateHeureRdv) { this.dateHeureRdv = dateHeureRdv; }

    public Integer getDureeEstimee() { return dureeEstimee; }
    public void setDureeEstimee(Integer dureeEstimee) { this.dureeEstimee = dureeEstimee; }

    public String getStatutRdv() { return statutRdv; }
    public void setStatutRdv(String statutRdv) { this.statutRdv = statutRdv; }

    public String getCanal() { return canal; }
    public void setCanal(String canal) { this.canal = canal; }
}