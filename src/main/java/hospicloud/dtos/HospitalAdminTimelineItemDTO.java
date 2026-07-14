package hospicloud.dtos;

public class HospitalAdminTimelineItemDTO {
    private int id;
    private String text;
    private String textFr;
    private String time;
    private String actor;

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getText() { return text; }
    public void setText(String text) { this.text = text; }

    public String getTextFr() { return textFr; }
    public void setTextFr(String textFr) { this.textFr = textFr; }

    public String getTime() { return time; }
    public void setTime(String time) { this.time = time; }

    public String getActor() { return actor; }
    public void setActor(String actor) { this.actor = actor; }
}
