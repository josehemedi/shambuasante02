package hospicloud.mappers;

import hospicloud.dtos.archive.ArchiveDossierResponseDto;
import hospicloud.dtos.archive.DemandeAccesArchiveDto;
import hospicloud.dtos.archive.HistoriqueArchivageDto;
import hospicloud.model.archive.ArchiveDossier;
import hospicloud.model.archive.DemandeAccesArchive;
import hospicloud.model.archive.HistoriqueArchivage;
import hospicloud.model.archive.StatutArchive;
import hospicloud.security.archive.ArchivePermissionService;

import java.util.ArrayList;
import java.util.List;

public final class ArchiveMapper {

    private ArchiveMapper() {}

    public static ArchiveDossierResponseDto toDto(ArchiveDossier archive,
                                                  ArchivePermissionService permissions) {
        ArchiveDossierResponseDto dto = new ArchiveDossierResponseDto();
        dto.setId(archive.getId());
        dto.setPatientId(archive.getPatientId());
        dto.setNomPatient(archive.getNomPatient());
        dto.setNumeroDossier(archive.getNumeroDossier());
        dto.setTypeEpisode(archive.getTypeEpisode());
        dto.setEpisodeId(archive.getEpisodeId());
        dto.setStatutArchive(archive.getStatutArchive());
        dto.setDateFinEpisode(archive.getDateFinEpisode());
        dto.setDateDemandeArchivage(archive.getDateDemandeArchivage());
        dto.setDateArchivage(archive.getDateArchivage());
        dto.setArchivePar(archive.getArchivePar());
        dto.setNomArchiviste(archive.getNomArchiviste());
        dto.setVerifiePar(archive.getVerifiePar());
        dto.setNomVerificateur(archive.getNomVerificateur());
        dto.setMotifArchivage(archive.getMotifArchivage());
        dto.setObservation(archive.getObservation());
        dto.setDossierComplet(archive.isDossierComplet());
        dto.setEmplacementPhysique(archive.getEmplacementPhysique());
        dto.setNumeroBoiteArchive(archive.getNumeroBoiteArchive());
        dto.setNumeroRayon(archive.getNumeroRayon());
        dto.setDateRestauration(archive.getDateRestauration());
        dto.setRestaurePar(archive.getRestaurePar());
        dto.setMotifRestauration(archive.getMotifRestauration());
        dto.setIdMedecin(archive.getIdMedecin());
        dto.setNomMedecin(archive.getNomMedecin());
        dto.setIdService(archive.getIdService());
        dto.setVersion(archive.getVersion());
        dto.setCreatedAt(archive.getCreatedAt());
        dto.setUpdatedAt(archive.getUpdatedAt());
        if (permissions != null) {
            dto.setActionsAutorisees(resolveActions(archive, permissions));
        }
        return dto;
    }

    public static HistoriqueArchivageDto toDto(HistoriqueArchivage h) {
        HistoriqueArchivageDto dto = new HistoriqueArchivageDto();
        dto.setId(h.getId());
        dto.setArchiveId(h.getArchiveId());
        dto.setAncienStatut(h.getAncienStatut());
        dto.setNouveauStatut(h.getNouveauStatut());
        dto.setAction(h.getAction());
        dto.setMotif(h.getMotif());
        dto.setObservation(h.getObservation());
        dto.setEffectuePar(h.getEffectuePar());
        dto.setNomEffectuePar(h.getNomEffectuePar());
        dto.setDateAction(h.getDateAction());
        return dto;
    }

    public static DemandeAccesArchiveDto toDto(DemandeAccesArchive d) {
        DemandeAccesArchiveDto dto = new DemandeAccesArchiveDto();
        dto.setId(d.getId());
        dto.setArchiveId(d.getArchiveId());
        dto.setDemandeurId(d.getDemandeurId());
        dto.setNomDemandeur(d.getNomDemandeur());
        dto.setMotif(d.getMotif());
        dto.setStatut(d.getStatut());
        dto.setDateDemande(d.getDateDemande());
        dto.setTraitePar(d.getTraitePar());
        dto.setNomTraitePar(d.getNomTraitePar());
        dto.setDateTraitement(d.getDateTraitement());
        dto.setObservation(d.getObservation());
        return dto;
    }

    private static List<String> resolveActions(ArchiveDossier archive, ArchivePermissionService permissions) {
        List<String> actions = new ArrayList<>();
        StatutArchive statut = archive.getStatutArchive();
        if (permissions.has(ArchivePermissionService.ARCHIVE_VERIFIER)
                && (statut == StatutArchive.A_VERIFIER || statut == StatutArchive.INCOMPLET)) {
            actions.add("VERIFIER");
            actions.add("MARQUER_INCOMPLET");
        }
        if (permissions.has(ArchivePermissionService.ARCHIVE_ARCHIVER)
                && statut == StatutArchive.PRET_A_ARCHIVER) {
            actions.add("ARCHIVER");
        }
        if (permissions.has(ArchivePermissionService.ARCHIVE_RESTAURER)
                && statut == StatutArchive.ARCHIVE) {
            actions.add("RESTAURER");
        }
        if (permissions.has(ArchivePermissionService.ARCHIVE_VOIR_HISTORIQUE)) {
            actions.add("VOIR_HISTORIQUE");
        }
        if (permissions.has(ArchivePermissionService.ARCHIVE_GERER_DEMANDES_ACCES)
                && statut == StatutArchive.ARCHIVE) {
            actions.add("DEMANDER_ACCES");
        }
        return actions;
    }
}
