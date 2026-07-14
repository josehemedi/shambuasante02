package hospicloud.dtos;

import java.time.LocalDateTime;

public class MedecinDemandeAnalyseResponseDTO {

    private Integer idAnalyse;
    private String id;
    private String patientName;
    private String patientId;
    private String testName;
    private String requestedBy;
    private LocalDateTime date;
    private String status;
    private String priority;
    private String notes;
    private Boolean fastingRequired;
    private Integer idConsultation;
    private Integer idPatient;
    private String resultatTexte;
    private String interpretation;
    private String valeursReference;
    private String observationsMedecin;

    public Integer getIdAnalyse() { return idAnalyse; }
    public void setIdAnalyse(Integer idAnalyse) { this.idAnalyse = idAnalyse; }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getPatientName() { return patientName; }
    public void setPatientName(String patientName) { this.patientName = patientName; }

    public String getPatientId() { return patientId; }
    public void setPatientId(String patientId) { this.patientId = patientId; }

    public String getTestName() { return testName; }
    public void setTestName(String testName) { this.testName = testName; }

    public String getRequestedBy() { return requestedBy; }
    public void setRequestedBy(String requestedBy) { this.requestedBy = requestedBy; }

    public LocalDateTime getDate() { return date; }
    public void setDate(LocalDateTime date) { this.date = date; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getPriority() { return priority; }
    public void setPriority(String priority) { this.priority = priority; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public Boolean getFastingRequired() { return fastingRequired; }
    public void setFastingRequired(Boolean fastingRequired) { this.fastingRequired = fastingRequired; }

    public Integer getIdConsultation() { return idConsultation; }
    public void setIdConsultation(Integer idConsultation) { this.idConsultation = idConsultation; }

    public Integer getIdPatient() { return idPatient; }
    public void setIdPatient(Integer idPatient) { this.idPatient = idPatient; }

    public String getResultatTexte() { return resultatTexte; }
    public void setResultatTexte(String resultatTexte) { this.resultatTexte = resultatTexte; }

    public String getInterpretation() { return interpretation; }
    public void setInterpretation(String interpretation) { this.interpretation = interpretation; }

    public String getValeursReference() { return valeursReference; }
    public void setValeursReference(String valeursReference) { this.valeursReference = valeursReference; }

    public String getObservationsMedecin() { return observationsMedecin; }
    public void setObservationsMedecin(String observationsMedecin) { this.observationsMedecin = observationsMedecin; }
}
