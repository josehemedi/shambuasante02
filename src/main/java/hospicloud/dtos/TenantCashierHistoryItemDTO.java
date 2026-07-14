package hospicloud.dtos;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class TenantCashierHistoryItemDTO {
    private String id;
    private String receiptNumber;
    private String invoiceNumber;
    private String patientName;
    private String patientId;
    private LocalDateTime paidAt;
    private BigDecimal amount = BigDecimal.ZERO;
    private String paymentType;
    private String method;
    private String cashierName;
    private BigDecimal balanceAfter = BigDecimal.ZERO;

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getReceiptNumber() { return receiptNumber; }
    public void setReceiptNumber(String receiptNumber) { this.receiptNumber = receiptNumber; }

    public String getInvoiceNumber() { return invoiceNumber; }
    public void setInvoiceNumber(String invoiceNumber) { this.invoiceNumber = invoiceNumber; }

    public String getPatientName() { return patientName; }
    public void setPatientName(String patientName) { this.patientName = patientName; }

    public String getPatientId() { return patientId; }
    public void setPatientId(String patientId) { this.patientId = patientId; }

    public LocalDateTime getPaidAt() { return paidAt; }
    public void setPaidAt(LocalDateTime paidAt) { this.paidAt = paidAt; }

    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }

    public String getPaymentType() { return paymentType; }
    public void setPaymentType(String paymentType) { this.paymentType = paymentType; }

    public String getMethod() { return method; }
    public void setMethod(String method) { this.method = method; }

    public String getCashierName() { return cashierName; }
    public void setCashierName(String cashierName) { this.cashierName = cashierName; }

    public BigDecimal getBalanceAfter() { return balanceAfter; }
    public void setBalanceAfter(BigDecimal balanceAfter) { this.balanceAfter = balanceAfter; }
}
