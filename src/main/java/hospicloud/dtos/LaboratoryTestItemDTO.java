package hospicloud.dtos;

import java.time.LocalDateTime;

public class LaboratoryTestItemDTO {
    private String id;
    private Integer idAnalyse;
    private String patient;
    private String testName;
    private LocalDateTime date;
    private String status;
    private String collectedBy;
    private String processedBy;

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public Integer getIdAnalyse() { return idAnalyse; }
    public void setIdAnalyse(Integer idAnalyse) { this.idAnalyse = idAnalyse; }

    public String getPatient() { return patient; }
    public void setPatient(String patient) { this.patient = patient; }

    public String getTestName() { return testName; }
    public void setTestName(String testName) { this.testName = testName; }

    public LocalDateTime getDate() { return date; }
    public void setDate(LocalDateTime date) { this.date = date; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getCollectedBy() { return collectedBy; }
    public void setCollectedBy(String collectedBy) { this.collectedBy = collectedBy; }

    public String getProcessedBy() { return processedBy; }
    public void setProcessedBy(String processedBy) { this.processedBy = processedBy; }
}
