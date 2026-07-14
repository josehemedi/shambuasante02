package hospicloud.dtos.archive;

public class ArchiveStatistiquesDto {

    private long aVerifier;
    private long incomplets;
    private long pretAArchiver;
    private long archives;
    private long archivesAujourdhui;
    private long archivesCeMois;
    private Double tempsMoyenAvantArchivageJours;

    public long getAVerifier() { return aVerifier; }
    public void setAVerifier(long aVerifier) { this.aVerifier = aVerifier; }

    public long getIncomplets() { return incomplets; }
    public void setIncomplets(long incomplets) { this.incomplets = incomplets; }

    public long getPretAArchiver() { return pretAArchiver; }
    public void setPretAArchiver(long pretAArchiver) { this.pretAArchiver = pretAArchiver; }

    public long getArchives() { return archives; }
    public void setArchives(long archives) { this.archives = archives; }

    public long getArchivesAujourdhui() { return archivesAujourdhui; }
    public void setArchivesAujourdhui(long archivesAujourdhui) { this.archivesAujourdhui = archivesAujourdhui; }

    public long getArchivesCeMois() { return archivesCeMois; }
    public void setArchivesCeMois(long archivesCeMois) { this.archivesCeMois = archivesCeMois; }

    public Double getTempsMoyenAvantArchivageJours() { return tempsMoyenAvantArchivageJours; }
    public void setTempsMoyenAvantArchivageJours(Double tempsMoyenAvantArchivageJours) {
        this.tempsMoyenAvantArchivageJours = tempsMoyenAvantArchivageJours;
    }
}
