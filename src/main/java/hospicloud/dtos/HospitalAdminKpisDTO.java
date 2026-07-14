package hospicloud.dtos;

import java.math.BigDecimal;

public class HospitalAdminKpisDTO {
    private long totalPatients;
    private double deltaTotalPatients;
    private long activeConsultations;
    private double deltaActiveConsultations;
    private BigDecimal revenueMtd = BigDecimal.ZERO;
    private double deltaRevenueMtd;
    private double occupancy;
    private double deltaOccupancy;

    public long getTotalPatients() { return totalPatients; }
    public void setTotalPatients(long totalPatients) { this.totalPatients = totalPatients; }

    public double getDeltaTotalPatients() { return deltaTotalPatients; }
    public void setDeltaTotalPatients(double deltaTotalPatients) { this.deltaTotalPatients = deltaTotalPatients; }

    public long getActiveConsultations() { return activeConsultations; }
    public void setActiveConsultations(long activeConsultations) { this.activeConsultations = activeConsultations; }

    public double getDeltaActiveConsultations() { return deltaActiveConsultations; }
    public void setDeltaActiveConsultations(double deltaActiveConsultations) { this.deltaActiveConsultations = deltaActiveConsultations; }

    public BigDecimal getRevenueMtd() { return revenueMtd; }
    public void setRevenueMtd(BigDecimal revenueMtd) { this.revenueMtd = revenueMtd; }

    public double getDeltaRevenueMtd() { return deltaRevenueMtd; }
    public void setDeltaRevenueMtd(double deltaRevenueMtd) { this.deltaRevenueMtd = deltaRevenueMtd; }

    public double getOccupancy() { return occupancy; }
    public void setOccupancy(double occupancy) { this.occupancy = occupancy; }

    public double getDeltaOccupancy() { return deltaOccupancy; }
    public void setDeltaOccupancy(double deltaOccupancy) { this.deltaOccupancy = deltaOccupancy; }
}
