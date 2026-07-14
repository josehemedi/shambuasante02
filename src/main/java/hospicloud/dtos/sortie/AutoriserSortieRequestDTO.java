package hospicloud.dtos.sortie;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class AutoriserSortieRequestDTO {

    @NotNull(message = "L'identifiant patient est obligatoire")
    private Integer idPatient;

    private Long idConsultation;
    private Integer idAdmission;

    @NotBlank(message = "Le diagnostic final est obligatoire")
    private String diagnosticFinal;

    @NotBlank(message = "L'état de sortie est obligatoire")
    private String etatSortie;

    private String recommandationsPostHospitalisation;
    private String contenuOrdonnance;
    private String observationsOrdonnance;

    public Integer getIdPatient() { return idPatient; }
    public void setIdPatient(Integer idPatient) { this.idPatient = idPatient; }

    public Long getIdConsultation() { return idConsultation; }
    public void setIdConsultation(Long idConsultation) { this.idConsultation = idConsultation; }

    public Integer getIdAdmission() { return idAdmission; }
    public void setIdAdmission(Integer idAdmission) { this.idAdmission = idAdmission; }

    public String getDiagnosticFinal() { return diagnosticFinal; }
    public void setDiagnosticFinal(String diagnosticFinal) { this.diagnosticFinal = diagnosticFinal; }

    public String getEtatSortie() { return etatSortie; }
    public void setEtatSortie(String etatSortie) { this.etatSortie = etatSortie; }

    public String getRecommandationsPostHospitalisation() { return recommandationsPostHospitalisation; }
    public void setRecommandationsPostHospitalisation(String recommandationsPostHospitalisation) {
        this.recommandationsPostHospitalisation = recommandationsPostHospitalisation;
    }

    public String getContenuOrdonnance() { return contenuOrdonnance; }
    public void setContenuOrdonnance(String contenuOrdonnance) { this.contenuOrdonnance = contenuOrdonnance; }

    public String getObservationsOrdonnance() { return observationsOrdonnance; }
    public void setObservationsOrdonnance(String observationsOrdonnance) { this.observationsOrdonnance = observationsOrdonnance; }
}
