package hospicloud.dtos;

import java.math.BigDecimal;

public class HospitalPlatformStatsDTO {
    private long total;
    private long active;
    private long trial;
    private long suspended;
    private long totalUsers;
    private BigDecimal totalMrr;
    private BigDecimal deltaTotal;
    private BigDecimal deltaActive;
    private BigDecimal deltaTrial;
    private BigDecimal deltaSuspended;
    private BigDecimal deltaTotalUsers;
    private BigDecimal deltaTotalMrr;

    public long getTotal() { return total; }
    public void setTotal(long total) { this.total = total; }

    public long getActive() { return active; }
    public void setActive(long active) { this.active = active; }

    public long getTrial() { return trial; }
    public void setTrial(long trial) { this.trial = trial; }

    public long getSuspended() { return suspended; }
    public void setSuspended(long suspended) { this.suspended = suspended; }

    public long getTotalUsers() { return totalUsers; }
    public void setTotalUsers(long totalUsers) { this.totalUsers = totalUsers; }

    public BigDecimal getTotalMrr() { return totalMrr; }
    public void setTotalMrr(BigDecimal totalMrr) { this.totalMrr = totalMrr; }

    public BigDecimal getDeltaTotal() { return deltaTotal; }
    public void setDeltaTotal(BigDecimal deltaTotal) { this.deltaTotal = deltaTotal; }

    public BigDecimal getDeltaActive() { return deltaActive; }
    public void setDeltaActive(BigDecimal deltaActive) { this.deltaActive = deltaActive; }

    public BigDecimal getDeltaTrial() { return deltaTrial; }
    public void setDeltaTrial(BigDecimal deltaTrial) { this.deltaTrial = deltaTrial; }

    public BigDecimal getDeltaSuspended() { return deltaSuspended; }
    public void setDeltaSuspended(BigDecimal deltaSuspended) { this.deltaSuspended = deltaSuspended; }

    public BigDecimal getDeltaTotalUsers() { return deltaTotalUsers; }
    public void setDeltaTotalUsers(BigDecimal deltaTotalUsers) { this.deltaTotalUsers = deltaTotalUsers; }

    public BigDecimal getDeltaTotalMrr() { return deltaTotalMrr; }
    public void setDeltaTotalMrr(BigDecimal deltaTotalMrr) { this.deltaTotalMrr = deltaTotalMrr; }
}
