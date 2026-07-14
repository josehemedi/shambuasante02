package hospicloud.model;

import java.time.LocalDateTime;

/**
 * Entité représentant un utilisateur du système Hospicloud.
 * Gère l'authentification et le lien avec un établissement de santé.
 * * Développé par Siku Hemedi Jose - Projet UNIKIN / UPC.
 */
public class Utilisateur {

    private Integer idUtilisateur;
    private Integer idHopital; // Peut être null pour les super-admins de la plateforme
    private Integer idMedecin;
    private Long idPatient;
    private String nom;
    private String prenom;
    private String email;
    private String motDePasse; // Toujours stocké haché (ex: BCrypt)
    private String telephone;
    private Role role;
    private boolean estActif;
    private LocalDateTime dateCreation;

    // Constructeur par défaut
    public Utilisateur() {
    }

    // Constructeur complet
    public Utilisateur(Integer idUtilisateur, Integer idHopital, Integer idMedecin, Long idPatient,
                       String nom, String prenom, 
                       String email, String motDePasse, String telephone, Role role,
                       boolean estActif, LocalDateTime dateCreation) {
        this.idUtilisateur = idUtilisateur;
        this.idHopital = idHopital;
        this.idMedecin = idMedecin;
        this.idPatient = idPatient;
        this.nom = nom;
        this.prenom = prenom;
        this.email = email;
        this.motDePasse = motDePasse;
        this.telephone = telephone;
        this.role = role;
        this.estActif = estActif;
        this.dateCreation = dateCreation;
    }

    // Getters & Setters
    public Integer getIdUtilisateur() {
        return idUtilisateur;
    }

    public void setIdUtilisateur(Integer idUtilisateur) {
        this.idUtilisateur = idUtilisateur;
    }

    public Integer getIdHopital() {
        return idHopital;
    }

    public void setIdHopital(Integer idHopital) {
        this.idHopital = idHopital;
    }

    public Integer getIdMedecin() {
        return idMedecin;
    }

    public void setIdMedecin(Integer idMedecin) {
        this.idMedecin = idMedecin;
    }

    public Long getIdPatient() {
        return idPatient;
    }

    public void setIdPatient(Long idPatient) {
        this.idPatient = idPatient;
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

    public String getMotDePasse() {
        return motDePasse;
    }

    public void setMotDePasse(String motDePasse) {
        this.motDePasse = motDePasse;
    }

    public String getTelephone() {
        return telephone;
    }

    public void setTelephone(String telephone) {
        this.telephone = telephone;
    }
     
    public Role getRole() {
		return role;
	}

	public void setRole(Role role) {
		this.role = role;
	}

	public boolean isEstActif() {
        return estActif;
    }

    public void setEstActif(boolean estActif) {
        this.estActif = estActif;
    }

    public LocalDateTime getDateCreation() {
        return dateCreation;
    }

    public void setDateCreation(LocalDateTime dateCreation) {
        this.dateCreation = dateCreation;
    }

    @Override
    public String toString() {
        return "Utilisateur{" +
                "id=" + idUtilisateur +
                ", email='" + email + '\'' +
                ", hopitalId=" + idHopital +
                ", actif=" + estActif +
                '}';
    }
}