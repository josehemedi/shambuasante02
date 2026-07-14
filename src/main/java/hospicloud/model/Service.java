package hospicloud.model;

/**
 * Entité représentant une unité fonctionnelle au sein d'un département (ex: Soins Intensifs, Urgences).
 * Cette classe permet une organisation granulaire des soins dans Hospicloud.
 * * Développé par Siku Hemedi Jose - Projet UNIKIN / UPC.
 */
public class Service {

    private Integer idService;
    private Integer idHopital;
    private Integer idDepartement; // Clé étrangère vers la table departements
    private String nomService;
    private String telephoneInterne;

    // Constructeur par défaut
    public Service() {
    }

    // Constructeur complet
    public Service(Integer idService, Integer idHopital, Integer idDepartement, String nomService, String telephoneInterne) {
        this.idService = idService;
        this.idHopital = idHopital;
        this.idDepartement = idDepartement;
        this.nomService = nomService;
        this.telephoneInterne = telephoneInterne;
    }

    // Getters & Setters
    public Integer getIdService() {
        return idService;
    }

    public void setIdService(Integer idService) {
        this.idService = idService;
    }

    public Integer getIdHopital() {
        return idHopital;
    }

    public void setIdHopital(Integer idHopital) {
        this.idHopital = idHopital;
    }

    public Integer getIdDepartement() {
        return idDepartement;
    }

    public void setIdDepartement(Integer idDepartement) {
        this.idDepartement = idDepartement;
    }

    public String getNomService() {
        return nomService;
    }

    public void setNomService(String nomService) {
        this.nomService = nomService;
    }

    public String getTelephoneInterne() {
        return telephoneInterne;
    }

    public void setTelephoneInterne(String telephoneInterne) {
        this.telephoneInterne = telephoneInterne;
    }

    @Override
    public String toString() {
        return "Service{" +
                "id=" + idService +
                ", idDepartement=" + idDepartement +
                ", nom='" + nomService + '\'' +
                ", tel='" + telephoneInterne + '\'' +
                '}';
    }
}