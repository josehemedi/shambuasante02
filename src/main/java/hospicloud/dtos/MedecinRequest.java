package hospicloud.dtos;

/**
 * DTO utilisé pour créer ou mettre à jour le profil médecin.
 */
public class MedecinRequest {

    private String nom;
    private String prenom;
    private String email;

    private String specialite;
    private String numeroOrdre;
    private String telephonePro;

    private Boolean disponibiliteStatus;

    // =========================
    // GETTERS & SETTERS
    // =========================

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
}