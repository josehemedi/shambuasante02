package hospicloud.dtos.archive;

import hospicloud.model.archive.StatutDemandeAccesArchive;

import java.time.LocalDateTime;

public class DemandeAccesArchiveDto {

    private Long id;
    private Long archiveId;
    private Integer demandeurId;
    private String nomDemandeur;
    private String motif;
    private StatutDemandeAccesArchive statut;
    private LocalDateTime dateDemande;
    private Integer traitePar;
    private String nomTraitePar;
    private LocalDateTime dateTraitement;
    private String observation;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getArchiveId() { return archiveId; }
    public void setArchiveId(Long archiveId) { this.archiveId = archiveId; }

    public Integer getDemandeurId() { return demandeurId; }
    public void setDemandeurId(Integer demandeurId) { this.demandeurId = demandeurId; }

    public String getNomDemandeur() { return nomDemandeur; }
    public void setNomDemandeur(String nomDemandeur) { this.nomDemandeur = nomDemandeur; }

    public String getMotif() { return motif; }
    public void setMotif(String motif) { this.motif = motif; }

    public StatutDemandeAccesArchive getStatut() { return statut; }
    public void setStatut(StatutDemandeAccesArchive statut) { this.statut = statut; }

    public LocalDateTime getDateDemande() { return dateDemande; }
    public void setDateDemande(LocalDateTime dateDemande) { this.dateDemande = dateDemande; }

    public Integer getTraitePar() { return traitePar; }
    public void setTraitePar(Integer traitePar) { this.traitePar = traitePar; }

    public String getNomTraitePar() { return nomTraitePar; }
    public void setNomTraitePar(String nomTraitePar) { this.nomTraitePar = nomTraitePar; }

    public LocalDateTime getDateTraitement() { return dateTraitement; }
    public void setDateTraitement(LocalDateTime dateTraitement) { this.dateTraitement = dateTraitement; }

    public String getObservation() { return observation; }
    public void setObservation(String observation) { this.observation = observation; }
}
