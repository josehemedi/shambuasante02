package hospicloud.dtos;

import java.math.BigDecimal;

public class BillingDraftLineDTO {
    private String categorie;
    private String designation;
    private int quantite = 1;
    private BigDecimal prixUnitaire = BigDecimal.ZERO;
    private String sourceType;
    private Long sourceId;
    private Integer idProduitPharmacie;
    private Integer idActeMedical;

    public String getCategorie() { return categorie; }
    public void setCategorie(String categorie) { this.categorie = categorie; }

    public String getDesignation() { return designation; }
    public void setDesignation(String designation) { this.designation = designation; }

    public int getQuantite() { return quantite; }
    public void setQuantite(int quantite) { this.quantite = quantite; }

    public BigDecimal getPrixUnitaire() { return prixUnitaire; }
    public void setPrixUnitaire(BigDecimal prixUnitaire) { this.prixUnitaire = prixUnitaire; }

    public BigDecimal getSousTotal() {
        BigDecimal qty = BigDecimal.valueOf(Math.max(quantite, 1));
        BigDecimal unit = prixUnitaire != null ? prixUnitaire : BigDecimal.ZERO;
        return unit.multiply(qty);
    }

    public String getSourceType() { return sourceType; }
    public void setSourceType(String sourceType) { this.sourceType = sourceType; }

    public Long getSourceId() { return sourceId; }
    public void setSourceId(Long sourceId) { this.sourceId = sourceId; }

    public Integer getIdProduitPharmacie() { return idProduitPharmacie; }
    public void setIdProduitPharmacie(Integer idProduitPharmacie) {
        this.idProduitPharmacie = idProduitPharmacie;
    }

    public Integer getIdActeMedical() { return idActeMedical; }
    public void setIdActeMedical(Integer idActeMedical) { this.idActeMedical = idActeMedical; }
}
