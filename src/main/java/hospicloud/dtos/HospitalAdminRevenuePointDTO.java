package hospicloud.dtos;

import java.math.BigDecimal;

public class HospitalAdminRevenuePointDTO {
    private int month;
    private int year;
    private BigDecimal inpatient = BigDecimal.ZERO;
    private BigDecimal outpatient = BigDecimal.ZERO;
    private BigDecimal tele = BigDecimal.ZERO;

    public HospitalAdminRevenuePointDTO() {}

    public HospitalAdminRevenuePointDTO(int month, int year, BigDecimal inpatient, BigDecimal outpatient, BigDecimal tele) {
        this.month = month;
        this.year = year;
        this.inpatient = inpatient;
        this.outpatient = outpatient;
        this.tele = tele;
    }

    public int getMonth() { return month; }
    public void setMonth(int month) { this.month = month; }

    public int getYear() { return year; }
    public void setYear(int year) { this.year = year; }

    public BigDecimal getInpatient() { return inpatient; }
    public void setInpatient(BigDecimal inpatient) { this.inpatient = inpatient; }

    public BigDecimal getOutpatient() { return outpatient; }
    public void setOutpatient(BigDecimal outpatient) { this.outpatient = outpatient; }

    public BigDecimal getTele() { return tele; }
    public void setTele(BigDecimal tele) { this.tele = tele; }
}
