package hospicloud.dtos;

public class SubscriptionKpisDTO {
    private SubscriptionKpiMetricDTO activeSubscriptions;
    private SubscriptionKpiMetricDTO mrr;
    private SubscriptionKpiMetricDTO arpu;
    private SubscriptionKpiMetricDTO churnRate;

    public SubscriptionKpiMetricDTO getActiveSubscriptions() { return activeSubscriptions; }
    public void setActiveSubscriptions(SubscriptionKpiMetricDTO activeSubscriptions) {
        this.activeSubscriptions = activeSubscriptions;
    }

    public SubscriptionKpiMetricDTO getMrr() { return mrr; }
    public void setMrr(SubscriptionKpiMetricDTO mrr) { this.mrr = mrr; }

    public SubscriptionKpiMetricDTO getArpu() { return arpu; }
    public void setArpu(SubscriptionKpiMetricDTO arpu) { this.arpu = arpu; }

    public SubscriptionKpiMetricDTO getChurnRate() { return churnRate; }
    public void setChurnRate(SubscriptionKpiMetricDTO churnRate) { this.churnRate = churnRate; }
}
