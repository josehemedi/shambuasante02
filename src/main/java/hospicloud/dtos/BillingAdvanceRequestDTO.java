package hospicloud.dtos;

import java.math.BigDecimal;

public class BillingAdvanceRequestDTO {
    private Integer idPatient;
    private BigDecimal montant;
    private String method;
    private String reference;
    private String notes;

    public Integer getIdPatient() { return idPatient; }
    public void setIdPatient(Integer idPatient) { this.idPatient = idPatient; }

    public BigDecimal getMontant() { return montant; }
    public void setMontant(BigDecimal montant) { this.montant = montant; }

    public String getMethod() { return method; }
    public void setMethod(String method) { this.method = method; }

    public String getReference() { return reference; }
    public void setReference(String reference) { this.reference = reference; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
}
