package hospicloud.dtos;

import java.math.BigDecimal;

public class DashboardStatsDTO {
    private Long hopitauxActifs;
    private Long utilisateursActifs;
    private BigDecimal mrr;
    private BigDecimal croissanceSaaS;
    private BigDecimal pourcentageCroissanceHopitaux;
    private BigDecimal pourcentageCroissanceUtilisateurs;
    private BigDecimal pourcentageCroissanceMrr;

    public DashboardStatsDTO() {
    }

    public DashboardStatsDTO(Long hopitauxActifs, Long utilisateursActifs, BigDecimal mrr,
                             BigDecimal croissanceSaaS, BigDecimal pourcentageCroissanceHopitaux,
                             BigDecimal pourcentageCroissanceUtilisateurs, BigDecimal pourcentageCroissanceMrr) {
        this.hopitauxActifs = hopitauxActifs;
        this.utilisateursActifs = utilisateursActifs;
        this.mrr = mrr;
        this.croissanceSaaS = croissanceSaaS;
        this.pourcentageCroissanceHopitaux = pourcentageCroissanceHopitaux;
        this.pourcentageCroissanceUtilisateurs = pourcentageCroissanceUtilisateurs;
        this.pourcentageCroissanceMrr = pourcentageCroissanceMrr;
    }

    public Long getHopitauxActifs() { return hopitauxActifs; }
    public void setHopitauxActifs(Long hopitauxActifs) { this.hopitauxActifs = hopitauxActifs; }

    public Long getUtilisateursActifs() { return utilisateursActifs; }
    public void setUtilisateursActifs(Long utilisateursActifs) { this.utilisateursActifs = utilisateursActifs; }

    public BigDecimal getMrr() { return mrr; }
    public void setMrr(BigDecimal mrr) { this.mrr = mrr; }

    public BigDecimal getCroissanceSaaS() { return croissanceSaaS; }
    public void setCroissanceSaaS(BigDecimal croissanceSaaS) { this.croissanceSaaS = croissanceSaaS; }

    public BigDecimal getPourcentageCroissanceHopitaux() { return pourcentageCroissanceHopitaux; }
    public void setPourcentageCroissanceHopitaux(BigDecimal pourcentageCroissanceHopitaux) { this.pourcentageCroissanceHopitaux = pourcentageCroissanceHopitaux; }

    public BigDecimal getPourcentageCroissanceUtilisateurs() { return pourcentageCroissanceUtilisateurs; }
    public void setPourcentageCroissanceUtilisateurs(BigDecimal pourcentageCroissanceUtilisateurs) { this.pourcentageCroissanceUtilisateurs = pourcentageCroissanceUtilisateurs; }

    public BigDecimal getPourcentageCroissanceMrr() { return pourcentageCroissanceMrr; }
    public void setPourcentageCroissanceMrr(BigDecimal pourcentageCroissanceMrr) { this.pourcentageCroissanceMrr = pourcentageCroissanceMrr; }
}
