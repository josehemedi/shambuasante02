package hospicloud.model;

import java.time.LocalDateTime;

/**
 * Entité représentant l'acte de délivrance des médicaments au patient.
 * Fait le lien entre la prescription, la sortie de stock et le paiement.
 * * Développé par Siku Hemedi Jose - Projet Hospicloud.
 */
public class Dispensation {

    private Integer idDispensation;
    private Integer idPatient;
    private Integer idConsultation; // Peut être null si vente directe sans consultation
    private Integer idPharmacien;   // Référence à l'utilisateur (rôle Pharmacien)
    private LocalDateTime dateDispensation;
    private String statutPaiement;  // PAYE, EN_ATTENTE, PRIS_EN_CHARGE

    // Constructeur par défaut
    public Dispensation() {
    }

    // Constructeur complet
    public Dispensation(Integer idDispensation, Integer idPatient, Integer idConsultation, 
                        Integer idPharmacien, LocalDateTime dateDispensation, String statutPaiement) {
        this.idDispensation = idDispensation;
        this.idPatient = idPatient;
        this.idConsultation = idConsultation;
        this.idPharmacien = idPharmacien;
        this.dateDispensation = dateDispensation;
        this.statutPaiement = statutPaiement;
    }

    // Getters & Setters
    public Integer getIdDispensation() {
        return idDispensation;
    }

    public void setIdDispensation(Integer idDispensation) {
        this.idDispensation = idDispensation;
    }

    public Integer getIdPatient() {
        return idPatient;
    }

    public void setIdPatient(Integer idPatient) {
        this.idPatient = idPatient;
    }

    public Integer getIdConsultation() {
        return idConsultation;
    }

    public void setIdConsultation(Integer idConsultation) {
        this.idConsultation = idConsultation;
    }

    public Integer getIdPharmacien() {
        return idPharmacien;
    }

    public void setIdPharmacien(Integer idPharmacien) {
        this.idPharmacien = idPharmacien;
    }

    public LocalDateTime getDateDispensation() {
        return dateDispensation;
    }

    public void setDateDispensation(LocalDateTime dateDispensation) {
        this.dateDispensation = dateDispensation;
    }

    public String getStatutPaiement() {
        return statutPaiement;
    }

    public void setStatutPaiement(String statutPaiement) {
        this.statutPaiement = statutPaiement;
    }

    @Override
    public String toString() {
        return "Dispensation{" +
                "id=" + idDispensation +
                ", patient=" + idPatient +
                ", statut='" + statutPaiement + '\'' +
                ", date=" + dateDispensation +
                '}';
    }
}