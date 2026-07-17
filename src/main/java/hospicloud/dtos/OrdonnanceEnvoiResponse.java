package hospicloud.dtos;

import java.time.LocalDateTime;

public class OrdonnanceEnvoiResponse {

    private Long idOrdonnance;
    private String numeroOrdonnance;
    private String nomPatient;
    private String emailMasque;
    private LocalDateTime envoyeLe;
    private String message;

    public Long getIdOrdonnance() {
        return idOrdonnance;
    }

    public void setIdOrdonnance(Long idOrdonnance) {
        this.idOrdonnance = idOrdonnance;
    }

    public String getNumeroOrdonnance() {
        return numeroOrdonnance;
    }

    public void setNumeroOrdonnance(String numeroOrdonnance) {
        this.numeroOrdonnance = numeroOrdonnance;
    }

    public String getNomPatient() {
        return nomPatient;
    }

    public void setNomPatient(String nomPatient) {
        this.nomPatient = nomPatient;
    }

    public String getEmailMasque() {
        return emailMasque;
    }

    public void setEmailMasque(String emailMasque) {
        this.emailMasque = emailMasque;
    }

    public LocalDateTime getEnvoyeLe() {
        return envoyeLe;
    }

    public void setEnvoyeLe(LocalDateTime envoyeLe) {
        this.envoyeLe = envoyeLe;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
