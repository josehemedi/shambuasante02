package hospicloud.enumeration;

public enum StatutAntecedent {
	ACTIF,
    GUERI,
    CHRONIQUE;
    public StatutAntecedent toggle() {
        return switch (this) {
            case ACTIF -> CHRONIQUE;
            case CHRONIQUE -> ACTIF;
            case GUERI -> ACTIF;
        };
    }

}
