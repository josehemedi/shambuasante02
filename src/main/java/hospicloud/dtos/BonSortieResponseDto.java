package hospicloud.dtos;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import java.time.LocalDateTime;

/**
 * DTO de réponse pour les opérations liées aux Bons de Sortie.
 * Conçu pour être sérialisé en JSON pour le client (Mobile/Web).
 */
public class BonSortieResponseDto {

    private Integer idBonSortie;
    private String numeroBon;
    
    // Formatage ISO-8601 strict pour garantir la compatibilité front-end
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime dateSortie;

    private String nomPatient; // Plus utile pour le front que juste un ID
    private String nomMedecin;
    
    private String diagnosticFinal;
    private String etatSortie;
    private String recommandations;
    
    private Boolean statutPaiementFinal;
    private String autorisePar;
    private String statutWorkflow;
    private Integer idPatient;
	public BonSortieResponseDto(Integer idBonSortie, String numeroBon, LocalDateTime dateSortie, String nomPatient,
			String nomMedecin, String diagnosticFinal, String etatSortie, String recommandations,
			Boolean statutPaiementFinal, String autorisePar) {
		super();
		this.idBonSortie = idBonSortie;
		this.numeroBon = numeroBon;
		this.dateSortie = dateSortie;
		this.nomPatient = nomPatient;
		this.nomMedecin = nomMedecin;
		this.diagnosticFinal = diagnosticFinal;
		this.etatSortie = etatSortie;
		this.recommandations = recommandations;
		this.statutPaiementFinal = statutPaiementFinal;
		this.autorisePar = autorisePar;
	}
	public Integer getIdBonSortie() {
		return idBonSortie;
	}
	public void setIdBonSortie(Integer idBonSortie) {
		this.idBonSortie = idBonSortie;
	}
	public String getNumeroBon() {
		return numeroBon;
	}
	public void setNumeroBon(String numeroBon) {
		this.numeroBon = numeroBon;
	}
	public LocalDateTime getDateSortie() {
		return dateSortie;
	}
	public void setDateSortie(LocalDateTime dateSortie) {
		this.dateSortie = dateSortie;
	}
	public String getNomPatient() {
		return nomPatient;
	}
	public void setNomPatient(String nomPatient) {
		this.nomPatient = nomPatient;
	}
	public String getNomMedecin() {
		return nomMedecin;
	}
	public void setNomMedecin(String nomMedecin) {
		this.nomMedecin = nomMedecin;
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
	public String getRecommandations() {
		return recommandations;
	}
	public void setRecommandations(String recommandations) {
		this.recommandations = recommandations;
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

	public String getStatutWorkflow() {
		return statutWorkflow;
	}

	public void setStatutWorkflow(String statutWorkflow) {
		this.statutWorkflow = statutWorkflow;
	}

	public Integer getIdPatient() {
		return idPatient;
	}

	public void setIdPatient(Integer idPatient) {
		this.idPatient = idPatient;
	}
	
	public BonSortieResponseDto() {
		super();
		// TODO Auto-generated constructor stub
	}
	
	@Override
	public String toString() {
		return "BonSortieResponseDto [idBonSortie=" + idBonSortie + ", numeroBon=" + numeroBon + ", dateSortie="
				+ dateSortie + ", nomPatient=" + nomPatient + ", nomMedecin=" + nomMedecin + ", diagnosticFinal="
				+ diagnosticFinal + ", etatSortie=" + etatSortie + ", recommandations=" + recommandations
				+ ", statutPaiementFinal=" + statutPaiementFinal + ", autorisePar=" + autorisePar + "]";
	}

    /**
     * Note d'expert : 
     * Dans un système complexe, on ajoute souvent un champ "meta" ou "links" 
     * pour supporter l'HATEOAS (liens vers les ressources associées).
     */
    
}