package hospicloud.dtos;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class PharmacieMedicamentDTO {
    private Long id;
    private Integer hopitalId;
    private String nomMedicament;
    private String nomGenerique;
    private String categorie;
    private String dosage;
    private String forme;
    private String unite;
    private int quantiteStock;
    private int stockMinimum;
    private BigDecimal prixAchat;
    private BigDecimal prixVente;
    private String numeroLot;
    private LocalDate dateExpiration;
    private String fournisseur;
    private String statut;
    private Integer creeParUtilisateurId;
    private LocalDateTime dateCreation;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Integer getHopitalId() { return hopitalId; }
    public void setHopitalId(Integer hopitalId) { this.hopitalId = hopitalId; }

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

    public String getStatut() { return statut; }
    public void setStatut(String statut) { this.statut = statut; }

    public Integer getCreeParUtilisateurId() { return creeParUtilisateurId; }
    public void setCreeParUtilisateurId(Integer creeParUtilisateurId) { this.creeParUtilisateurId = creeParUtilisateurId; }

    public LocalDateTime getDateCreation() { return dateCreation; }
    public void setDateCreation(LocalDateTime dateCreation) { this.dateCreation = dateCreation; }
}
