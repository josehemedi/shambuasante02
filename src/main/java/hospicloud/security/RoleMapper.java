package hospicloud.security;

import hospicloud.model.Role;

public final class RoleMapper {

    private RoleMapper() {}

    public static String toFrontendRole(Role role) {
        if (role == null) {
            return null;
        }
        return switch (role) {
            case SUPER_ADMIN -> "superadmin";
            case TENANT_ADMIN -> "hospital_admin";
            case MEDECIN -> "doctor";
            case RECEPTION -> "receptionist";
            case PATIENT -> "patient";
            case LABORANTIN -> "lab_tech";
            case CAISSIER -> "cashier";
            case ARCHIVISTE -> "archivist";
            case USER -> "user";
        };
    }
}
