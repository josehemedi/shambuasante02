package hospicloud.dtos;

import java.math.BigDecimal;

public class SubscriptionTimelineEventDTO {
    private long id;
    private String tenant;
    private String action;
    private String plan;
    private String date;
    private BigDecimal amount;

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public String getTenant() { return tenant; }
    public void setTenant(String tenant) { this.tenant = tenant; }

    public String getAction() { return action; }
    public void setAction(String action) { this.action = action; }

    public String getPlan() { return plan; }
    public void setPlan(String plan) { this.plan = plan; }

    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }

    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }
}
