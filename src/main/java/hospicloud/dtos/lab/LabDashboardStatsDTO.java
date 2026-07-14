package hospicloud.dtos.lab;

public class LabDashboardStatsDTO {
    private long enAttente;
    private long enCours;
    private long terminees;
    private long resultatsCritiquesNonAcquittes;

    public LabDashboardStatsDTO() {}

    public LabDashboardStatsDTO(long enAttente, long enCours, long terminees, long resultatsCritiquesNonAcquittes) {
        this.enAttente = enAttente;
        this.enCours = enCours;
        this.terminees = terminees;
        this.resultatsCritiquesNonAcquittes = resultatsCritiquesNonAcquittes;
    }

    public long getEnAttente() { return enAttente; }
    public void setEnAttente(long enAttente) { this.enAttente = enAttente; }

    public long getEnCours() { return enCours; }
    public void setEnCours(long enCours) { this.enCours = enCours; }

    public long getTerminees() { return terminees; }
    public void setTerminees(long terminees) { this.terminees = terminees; }

    public long getResultatsCritiquesNonAcquittes() { return resultatsCritiquesNonAcquittes; }
    public void setResultatsCritiquesNonAcquittes(long resultatsCritiquesNonAcquittes) { this.resultatsCritiquesNonAcquittes = resultatsCritiquesNonAcquittes; }
}
