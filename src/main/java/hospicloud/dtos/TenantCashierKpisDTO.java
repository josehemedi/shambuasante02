package hospicloud.dtos;

public class TenantCashierKpisDTO {
    private long waitingPayment;
    private long collectedToday;
    private long partialPayments;
    private long adminDischargePending;

    public long getWaitingPayment() { return waitingPayment; }
    public void setWaitingPayment(long waitingPayment) { this.waitingPayment = waitingPayment; }

    public long getCollectedToday() { return collectedToday; }
    public void setCollectedToday(long collectedToday) { this.collectedToday = collectedToday; }

    public long getPartialPayments() { return partialPayments; }
    public void setPartialPayments(long partialPayments) { this.partialPayments = partialPayments; }

    public long getAdminDischargePending() { return adminDischargePending; }
    public void setAdminDischargePending(long adminDischargePending) { this.adminDischargePending = adminDischargePending; }
}
