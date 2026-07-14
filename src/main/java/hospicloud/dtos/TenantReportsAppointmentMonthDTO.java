package hospicloud.dtos;

public class TenantReportsAppointmentMonthDTO {

    private String name;
    private int month;
    private int year;
    private long total;
    private long consultation;
    private long followUp;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getMonth() {
        return month;
    }

    public void setMonth(int month) {
        this.month = month;
    }

    public int getYear() {
        return year;
    }

    public void setYear(int year) {
        this.year = year;
    }

    public long getTotal() {
        return total;
    }

    public void setTotal(long total) {
        this.total = total;
    }

    public long getConsultation() {
        return consultation;
    }

    public void setConsultation(long consultation) {
        this.consultation = consultation;
    }

    public long getFollowUp() {
        return followUp;
    }

    public void setFollowUp(long followUp) {
        this.followUp = followUp;
    }
}
