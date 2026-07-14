package hospicloud.dtos.reporting;

public class CashierQueueReportRowDTO {

    private String numero;
    private String patient;
    private String patientId;
    private String facture;
    private String statut;
    private String total;
    private String paye;
    private String solde;
    private String priorite;
    private String service;
    private String medecin;

    public CashierQueueReportRowDTO() {
    }

    public CashierQueueReportRowDTO(
            String numero,
            String patient,
            String patientId,
            String facture,
            String statut,
            String total,
            String paye,
            String solde,
            String priorite,
            String service,
            String medecin) {
        this.numero = numero;
        this.patient = patient;
        this.patientId = patientId;
        this.facture = facture;
        this.statut = statut;
        this.total = total;
        this.paye = paye;
        this.solde = solde;
        this.priorite = priorite;
        this.service = service;
        this.medecin = medecin;
    }

    public String getNumero() { return numero; }
    public void setNumero(String numero) { this.numero = numero; }

    public String getPatient() { return patient; }
    public void setPatient(String patient) { this.patient = patient; }

    public String getPatientId() { return patientId; }
    public void setPatientId(String patientId) { this.patientId = patientId; }

    public String getFacture() { return facture; }
    public void setFacture(String facture) { this.facture = facture; }

    public String getStatut() { return statut; }
    public void setStatut(String statut) { this.statut = statut; }

    public String getTotal() { return total; }
    public void setTotal(String total) { this.total = total; }

    public String getPaye() { return paye; }
    public void setPaye(String paye) { this.paye = paye; }

    public String getSolde() { return solde; }
    public void setSolde(String solde) { this.solde = solde; }

    public String getPriorite() { return priorite; }
    public void setPriorite(String priorite) { this.priorite = priorite; }

    public String getService() { return service; }
    public void setService(String service) { this.service = service; }

    public String getMedecin() { return medecin; }
    public void setMedecin(String medecin) { this.medecin = medecin; }
}
