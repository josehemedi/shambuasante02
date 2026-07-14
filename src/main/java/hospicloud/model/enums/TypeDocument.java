package hospicloud.model.enums;

public enum TypeDocument {
    CONSULTATION,
    ORDONNANCE,
    DEMANDE_EXAMEN,
    COMPTE_RENDU,
    CERTIFICAT_MEDICAL;

    public static TypeDocument fromDb(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Type de document requis.");
        }
        return TypeDocument.valueOf(value.trim().toUpperCase());
    }
}
