package hospicloud.dtos.reporting;

public class DossierConsultationRowDTO {

    private String dateConsultation;
    private String motif;
    private String diagnostic;
    private String medecin;

    public DossierConsultationRowDTO() {
    }

    public DossierConsultationRowDTO(String dateConsultation, String motif, String diagnostic, String medecin) {
        this.dateConsultation = dateConsultation;
        this.motif = motif;
        this.diagnostic = diagnostic;
        this.medecin = medecin;
    }

    public String getDateConsultation() {
        return dateConsultation;
    }

    public void setDateConsultation(String dateConsultation) {
        this.dateConsultation = dateConsultation;
    }

    public String getMotif() {
        return motif;
    }

    public void setMotif(String motif) {
        this.motif = motif;
    }

    public String getDiagnostic() {
        return diagnostic;
    }

    public void setDiagnostic(String diagnostic) {
        this.diagnostic = diagnostic;
    }

    public String getMedecin() {
        return medecin;
    }

    public void setMedecin(String medecin) {
        this.medecin = medecin;
    }
}
