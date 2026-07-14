package hospicloud.dtos;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class SocieteDTO {

    private Long idSociete;

    @NotBlank(message = "nomSociete est obligatoire")
    @Size(min = 2, max = 150, message = "Le nom doit contenir entre 2 et 150 caractères")
    private String nomSociete;
    private String adresseFacturation;

    @Pattern(regexp = "^\\+?[0-9. ]{9,18}$", message = "Format du téléphone invalide")
    private String telephoneContact;

    @Email(message = "Format de l'email invalide")
    private String emailContact;

    // allow null for update operations
    @DecimalMin(value = "0", message = "Le taux ne peut pas être inférieur à 0%")
    @DecimalMax(value = "100", message = "Le taux ne peut pas dépasser 1.0 (100%)")
    private Double tauxCouverture;

    private Integer idHopital; // ajouté pour SaaS
    
    private String nomHopital;

    public SocieteDTO() {
    }

    public SocieteDTO(Long idSociete, String nomSociete, String adresseFacturation, String telephoneContact, String emailContact, Double tauxCouverture, Integer idHopital,String nomHopital) {
        this.idSociete = idSociete;
        this.nomSociete = nomSociete;
        this.adresseFacturation = adresseFacturation;
        this.telephoneContact = telephoneContact;
        this.emailContact = emailContact;
        this.tauxCouverture = tauxCouverture;
        this.idHopital = idHopital;
        this.nomHopital = nomHopital;
    }

    // getters and setters
    public Long getIdSociete() {
        return idSociete;
    }

    public void setIdSociete(Long idSociete) {
        this.idSociete = idSociete;
    }

    public String getNomSociete() {
        return nomSociete;
    }

    public void setNomSociete(String nomSociete) {
        this.nomSociete = nomSociete;
    }

    public String getAdresseFacturation() {
        return adresseFacturation;
    }

    public void setAdresseFacturation(String adresseFacturation) {
        this.adresseFacturation = adresseFacturation;
    }

    public String getTelephoneContact() {
        return telephoneContact;
    }

    public void setTelephoneContact(String telephoneContact) {
        this.telephoneContact = telephoneContact;
    }

    public String getEmailContact() {
        return emailContact;
    }

    public void setEmailContact(String emailContact) {
        this.emailContact = emailContact;
    }

    public Double getTauxCouverture() {
        return tauxCouverture;
    }

    public void setTauxCouverture(Double tauxCouverture) {
        this.tauxCouverture = tauxCouverture;
    }

    public Integer getIdHopital() {
        return idHopital;
    }

    public void setIdHopital(Integer idHopital) {
        this.idHopital = idHopital;
    }

    public String getTauxPourcentage() {
        return (tauxCouverture != null) ? (tauxCouverture * 100) /100 + "%" : "0%";
    }
    

    public String getNomHopital() {
		return nomHopital;
	}

	public void setNomHopital(String nomHopital) {
		this.nomHopital = nomHopital;
	}

	// Simple builder implementation to replace Lombok's @Builder
    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private Long idSociete;
        private String nomSociete;
        private String adresseFacturation;
        private String telephoneContact;
        private String emailContact;
        private Double tauxCouverture;
        private Integer idHopital;
        private String nomHopital;

        public Builder idSociete(Long id) { this.idSociete = id; return this; }
        public Builder nomSociete(String nom) { this.nomSociete = nom; return this; }
        public Builder adresseFacturation(String addr) { this.adresseFacturation = addr; return this; }
        public Builder telephoneContact(String tel) { this.telephoneContact = tel; return this; }
        public Builder emailContact(String email) { this.emailContact = email; return this; }
        public Builder tauxCouverture(Double taux) { this.tauxCouverture = taux; return this; }
        public Builder idHopital(Integer idHopital) { this.idHopital = idHopital; return this; }
        public Builder nomHopital(String nomHopital) { this.nomHopital = nomHopital; return this; }
        public SocieteDTO build() {
            return new SocieteDTO(idSociete, nomSociete, adresseFacturation, telephoneContact, emailContact, tauxCouverture, idHopital,nomHopital);
        }
    }

}