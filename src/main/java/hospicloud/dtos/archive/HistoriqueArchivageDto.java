package hospicloud.dtos.archive;

import hospicloud.model.archive.StatutArchive;
import hospicloud.model.archive.StatutDemandeAccesArchive;

import java.time.LocalDateTime;

public class HistoriqueArchivageDto {

    private Long id;
    private Long archiveId;
    private StatutArchive ancienStatut;
    private StatutArchive nouveauStatut;
    private String action;
    private String motif;
    private String observation;
    private Integer effectuePar;
    private String nomEffectuePar;
    private LocalDateTime dateAction;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

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

    public String getNomEffectuePar() { return nomEffectuePar; }
    public void setNomEffectuePar(String nomEffectuePar) { this.nomEffectuePar = nomEffectuePar; }

    public LocalDateTime getDateAction() { return dateAction; }
    public void setDateAction(LocalDateTime dateAction) { this.dateAction = dateAction; }
}
