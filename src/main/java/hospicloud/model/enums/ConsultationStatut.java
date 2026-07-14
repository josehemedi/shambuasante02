package hospicloud.model.enums;

public enum ConsultationStatut {
    BROUILLON,
    SIGNEE,
    ANNULEE;

    public static ConsultationStatut fromDb(String value) {
        if (value == null || value.isBlank()) {
            return BROUILLON;
        }
        return ConsultationStatut.valueOf(value.trim().toUpperCase());
    }
}
