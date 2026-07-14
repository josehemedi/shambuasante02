package hospicloud.dtos;

import java.math.BigDecimal;

public class SubscriptionKpiMetricDTO {
    private BigDecimal value;
    private BigDecimal delta;

    public SubscriptionKpiMetricDTO() {
    }

    public SubscriptionKpiMetricDTO(BigDecimal value, BigDecimal delta) {
        this.value = value;
        this.delta = delta;
    }

    public BigDecimal getValue() { return value; }
    public void setValue(BigDecimal value) { this.value = value; }

    public BigDecimal getDelta() { return delta; }
    public void setDelta(BigDecimal delta) { this.delta = delta; }
}
