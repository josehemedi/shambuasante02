package hospicloud.dtos;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class PatientDto extends BaseDto {
    private Integer idPatient;
    private Integer idHopital;
    private String codePatient;
    private String nom;
    private String prenom;
    private String sexe;
    private LocalDate dateNaissance;
    private String groupeSanguin;
    private String adresse;
    private String telephone;
    private String email;
    private String profession;
    private boolean estActif;
    private LocalDateTime dateEnregistrement;
    private Integer idSociete;
    private String numeroMatricule;

    public Integer getIdPatient() { return idPatient; }
    public void setIdPatient(Integer idPatient) { this.idPatient = idPatient; }

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
    
    
}
