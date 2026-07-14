package hospicloud.model;

public enum Role {
    SUPER_ADMIN,
    TENANT_ADMIN,
    MEDECIN,
    RECEPTION,
    PATIENT,
    LABORANTIN,
    CAISSIER,
    ARCHIVISTE,
    USER;

    public static Role fromDatabaseValue(String raw) {
        if (raw == null || raw.isBlank()) {
            throw new IllegalArgumentException("Rôle utilisateur vide");
        }

        String normalized = raw.trim().toUpperCase();
        if (normalized.startsWith("ROLE_")) {
            normalized = normalized.substring("ROLE_".length());
        }

        return switch (normalized) {
            case "RECEPTIONNISTE", "RECEPTIONIST" -> RECEPTION;
            case "DOCTOR" -> MEDECIN;
            case "LAB_TECH", "LABORATOIRE" -> LABORANTIN;
            case "HOSPITAL_ADMIN", "ADMIN" -> TENANT_ADMIN;
            case "CASHIER" -> CAISSIER;
            case "ARCHIVIST" -> ARCHIVISTE;
            default -> valueOf(normalized);
        };
    }
}
