package hospicloud.dtos;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;

public class CreatePharmacieMedicamentRequest {

    @NotBlank(message = "Le nom du médicament est obligatoire")
    @Size(max = 150)
    private String nomMedicament;

    @Size(max = 150)
    private String nomGenerique;

    @Size(max = 100)
    private String categorie;

    @Size(max = 100)
    private String dosage;

    @Size(max = 50)
    private String forme;

    @Size(max = 50)
    private String unite;

    @Min(0)
    private int quantiteStock;

    @Min(0)
    private int stockMinimum;

    private BigDecimal prixAchat;
    private BigDecimal prixVente;

    @Size(max = 100)
    private String numeroLot;

    private LocalDate dateExpiration;

    @Size(max = 150)
    private String fournisseur;

    public String getNomMedicament() { return nomMedicament; }
    public void setNomMedicament(String nomMedicament) { this.nomMedicament = nomMedicament; }

    public String getNomGenerique() { return nomGenerique; }
    public void setNomGenerique(String nomGenerique) { this.nomGenerique = nomGenerique; }

    public String getCategorie() { return categorie; }
    public void setCategorie(String categorie) { this.categorie = categorie; }

    public String getDosage() { return dosage; }
    public void setDosage(String dosage) { this.dosage = dosage; }

    public String getForme() { return forme; }
    public void setForme(String forme) { this.forme = forme; }

    public String getUnite() { return unite; }
    public void setUnite(String unite) { this.unite = unite; }

    public int getQuantiteStock() { return quantiteStock; }
    public void setQuantiteStock(int quantiteStock) { this.quantiteStock = quantiteStock; }

    public int getStockMinimum() { return stockMinimum; }
    public void setStockMinimum(int stockMinimum) { this.stockMinimum = stockMinimum; }

    public BigDecimal getPrixAchat() { return prixAchat; }
    public void setPrixAchat(BigDecimal prixAchat) { this.prixAchat = prixAchat; }

    public BigDecimal getPrixVente() { return prixVente; }
    public void setPrixVente(BigDecimal prixVente) { this.prixVente = prixVente; }

    public String getNumeroLot() { return numeroLot; }
    public void setNumeroLot(String numeroLot) { this.numeroLot = numeroLot; }

    public LocalDate getDateExpiration() { return dateExpiration; }
    public void setDateExpiration(LocalDate dateExpiration) { this.dateExpiration = dateExpiration; }

    public String getFournisseur() { return fournisseur; }
    public void setFournisseur(String fournisseur) { this.fournisseur = fournisseur; }
}
