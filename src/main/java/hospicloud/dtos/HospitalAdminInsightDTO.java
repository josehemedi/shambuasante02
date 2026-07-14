package hospicloud.dtos;

public class HospitalAdminInsightDTO {
    private int id;
    private String title;
    private String titleFr;
    private String detail;
    private String detailFr;
    private String tone;

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getTitleFr() { return titleFr; }
    public void setTitleFr(String titleFr) { this.titleFr = titleFr; }

    public String getDetail() { return detail; }
    public void setDetail(String detail) { this.detail = detail; }

    public String getDetailFr() { return detailFr; }
    public void setDetailFr(String detailFr) { this.detailFr = detailFr; }

    public String getTone() { return tone; }
    public void setTone(String tone) { this.tone = tone; }
}
