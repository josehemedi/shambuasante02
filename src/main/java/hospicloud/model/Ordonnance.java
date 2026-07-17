package hospicloud.model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;

/**
 * Modèle pour les ordonnances médicales.
 * Compatible JasperReports et préparé pour une future intégration FHIR.
 */
public class Ordonnance {

    private Long idOrdonnance;

    /**
     * Numéro métier de l'ordonnance.
     * Exemple : ORD-2026-00001
     */
    private String numeroOrdonnance;

    private Integer idPatient;

    /** Nom affiché (jointure lecture seule, non persisté). */
    private String nomPatient;

    /**
     * Isolation Multi-Tenant
     */
    private Integer hospitalId;

    private Integer idMedecin;

    private LocalDateTime datePrescription;
    private byte[] qrCodeImage;

    /**
     * Diagnostic médical
     */
    private String diagnostic;

    /**
     * Prescription détaillée
     */
    private String contenuOrdonnance;

    /**
     * Observations ou recommandations
     */
    private String observations;

    /**
     * ACTIVE, RENOUVELEE, ARCHIVEE
     */
    private String statut;

    private LocalDate dateExpiration;

    /**
     * Constructeur utilisé lors de la création d'une nouvelle ordonnance.
     */
    public Ordonnance(
            String numeroOrdonnance,
            Integer idPatient,
            Integer hospitalId,
            Integer idMedecin,
            String diagnostic,
            String contenuOrdonnance,
            String observations,
            LocalDate dateExpiration,byte[] qrCodeImage) {

        this.numeroOrdonnance = numeroOrdonnance;
        this.idPatient = idPatient;
        this.hospitalId = hospitalId;
        this.idMedecin = idMedecin;
        this.diagnostic = diagnostic;
        this.contenuOrdonnance = contenuOrdonnance;
        this.observations = observations;
        this.dateExpiration = dateExpiration;

        this.datePrescription = LocalDateTime.now();
        this.statut = "ACTIVE";
        this.qrCodeImage =qrCodeImage;    }

	public Long getIdOrdonnance() {
		return idOrdonnance;
	}

	public void setIdOrdonnance(Long idOrdonnance) {
		this.idOrdonnance = idOrdonnance;
	}

	public String getNumeroOrdonnance() {
		return numeroOrdonnance;
	}

	public void setNumeroOrdonnance(String numeroOrdonnance) {
		this.numeroOrdonnance = numeroOrdonnance;
	}

	public Integer getIdPatient() {
		return idPatient;
	}

	public void setIdPatient(Integer idPatient) {
		this.idPatient = idPatient;
	}

	public String getNomPatient() {
		return nomPatient;
	}

	public void setNomPatient(String nomPatient) {
		this.nomPatient = nomPatient;
	}

	public Integer getHospitalId() {
		return hospitalId;
	}

	public void setHospitalId(Integer hospitalId) {
		this.hospitalId = hospitalId;
	}

	public Integer getIdMedecin() {
		return idMedecin;
	}

	public void setIdMedecin(Integer idMedecin) {
		this.idMedecin = idMedecin;
	}

	public LocalDateTime getDatePrescription() {
		return datePrescription;
	}

	public void setDatePrescription(LocalDateTime datePrescription) {
		this.datePrescription = datePrescription;
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

	public String getStatut() {
		return statut;
	}

	public void setStatut(String statut) {
		this.statut = statut;
	}

	public LocalDate getDateExpiration() {
		return dateExpiration;
	}

	public void setDateExpiration(LocalDate dateExpiration) {
		this.dateExpiration = dateExpiration;
	}
	
	

	public byte[] getQrCodeImage() {
		return qrCodeImage;
	}

	public void setQrCodeImage(byte[] qrCodeImage) {
		this.qrCodeImage = qrCodeImage;
	}

	

	public Ordonnance() {
		super();
		// TODO Auto-generated constructor stub
	}

	@Override
	public String toString() {
		return "Ordonnance [idOrdonnance=" + idOrdonnance + ", numeroOrdonnance=" + numeroOrdonnance + ", idPatient="
				+ idPatient + ", hospitalId=" + hospitalId + ", idMedecin=" + idMedecin + ", datePrescription="
				+ datePrescription + ", qrCodeImage=" + Arrays.toString(qrCodeImage) + ", diagnostic=" + diagnostic
				+ ", contenuOrdonnance=" + contenuOrdonnance + ", observations=" + observations + ", statut=" + statut
				+ ", dateExpiration=" + dateExpiration + "]";
	}
}