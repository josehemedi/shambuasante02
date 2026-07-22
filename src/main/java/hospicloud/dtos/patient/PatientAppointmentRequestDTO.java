package hospicloud.dtos.patient;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class PatientAppointmentRequestDTO {

    @NotNull
    private Integer idMedecin;

    @NotBlank
    private String dateHeureRdv;

    /** PHYSIQUE ou TELECONSULTATION */
    @NotBlank
    private String canal;

    @NotBlank
    private String motifVisite;

    private Integer dureeEstimee;

    public Integer getIdMedecin() { return idMedecin; }
    public void setIdMedecin(Integer idMedecin) { this.idMedecin = idMedecin; }
    public String getDateHeureRdv() { return dateHeureRdv; }
    public void setDateHeureRdv(String dateHeureRdv) { this.dateHeureRdv = dateHeureRdv; }
    public String getCanal() { return canal; }
    public void setCanal(String canal) { this.canal = canal; }
    public String getMotifVisite() { return motifVisite; }
    public void setMotifVisite(String motifVisite) { this.motifVisite = motifVisite; }
    public Integer getDureeEstimee() { return dureeEstimee; }
    public void setDureeEstimee(Integer dureeEstimee) { this.dureeEstimee = dureeEstimee; }
}
