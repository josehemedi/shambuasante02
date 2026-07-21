package hospicloud.dtos.reporting;

public class PlatformInvoiceReportRowDTO {

    private String numero;
    private String hopital;
    private String reference;
    private String planNom;
    private String montant;
    private String statut;
    private String dateFacture;
    private String dateEcheance;

    public PlatformInvoiceReportRowDTO() {}

    public PlatformInvoiceReportRowDTO(
            String numero,
            String hopital,
            String reference,
            String planNom,
            String montant,
            String statut,
            String dateFacture,
            String dateEcheance) {
        this.numero = numero;
        this.hopital = hopital;
        this.reference = reference;
        this.planNom = planNom;
        this.montant = montant;
        this.statut = statut;
        this.dateFacture = dateFacture;
        this.dateEcheance = dateEcheance;
    }

    public String getNumero() { return numero; }
    public void setNumero(String numero) { this.numero = numero; }

    public String getHopital() { return hopital; }
    public void setHopital(String hopital) { this.hopital = hopital; }

    public String getReference() { return reference; }
    public void setReference(String reference) { this.reference = reference; }

    public String getPlanNom() { return planNom; }
    public void setPlanNom(String planNom) { this.planNom = planNom; }

    public String getMontant() { return montant; }
    public void setMontant(String montant) { this.montant = montant; }

    public String getStatut() { return statut; }
    public void setStatut(String statut) { this.statut = statut; }

    public String getDateFacture() { return dateFacture; }
    public void setDateFacture(String dateFacture) { this.dateFacture = dateFacture; }

    public String getDateEcheance() { return dateEcheance; }
    public void setDateEcheance(String dateEcheance) { this.dateEcheance = dateEcheance; }
}
