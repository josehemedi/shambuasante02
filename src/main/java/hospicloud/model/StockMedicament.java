package hospicloud.model;

import java.time.LocalDate;

/**
 * Entité gérant l'inventaire physique des médicaments par hôpital.
 * Permet le suivi des quantités, des alertes de rupture et des péremptions.
 * * Développé par Siku Hemedi Jose - Projet Hospicloud.
 */
public class StockMedicament {

    private Integer idStock;
    private Integer idHopital;      // Isolation SaaS
    private Integer idMedicament;   // Référence au catalogue général
    private Integer quantiteDisponible;
    private Integer seuilAlerte;    // Quantité minimale avant notification
    private LocalDate datePeremption;
    private String emplacementRayon; // Ex: "A-12", "Frigo-01"

    // Constructeur par défaut
    public StockMedicament() {
    }

    // Constructeur complet
    public StockMedicament(Integer idStock, Integer idHopital, Integer idMedicament, 
                           Integer quantiteDisponible, Integer seuilAlerte, 
                           LocalDate datePeremption, String emplacementRayon) {
        this.idStock = idStock;
        this.idHopital = idHopital;
        this.idMedicament = idMedicament;
        this.quantiteDisponible = quantiteDisponible;
        this.seuilAlerte = seuilAlerte;
        this.datePeremption = datePeremption;
        this.emplacementRayon = emplacementRayon;
    }

    // Getters & Setters
    public Integer getIdStock() { return idStock; }
    public void setIdStock(Integer idStock) { this.idStock = idStock; }

    public Integer getIdHopital() { return idHopital; }
    public void setIdHopital(Integer idHopital) { this.idHopital = idHopital; }

    public Integer getIdMedicament() { return idMedicament; }
    public void setIdMedicament(Integer idMedicament) { this.idMedicament = idMedicament; }

    public Integer getQuantiteDisponible() { return quantiteDisponible; }
    public void setQuantiteDisponible(Integer quantiteDisponible) { this.quantiteDisponible = quantiteDisponible; }

    public Integer getSeuilAlerte() { return seuilAlerte; }
    public void setSeuilAlerte(Integer seuilAlerte) { this.seuilAlerte = seuilAlerte; }

    public LocalDate getDatePeremption() { return datePeremption; }
    public void setDatePeremption(LocalDate datePeremption) { this.datePeremption = datePeremption; }

    public String getEmplacementRayon() { return emplacementRayon; }
    public void setEmplacementRayon(String emplacementRayon) { this.emplacementRayon = emplacementRayon; }

    @Override
    public String toString() {
        return "Stock{" +
                "id=" + idStock +
                ", dispo=" + quantiteDisponible +
                ", alerte=" + seuilAlerte +
                ", expire=" + datePeremption +
                '}';
    }
}