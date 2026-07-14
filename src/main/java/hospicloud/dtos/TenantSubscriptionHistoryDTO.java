package hospicloud.dtos;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class TenantSubscriptionHistoryDTO {
    private Integer idAbonnement;
    private String planNom;
    private BigDecimal montantMensuel;
    private String statut;
    private String action;
    private LocalDateTime dateDebut;
    private LocalDateTime dateFin;

    public Integer getIdAbonnement() { return idAbonnement; }
    public void setIdAbonnement(Integer idAbonnement) { this.idAbonnement = idAbonnement; }

    public String getPlanNom() { return planNom; }
    public void setPlanNom(String planNom) { this.planNom = planNom; }

    public BigDecimal getMontantMensuel() { return montantMensuel; }
    public void setMontantMensuel(BigDecimal montantMensuel) { this.montantMensuel = montantMensuel; }

    public String getStatut() { return statut; }
    public void setStatut(String statut) { this.statut = statut; }

    public String getAction() { return action; }
    public void setAction(String action) { this.action = action; }

    public LocalDateTime getDateDebut() { return dateDebut; }
    public void setDateDebut(LocalDateTime dateDebut) { this.dateDebut = dateDebut; }

    public LocalDateTime getDateFin() { return dateFin; }
    public void setDateFin(LocalDateTime dateFin) { this.dateFin = dateFin; }
}
