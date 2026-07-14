package hospicloud.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Entité enregistrant chaque transaction financière liée à une facture.
 * Gère les paiements multiples et la traçabilité des transactions (Mobile Money, Espèces).
 * * Développé par Siku Hemedi Jose - Projet Hospicloud.
 */
public class Paiement {

    private Integer idPaiement;
    private Integer idFacture;
    private Integer idModePaiement; // Lien vers M-Pesa, Espèces, etc.
    private BigDecimal montantPaye;
    private LocalDateTime datePaiement;
    private String referenceTransaction; // ID de transaction externe (ex: MP260310.1532.C12345)

    // Constructeur par défaut
    public Paiement() {
    }

    // Constructeur complet
    public Paiement(Integer idPaiement, Integer idFacture, Integer idModePaiement, 
                    BigDecimal montantPaye, LocalDateTime datePaiement, String referenceTransaction) {
        this.idPaiement = idPaiement;
        this.idFacture = idFacture;
        this.idModePaiement = idModePaiement;
        this.montantPaye = montantPaye;
        this.datePaiement = datePaiement;
        this.referenceTransaction = referenceTransaction;
    }

    // Getters & Setters
    public Integer getIdPaiement() { return idPaiement; }
    public void setIdPaiement(Integer idPaiement) { this.idPaiement = idPaiement; }

    public Integer getIdFacture() { return idFacture; }
    public void setIdFacture(Integer idFacture) { this.idFacture = idFacture; }

    public Integer getIdModePaiement() { return idModePaiement; }
    public void setIdModePaiement(Integer idModePaiement) { this.idModePaiement = idModePaiement; }

    public BigDecimal getMontantPaye() { return montantPaye; }
    public void setMontantPaye(BigDecimal montantPaye) { this.montantPaye = montantPaye; }

    public LocalDateTime getDatePaiement() { return datePaiement; }
    public void setDatePaiement(LocalDateTime datePaiement) { this.datePaiement = datePaiement; }

    public String getReferenceTransaction() { return referenceTransaction; }
    public void setReferenceTransaction(String referenceTransaction) { this.referenceTransaction = referenceTransaction; }

    @Override
    public String toString() {
        return "Paiement{" +
                "id=" + idPaiement +
                ", factureId=" + idFacture +
                ", montant=" + montantPaye +
                ", ref='" + referenceTransaction + '\'' +
                '}';
    }
}