package hospicloud.dtos;

public class CommencerConsultationResponseDTO {
    private Integer idAdmission;
    private String statutAdmission;
    private ConsultationResponseDTO consultation;

    public Integer getIdAdmission() { return idAdmission; }
    public void setIdAdmission(Integer idAdmission) { this.idAdmission = idAdmission; }

    public String getStatutAdmission() { return statutAdmission; }
    public void setStatutAdmission(String statutAdmission) { this.statutAdmission = statutAdmission; }

    public ConsultationResponseDTO getConsultation() { return consultation; }
    public void setConsultation(ConsultationResponseDTO consultation) { this.consultation = consultation; }
}
