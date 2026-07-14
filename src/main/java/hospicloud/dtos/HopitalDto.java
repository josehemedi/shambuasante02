package hospicloud.dtos;

import java.time.LocalDateTime;
import jakarta.validation.constraints.NotBlank;

public class HopitalDto extends BaseDto {
    private Integer idHopital;

    @NotBlank(message = "Le nom de l'hôpital est requis")
    private String nom;
    private String adresse;
    private String telephone;
    private String email;
    private String logoUrl;
    private LocalDateTime dateCreation;
    private boolean estActif;

    public Integer getIdHopital() { return idHopital; }
    public void setIdHopital(Integer idHopital) { this.idHopital = idHopital; }

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

    public LocalDateTime getDateCreation() { return dateCreation; }
    public void setDateCreation(LocalDateTime dateCreation) { this.dateCreation = dateCreation; }

    public boolean isEstActif() { return estActif; }
    public void setEstActif(boolean estActif) { this.estActif = estActif; }
}