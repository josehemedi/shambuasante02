package hospicloud.dtos;

import java.math.BigDecimal;

public class TenantBillingRevenuePointDTO {
    private int month;
    private int year;
    private String label;
    private BigDecimal revenue = BigDecimal.ZERO;

    public TenantBillingRevenuePointDTO() {}

    public TenantBillingRevenuePointDTO(int month, int year, String label, BigDecimal revenue) {
        this.month = month;
        this.year = year;
        this.label = label;
        this.revenue = revenue != null ? revenue : BigDecimal.ZERO;
    }

    public int getMonth() { return month; }
    public void setMonth(int month) { this.month = month; }

    public int getYear() { return year; }
    public void setYear(int year) { this.year = year; }

    public String getLabel() { return label; }
    public void setLabel(String label) { this.label = label; }

    public BigDecimal getRevenue() { return revenue; }
    public void setRevenue(BigDecimal revenue) { this.revenue = revenue; }
}
