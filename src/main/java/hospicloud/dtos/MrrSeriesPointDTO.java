package hospicloud.dtos;

import java.math.BigDecimal;

public class MrrSeriesPointDTO {
    private String month;
    private BigDecimal mrr;

    public MrrSeriesPointDTO() {
    }

    public MrrSeriesPointDTO(String month, BigDecimal mrr) {
        this.month = month;
        this.mrr = mrr;
    }

    public String getMonth() { return month; }
    public void setMonth(String month) { this.month = month; }

    public BigDecimal getMrr() { return mrr; }
    public void setMrr(BigDecimal mrr) { this.mrr = mrr; }
}
