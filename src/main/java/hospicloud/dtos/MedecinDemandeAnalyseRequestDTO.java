package hospicloud.dtos;

public class MedecinDemandeAnalyseRequestDTO {

    private Integer idPatient;
    private String testCode;
    private String testName;
    private String priority;
    private String notes;
    private Boolean fastingRequired;
    private Boolean submit;
    private Integer idConsultation;

    public Integer getIdPatient() { return idPatient; }
    public void setIdPatient(Integer idPatient) { this.idPatient = idPatient; }

    public String getTestCode() { return testCode; }
    public void setTestCode(String testCode) { this.testCode = testCode; }

    public String getTestName() { return testName; }
    public void setTestName(String testName) { this.testName = testName; }

    public String getPriority() { return priority; }
    public void setPriority(String priority) { this.priority = priority; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public Boolean getFastingRequired() { return fastingRequired; }
    public void setFastingRequired(Boolean fastingRequired) { this.fastingRequired = fastingRequired; }

    public Boolean getSubmit() { return submit; }
    public void setSubmit(Boolean submit) { this.submit = submit; }

    public Integer getIdConsultation() { return idConsultation; }
    public void setIdConsultation(Integer idConsultation) { this.idConsultation = idConsultation; }
}
