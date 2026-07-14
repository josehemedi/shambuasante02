package hospicloud.model.archive;

import java.time.LocalDateTime;

public class DemandeAccesArchive {

    private Long id;
    private Integer hopitalId;
    private Long archiveId;
    private Integer demandeurId;
    private String motif;
    private StatutDemandeAccesArchive statut;
    private LocalDateTime dateDemande;
    private Integer traitePar;
    private LocalDateTime dateTraitement;
    private String observation;
    private String nomDemandeur;
    private String nomTraitePar;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Integer getHopitalId() { return hopitalId; }
    public void setHopitalId(Integer hopitalId) { this.hopitalId = hopitalId; }

    public Long getArchiveId() { return archiveId; }
    public void setArchiveId(Long archiveId) { this.archiveId = archiveId; }

    public Integer getDemandeurId() { return demandeurId; }
    public void setDemandeurId(Integer demandeurId) { this.demandeurId = demandeurId; }

    public String getMotif() { return motif; }
    public void setMotif(String motif) { this.motif = motif; }

    public StatutDemandeAccesArchive getStatut() { return statut; }
    public void setStatut(StatutDemandeAccesArchive statut) { this.statut = statut; }

    public LocalDateTime getDateDemande() { return dateDemande; }
    public void setDateDemande(LocalDateTime dateDemande) { this.dateDemande = dateDemande; }

    public Integer getTraitePar() { return traitePar; }
    public void setTraitePar(Integer traitePar) { this.traitePar = traitePar; }

    public LocalDateTime getDateTraitement() { return dateTraitement; }
    public void setDateTraitement(LocalDateTime dateTraitement) { this.dateTraitement = dateTraitement; }

    public String getObservation() { return observation; }
    public void setObservation(String observation) { this.observation = observation; }

    public String getNomDemandeur() { return nomDemandeur; }
    public void setNomDemandeur(String nomDemandeur) { this.nomDemandeur = nomDemandeur; }

    public String getNomTraitePar() { return nomTraitePar; }
    public void setNomTraitePar(String nomTraitePar) { this.nomTraitePar = nomTraitePar; }
}
