package hospicloud.dtos;

import java.math.BigDecimal;

public class SubscriptionInvoiceDTO {
    private String id;
    private String tenant;
    private String plan;
    private BigDecimal amount;
    private String status;
    private String date;
    private String dueDate;

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getTenant() { return tenant; }
    public void setTenant(String tenant) { this.tenant = tenant; }

    public String getPlan() { return plan; }
    public void setPlan(String plan) { this.plan = plan; }

    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }

    public String getDueDate() { return dueDate; }
    public void setDueDate(String dueDate) { this.dueDate = dueDate; }
}
