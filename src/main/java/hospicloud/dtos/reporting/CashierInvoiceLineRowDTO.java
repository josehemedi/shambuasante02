package hospicloud.dtos.reporting;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class CashierInvoiceLineRowDTO {

    private String numero;
    private String designation;
    private String quantite;
    private String prixUnitaire;
    private String sousTotal;
    private String categorie;

    public CashierInvoiceLineRowDTO() {
    }

    public CashierInvoiceLineRowDTO(
            String numero,
            String designation,
            String quantite,
            String prixUnitaire,
            String sousTotal,
            String categorie) {
        this.numero = numero;
        this.designation = designation;
        this.quantite = quantite;
        this.prixUnitaire = prixUnitaire;
        this.sousTotal = sousTotal;
        this.categorie = categorie;
    }

    public String getNumero() { return numero; }
    public void setNumero(String numero) { this.numero = numero; }

    public String getDesignation() { return designation; }
    public void setDesignation(String designation) { this.designation = designation; }

    public String getQuantite() { return quantite; }
    public void setQuantite(String quantite) { this.quantite = quantite; }

    public String getPrixUnitaire() { return prixUnitaire; }
    public void setPrixUnitaire(String prixUnitaire) { this.prixUnitaire = prixUnitaire; }

    public String getSousTotal() { return sousTotal; }
    public void setSousTotal(String sousTotal) { this.sousTotal = sousTotal; }

    public String getCategorie() { return categorie; }
    public void setCategorie(String categorie) { this.categorie = categorie; }
}
