package hospicloud.model;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Entité gérant la couverture d'assurance d'un patient.
 * Permet le calcul automatique de la prise en charge lors de la facturation.
 * * Développé par Siku Hemedi Jose - Projet Hospicloud.
 */
public class PatientAssurance {

    private Integer idAssurance;
    private Integer idPatient; // Clé étrangère vers la table patients
    private String nomCompagnie;
    private String numeroPolice;
    private BigDecimal tauxCouverture; // ex: 80.00 pour 80%
    private LocalDate dateExpiration;

    // Constructeur par défaut
    public PatientAssurance() {
    }

    // Constructeur complet
    public PatientAssurance(Integer idAssurance, Integer idPatient, String nomCompagnie, 
                            String numeroPolice, BigDecimal tauxCouverture, LocalDate dateExpiration) {
        this.idAssurance = idAssurance;
        this.idPatient = idPatient;
        this.nomCompagnie = nomCompagnie;
        this.numeroPolice = numeroPolice;
        this.tauxCouverture = tauxCouverture;
        this.dateExpiration = dateExpiration;
    }

    // Getters & Setters
    public Integer getIdAssurance() {
        return idAssurance;
    }

    public void setIdAssurance(Integer idAssurance) {
        this.idAssurance = idAssurance;
    }

    public Integer getIdPatient() {
        return idPatient;
    }

    public void setIdPatient(Integer idPatient) {
        this.idPatient = idPatient;
    }

    public String getNomCompagnie() {
        return nomCompagnie;
    }

    public void setNomCompagnie(String nomCompagnie) {
        this.nomCompagnie = nomCompagnie;
    }

    public String getNumeroPolice() {
        return numeroPolice;
    }

    public void setNumeroPolice(String numeroPolice) {
        this.numeroPolice = numeroPolice;
    }

    public BigDecimal getTauxCouverture() {
        return tauxCouverture;
    }

    public void setTauxCouverture(BigDecimal tauxCouverture) {
        this.tauxCouverture = tauxCouverture;
    }

    public LocalDate getDateExpiration() {
        return dateExpiration;
    }

    public void setDateExpiration(LocalDate dateExpiration) {
        this.dateExpiration = dateExpiration;
    }

    @Override
    public String toString() {
        return "PatientAssurance{" +
                "id=" + idAssurance +
                ", compagnie='" + nomCompagnie + '\'' +
                ", taux=" + tauxCouverture + "%" +
                '}';
    }
}