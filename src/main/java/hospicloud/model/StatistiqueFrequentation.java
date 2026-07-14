package hospicloud.model;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Entité stockant les agrégats journaliers de l'activité hospitalière.
 * Utilisée pour le reporting historique et l'analyse des tendances de fréquentation.
 * * Développé par Siku Hemedi Jose - Projet Hospicloud.
 */
public class StatistiqueFrequentation {

    private Integer idStat;
    private Integer idHopital;
    private LocalDate dateStat;
    private Integer nombreConsultations;
    private Integer nombreHospitalisations;
    private Integer nombreNouveauxPatients;
    private BigDecimal chiffreAffaireJournalier;

    // Constructeur par défaut
    public StatistiqueFrequentation() {
    }

    // Constructeur complet
    public StatistiqueFrequentation(Integer idStat, Integer idHopital, LocalDate dateStat, 
                                   Integer nombreConsultations, Integer nombreHospitalisations, 
                                   Integer nombreNouveauxPatients, BigDecimal chiffreAffaireJournalier) {
        this.idStat = idStat;
        this.idHopital = idHopital;
        this.dateStat = dateStat;
        this.nombreConsultations = nombreConsultations;
        this.nombreHospitalisations = nombreHospitalisations;
        this.nombreNouveauxPatients = nombreNouveauxPatients;
        this.chiffreAffaireJournalier = chiffreAffaireJournalier;
    }

    // Getters & Setters
    public Integer getIdStat() { return idStat; }
    public void setIdStat(Integer idStat) { this.idStat = idStat; }

    public Integer getIdHopital() { return idHopital; }
    public void setIdHopital(Integer idHopital) { this.idHopital = idHopital; }

    public LocalDate getDateStat() { return dateStat; }
    public void setDateStat(LocalDate dateStat) { this.dateStat = dateStat; }

    public Integer getNombreConsultations() { return nombreConsultations; }
    public void setNombreConsultations(Integer nombreConsultations) { this.nombreConsultations = nombreConsultations; }

    public Integer getNombreHospitalisations() { return nombreHospitalisations; }
    public void setNombreHospitalisations(Integer nombreHospitalisations) { this.nombreHospitalisations = nombreHospitalisations; }

    public Integer getNombreNouveauxPatients() { return nombreNouveauxPatients; }
    public void setNombreNouveauxPatients(Integer nombreNouveauxPatients) { this.nombreNouveauxPatients = nombreNouveauxPatients; }

    public BigDecimal getChiffreAffaireJournalier() { return chiffreAffaireJournalier; }
    public void setChiffreAffaireJournalier(BigDecimal chiffreAffaireJournalier) { this.chiffreAffaireJournalier = chiffreAffaireJournalier; }

    @Override
    public String toString() {
        return "Stat{" +
                "date=" + dateStat +
                ", consultations=" + nombreConsultations +
                ", CA=" + chiffreAffaireJournalier +
                '}';
    }
}