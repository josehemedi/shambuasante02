package hospicloud.model;

import java.math.BigDecimal;

/**
 * Entité représentant une fonctionnalité optionnelle du SaaS Hospicloud.
 * Permet de gérer la facturation des abonnements pour chaque établissement.
 * * Développé par Siku Hemedi Jose - Projet Hospicloud.
 */
public class Module {

    private Integer idModule;
    private String nomModule; // Ex: "Pharmacie", "Laboratoire", "Télémédecine"
    private String description;
    private BigDecimal prixAbonnementMensuel;

    // Constructeur par défaut
    public Module() {
    }

    // Constructeur complet
    public Module(Integer idModule, String nomModule, String description, BigDecimal prixAbonnementMensuel) {
        this.idModule = idModule;
        this.nomModule = nomModule;
        this.description = description;
        this.prixAbonnementMensuel = prixAbonnementMensuel;
    }

    // Getters & Setters
    public Integer getIdModule() {
        return idModule;
    }

    public void setIdModule(Integer idModule) {
        this.idModule = idModule;
    }

    public String getNomModule() {
        return nomModule;
    }

    public void setNomModule(String nomModule) {
        this.nomModule = nomModule;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public BigDecimal getPrixAbonnementMensuel() {
        return prixAbonnementMensuel;
    }

    public void setPrixAbonnementMensuel(BigDecimal prixAbonnementMensuel) {
        this.prixAbonnementMensuel = prixAbonnementMensuel;
    }

    @Override
    public String toString() {
        return "Module{" +
                "id=" + idModule +
                ", nom='" + nomModule + '\'' +
                ", prix=" + prixAbonnementMensuel +
                '}';
    }
}