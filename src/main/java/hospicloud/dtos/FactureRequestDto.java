package hospicloud.dtos;

import java.math.BigDecimal;

public class FactureRequestDto {

    private Integer idPatient;
    private String numeroFacture;
    private BigDecimal montantTotalHt;
    private BigDecimal tauxTva;
    private Integer idCaissier;

    public Integer getIdPatient() {
        return idPatient;
    }

    public void setIdPatient(Integer idPatient) {
        this.idPatient = idPatient;
    }

    public String getNumeroFacture() {
        return numeroFacture;
    }

    public void setNumeroFacture(String numeroFacture) {
        this.numeroFacture = numeroFacture;
    }

    public BigDecimal getMontantTotalHt() {
        return montantTotalHt;
    }

    public void setMontantTotalHt(BigDecimal montantTotalHt) {
        this.montantTotalHt = montantTotalHt;
    }

    public BigDecimal getTauxTva() {
        return tauxTva;
    }

    public void setTauxTva(BigDecimal tauxTva) {
        this.tauxTva = tauxTva;
    }

    public Integer getIdCaissier() {
        return idCaissier;
    }

    public void setIdCaissier(Integer idCaissier) {
        this.idCaissier = idCaissier;
    }
}