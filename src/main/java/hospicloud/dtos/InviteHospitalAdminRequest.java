package hospicloud.dtos;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** Invitation d'un administrateur d'hôpital par le SUPER_ADMIN (hôpital déjà existant). */
public class InviteHospitalAdminRequest {

    @NotBlank(message = "Le prénom est requis")
    @Size(max = 100)
    private String adminPrenom;

    @NotBlank(message = "Le nom est requis")
    @Size(max = 100)
    private String adminNom;

    @NotBlank(message = "L'email de l'administrateur est requis")
    @Email(message = "Format d'email invalide")
    @Size(max = 150)
    private String adminEmail;

    @Size(max = 30)
    private String adminTelephone;

    public String getAdminPrenom() { return adminPrenom; }
    public void setAdminPrenom(String adminPrenom) { this.adminPrenom = adminPrenom; }

    public String getAdminNom() { return adminNom; }
    public void setAdminNom(String adminNom) { this.adminNom = adminNom; }

    public String getAdminEmail() { return adminEmail; }
    public void setAdminEmail(String adminEmail) { this.adminEmail = adminEmail; }

    public String getAdminTelephone() { return adminTelephone; }
    public void setAdminTelephone(String adminTelephone) { this.adminTelephone = adminTelephone; }
}
