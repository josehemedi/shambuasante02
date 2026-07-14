package hospicloud.dtos;

import java.time.LocalDateTime;

public class DoctorWorkspaceActivityDTO {

    private Long id;
    private String type;
    private String patientName;
    private String detail;
    private LocalDateTime occurredAt;

    public DoctorWorkspaceActivityDTO() {
    }

    public DoctorWorkspaceActivityDTO(Long id, String type, String patientName, String detail, LocalDateTime occurredAt) {
        this.id = id;
        this.type = type;
        this.patientName = patientName;
        this.detail = detail;
        this.occurredAt = occurredAt;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getPatientName() {
        return patientName;
    }

    public void setPatientName(String patientName) {
        this.patientName = patientName;
    }

    public String getDetail() {
        return detail;
    }

    public void setDetail(String detail) {
        this.detail = detail;
    }

    public LocalDateTime getOccurredAt() {
        return occurredAt;
    }

    public void setOccurredAt(LocalDateTime occurredAt) {
        this.occurredAt = occurredAt;
    }
}
