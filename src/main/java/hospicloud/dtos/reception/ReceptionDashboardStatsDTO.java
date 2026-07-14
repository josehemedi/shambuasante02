package hospicloud.dtos.reception;

public class ReceptionDashboardStatsDTO {
    private long rendezVousJour;
    private long patientsEnAttente;
    private long patientsEnregistres;
    private long nouvellesInscriptions;
    private long deltaRendezVousJour;
    private long deltaPatientsEnAttente;
    private long deltaPatientsEnregistres;
    private long deltaNouvellesInscriptions;

    public ReceptionDashboardStatsDTO() {}

    public ReceptionDashboardStatsDTO(
            long rendezVousJour,
            long patientsEnAttente,
            long patientsEnregistres,
            long nouvellesInscriptions) {
        this(rendezVousJour, patientsEnAttente, patientsEnregistres, nouvellesInscriptions, 0, 0, 0, 0);
    }

    public ReceptionDashboardStatsDTO(
            long rendezVousJour,
            long patientsEnAttente,
            long patientsEnregistres,
            long nouvellesInscriptions,
            long deltaRendezVousJour,
            long deltaPatientsEnAttente,
            long deltaPatientsEnregistres,
            long deltaNouvellesInscriptions) {
        this.rendezVousJour = rendezVousJour;
        this.patientsEnAttente = patientsEnAttente;
        this.patientsEnregistres = patientsEnregistres;
        this.nouvellesInscriptions = nouvellesInscriptions;
        this.deltaRendezVousJour = deltaRendezVousJour;
        this.deltaPatientsEnAttente = deltaPatientsEnAttente;
        this.deltaPatientsEnregistres = deltaPatientsEnregistres;
        this.deltaNouvellesInscriptions = deltaNouvellesInscriptions;
    }

    public long getRendezVousJour() { return rendezVousJour; }
    public void setRendezVousJour(long rendezVousJour) { this.rendezVousJour = rendezVousJour; }

    public long getPatientsEnAttente() { return patientsEnAttente; }
    public void setPatientsEnAttente(long patientsEnAttente) { this.patientsEnAttente = patientsEnAttente; }

    public long getPatientsEnregistres() { return patientsEnregistres; }
    public void setPatientsEnregistres(long patientsEnregistres) { this.patientsEnregistres = patientsEnregistres; }

    public long getNouvellesInscriptions() { return nouvellesInscriptions; }
    public void setNouvellesInscriptions(long nouvellesInscriptions) { this.nouvellesInscriptions = nouvellesInscriptions; }

    public long getDeltaRendezVousJour() { return deltaRendezVousJour; }
    public void setDeltaRendezVousJour(long deltaRendezVousJour) { this.deltaRendezVousJour = deltaRendezVousJour; }

    public long getDeltaPatientsEnAttente() { return deltaPatientsEnAttente; }
    public void setDeltaPatientsEnAttente(long deltaPatientsEnAttente) { this.deltaPatientsEnAttente = deltaPatientsEnAttente; }

    public long getDeltaPatientsEnregistres() { return deltaPatientsEnregistres; }
    public void setDeltaPatientsEnregistres(long deltaPatientsEnregistres) { this.deltaPatientsEnregistres = deltaPatientsEnregistres; }

    public long getDeltaNouvellesInscriptions() { return deltaNouvellesInscriptions; }
    public void setDeltaNouvellesInscriptions(long deltaNouvellesInscriptions) { this.deltaNouvellesInscriptions = deltaNouvellesInscriptions; }
}
