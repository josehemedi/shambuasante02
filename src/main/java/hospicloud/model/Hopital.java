package hospicloud.model;

import java.time.LocalDateTime;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class Hopital {

    private Integer idHopital;
    private String nom;
    private String adresse;
    private String telephone;
    private String email;
    private String logoUrl;

    // Nouvelles colonnes
    private String ville;
    private String pays;
    private String type;

    private LocalDateTime dateCreation;
    private LocalDateTime dateModification;
    
    @NotNull(message = "Le sous-domaine est obligatoire pour le mode SaaS")
    @Pattern(regexp = "^[a-z0-9-]+$", message = "Le sous-domaine ne doit contenir que des minuscules, chiffres ou tirets")
    @Size(min=3,max=63, message = "Le sous domaine doit etre entre 3 à 63 carateres")
    private String sousDomaine;
    @NotBlank(message = "Le nom commercial est requis")
    private String nomCommercial;
    @NotBlank(message = "L'adresse complète est requise")
    private String adresseComplete;

    private boolean estActif;

    // Constructeur vide
    public Hopital() {
    }

    // Constructeur complet
    public Hopital(Integer idHopital, String nom, String adresse, String telephone,
                   String email, String logoUrl, String ville, String pays,
                   String type, LocalDateTime dateCreation,
                   LocalDateTime dateModification,String sousDomaine,String nomCommercial,String adresseComplete, boolean estActif) {

        this.idHopital = idHopital;
        this.nom = nom;
        this.adresse = adresse;
        this.telephone = telephone;
        this.email = email;
        this.logoUrl = logoUrl;
        this.ville = ville;
        this.pays = pays;
        this.type = type;
        this.dateCreation = dateCreation;
        this.dateModification = dateModification;
        this.sousDomaine=sousDomaine;
        this.nomCommercial=nomCommercial;
        this.adresseComplete=adresseComplete;
        this.estActif = estActif;
    }

    // Getters et Setters

    public Integer getIdHopital() {
        return idHopital;
    }

    public void setIdHopital(Integer idHopital) {
        this.idHopital = idHopital;
    }

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public String getAdresse() {
        return adresse;
    }

    public void setAdresse(String adresse) {
        this.adresse = adresse;
    }

    public String getTelephone() {
        return telephone;
    }

    public void setTelephone(String telephone) {
        this.telephone = telephone;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getLogoUrl() {
        return logoUrl;
    }

    public void setLogoUrl(String logoUrl) {
        this.logoUrl = logoUrl;
    }

    public String getVille() {
        return ville;
    }

    public void setVille(String ville) {
        this.ville = ville;
    }

    public String getPays() {
        return pays;
    }

    public void setPays(String pays) {
        this.pays = pays;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public LocalDateTime getDateCreation() {
        return dateCreation;
    }

    public void setDateCreation(LocalDateTime dateCreation) {
        this.dateCreation = dateCreation;
    }

    public LocalDateTime getDateModification() {
        return dateModification;
    }

    public void setDateModification(LocalDateTime dateModification) {
        this.dateModification = dateModification;
    }

    public boolean isEstActif() {
        return estActif;
    }
    

    public String getSousDomaine() {
		return sousDomaine;
	}

	public void setSousDomaine(String sousDomaine) {
		this.sousDomaine = sousDomaine;
	}

	public String getNomCommercial() {
		return nomCommercial;
	}

	public void setNomCommercial(String nomCommercial) {
		this.nomCommercial = nomCommercial;
	}

	public String getAdresseComplete() {
		return adresseComplete;
	}

	public void setAdresseComplete(String adresseComplete) {
		this.adresseComplete = adresseComplete;
	}

	public void setEstActif(boolean estActif) {
        this.estActif = estActif;
    }
    
    

    @Override
    public String toString() {
        return "Hopital{" +
                "idHopital=" + idHopital +
                ", nom='" + nom + '\'' +
                ", ville='" + ville + '\'' +
                ", pays='" + pays + '\'' +
                ", type='" + type + '\'' +
                ", estActif=" + estActif +
                '}';
    }
}