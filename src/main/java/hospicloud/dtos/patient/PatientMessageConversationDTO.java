package hospicloud.dtos.patient;

import java.time.LocalDateTime;

public class PatientMessageConversationDTO {

    private Integer idRdv;
    private Integer idHopital;
    private String doctorName;
    private String motifVisite;
    private LocalDateTime dateHeureRdv;
    private String statutRdv;
    private long unreadCount;
    private String lastMessagePreview;
    private LocalDateTime lastMessageAt;
    private String lastSenderRole;

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

    public String getDoctorName() {
        return doctorName;
    }

    public void setDoctorName(String doctorName) {
        this.doctorName = doctorName;
    }

    public String getMotifVisite() {
        return motifVisite;
    }

    public void setMotifVisite(String motifVisite) {
        this.motifVisite = motifVisite;
    }

    public LocalDateTime getDateHeureRdv() {
        return dateHeureRdv;
    }

    public void setDateHeureRdv(LocalDateTime dateHeureRdv) {
        this.dateHeureRdv = dateHeureRdv;
    }

    public String getStatutRdv() {
        return statutRdv;
    }

    public void setStatutRdv(String statutRdv) {
        this.statutRdv = statutRdv;
    }

    public long getUnreadCount() {
        return unreadCount;
    }

    public void setUnreadCount(long unreadCount) {
        this.unreadCount = unreadCount;
    }

    public String getLastMessagePreview() {
        return lastMessagePreview;
    }

    public void setLastMessagePreview(String lastMessagePreview) {
        this.lastMessagePreview = lastMessagePreview;
    }

    public LocalDateTime getLastMessageAt() {
        return lastMessageAt;
    }

    public void setLastMessageAt(LocalDateTime lastMessageAt) {
        this.lastMessageAt = lastMessageAt;
    }

    public String getLastSenderRole() {
        return lastSenderRole;
    }

    public void setLastSenderRole(String lastSenderRole) {
        this.lastSenderRole = lastSenderRole;
    }
}
