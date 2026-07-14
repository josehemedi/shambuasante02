package hospicloud.model.archive;

public enum StatutArchive {
    ACTIF,
    EN_COURS,
    TERMINE,
    A_VERIFIER,
    INCOMPLET,
    PRET_A_ARCHIVER,
    ARCHIVE,
    RESTAURE;

    public boolean isLocked() {
        return this == ARCHIVE;
    }

    public boolean allowsMedicalEdit() {
        return this != ARCHIVE && this != RESTAURE;
    }
}
