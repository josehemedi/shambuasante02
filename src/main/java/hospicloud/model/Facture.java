package hospicloud.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Entité représentant la facture globale d'un patient.
 * Centralise les montants HT, la TVA et le TTC pour la comptabilité de l'hôpital.
 * * Développé par Siku Hemedi Jose - Projet Hospicloud.
 */
public class Facture {

    private Integer idFacture;
    private Integer idPatient;
    private Integer idHopital;
    private String numeroFacture; // Format unique ex: FAC-2026-0001
    private LocalDateTime dateFacture;
    
    private BigDecimal montantTotalHt;
    private BigDecimal tva; // Taux en pourcentage (ex: 16.00)
    private BigDecimal montantTotalTtc;
    
    private String statutPaiement; // IMPAYE, PARTIEL, PAYE, ANNULE
    private Integer idCaissier;

    // Constructeur par défaut
    public Facture() {
    }

    // Constructeur complet
    public Facture(Integer idFacture, Integer idPatient, Integer idHopital, String numeroFacture, 
                   LocalDateTime dateFacture, BigDecimal montantTotalHt, BigDecimal tva, 
                   BigDecimal montantTotalTtc, String statutPaiement, Integer idCaissier) {
        this.idFacture = idFacture;
        this.idPatient = idPatient;
        this.idHopital = idHopital;
        this.numeroFacture = numeroFacture;
        this.dateFacture = dateFacture;
        this.montantTotalHt = montantTotalHt;
        this.tva = tva;
        this.montantTotalTtc = montantTotalTtc;
        this.statutPaiement = statutPaiement;
        this.idCaissier = idCaissier;
    }

    // Getters & Setters
    public Integer getIdFacture() { return idFacture; }
    public void setIdFacture(Integer idFacture) { this.idFacture = idFacture; }

    public Integer getIdPatient() { return idPatient; }
    public void setIdPatient(Integer idPatient) { this.idPatient = idPatient; }

    public Integer getIdHopital() { return idHopital; }
    public void setIdHopital(Integer idHopital) { this.idHopital = idHopital; }

    public String getNumeroFacture() { return numeroFacture; }
    public void setNumeroFacture(String numeroFacture) { this.numeroFacture = numeroFacture; }

    public LocalDateTime getDateFacture() { return dateFacture; }
    public void setDateFacture(LocalDateTime dateFacture) { this.dateFacture = dateFacture; }

    public BigDecimal getMontantTotalHt() { return montantTotalHt; }
    public void setMontantTotalHt(BigDecimal montantTotalHt) { this.montantTotalHt = montantTotalHt; }

    public BigDecimal getTva() { return tva; }
    public void setTva(BigDecimal tva) { this.tva = tva; }

    public BigDecimal getMontantTotalTtc() { return montantTotalTtc; }
    public void setMontantTotalTtc(BigDecimal montantTotalTtc) { this.montantTotalTtc = montantTotalTtc; }

    public String getStatutPaiement() { return statutPaiement; }
    public void setStatutPaiement(String statutPaiement) { this.statutPaiement = statutPaiement; }

    public Integer getIdCaissier() { return idCaissier; }
    public void setIdCaissier(Integer idCaissier) { this.idCaissier = idCaissier; }

    @Override
    public String toString() {
        return "Facture{" +
                "numero='" + numeroFacture + '\'' +
                ", montantTtc=" + montantTotalTtc +
                ", statut='" + statutPaiement + '\'' +
                '}';
    }
}