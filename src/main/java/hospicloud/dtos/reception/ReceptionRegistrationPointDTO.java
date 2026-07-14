package hospicloud.dtos.reception;

public class ReceptionRegistrationPointDTO {
    private int hour;
    private long count;

    public ReceptionRegistrationPointDTO() {}

    public ReceptionRegistrationPointDTO(int hour, long count) {
        this.hour = hour;
        this.count = count;
    }

    public int getHour() { return hour; }
    public void setHour(int hour) { this.hour = hour; }

    public long getCount() { return count; }
    public void setCount(long count) { this.count = count; }
}
