package hospicloud.dtos;

public class TechnicalLogKpisDTO {
    private SubscriptionKpiMetricDTO totalEvents;
    private SubscriptionKpiMetricDTO securityAlerts;
    private SubscriptionKpiMetricDTO dataChanges;
    private SubscriptionKpiMetricDTO complianceScore;
    private SubscriptionKpiMetricDTO openTickets;

    public SubscriptionKpiMetricDTO getTotalEvents() { return totalEvents; }
    public void setTotalEvents(SubscriptionKpiMetricDTO totalEvents) { this.totalEvents = totalEvents; }

    public SubscriptionKpiMetricDTO getSecurityAlerts() { return securityAlerts; }
    public void setSecurityAlerts(SubscriptionKpiMetricDTO securityAlerts) { this.securityAlerts = securityAlerts; }

    public SubscriptionKpiMetricDTO getDataChanges() { return dataChanges; }
    public void setDataChanges(SubscriptionKpiMetricDTO dataChanges) { this.dataChanges = dataChanges; }

    public SubscriptionKpiMetricDTO getComplianceScore() { return complianceScore; }
    public void setComplianceScore(SubscriptionKpiMetricDTO complianceScore) { this.complianceScore = complianceScore; }

    public SubscriptionKpiMetricDTO getOpenTickets() { return openTickets; }
    public void setOpenTickets(SubscriptionKpiMetricDTO openTickets) { this.openTickets = openTickets; }
}
