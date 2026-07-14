package hospicloud.dtos;

import java.awt.image.BufferedImage;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;

public class OrdonnanceResponse {

    /**
     * Image QR Code directement exploitable par JasperReports
     */
 

    private Long idOrdonnance;
    private String numeroOrdonnance;
    private Integer idPatient;
    private Integer idMedecin;
    private LocalDateTime datePrescription;
    private String diagnostic;
    private String contenuOrdonnance;
    private String observations;
    private String statut;
    private LocalDate dateExpiration;
    private byte[] qrCodeImage;

    // ==========================
    // GETTERS & SETTERS
    // ==========================

  
    public byte[] getQrCodeImage() {
		return qrCodeImage;
	}

	public void setQrCodeImage(byte[] qrCodeImage) {
		this.qrCodeImage = qrCodeImage;
	}



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

    

    @Override
	public String toString() {
		return "OrdonnanceResponse [idOrdonnance=" + idOrdonnance + ", numeroOrdonnance=" + numeroOrdonnance
				+ ", idPatient=" + idPatient + ", idMedecin=" + idMedecin + ", datePrescription=" + datePrescription
				+ ", diagnostic=" + diagnostic + ", contenuOrdonnance=" + contenuOrdonnance + ", observations="
				+ observations + ", statut=" + statut + ", dateExpiration=" + dateExpiration + ", qrCodeImage="
				+ Arrays.toString(qrCodeImage) + "]";
	}
}