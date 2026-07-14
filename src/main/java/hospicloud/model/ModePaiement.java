package hospicloud.model;

/**
 * Entité représentant les différents moyens de paiement acceptés par un hôpital.
 * Permet de supporter le Mobile Money (M-Pesa, Airtel Money), les espèces, etc.
 * * Développé par Siku Hemedi Jose - Projet Hospicloud.
 */
public class ModePaiement {

    private Integer idModePaiement;
    private Integer idHopital; // Isolation SaaS
    private String nomMode;    // ex: "M-Pesa", "Orange Money", "Espèces", "Carte Bancaire"
    private String description;

    // Constructeur par défaut
    public ModePaiement() {
    }

    // Constructeur complet
    public ModePaiement(Integer idModePaiement, Integer idHopital, String nomMode, String description) {
        this.idModePaiement = idModePaiement;
        this.idHopital = idHopital;
        this.nomMode = nomMode;
        this.description = description;
    }

    // Getters & Setters
    public Integer getIdModePaiement() {
        return idModePaiement;
    }

    public void setIdModePaiement(Integer idModePaiement) {
        this.idModePaiement = idModePaiement;
    }

    public Integer getIdHopital() {
        return idHopital;
    }

    public void setIdHopital(Integer idHopital) {
        this.idHopital = idHopital;
    }

    public String getNomMode() {
        return nomMode;
    }

    public void setNomMode(String nomMode) {
        this.nomMode = nomMode;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public String toString() {
        return "ModePaiement{" +
                "id=" + idModePaiement +
                ", mode='" + nomMode + '\'' +
                ", hopitalId=" + idHopital +
                '}';
    }
}