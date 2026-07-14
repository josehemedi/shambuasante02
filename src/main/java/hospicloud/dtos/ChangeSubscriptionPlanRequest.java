package hospicloud.dtos;

import jakarta.validation.constraints.NotBlank;

public class ChangeSubscriptionPlanRequest {
    @NotBlank
    private String planNom;

    public String getPlanNom() { return planNom; }
    public void setPlanNom(String planNom) { this.planNom = planNom; }
}
