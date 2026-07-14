package hospicloud.dtos;

public class HospitalAdminAlertDTO {
    private String id;
    private String level;
    private String title;
    private String titleFr;
    private String time;
    private String dept;

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getLevel() { return level; }
    public void setLevel(String level) { this.level = level; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getTitleFr() { return titleFr; }
    public void setTitleFr(String titleFr) { this.titleFr = titleFr; }

    public String getTime() { return time; }
    public void setTime(String time) { this.time = time; }

    public String getDept() { return dept; }
    public void setDept(String dept) { this.dept = dept; }
}
