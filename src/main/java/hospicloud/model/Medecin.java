package hospicloud.model;

/**
 * Modèle représentant un médecin dans le système Hospicloud.
 */
public class Medecin {

    private Integer idMedecin;
    private Integer idHopital;

    private String nom;
    private String prenom;
    private String email;

    private String specialite;
    private String numeroOrdre;
    private String telephonePro;

    private Boolean disponibiliteStatus = true;

    // =========================
    // GETTERS & SETTERS
    // =========================

    public Integer getIdMedecin() {
        return idMedecin;
    }

    public void setIdMedecin(Integer idMedecin) {
        this.idMedecin = idMedecin;
    }

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

    public String getPrenom() {
        return prenom;
    }

    public void setPrenom(String prenom) {
        this.prenom = prenom;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getSpecialite() {
        return specialite;
    }

    public void setSpecialite(String specialite) {
        this.specialite = specialite;
    }

    public String getNumeroOrdre() {
        return numeroOrdre;
    }

    public void setNumeroOrdre(String numeroOrdre) {
        this.numeroOrdre = numeroOrdre;
    }

    public String getTelephonePro() {
        return telephonePro;
    }

    public void setTelephonePro(String telephonePro) {
        this.telephonePro = telephonePro;
    }

    public Boolean getDisponibiliteStatus() {
        return disponibiliteStatus;
    }

    public void setDisponibiliteStatus(Boolean disponibiliteStatus) {
        this.disponibiliteStatus = disponibiliteStatus;
    }

    // =========================
    // TO STRING
    // =========================

    public Medecin(Integer idMedecin, Integer idHopital, String nom, String prenom, String email, String specialite,
			String numeroOrdre, String telephonePro, Boolean disponibiliteStatus) {
		super();
		this.idMedecin = idMedecin;
		this.idHopital = idHopital;
		this.nom = nom;
		this.prenom = prenom;
		this.email = email;
		this.specialite = specialite;
		this.numeroOrdre = numeroOrdre;
		this.telephonePro = telephonePro;
		this.disponibiliteStatus = disponibiliteStatus;
	}

	public Medecin() {
		super();
		// TODO Auto-generated constructor stub
	}

	@Override
    public String toString() {
        return "Medecin{" +
                "idMedecin=" + idMedecin +
                ", idHopital=" + idHopital +
                ", nom='" + nom + '\'' +
                ", prenom='" + prenom + '\'' +
                ", email='" + email + '\'' +
                ", specialite='" + specialite + '\'' +
                ", numeroOrdre='" + numeroOrdre + '\'' +
                ", telephonePro='" + telephonePro + '\'' +
                ", disponibiliteStatus=" + disponibiliteStatus +
                '}';
    }
}