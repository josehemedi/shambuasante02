package hospicloud.dtos;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class TenantBillingInvoiceDTO {
    private Integer idFacture;
    private String numeroFacture;
    private String patient;
    private LocalDateTime dateFacture;
    private BigDecimal montantHt;
    private BigDecimal tva;
    private BigDecimal montantTtc;
    private String statutPaiement;
    private String uiStatus;
    private String service;

    public Integer getIdFacture() { return idFacture; }
    public void setIdFacture(Integer idFacture) { this.idFacture = idFacture; }

    public String getNumeroFacture() { return numeroFacture; }
    public void setNumeroFacture(String numeroFacture) { this.numeroFacture = numeroFacture; }

    public String getPatient() { return patient; }
    public void setPatient(String patient) { this.patient = patient; }

    public LocalDateTime getDateFacture() { return dateFacture; }
    public void setDateFacture(LocalDateTime dateFacture) { this.dateFacture = dateFacture; }

    public BigDecimal getMontantHt() { return montantHt; }
    public void setMontantHt(BigDecimal montantHt) { this.montantHt = montantHt; }

    public BigDecimal getTva() { return tva; }
    public void setTva(BigDecimal tva) { this.tva = tva; }

    public BigDecimal getMontantTtc() { return montantTtc; }
    public void setMontantTtc(BigDecimal montantTtc) { this.montantTtc = montantTtc; }

    public String getStatutPaiement() { return statutPaiement; }
    public void setStatutPaiement(String statutPaiement) { this.statutPaiement = statutPaiement; }

    public String getUiStatus() { return uiStatus; }
    public void setUiStatus(String uiStatus) { this.uiStatus = uiStatus; }

    public String getService() { return service; }
    public void setService(String service) { this.service = service; }
}
