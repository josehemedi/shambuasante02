package hospicloud.dtos.sortie;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDateTime;

public class ContexteSortieDTO {

    private Integer idPatient;
    private String nomPatient;
    private String statutClinique;
    private boolean peutAutoriser;
    private String message;
    private Long idConsultationActive;
    private Integer idAdmissionActive;
    private String typePriseEnCharge;
    private String motifPriseEnCharge;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime datePriseEnCharge;

    public Integer getIdPatient() { return idPatient; }
    public void setIdPatient(Integer idPatient) { this.idPatient = idPatient; }

    public String getNomPatient() { return nomPatient; }
    public void setNomPatient(String nomPatient) { this.nomPatient = nomPatient; }

    public String getStatutClinique() { return statutClinique; }
    public void setStatutClinique(String statutClinique) { this.statutClinique = statutClinique; }

    public boolean isPeutAutoriser() { return peutAutoriser; }
    public void setPeutAutoriser(boolean peutAutoriser) { this.peutAutoriser = peutAutoriser; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public Long getIdConsultationActive() { return idConsultationActive; }
    public void setIdConsultationActive(Long idConsultationActive) { this.idConsultationActive = idConsultationActive; }

    public Integer getIdAdmissionActive() { return idAdmissionActive; }
    public void setIdAdmissionActive(Integer idAdmissionActive) { this.idAdmissionActive = idAdmissionActive; }

    public String getTypePriseEnCharge() { return typePriseEnCharge; }
    public void setTypePriseEnCharge(String typePriseEnCharge) { this.typePriseEnCharge = typePriseEnCharge; }

    public String getMotifPriseEnCharge() { return motifPriseEnCharge; }
    public void setMotifPriseEnCharge(String motifPriseEnCharge) { this.motifPriseEnCharge = motifPriseEnCharge; }

    public LocalDateTime getDatePriseEnCharge() { return datePriseEnCharge; }
    public void setDatePriseEnCharge(LocalDateTime datePriseEnCharge) { this.datePriseEnCharge = datePriseEnCharge; }
}
