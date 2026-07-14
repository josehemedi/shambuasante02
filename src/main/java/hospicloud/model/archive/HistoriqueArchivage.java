package hospicloud.model.archive;

import java.time.LocalDateTime;

public class HistoriqueArchivage {

    private Long id;
    private Integer hopitalId;
    private Long archiveId;
    private StatutArchive ancienStatut;
    private StatutArchive nouveauStatut;
    private String action;
    private String motif;
    private String observation;
    private Integer effectuePar;
    private LocalDateTime dateAction;
    private String adresseIp;
    private String userAgent;
    private String nomEffectuePar;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Integer getHopitalId() { return hopitalId; }
    public void setHopitalId(Integer hopitalId) { this.hopitalId = hopitalId; }

    public Long getArchiveId() { return archiveId; }
    public void setArchiveId(Long archiveId) { this.archiveId = archiveId; }

    public StatutArchive getAncienStatut() { return ancienStatut; }
    public void setAncienStatut(StatutArchive ancienStatut) { this.ancienStatut = ancienStatut; }

    public StatutArchive getNouveauStatut() { return nouveauStatut; }
    public void setNouveauStatut(StatutArchive nouveauStatut) { this.nouveauStatut = nouveauStatut; }

    public String getAction() { return action; }
    public void setAction(String action) { this.action = action; }

    public String getMotif() { return motif; }
    public void setMotif(String motif) { this.motif = motif; }

    public String getObservation() { return observation; }
    public void setObservation(String observation) { this.observation = observation; }

    public Integer getEffectuePar() { return effectuePar; }
    public void setEffectuePar(Integer effectuePar) { this.effectuePar = effectuePar; }

    public LocalDateTime getDateAction() { return dateAction; }
    public void setDateAction(LocalDateTime dateAction) { this.dateAction = dateAction; }

    public String getAdresseIp() { return adresseIp; }
    public void setAdresseIp(String adresseIp) { this.adresseIp = adresseIp; }

    public String getUserAgent() { return userAgent; }
    public void setUserAgent(String userAgent) { this.userAgent = userAgent; }

    public String getNomEffectuePar() { return nomEffectuePar; }
    public void setNomEffectuePar(String nomEffectuePar) { this.nomEffectuePar = nomEffectuePar; }
}
