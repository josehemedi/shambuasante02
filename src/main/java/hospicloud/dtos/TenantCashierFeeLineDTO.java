package hospicloud.dtos;

import java.math.BigDecimal;

public class TenantCashierFeeLineDTO {
    private String id;
    private String label;
    private int qty = 1;
    private BigDecimal unitPrice = BigDecimal.ZERO;
    private BigDecimal total = BigDecimal.ZERO;

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getLabel() { return label; }
    public void setLabel(String label) { this.label = label; }

    public int getQty() { return qty; }
    public void setQty(int qty) { this.qty = qty; }

    public BigDecimal getUnitPrice() { return unitPrice; }
    public void setUnitPrice(BigDecimal unitPrice) { this.unitPrice = unitPrice; }

    public BigDecimal getTotal() { return total; }
    public void setTotal(BigDecimal total) { this.total = total; }
}
