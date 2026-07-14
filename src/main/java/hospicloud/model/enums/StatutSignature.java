package hospicloud.model.enums;

public enum StatutSignature {
    VALIDEE,
    ANNULEE,
    INVALIDE;

    public String toDbValue() {
        return switch (this) {
            case VALIDEE -> "SIGNE";
            case ANNULEE -> "ANNULE";
            case INVALIDE -> "INVALIDE";
        };
    }

    public static StatutSignature fromDb(String value) {
        if (value == null || value.isBlank()) {
            return VALIDEE;
        }
        return switch (value.trim().toUpperCase()) {
            case "SIGNE" -> VALIDEE;
            case "ANNULE" -> ANNULEE;
            case "INVALIDE" -> INVALIDE;
            default -> StatutSignature.valueOf(value.trim().toUpperCase());
        };
    }
}
