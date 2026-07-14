package hospicloud.dtos;

import hospicloud.model.Antecedent;
import hospicloud.model.Patient;
import hospicloud.model.RendezVous;

import java.util.ArrayList;
import java.util.List;

public class PatientDossierDTO {

    private Patient patient;
    private List<RendezVous> rendezVous = new ArrayList<>();
    private List<ConsultationResponseDTO> consultations = new ArrayList<>();
    private List<Antecedent> antecedents = new ArrayList<>();

    public Patient getPatient() {
        return patient;
    }

    public void setPatient(Patient patient) {
        this.patient = patient;
    }

    public List<RendezVous> getRendezVous() {
        return rendezVous;
    }

    public void setRendezVous(List<RendezVous> rendezVous) {
        this.rendezVous = rendezVous != null ? rendezVous : new ArrayList<>();
    }

    public List<ConsultationResponseDTO> getConsultations() {
        return consultations;
    }

    public void setConsultations(List<ConsultationResponseDTO> consultations) {
        this.consultations = consultations != null ? consultations : new ArrayList<>();
    }

    public List<Antecedent> getAntecedents() {
        return antecedents;
    }

    public void setAntecedents(List<Antecedent> antecedents) {
        this.antecedents = antecedents != null ? antecedents : new ArrayList<>();
    }
}
