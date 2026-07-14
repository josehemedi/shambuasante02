package hospicloud.model;



import java.time.LocalDateTime;

public class BonSortie {
    private Integer idBonSortie;
    private Integer idHopital;
    private Integer idPatient;
    private Integer idConsultation;
    private String numeroBon;
    private LocalDateTime dateSortie;
    private String diagnosticFinal;
    private String etatSortie; // "GUERI", "AMELIORE", etc.
    private String recommandationsPostHospitalisation;
    private Boolean statutPaiementFinal;
    private String autorisePar;
    private String statutWorkflow;
    private Long idOrdonnance;
    private Integer idAdmission;
	public Integer getIdBonSortie() {
		return idBonSortie;
	}
	public void setIdBonSortie(Integer idBonSortie) {
		this.idBonSortie = idBonSortie;
	}
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
	public LocalDateTime getDateSortie() {
		return dateSortie;
	}
	public void setDateSortie(LocalDateTime dateSortie) {
		this.dateSortie = dateSortie;
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

	public String getStatutWorkflow() {
		return statutWorkflow;
	}

	public void setStatutWorkflow(String statutWorkflow) {
		this.statutWorkflow = statutWorkflow;
	}

	public Long getIdOrdonnance() {
		return idOrdonnance;
	}

	public void setIdOrdonnance(Long idOrdonnance) {
		this.idOrdonnance = idOrdonnance;
	}

	public Integer getIdAdmission() {
		return idAdmission;
	}

	public void setIdAdmission(Integer idAdmission) {
		this.idAdmission = idAdmission;
	}
	public BonSortie(Integer idBonSortie, Integer idHopital, Integer idPatient, Integer idConsultation,
			String numeroBon, LocalDateTime dateSortie, String diagnosticFinal, String etatSortie,
			String recommandationsPostHospitalisation, Boolean statutPaiementFinal, String autorisePar) {
		super();
		this.idBonSortie = idBonSortie;
		this.idHopital = idHopital;
		this.idPatient = idPatient;
		this.idConsultation = idConsultation;
		this.numeroBon = numeroBon;
		this.dateSortie = dateSortie;
		this.diagnosticFinal = diagnosticFinal;
		this.etatSortie = etatSortie;
		this.recommandationsPostHospitalisation = recommandationsPostHospitalisation;
		this.statutPaiementFinal = statutPaiementFinal;
		this.autorisePar = autorisePar;
	}
	public BonSortie() {
		super();
		// TODO Auto-generated constructor stub
	}
	@Override
	public String toString() {
		return "BonSortie [idBonSortie=" + idBonSortie + ", idHopital=" + idHopital + ", idPatient=" + idPatient
				+ ", idConsultation=" + idConsultation + ", numeroBon=" + numeroBon + ", dateSortie=" + dateSortie
				+ ", diagnosticFinal=" + diagnosticFinal + ", etatSortie=" + etatSortie
				+ ", recommandationsPostHospitalisation=" + recommandationsPostHospitalisation
				+ ", statutPaiementFinal=" + statutPaiementFinal + ", autorisePar=" + autorisePar + "]";
	}

    // Getters et Setters (indispensables pour JdbcTemplate)
    // ...
}