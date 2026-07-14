package hospicloud.model;

import java.time.LocalDate;

/**
 * Entité représentant l'historique médical (antécédents) d'un patient.
 * Aide au diagnostic en fournissant le contexte clinique passé.
 * * Développé par Siku Hemedi Jose - Projet Hospicloud.
 */
public class PatientAntecedent {

    private Integer idAntecedent;
    private Integer idPatient; // Clé étrangère vers la table patients
    private String typeAntecedent; // CHIRURGICAL, MEDICAL, FAMILIAL
    private String description;
    private LocalDate dateEvenement;

    // Constructeur par défaut
    public PatientAntecedent() {
    }

    // Constructeur complet
    public PatientAntecedent(Integer idAntecedent, Integer idPatient, String typeAntecedent, 
                            String description, LocalDate dateEvenement) {
        this.idAntecedent = idAntecedent;
        this.idPatient = idPatient;
        this.typeAntecedent = typeAntecedent;
        this.description = description;
        this.dateEvenement = dateEvenement;
    }

    // Getters & Setters
    public Integer getIdAntecedent() {
        return idAntecedent;
    }

    public void setIdAntecedent(Integer idAntecedent) {
        this.idAntecedent = idAntecedent;
    }

    public Integer getIdPatient() {
        return idPatient;
    }

    public void setIdPatient(Integer idPatient) {
        this.idPatient = idPatient;
    }

    public String getTypeAntecedent() {
        return typeAntecedent;
    }

    public void setTypeAntecedent(String typeAntecedent) {
        this.typeAntecedent = typeAntecedent;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public LocalDate getDateEvenement() {
        return dateEvenement;
    }

    public void setDateEvenement(LocalDate dateEvenement) {
        this.dateEvenement = dateEvenement;
    }

    @Override
    public String toString() {
        return "PatientAntecedent{" +
                "id=" + idAntecedent +
                ", type='" + typeAntecedent + '\'' +
                ", description='" + description + '\'' +
                '}';
    }
}