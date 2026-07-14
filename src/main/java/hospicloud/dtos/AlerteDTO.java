package hospicloud.dtos;

import java.time.LocalDateTime;

public class AlerteDTO {
    private Integer idAlerte;
    private String typeAlerte;
    private String priorite;
    private String messageAlerte;
    private boolean estResolu;
    private LocalDateTime dateCreation;

    public AlerteDTO() {}

    public AlerteDTO(Integer idAlerte, String typeAlerte, String priorite, 
                     String messageAlerte, boolean estResolu, LocalDateTime dateCreation) {
        this.idAlerte = idAlerte;
        this.typeAlerte = typeAlerte;
        this.priorite = priorite;
        this.messageAlerte = messageAlerte;
        this.estResolu = estResolu;
        this.dateCreation = dateCreation;
    }

    public Integer getIdAlerte() { return idAlerte; }
    public void setIdAlerte(Integer idAlerte) { this.idAlerte = idAlerte; }

    public String getTypeAlerte() { return typeAlerte; }
    public void setTypeAlerte(String typeAlerte) { this.typeAlerte = typeAlerte; }

    public String getPriorite() { return priorite; }
    public void setPriorite(String priorite) { this.priorite = priorite; }

    public String getMessageAlerte() { return messageAlerte; }
    public void setMessageAlerte(String messageAlerte) { this.messageAlerte = messageAlerte; }

    public boolean isEstResolu() { return estResolu; }
    public void setEstResolu(boolean estResolu) { this.estResolu = estResolu; }

    public LocalDateTime getDateCreation() { return dateCreation; }
    public void setDateCreation(LocalDateTime dateCreation) { this.dateCreation = dateCreation; }
}