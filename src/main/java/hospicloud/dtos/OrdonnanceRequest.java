package hospicloud.dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

public class OrdonnanceRequest {

    @NotNull(message = "L'ID du patient est obligatoire")
    private Integer idPatient;

    @NotNull(message = "L'ID du médecin est obligatoire")
    private Integer idMedecin;

    /**
     * Optionnel.
     * Peut être généré automatiquement par le service.
     */
    private String numeroOrdonnance;

    /**
     * Diagnostic médical.
     */
    private String diagnostic;

    @NotBlank(message = "Le contenu de l'ordonnance ne peut pas être vide")
    private String contenuOrdonnance;

    /**
     * Recommandations complémentaires.
     */
    private String observations;

    private LocalDate dateExpiration;

	public Integer getIdPatient() {
		return idPatient;
	}

	public void setIdPatient(Integer idPatient) {
		this.idPatient = idPatient;
	}

	public Integer getIdMedecin() {
		return idMedecin;
	}

	public void setIdMedecin(Integer idMedecin) {
		this.idMedecin = idMedecin;
	}

	public String getNumeroOrdonnance() {
		return numeroOrdonnance;
	}

	public void setNumeroOrdonnance(String numeroOrdonnance) {
		this.numeroOrdonnance = numeroOrdonnance;
	}

	public String getDiagnostic() {
		return diagnostic;
	}

	public void setDiagnostic(String diagnostic) {
		this.diagnostic = diagnostic;
	}

	public String getContenuOrdonnance() {
		return contenuOrdonnance;
	}

	public void setContenuOrdonnance(String contenuOrdonnance) {
		this.contenuOrdonnance = contenuOrdonnance;
	}

	public String getObservations() {
		return observations;
	}

	public void setObservations(String observations) {
		this.observations = observations;
	}

	public LocalDate getDateExpiration() {
		return dateExpiration;
	}

	public void setDateExpiration(LocalDate dateExpiration) {
		this.dateExpiration = dateExpiration;
	}
    
    
    
}