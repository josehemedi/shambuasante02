package hospicloud.dtos.patient;

public class DashboardStatsDTO {
    private long rendezVousCount;
    private long ordonnancesCount;
    private long rapportsCount;
    private double soldeA_Regler;

    public DashboardStatsDTO(long rendezVousCount, long ordonnancesCount, long rapportsCount, double soldeA_Regler) {
        this.rendezVousCount = rendezVousCount;
        this.ordonnancesCount = ordonnancesCount;
        this.rapportsCount = rapportsCount;
        this.soldeA_Regler = soldeA_Regler;
    }

    public long getRendezVousCount() { return rendezVousCount; }
    public void setRendezVousCount(long rendezVousCount) { this.rendezVousCount = rendezVousCount; }

    public long getOrdonnancesCount() { return ordonnancesCount; }
    public void setOrdonnancesCount(long ordonnancesCount) { this.ordonnancesCount = ordonnancesCount; }

    public long getRapportsCount() { return rapportsCount; }
    public void setRapportsCount(long rapportsCount) { this.rapportsCount = rapportsCount; }

    public double getSoldeA_Regler() { return soldeA_Regler; }
    public void setSoldeA_Regler(double soldeA_Regler) { this.soldeA_Regler = soldeA_Regler; }
}
