package hospicloud.dtos;

import java.math.BigDecimal;

public class TenantCashierPaymentRequestDTO {
    private Integer idFacture;
    private BigDecimal amount;
    private String paymentType;
    private String method;
    private String reference;
    private String notes;

    public Integer getIdFacture() { return idFacture; }
    public void setIdFacture(Integer idFacture) { this.idFacture = idFacture; }

    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }

    public String getPaymentType() { return paymentType; }
    public void setPaymentType(String paymentType) { this.paymentType = paymentType; }

    public String getMethod() { return method; }
    public void setMethod(String method) { this.method = method; }

    public String getReference() { return reference; }
    public void setReference(String reference) { this.reference = reference; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
}
