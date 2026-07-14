package hospicloud.dtos;

public class TeleconsultationChatEventDTO {

    private String eventType;
    private TeleconsultationChatMessageDTO message;
    private TeleconsultationChatReadReceiptDTO readReceipt;

    public static TeleconsultationChatEventDTO message(TeleconsultationChatMessageDTO message) {
        TeleconsultationChatEventDTO event = new TeleconsultationChatEventDTO();
        event.setEventType("message");
        event.setMessage(message);
        return event;
    }

    public static TeleconsultationChatEventDTO readReceipt(TeleconsultationChatReadReceiptDTO receipt) {
        TeleconsultationChatEventDTO event = new TeleconsultationChatEventDTO();
        event.setEventType("read_receipt");
        event.setReadReceipt(receipt);
        return event;
    }

    public String getEventType() {
        return eventType;
    }

    public void setEventType(String eventType) {
        this.eventType = eventType;
    }

    public TeleconsultationChatMessageDTO getMessage() {
        return message;
    }

    public void setMessage(TeleconsultationChatMessageDTO message) {
        this.message = message;
    }

    public TeleconsultationChatReadReceiptDTO getReadReceipt() {
        return readReceipt;
    }

    public void setReadReceipt(TeleconsultationChatReadReceiptDTO readReceipt) {
        this.readReceipt = readReceipt;
    }
}
