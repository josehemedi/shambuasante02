package hospicloud.dtos;

import java.math.BigDecimal;

public class BillingComposeRequestDTO {
    private Integer idPatient;
    private Integer idFacture;
    private BigDecimal montantRemise;
    private BigDecimal tauxAssuranceOverride;
    /** Si true, crée ou met à jour la facture à partir des consommations non encore facturées. */
    private boolean rebuildExistingLines;

    public Integer getIdPatient() { return idPatient; }
    public void setIdPatient(Integer idPatient) { this.idPatient = idPatient; }

    public Integer getIdFacture() { return idFacture; }
    public void setIdFacture(Integer idFacture) { this.idFacture = idFacture; }

    public BigDecimal getMontantRemise() { return montantRemise; }
    public void setMontantRemise(BigDecimal montantRemise) { this.montantRemise = montantRemise; }

    public BigDecimal getTauxAssuranceOverride() { return tauxAssuranceOverride; }
    public void setTauxAssuranceOverride(BigDecimal tauxAssuranceOverride) {
        this.tauxAssuranceOverride = tauxAssuranceOverride;
    }

    public boolean isRebuildExistingLines() { return rebuildExistingLines; }
    public void setRebuildExistingLines(boolean rebuildExistingLines) {
        this.rebuildExistingLines = rebuildExistingLines;
    }
}
