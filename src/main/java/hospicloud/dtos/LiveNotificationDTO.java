package hospicloud.dtos;

import java.time.LocalDateTime;

public class LiveNotificationDTO {
    private String id;
    private String type;
    private String title;
    private String titleFr;
    private String message;
    private String messageFr;
    private String tone = "primary";
    private LocalDateTime createdAt;
    private Integer idRdv;
    private Integer idHopital;
    private String link;

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getTitleFr() { return titleFr; }
    public void setTitleFr(String titleFr) { this.titleFr = titleFr; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public String getMessageFr() { return messageFr; }
    public void setMessageFr(String messageFr) { this.messageFr = messageFr; }

    public String getTone() { return tone; }
    public void setTone(String tone) { this.tone = tone; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public Integer getIdRdv() { return idRdv; }
    public void setIdRdv(Integer idRdv) { this.idRdv = idRdv; }

    public Integer getIdHopital() { return idHopital; }
    public void setIdHopital(Integer idHopital) { this.idHopital = idHopital; }

    public String getLink() { return link; }
    public void setLink(String link) { this.link = link; }
}
