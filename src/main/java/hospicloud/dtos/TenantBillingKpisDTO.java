package hospicloud.dtos;

import java.math.BigDecimal;

public class TenantBillingKpisDTO {
    private BigDecimal totalRevenueYtd = BigDecimal.ZERO;
    private BigDecimal totalPaid = BigDecimal.ZERO;
    private BigDecimal outstanding = BigDecimal.ZERO;
    private BigDecimal overdue = BigDecimal.ZERO;
    private long invoiceCount;

    public BigDecimal getTotalRevenueYtd() { return totalRevenueYtd; }
    public void setTotalRevenueYtd(BigDecimal totalRevenueYtd) { this.totalRevenueYtd = totalRevenueYtd; }

    public BigDecimal getTotalPaid() { return totalPaid; }
    public void setTotalPaid(BigDecimal totalPaid) { this.totalPaid = totalPaid; }

    public BigDecimal getOutstanding() { return outstanding; }
    public void setOutstanding(BigDecimal outstanding) { this.outstanding = outstanding; }

    public BigDecimal getOverdue() { return overdue; }
    public void setOverdue(BigDecimal overdue) { this.overdue = overdue; }

    public long getInvoiceCount() { return invoiceCount; }
    public void setInvoiceCount(long invoiceCount) { this.invoiceCount = invoiceCount; }
}
