package hospicloud.dtos;

public class TenantCashierPaymentContextDTO {

    private String invoiceNumber;
    private String patientName;

    public TenantCashierPaymentContextDTO() {
    }

    public TenantCashierPaymentContextDTO(String invoiceNumber, String patientName) {
        this.invoiceNumber = invoiceNumber;
        this.patientName = patientName;
    }

    public String getInvoiceNumber() {
        return invoiceNumber;
    }

    public void setInvoiceNumber(String invoiceNumber) {
        this.invoiceNumber = invoiceNumber;
    }

    public String getPatientName() {
        return patientName;
    }

    public void setPatientName(String patientName) {
        this.patientName = patientName;
    }
}
