package hospicloud.dtos;

import java.time.LocalDateTime;

public class DocumentEnvoiResponse {
    private Integer idDocument;
    private Integer idPatient;
    private String nomPatient;
    private String typeDocument;
    private String titre;
    private String emailMasque;
    private LocalDateTime envoyeLe;
    private String message;

    public Integer getIdDocument() { return idDocument; }
    public void setIdDocument(Integer idDocument) { this.idDocument = idDocument; }
    public Integer getIdPatient() { return idPatient; }
    public void setIdPatient(Integer idPatient) { this.idPatient = idPatient; }
    public String getNomPatient() { return nomPatient; }
    public void setNomPatient(String nomPatient) { this.nomPatient = nomPatient; }
    public String getTypeDocument() { return typeDocument; }
    public void setTypeDocument(String typeDocument) { this.typeDocument = typeDocument; }
    public String getTitre() { return titre; }
    public void setTitre(String titre) { this.titre = titre; }
    public String getEmailMasque() { return emailMasque; }
    public void setEmailMasque(String emailMasque) { this.emailMasque = emailMasque; }
    public LocalDateTime getEnvoyeLe() { return envoyeLe; }
    public void setEnvoyeLe(LocalDateTime envoyeLe) { this.envoyeLe = envoyeLe; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
}
