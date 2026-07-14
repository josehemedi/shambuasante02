package hospicloud.dtos;

import jakarta.validation.constraints.NotNull;

public class HospitalStatusUpdateDTO {
    @NotNull(message = "Le statut actif est requis")
    private Boolean active;

    public Boolean getActive() { return active; }
    public void setActive(Boolean active) { this.active = active; }
}
