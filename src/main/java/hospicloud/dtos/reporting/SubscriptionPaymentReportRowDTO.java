package hospicloud.dtos.reporting;

public class SubscriptionPaymentReportRowDTO {

    private String numero;
    private String reference;
    private String planNom;
    private String montant;
    private String statut;
    private String datePaiement;
    private String dateEcheance;
    private String typePaiement;

    public SubscriptionPaymentReportRowDTO() {}

    public SubscriptionPaymentReportRowDTO(
            String numero,
            String reference,
            String planNom,
            String montant,
            String statut,
            String datePaiement,
            String dateEcheance,
            String typePaiement) {
        this.numero = numero;
        this.reference = reference;
        this.planNom = planNom;
        this.montant = montant;
        this.statut = statut;
        this.datePaiement = datePaiement;
        this.dateEcheance = dateEcheance;
        this.typePaiement = typePaiement;
    }

    public String getNumero() { return numero; }
    public void setNumero(String numero) { this.numero = numero; }

    public String getReference() { return reference; }
    public void setReference(String reference) { this.reference = reference; }

    public String getPlanNom() { return planNom; }
    public void setPlanNom(String planNom) { this.planNom = planNom; }

    public String getMontant() { return montant; }
    public void setMontant(String montant) { this.montant = montant; }

    public String getStatut() { return statut; }
    public void setStatut(String statut) { this.statut = statut; }

    public String getDatePaiement() { return datePaiement; }
    public void setDatePaiement(String datePaiement) { this.datePaiement = datePaiement; }

    public String getDateEcheance() { return dateEcheance; }
    public void setDateEcheance(String dateEcheance) { this.dateEcheance = dateEcheance; }

    public String getTypePaiement() { return typePaiement; }
    public void setTypePaiement(String typePaiement) { this.typePaiement = typePaiement; }
}
