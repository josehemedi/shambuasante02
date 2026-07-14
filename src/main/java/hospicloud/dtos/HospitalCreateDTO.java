package hospicloud.dtos;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class HospitalCreateDTO {

    @NotBlank(message = "Le nom de l'hôpital est requis")
    @Size(max = 150)
    private String nom;

    private String adresse;

    @Size(max = 20)
    private String telephone;

    @NotBlank(message = "L'email est requis")
    @Email(message = "Format d'email invalide")
    @Size(max = 100)
    private String email;

    @Size(max = 255)
    private String logoUrl;

    private boolean estActif = true;

    @NotBlank(message = "La ville est requise")
    @Size(max = 100)
    private String ville;

    @NotBlank(message = "Le pays est requis")
    @Size(max = 100)
    private String pays;

    @NotBlank(message = "Le type est requis")
    private String type = "CLINIQUE";

    @NotBlank(message = "Le sous-domaine est requis")
    @Pattern(regexp = "^[a-z0-9-]{3,63}$", message = "Sous-domaine invalide (minuscules, chiffres, tirets)")
    @Size(min = 3, max = 100)
    private String sousDomaine;

    @Size(max = 255)
    private String nomCommercial;

    private String adresseComplete;

    /** Forfait SaaS — table abonnements (hors hopitaux). */
    private String planNom = "Starter";

    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }

    public String getAdresse() { return adresse; }
    public void setAdresse(String adresse) { this.adresse = adresse; }

    public String getTelephone() { return telephone; }
    public void setTelephone(String telephone) { this.telephone = telephone; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getLogoUrl() { return logoUrl; }
    public void setLogoUrl(String logoUrl) { this.logoUrl = logoUrl; }

    public boolean isEstActif() { return estActif; }
    public void setEstActif(boolean estActif) { this.estActif = estActif; }

    public String getVille() { return ville; }
    public void setVille(String ville) { this.ville = ville; }

    public String getPays() { return pays; }
    public void setPays(String pays) { this.pays = pays; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getSousDomaine() { return sousDomaine; }
    public void setSousDomaine(String sousDomaine) { this.sousDomaine = sousDomaine; }

    public String getNomCommercial() { return nomCommercial; }
    public void setNomCommercial(String nomCommercial) { this.nomCommercial = nomCommercial; }

    public String getAdresseComplete() { return adresseComplete; }
    public void setAdresseComplete(String adresseComplete) { this.adresseComplete = adresseComplete; }

    public String getPlanNom() { return planNom; }
    public void setPlanNom(String planNom) { this.planNom = planNom; }
}
