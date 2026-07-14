package hospicloud.model;

/**
 * Entité représentant une action spécifique autorisée dans le système.
 * Utilisée pour le contrôle d'accès granulaire (RBAC).
 * * Développé par Siku Hemedi Jose - Projet Hospicloud.
 */
public class Permission {

    private Integer idPermission;
    private String nomPermission;  // Libellé lisible (ex: "Peut voir les factures")
    private String codePermission; // Code technique unique (ex: "BILL_READ", "OP_PRESCRIPTION_WRITE")

    // Constructeur par défaut
    public Permission() {
    }

    // Constructeur complet
    public Permission(Integer idPermission, String nomPermission, String codePermission) {
        this.idPermission = idPermission;
        this.nomPermission = nomPermission;
        this.codePermission = codePermission;
    }

    // Getters & Setters
    public Integer getIdPermission() {
        return idPermission;
    }

    public void setIdPermission(Integer idPermission) {
        this.idPermission = idPermission;
    }

    public String getNomPermission() {
        return nomPermission;
    }

    public void setNomPermission(String nomPermission) {
        this.nomPermission = nomPermission;
    }

    public String getCodePermission() {
        return codePermission;
    }

    public void setCodePermission(String codePermission) {
        this.codePermission = codePermission;
    }

    @Override
    public String toString() {
        return "Permission{" +
                "id=" + idPermission +
                ", code='" + codePermission + '\'' +
                '}';
    }
}