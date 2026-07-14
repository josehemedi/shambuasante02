package hospicloud.dtos;

import jakarta.validation.constraints.*;
import java.time.LocalDateTime;

/**
 * DTO utilisé pour la réception des données de création d'un Bon de Sortie.
 * Utilise Lombok pour réduire la verbosité.
 */
public class BonSortieRequestDto {

	@NotNull(message = "L'ID de l'hôpital est obligatoire")
	private Integer idHopital;

	@NotNull(message = "L'ID du patient est obligatoire")
	private Integer idPatient;

	private Integer idConsultation;

	@Size(max = 50, message = "Le numéro de bon ne doit pas dépasser 50 caractères")
	private String numeroBon;

	@NotBlank(message = "Le diagnostic final est requis")
	private String diagnosticFinal;

	@NotNull(message = "L'état de sortie est obligatoire")
	private String etatSortie; // Ex: GUERI, AMELIORE, etc.

	@Size(max = 2000, message = "Les recommandations sont trop longues")
	private String recommandationsPostHospitalisation;

	@NotNull(message = "Le statut de paiement est obligatoire")
	private Boolean statutPaiementFinal;

	@NotBlank(message = "Le nom du praticien autorisant la sortie est requis")
	private String autorisePar;

	private LocalDateTime dateSortie;

	public Integer getIdHopital() {
		return idHopital;
	}

	public void setIdHopital(Integer idHopital) {
		this.idHopital = idHopital;
	}

	public Integer getIdPatient() {
		return idPatient;
	}

	public void setIdPatient(Integer idPatient) {
		this.idPatient = idPatient;
	}

	public Integer getIdConsultation() {
		return idConsultation;
	}

	public void setIdConsultation(Integer idConsultation) {
		this.idConsultation = idConsultation;
	}

	public String getNumeroBon() {
		return numeroBon;
	}

	public void setNumeroBon(String numeroBon) {
		this.numeroBon = numeroBon;
	}

	public String getDiagnosticFinal() {
		return diagnosticFinal;
	}

	public void setDiagnosticFinal(String diagnosticFinal) {
		this.diagnosticFinal = diagnosticFinal;
	}

	public String getEtatSortie() {
		return etatSortie;
	}

	public void setEtatSortie(String etatSortie) {
		this.etatSortie = etatSortie;
	}

	public String getRecommandationsPostHospitalisation() {
		return recommandationsPostHospitalisation;
	}

	public void setRecommandationsPostHospitalisation(String recommandationsPostHospitalisation) {
		this.recommandationsPostHospitalisation = recommandationsPostHospitalisation;
	}

	public Boolean getStatutPaiementFinal() {
		return statutPaiementFinal;
	}

	public void setStatutPaiementFinal(Boolean statutPaiementFinal) {
		this.statutPaiementFinal = statutPaiementFinal;
	}

	public String getAutorisePar() {
		return autorisePar;
	}

	public void setAutorisePar(String autorisePar) {
		this.autorisePar = autorisePar;
	}

	public LocalDateTime getDateSortie() {
		return dateSortie;
	}

	public void setDateSortie(LocalDateTime dateSortie) {
		this.dateSortie = dateSortie;
	}

	@Override
	public String toString() {
		return "BonSortieRequestDto [idHopital=" + idHopital + ", idPatient=" + idPatient + ", idConsultation="
				+ idConsultation + ", numeroBon=" + numeroBon + ", diagnosticFinal=" + diagnosticFinal + ", etatSortie="
				+ etatSortie + ", recommandationsPostHospitalisation=" + recommandationsPostHospitalisation
				+ ", statutPaiementFinal=" + statutPaiementFinal + ", autorisePar=" + autorisePar + ", dateSortie="
				+ dateSortie + "]";
	}

	public BonSortieRequestDto() {
		super();
		// TODO Auto-generated constructor stub
	}

	/**
	 * Note : En production, assurez-vous que votre contrôleur utilise
	 * l'annotation @Valid
	 * pour déclencher les contraintes définies ci-dessus.
	 */

}