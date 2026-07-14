package hospicloud.dtos;

import java.util.List;

public class TeleconsultationChatReadReceiptDTO {

    private String eventType = "read_receipt";
    private Integer idRdv;
    private Integer idHopital;
    private String readerRole;
    private String readAt;
    private List<Long> messageIds;

    public String getEventType() {
        return eventType;
    }

    public void setEventType(String eventType) {
        this.eventType = eventType;
    }

    public Integer getIdRdv() {
        return idRdv;
    }

    public void setIdRdv(Integer idRdv) {
        this.idRdv = idRdv;
    }

    public Integer getIdHopital() {
        return idHopital;
    }

    public void setIdHopital(Integer idHopital) {
        this.idHopital = idHopital;
    }

    public String getReaderRole() {
        return readerRole;
    }

    public void setReaderRole(String readerRole) {
        this.readerRole = readerRole;
    }

    public String getReadAt() {
        return readAt;
    }

    public void setReadAt(String readAt) {
        this.readAt = readAt;
    }

    public List<Long> getMessageIds() {
        return messageIds;
    }

    public void setMessageIds(List<Long> messageIds) {
        this.messageIds = messageIds;
    }
}
