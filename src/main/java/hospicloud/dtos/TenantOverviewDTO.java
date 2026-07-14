package hospicloud.dtos;

import java.math.BigDecimal;

public class TenantOverviewDTO {
    private String id;
    private String name;
    private String country;
    private String plan;
    private Long users;
    private BigDecimal mrr;
    private String status;

    public TenantOverviewDTO() {
    }

    public TenantOverviewDTO(String id, String name, String country, String plan,
                             Long users, BigDecimal mrr, String status) {
        this.id = id;
        this.name = name;
        this.country = country;
        this.plan = plan;
        this.users = users;
        this.mrr = mrr;
        this.status = status;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getCountry() { return country; }
    public void setCountry(String country) { this.country = country; }

    public String getPlan() { return plan; }
    public void setPlan(String plan) { this.plan = plan; }

    public Long getUsers() { return users; }
    public void setUsers(Long users) { this.users = users; }

    public BigDecimal getMrr() { return mrr; }
    public void setMrr(BigDecimal mrr) { this.mrr = mrr; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
