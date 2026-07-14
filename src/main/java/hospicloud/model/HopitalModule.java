package hospicloud.model;

import java.time.LocalDate;

/**
 * Table de liaison gérant les abonnements des hôpitaux aux différents modules.
 * Permet d'activer ou de désactiver des fonctionnalités (ex: Pharmacie) selon le contrat.
 * * Développé par Siku Hemedi Jose - Projet Hospicloud.
 */
public class HopitalModule {

    private Integer idHopital;
    private Integer idModule;
    private LocalDate dateActivation;
    private LocalDate dateExpiration;
    private boolean estActif;

    // Constructeur par défaut
    public HopitalModule() {
    }

    // Constructeur complet
    public HopitalModule(Integer idHopital, Integer idModule, LocalDate dateActivation, 
                         LocalDate dateExpiration, boolean estActif) {
        this.idHopital = idHopital;
        this.idModule = idModule;
        this.dateActivation = dateActivation;
        this.dateExpiration = dateExpiration;
        this.estActif = estActif;
    }

    // Getters & Setters
    public Integer getIdHopital() {
        return idHopital;
    }

    public void setIdHopital(Integer idHopital) {
        this.idHopital = idHopital;
    }

    public Integer getIdModule() {
        return idModule;
    }

    public void setIdModule(Integer idModule) {
        this.idModule = idModule;
    }

    public LocalDate getDateActivation() {
        return dateActivation;
    }

    public void setDateActivation(LocalDate dateActivation) {
        this.dateActivation = dateActivation;
    }

    public LocalDate getDateExpiration() {
        return dateExpiration;
    }

    public void setDateExpiration(LocalDate dateExpiration) {
        this.dateExpiration = dateExpiration;
    }

    public boolean isEstActif() {
        return estActif;
    }

    public void setEstActif(boolean estActif) {
        this.estActif = estActif;
    }

    @Override
    public String toString() {
        return "HopitalModule{" +
                "hopitalId=" + idHopital +
                ", moduleId=" + idModule +
                ", actif=" + estActif +
                ", expireLe=" + dateExpiration +
                '}';
    }
}