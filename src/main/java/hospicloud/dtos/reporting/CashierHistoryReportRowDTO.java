package hospicloud.dtos.reporting;

public class CashierHistoryReportRowDTO {

    private String numero;
    private String recu;
    private String facture;
    private String patient;
    private String montant;
    private String mode;
    private String datePaiement;
    private String caissier;
    private String soldeRestant;

    public CashierHistoryReportRowDTO() {
    }

    public CashierHistoryReportRowDTO(
            String numero,
            String recu,
            String facture,
            String patient,
            String montant,
            String mode,
            String datePaiement,
            String caissier,
            String soldeRestant) {
        this.numero = numero;
        this.recu = recu;
        this.facture = facture;
        this.patient = patient;
        this.montant = montant;
        this.mode = mode;
        this.datePaiement = datePaiement;
        this.caissier = caissier;
        this.soldeRestant = soldeRestant;
    }

    public String getNumero() { return numero; }
    public void setNumero(String numero) { this.numero = numero; }

    public String getRecu() { return recu; }
    public void setRecu(String recu) { this.recu = recu; }

    public String getFacture() { return facture; }
    public void setFacture(String facture) { this.facture = facture; }

    public String getPatient() { return patient; }
    public void setPatient(String patient) { this.patient = patient; }

    public String getMontant() { return montant; }
    public void setMontant(String montant) { this.montant = montant; }

    public String getMode() { return mode; }
    public void setMode(String mode) { this.mode = mode; }

    public String getDatePaiement() { return datePaiement; }
    public void setDatePaiement(String datePaiement) { this.datePaiement = datePaiement; }

    public String getCaissier() { return caissier; }
    public void setCaissier(String caissier) { this.caissier = caissier; }

    public String getSoldeRestant() { return soldeRestant; }
    public void setSoldeRestant(String soldeRestant) { this.soldeRestant = soldeRestant; }
}
