package hospicloud.dtos;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class TenantSubscriptionDTO {
    private Integer idAbonnement;
    private Integer idHopital;
    private String hospitalName;
    private String planNom;
    private BigDecimal montantMensuel;
    private String statut;
    private String uiStatus;
    private LocalDateTime dateDebut;
    private LocalDateTime dateFin;
    private Integer daysUntilDue;
    private boolean needsPayment;
    private Integer maxUsers;
    private Integer currentUserCount;
    private java.util.List<String> features;
    private Integer teleconsultationMonthlyLimit;
    private Integer teleconsultationUsedThisMonth;
    private String targetAudienceFr;
    private String targetAudienceEn;

    public Integer getIdAbonnement() { return idAbonnement; }
    public void setIdAbonnement(Integer idAbonnement) { this.idAbonnement = idAbonnement; }

    public Integer getIdHopital() { return idHopital; }
    public void setIdHopital(Integer idHopital) { this.idHopital = idHopital; }

    public String getHospitalName() { return hospitalName; }
    public void setHospitalName(String hospitalName) { this.hospitalName = hospitalName; }

    public String getPlanNom() { return planNom; }
    public void setPlanNom(String planNom) { this.planNom = planNom; }

    public BigDecimal getMontantMensuel() { return montantMensuel; }
    public void setMontantMensuel(BigDecimal montantMensuel) { this.montantMensuel = montantMensuel; }

    public String getStatut() { return statut; }
    public void setStatut(String statut) { this.statut = statut; }

    public String getUiStatus() { return uiStatus; }
    public void setUiStatus(String uiStatus) { this.uiStatus = uiStatus; }

    public LocalDateTime getDateDebut() { return dateDebut; }
    public void setDateDebut(LocalDateTime dateDebut) { this.dateDebut = dateDebut; }

    public LocalDateTime getDateFin() { return dateFin; }
    public void setDateFin(LocalDateTime dateFin) { this.dateFin = dateFin; }

    public Integer getDaysUntilDue() { return daysUntilDue; }
    public void setDaysUntilDue(Integer daysUntilDue) { this.daysUntilDue = daysUntilDue; }

    public boolean isNeedsPayment() { return needsPayment; }
    public void setNeedsPayment(boolean needsPayment) { this.needsPayment = needsPayment; }

    public Integer getMaxUsers() { return maxUsers; }
    public void setMaxUsers(Integer maxUsers) { this.maxUsers = maxUsers; }

    public Integer getCurrentUserCount() { return currentUserCount; }
    public void setCurrentUserCount(Integer currentUserCount) { this.currentUserCount = currentUserCount; }

    public java.util.List<String> getFeatures() { return features; }
    public void setFeatures(java.util.List<String> features) { this.features = features; }

    public Integer getTeleconsultationMonthlyLimit() { return teleconsultationMonthlyLimit; }
    public void setTeleconsultationMonthlyLimit(Integer teleconsultationMonthlyLimit) {
        this.teleconsultationMonthlyLimit = teleconsultationMonthlyLimit;
    }

    public Integer getTeleconsultationUsedThisMonth() { return teleconsultationUsedThisMonth; }
    public void setTeleconsultationUsedThisMonth(Integer teleconsultationUsedThisMonth) {
        this.teleconsultationUsedThisMonth = teleconsultationUsedThisMonth;
    }

    public String getTargetAudienceFr() { return targetAudienceFr; }
    public void setTargetAudienceFr(String targetAudienceFr) { this.targetAudienceFr = targetAudienceFr; }

    public String getTargetAudienceEn() { return targetAudienceEn; }
    public void setTargetAudienceEn(String targetAudienceEn) { this.targetAudienceEn = targetAudienceEn; }
}
