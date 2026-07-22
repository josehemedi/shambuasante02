package hospicloud.dtos.patient;

import jakarta.validation.constraints.NotBlank;

public class PatientAssistanceRequestDTO {

    @NotBlank
    private String subject;

    @NotBlank
    private String description;

    private String priority = "NORMAL";

    public String getSubject() { return subject; }
    public void setSubject(String subject) { this.subject = subject; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getPriority() { return priority; }
    public void setPriority(String priority) { this.priority = priority; }
}
