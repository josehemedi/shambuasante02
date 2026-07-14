package hospicloud.dtos;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class FactureResponseDto {

    private Integer idFacture;
    private String numeroFacture;

    private LocalDateTime dateFacture;

    private BigDecimal montantTotalHt;
    private BigDecimal tva;
    private BigDecimal montantTotalTtc;

    private String statutPaiement;

    private Integer idPatient;
    private String nomPatient; // optionnel (très utile)

    private Integer idHopital;

    private Integer idCaissier;

    // Getters & Setters

    public Integer getIdFacture() {
        return idFacture;
    }

    public void setIdFacture(Integer idFacture) {
        this.idFacture = idFacture;
    }

    public String getNumeroFacture() {
        return numeroFacture;
    }

    public void setNumeroFacture(String numeroFacture) {
        this.numeroFacture = numeroFacture;
    }

    public LocalDateTime getDateFacture() {
        return dateFacture;
    }

    public void setDateFacture(LocalDateTime dateFacture) {
        this.dateFacture = dateFacture;
    }

    public BigDecimal getMontantTotalHt() {
        return montantTotalHt;
    }

    public void setMontantTotalHt(BigDecimal montantTotalHt) {
        this.montantTotalHt = montantTotalHt;
    }

    public BigDecimal getTva() {
        return tva;
    }

    public void setTva(BigDecimal tva) {
        this.tva = tva;
    }

    public BigDecimal getMontantTotalTtc() {
        return montantTotalTtc;
    }

    public void setMontantTotalTtc(BigDecimal montantTotalTtc) {
        this.montantTotalTtc = montantTotalTtc;
    }

    public String getStatutPaiement() {
        return statutPaiement;
    }

    public void setStatutPaiement(String statutPaiement) {
        this.statutPaiement = statutPaiement;
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

    public Integer getIdHopital() {
        return idHopital;
    }

    public void setIdHopital(Integer idHopital) {
        this.idHopital = idHopital;
    }

    public Integer getIdCaissier() {
        return idCaissier;
    }

    public void setIdCaissier(Integer idCaissier) {
        this.idCaissier = idCaissier;
    }
}