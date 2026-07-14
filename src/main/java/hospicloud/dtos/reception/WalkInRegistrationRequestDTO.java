package hospicloud.dtos.reception;

import jakarta.validation.constraints.NotBlank;

public class WalkInRegistrationRequestDTO {

    /** Réutiliser un patient déjà connu (optionnel). */
    private Integer idPatient;

    @NotBlank
    private String nom;

    @NotBlank
    private String prenom;

    @NotBlank
    private String sexe;

    /** Âge en années si la date de naissance n'est pas fournie. */
    private Integer age;

    private String dateNaissance;

    private String telephone;

    @NotBlank
    private String motifConsultation;

    @NotBlank
    private String serviceDemande;

    /** Spécialité souhaitée (sinon dérivée du service). */
    private String specialite;

    /** URGENCE | HAUTE | NORMALE */
    @NotBlank
    private String niveauUrgence;

    /** Médecin choisi ; si null, le système propose le plus disponible. */
    private Integer idMedecin;

    private boolean affectationAutomatique = true;

    public Integer getIdPatient() { return idPatient; }
    public void setIdPatient(Integer idPatient) { this.idPatient = idPatient; }

    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }

    public String getPrenom() { return prenom; }
    public void setPrenom(String prenom) { this.prenom = prenom; }

    public String getSexe() { return sexe; }
    public void setSexe(String sexe) { this.sexe = sexe; }

    public Integer getAge() { return age; }
    public void setAge(Integer age) { this.age = age; }

    public String getDateNaissance() { return dateNaissance; }
    public void setDateNaissance(String dateNaissance) { this.dateNaissance = dateNaissance; }

    public String getTelephone() { return telephone; }
    public void setTelephone(String telephone) { this.telephone = telephone; }

    public String getMotifConsultation() { return motifConsultation; }
    public void setMotifConsultation(String motifConsultation) { this.motifConsultation = motifConsultation; }

    public String getServiceDemande() { return serviceDemande; }
    public void setServiceDemande(String serviceDemande) { this.serviceDemande = serviceDemande; }

    public String getSpecialite() { return specialite; }
    public void setSpecialite(String specialite) { this.specialite = specialite; }

    public String getNiveauUrgence() { return niveauUrgence; }
    public void setNiveauUrgence(String niveauUrgence) { this.niveauUrgence = niveauUrgence; }

    public Integer getIdMedecin() { return idMedecin; }
    public void setIdMedecin(Integer idMedecin) { this.idMedecin = idMedecin; }

    public boolean isAffectationAutomatique() { return affectationAutomatique; }
    public void setAffectationAutomatique(boolean affectationAutomatique) {
        this.affectationAutomatique = affectationAutomatique;
    }
}
