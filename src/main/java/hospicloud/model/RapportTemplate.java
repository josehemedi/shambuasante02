package hospicloud.model;

import java.time.LocalDateTime;

/**
 * Entité gérant les modèles de rapports JasperReports (.jrxml).
 * Permet une gestion dynamique des impressions (Factures, Ordonnances, Bilans).
 * * Développé par Siku Hemedi Jose - Projet Hospicloud.
 */
public class RapportTemplate {

    private Integer idTemplate;
    private String nomRapport;        // ex: "Fiche de Consultation", "Reçu de Paiement"
    private String codeTemplate;       // ex: TPL_INV_001 (Utile pour appeler le rapport par code)
    private String cheminFichierJrxml; // Emplacement physique sur le serveur
    private String moduleAssocie;      // ex: LABORATOIRE, PHARMACIE, CAISSE
    private LocalDateTime dateCreation;

    // Constructeur par défaut
    public RapportTemplate() {
    }

    // Constructeur complet
    public RapportTemplate(Integer idTemplate, String nomRapport, String codeTemplate, 
                           String cheminFichierJrxml, String moduleAssocie, 
                           LocalDateTime dateCreation) {
        this.idTemplate = idTemplate;
        this.nomRapport = nomRapport;
        this.codeTemplate = codeTemplate;
        this.cheminFichierJrxml = cheminFichierJrxml;
        this.moduleAssocie = moduleAssocie;
        this.dateCreation = dateCreation;
    }

    // Getters & Setters
    public Integer getIdTemplate() { return idTemplate; }
    public void setIdTemplate(Integer idTemplate) { this.idTemplate = idTemplate; }

    public String getNomRapport() { return nomRapport; }
    public void setNomRapport(String nomRapport) { this.nomRapport = nomRapport; }

    public String getCodeTemplate() { return codeTemplate; }
    public void setCodeTemplate(String codeTemplate) { this.codeTemplate = codeTemplate; }

    public String getCheminFichierJrxml() { return cheminFichierJrxml; }
    public void setCheminFichierJrxml(String cheminFichierJrxml) { this.cheminFichierJrxml = cheminFichierJrxml; }

    public String getModuleAssocie() { return moduleAssocie; }
    public void setModuleAssocie(String moduleAssocie) { this.moduleAssocie = moduleAssocie; }

    public LocalDateTime getDateCreation() { return dateCreation; }
    public void setDateCreation(LocalDateTime dateCreation) { this.dateCreation = dateCreation; }

    @Override
    public String toString() {
        return "RapportTemplate{" +
                "id=" + idTemplate +
                ", code='" + codeTemplate + '\'' +
                ", module='" + moduleAssocie + '\'' +
                '}';
    }
}