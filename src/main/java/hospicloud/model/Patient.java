package hospicloud.model;

import java.time.LocalDate;
import java.time.LocalDateTime;

import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

/**
 * Entité centrale représentant un patient au sein d'un établissement de santé.
 * Gère l'identification unique et les informations médicales de base.
 * * Développé par Siku Hemedi Jose - Projet Hospicloud.
 */
public class Patient {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idPatient;
    private Integer idHopital; // Isolation SaaS (très important !)
    private String codePatient; // Ex: PAT-2026-0001
    private String nom;
    private String prenom;
    private String sexe; // M, F
    private LocalDate dateNaissance;
    private String groupeSanguin; // A+, A-, etc.
    private String adresse;
    private String telephone;
    private String email;
    private String profession;
    private boolean estActif;
    private LocalDateTime dateEnregistrement;
    private Integer idSociete;
    private String numeroMatricule;
    private String contactUrgence;
    private String statutClinique;
    private Integer creePar;
    private Integer modifiePar;

    // Constructeur par défaut
    public Patient() {
    }

    // Constructeur complet
    public Patient(Long idPatient, Integer idHopital, String codePatient, String nom, String prenom, 
                   String sexe, LocalDate dateNaissance, String groupeSanguin, String adresse, 
                   String telephone, String email, String profession, boolean estActif, 
                   LocalDateTime dateEnregistrement,Integer idSociete,String numeroMatricule) {
        this.idPatient = idPatient;
        this.idHopital = idHopital;
        this.codePatient = codePatient;
        this.nom = nom;
        this.prenom = prenom;
        this.sexe = sexe;
        this.dateNaissance = dateNaissance;
        this.groupeSanguin = groupeSanguin;
        this.adresse = adresse;
        this.telephone = telephone;
        this.email = email;
        this.profession = profession;
        this.estActif = estActif;
        this.dateEnregistrement = dateEnregistrement;
        this.idSociete = idSociete;
        this.numeroMatricule = numeroMatricule;
    }

    // Getters & Setters
    public Long getIdPatient() { return idPatient; }
    public void setIdPatient(Long idPatient) { this.idPatient = idPatient; }

    public Integer getIdHopital() { return idHopital; }
    public void setIdHopital(Integer idHopital) { this.idHopital = idHopital; }

    public String getCodePatient() { return codePatient; }
    public void setCodePatient(String codePatient) { this.codePatient = codePatient; }

    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }

    public String getPrenom() { return prenom; }
    public void setPrenom(String prenom) { this.prenom = prenom; }

    public String getSexe() { return sexe; }
    public void setSexe(String sexe) { this.sexe = sexe; }

    public LocalDate getDateNaissance() { return dateNaissance; }
    public void setDateNaissance(LocalDate dateNaissance) { this.dateNaissance = dateNaissance; }

    public String getGroupeSanguin() { return groupeSanguin; }
    public void setGroupeSanguin(String groupeSanguin) { this.groupeSanguin = groupeSanguin; }

    public String getAdresse() { return adresse; }
    public void setAdresse(String adresse) { this.adresse = adresse; }

    public String getTelephone() { return telephone; }
    public void setTelephone(String telephone) { this.telephone = telephone; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getProfession() { return profession; }
    public void setProfession(String profession) { this.profession = profession; }

    public boolean isEstActif() { return estActif; }
    public void setEstActif(boolean estActif) { this.estActif = estActif; }

    public LocalDateTime getDateEnregistrement() { return dateEnregistrement; }
    public void setDateEnregistrement(LocalDateTime dateEnregistrement) { this.dateEnregistrement = dateEnregistrement; }
    
    // on va mettre en place des accesseurs et mutateurs  pour idSociete et le numeroMatricule
    
    
    @Override
    public String toString() {
        return "Patient{" +
                "id=" + idPatient +
                ", code='" + codePatient + '\'' +
                ", nom='" + nom + " " + prenom + '\'' +
                '}';
    }

	public Integer getIdSociete() {
		return idSociete;
	}

	public void setIdSociete(Integer idSociete) {
		this.idSociete = idSociete;
	}

	public String getNumeroMatricule() {
		return numeroMatricule;
	}

	public void setNumeroMatricule(String numeroMatricule) {
		this.numeroMatricule = numeroMatricule;
	}

	public String getContactUrgence() {
		return contactUrgence;
	}

	public void setContactUrgence(String contactUrgence) {
		this.contactUrgence = contactUrgence;
	}

	public String getStatutClinique() {
		return statutClinique;
	}

	public void setStatutClinique(String statutClinique) {
		this.statutClinique = statutClinique;
	}

	public Integer getCreePar() {
		return creePar;
	}

	public void setCreePar(Integer creePar) {
		this.creePar = creePar;
	}

	public Integer getModifiePar() {
		return modifiePar;
	}

	public void setModifiePar(Integer modifiePar) {
		this.modifiePar = modifiePar;
	}
}