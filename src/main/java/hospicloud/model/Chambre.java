package hospicloud.model;

import java.math.BigDecimal;

/**
 * Entité représentant une chambre d'hospitalisation au sein d'un service.
 * Permet de gérer la tarification et la localisation (étage) des lits.
 * * Développé par Siku Hemedi Jose - Projet Hospicloud.
 */
public class Chambre {

    private Integer idChambre;
    private Integer idHopital;
    private Integer idService; // Clé étrangère vers la table services
    private String numeroChambre;
    private String typeChambre; // PRIVE, COMMUNE, VIP, REANIMATION
    private Integer etage;
    private BigDecimal prixJournalier;

    // Constructeur par défaut
    public Chambre() {
    }

    // Constructeur complet
    public Chambre(Integer idChambre, Integer idHopital, Integer idService, String numeroChambre, 
                   String typeChambre, Integer etage, BigDecimal prixJournalier) {
        this.idChambre = idChambre;
        this.idHopital = idHopital;
        this.idService = idService;
        this.numeroChambre = numeroChambre;
        this.typeChambre = typeChambre;
        this.etage = etage;
        this.prixJournalier = prixJournalier;
    }

    // Getters & Setters
    public Integer getIdChambre() {
        return idChambre;
    }

    public void setIdChambre(Integer idChambre) {
        this.idChambre = idChambre;
    }

    public Integer getIdHopital() {
        return idHopital;
    }

    public void setIdHopital(Integer idHopital) {
        this.idHopital = idHopital;
    }

    public Integer getIdService() {
        return idService;
    }

    public void setIdService(Integer idService) {
        this.idService = idService;
    }

    public String getNumeroChambre() {
        return numeroChambre;
    }

    public void setNumeroChambre(String numeroChambre) {
        this.numeroChambre = numeroChambre;
    }

    public String getTypeChambre() {
        return typeChambre;
    }

    public void setTypeChambre(String typeChambre) {
        this.typeChambre = typeChambre;
    }

    public Integer getEtage() {
        return etage;
    }

    public void setEtage(Integer etage) {
        this.etage = etage;
    }

    public BigDecimal getPrixJournalier() {
        return prixJournalier;
    }

    public void setPrixJournalier(BigDecimal prixJournalier) {
        this.prixJournalier = prixJournalier;
    }

    @Override
    public String toString() {
        return "Chambre{" +
                "id=" + idChambre +
                ", numero='" + numeroChambre + '\'' +
                ", type='" + typeChambre + '\'' +
                ", prix=" + prixJournalier +
                '}';
    }
}