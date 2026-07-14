package hospicloud.dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class SignerConsultationRequestDTO {

    @NotBlank(message = "Le mot de passe est requis pour confirmer la signature.")
    private String motDePasse;

    @NotNull(message = "La confirmation est requise.")
    private Boolean confirmation;

    public String getMotDePasse() {
        return motDePasse;
    }

    public void setMotDePasse(String motDePasse) {
        this.motDePasse = motDePasse;
    }

    public Boolean getConfirmation() {
        return confirmation;
    }

    public void setConfirmation(Boolean confirmation) {
        this.confirmation = confirmation;
    }
}
