package hospicloud.dtos;

public class HospitalAdminFlowPointDTO {
    private String dayKey;
    private long admissions;
    private long discharges;

    public HospitalAdminFlowPointDTO() {}

    public HospitalAdminFlowPointDTO(String dayKey, long admissions, long discharges) {
        this.dayKey = dayKey;
        this.admissions = admissions;
        this.discharges = discharges;
    }

    public String getDayKey() { return dayKey; }
    public void setDayKey(String dayKey) { this.dayKey = dayKey; }

    public long getAdmissions() { return admissions; }
    public void setAdmissions(long admissions) { this.admissions = admissions; }

    public long getDischarges() { return discharges; }
    public void setDischarges(long discharges) { this.discharges = discharges; }
}
