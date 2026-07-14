package hospicloud.model.archive;

import java.time.LocalDateTime;

public class ArchiveDossier {

    private Long id;
    private Integer hopitalId;
    private Long patientId;
    private TypeEpisode typeEpisode;
    private Long episodeId;
    private StatutArchive statutArchive;
    private LocalDateTime dateFinEpisode;
    private LocalDateTime dateDemandeArchivage;
    private LocalDateTime dateArchivage;
    private Integer archivePar;
    private Integer verifiePar;
    private String motifArchivage;
    private String observation;
    private boolean dossierComplet;
    private String emplacementPhysique;
    private String numeroBoiteArchive;
    private String numeroRayon;
    private LocalDateTime dateRestauration;
    private Integer restaurePar;
    private String motifRestauration;
    private int version;
    private Integer idMedecin;
    private Integer idService;
    private Long dossierVirtuelId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Champs enrichis (jointures)
    private String nomPatient;
    private String numeroDossier;
    private String nomMedecin;
    private String nomArchiviste;
    private String nomVerificateur;
    private String nomDossierVirtuel;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Integer getHopitalId() { return hopitalId; }
    public void setHopitalId(Integer hopitalId) { this.hopitalId = hopitalId; }

    public Long getPatientId() { return patientId; }
    public void setPatientId(Long patientId) { this.patientId = patientId; }

    public TypeEpisode getTypeEpisode() { return typeEpisode; }
    public void setTypeEpisode(TypeEpisode typeEpisode) { this.typeEpisode = typeEpisode; }

    public Long getEpisodeId() { return episodeId; }
    public void setEpisodeId(Long episodeId) { this.episodeId = episodeId; }

    public StatutArchive getStatutArchive() { return statutArchive; }
    public void setStatutArchive(StatutArchive statutArchive) { this.statutArchive = statutArchive; }

    public LocalDateTime getDateFinEpisode() { return dateFinEpisode; }
    public void setDateFinEpisode(LocalDateTime dateFinEpisode) { this.dateFinEpisode = dateFinEpisode; }

    public LocalDateTime getDateDemandeArchivage() { return dateDemandeArchivage; }
    public void setDateDemandeArchivage(LocalDateTime dateDemandeArchivage) { this.dateDemandeArchivage = dateDemandeArchivage; }

    public LocalDateTime getDateArchivage() { return dateArchivage; }
    public void setDateArchivage(LocalDateTime dateArchivage) { this.dateArchivage = dateArchivage; }

    public Integer getArchivePar() { return archivePar; }
    public void setArchivePar(Integer archivePar) { this.archivePar = archivePar; }

    public Integer getVerifiePar() { return verifiePar; }
    public void setVerifiePar(Integer verifiePar) { this.verifiePar = verifiePar; }

    public String getMotifArchivage() { return motifArchivage; }
    public void setMotifArchivage(String motifArchivage) { this.motifArchivage = motifArchivage; }

    public String getObservation() { return observation; }
    public void setObservation(String observation) { this.observation = observation; }

    public boolean isDossierComplet() { return dossierComplet; }
    public void setDossierComplet(boolean dossierComplet) { this.dossierComplet = dossierComplet; }

    public String getEmplacementPhysique() { return emplacementPhysique; }
    public void setEmplacementPhysique(String emplacementPhysique) { this.emplacementPhysique = emplacementPhysique; }

    public String getNumeroBoiteArchive() { return numeroBoiteArchive; }
    public void setNumeroBoiteArchive(String numeroBoiteArchive) { this.numeroBoiteArchive = numeroBoiteArchive; }

    public String getNumeroRayon() { return numeroRayon; }
    public void setNumeroRayon(String numeroRayon) { this.numeroRayon = numeroRayon; }

    public LocalDateTime getDateRestauration() { return dateRestauration; }
    public void setDateRestauration(LocalDateTime dateRestauration) { this.dateRestauration = dateRestauration; }

    public Integer getRestaurePar() { return restaurePar; }
    public void setRestaurePar(Integer restaurePar) { this.restaurePar = restaurePar; }

    public String getMotifRestauration() { return motifRestauration; }
    public void setMotifRestauration(String motifRestauration) { this.motifRestauration = motifRestauration; }

    public int getVersion() { return version; }
    public void setVersion(int version) { this.version = version; }

    public Integer getIdMedecin() { return idMedecin; }
    public void setIdMedecin(Integer idMedecin) { this.idMedecin = idMedecin; }

    public Integer getIdService() { return idService; }
    public void setIdService(Integer idService) { this.idService = idService; }

    public Long getDossierVirtuelId() { return dossierVirtuelId; }
    public void setDossierVirtuelId(Long dossierVirtuelId) { this.dossierVirtuelId = dossierVirtuelId; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public String getNomPatient() { return nomPatient; }
    public void setNomPatient(String nomPatient) { this.nomPatient = nomPatient; }

    public String getNumeroDossier() { return numeroDossier; }
    public void setNumeroDossier(String numeroDossier) { this.numeroDossier = numeroDossier; }

    public String getNomMedecin() { return nomMedecin; }
    public void setNomMedecin(String nomMedecin) { this.nomMedecin = nomMedecin; }

    public String getNomArchiviste() { return nomArchiviste; }
    public void setNomArchiviste(String nomArchiviste) { this.nomArchiviste = nomArchiviste; }

    public String getNomVerificateur() { return nomVerificateur; }
    public void setNomVerificateur(String nomVerificateur) { this.nomVerificateur = nomVerificateur; }

    public String getNomDossierVirtuel() { return nomDossierVirtuel; }
    public void setNomDossierVirtuel(String nomDossierVirtuel) { this.nomDossierVirtuel = nomDossierVirtuel; }
}
