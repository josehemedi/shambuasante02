package hospicloud.dtos.reporting;

public class DoctorDashboardScheduleRowDTO {

    private String heure;
    private String patient;
    private String motif;
    private String statut;
    private String canal;

    public DoctorDashboardScheduleRowDTO() {
    }

    public DoctorDashboardScheduleRowDTO(String heure, String patient, String motif, String statut, String canal) {
        this.heure = heure;
        this.patient = patient;
        this.motif = motif;
        this.statut = statut;
        this.canal = canal;
    }

    public String getHeure() {
        return heure;
    }

    public void setHeure(String heure) {
        this.heure = heure;
    }

    public String getPatient() {
        return patient;
    }

    public void setPatient(String patient) {
        this.patient = patient;
    }

    public String getMotif() {
        return motif;
    }

    public void setMotif(String motif) {
        this.motif = motif;
    }

    public String getStatut() {
        return statut;
    }

    public void setStatut(String statut) {
        this.statut = statut;
    }

    public String getCanal() {
        return canal;
    }

    public void setCanal(String canal) {
        this.canal = canal;
    }
}
