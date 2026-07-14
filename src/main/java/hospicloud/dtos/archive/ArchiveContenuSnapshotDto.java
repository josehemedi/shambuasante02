package hospicloud.dtos.archive;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Instantané immuable du dossier patient figé au moment de l'archivage.
 */
public class ArchiveContenuSnapshotDto {

    private String version = "1.0";
    private LocalDateTime captureAt;
    private Long archiveId;
    private String typeEpisode;
    private Long episodeId;
    private Integer hopitalId;
    private Map<String, Object> patient;
    private List<Map<String, Object>> rendezVous = new ArrayList<>();
    private List<Map<String, Object>> consultations = new ArrayList<>();
    private List<Map<String, Object>> antecedents = new ArrayList<>();
    private List<Map<String, Object>> bonsSortie = new ArrayList<>();
    private List<Map<String, Object>> ordonnances = new ArrayList<>();
    private List<Map<String, Object>> admissions = new ArrayList<>();

    public String getVersion() { return version; }
    public void setVersion(String version) { this.version = version; }

    public LocalDateTime getCaptureAt() { return captureAt; }
    public void setCaptureAt(LocalDateTime captureAt) { this.captureAt = captureAt; }

    public Long getArchiveId() { return archiveId; }
    public void setArchiveId(Long archiveId) { this.archiveId = archiveId; }

    public String getTypeEpisode() { return typeEpisode; }
    public void setTypeEpisode(String typeEpisode) { this.typeEpisode = typeEpisode; }

    public Long getEpisodeId() { return episodeId; }
    public void setEpisodeId(Long episodeId) { this.episodeId = episodeId; }

    public Integer getHopitalId() { return hopitalId; }
    public void setHopitalId(Integer hopitalId) { this.hopitalId = hopitalId; }

    public Map<String, Object> getPatient() { return patient; }
    public void setPatient(Map<String, Object> patient) { this.patient = patient; }

    public List<Map<String, Object>> getRendezVous() { return rendezVous; }
    public void setRendezVous(List<Map<String, Object>> rendezVous) {
        this.rendezVous = rendezVous != null ? rendezVous : new ArrayList<>();
    }

    public List<Map<String, Object>> getConsultations() { return consultations; }
    public void setConsultations(List<Map<String, Object>> consultations) {
        this.consultations = consultations != null ? consultations : new ArrayList<>();
    }

    public List<Map<String, Object>> getAntecedents() { return antecedents; }
    public void setAntecedents(List<Map<String, Object>> antecedents) {
        this.antecedents = antecedents != null ? antecedents : new ArrayList<>();
    }

    public List<Map<String, Object>> getBonsSortie() { return bonsSortie; }
    public void setBonsSortie(List<Map<String, Object>> bonsSortie) {
        this.bonsSortie = bonsSortie != null ? bonsSortie : new ArrayList<>();
    }

    public List<Map<String, Object>> getOrdonnances() { return ordonnances; }
    public void setOrdonnances(List<Map<String, Object>> ordonnances) {
        this.ordonnances = ordonnances != null ? ordonnances : new ArrayList<>();
    }

    public List<Map<String, Object>> getAdmissions() { return admissions; }
    public void setAdmissions(List<Map<String, Object>> admissions) {
        this.admissions = admissions != null ? admissions : new ArrayList<>();
    }
}
