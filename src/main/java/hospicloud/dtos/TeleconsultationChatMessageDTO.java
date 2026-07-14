package hospicloud.dtos;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class TeleconsultationChatMessageDTO {

    private Long id;
    private Integer idHopital;
    private Integer idRdv;
    private Integer idEmetteur;
    private String senderRole;
    private String senderName;
    private String content;
    private String createdAt;
    private Boolean readByRecipient;
    private String readAt;

    @JsonIgnore
    private String readByDoctorAt;

    @JsonIgnore
    private String readByPatientAt;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Integer getIdHopital() {
        return idHopital;
    }

    public void setIdHopital(Integer idHopital) {
        this.idHopital = idHopital;
    }

    public Integer getIdRdv() {
        return idRdv;
    }

    public void setIdRdv(Integer idRdv) {
        this.idRdv = idRdv;
    }

    public Integer getIdEmetteur() {
        return idEmetteur;
    }

    public void setIdEmetteur(Integer idEmetteur) {
        this.idEmetteur = idEmetteur;
    }

    public String getSenderRole() {
        return senderRole;
    }

    public void setSenderRole(String senderRole) {
        this.senderRole = senderRole;
    }

    public String getSenderName() {
        return senderName;
    }

    public void setSenderName(String senderName) {
        this.senderName = senderName;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public Boolean getReadByRecipient() {
        return readByRecipient;
    }

    public void setReadByRecipient(Boolean readByRecipient) {
        this.readByRecipient = readByRecipient;
    }

    public String getReadAt() {
        return readAt;
    }

    public void setReadAt(String readAt) {
        this.readAt = readAt;
    }

    public String getReadByDoctorAt() {
        return readByDoctorAt;
    }

    public void setReadByDoctorAt(String readByDoctorAt) {
        this.readByDoctorAt = readByDoctorAt;
    }

    public String getReadByPatientAt() {
        return readByPatientAt;
    }

    public void setReadByPatientAt(String readByPatientAt) {
        this.readByPatientAt = readByPatientAt;
    }
}
