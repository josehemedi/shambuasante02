package hospicloud.dtos;

import java.time.LocalDateTime;

public class DoctorPendingNoteDTO {
    private Long id;
    private String patientName;
    private String motif;
    private LocalDateTime consultationDate;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getPatientName() { return patientName; }
    public void setPatientName(String patientName) { this.patientName = patientName; }

    public String getMotif() { return motif; }
    public void setMotif(String motif) { this.motif = motif; }

    public LocalDateTime getConsultationDate() { return consultationDate; }
    public void setConsultationDate(LocalDateTime consultationDate) { this.consultationDate = consultationDate; }
}
