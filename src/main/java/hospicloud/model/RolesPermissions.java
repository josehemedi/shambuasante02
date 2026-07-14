package hospicloud.model;

/**
 * Table de liaison entre les Rôles et les Permissions.
 * Définit quelles actions (permissions) sont autorisées pour chaque fonction (rôle).
 * * Développé par Siku Hemedi Jose - Projet Hospicloud.
 */
public class RolesPermissions {

    private Integer idRole;
    private Integer idPermission;

    // Constructeur par défaut
    public RolesPermissions() {
    }

    // Constructeur complet
    public RolesPermissions(Integer idRole, Integer idPermission) {
        this.idRole = idRole;
        this.idPermission = idPermission;
    }

    // Getters & Setters
    public Integer getIdRole() {
        return idRole;
    }

    public void setIdRole(Integer idRole) {
        this.idRole = idRole;
    }

    public Integer getIdPermission() {
        return idPermission;
    }

    public void setIdPermission(Integer idPermission) {
        this.idPermission = idPermission;
    }

    @Override
    public String toString() {
        return "RolesPermissions{" +
                "roleId=" + idRole +
                ", permissionId=" + idPermission +
                '}';
    }
}