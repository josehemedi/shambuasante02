package hospicloud.servicesImpl.archive;

import hospicloud.model.archive.ArchiveDossier;
import hospicloud.model.archive.HistoriqueArchivage;
import hospicloud.model.archive.StatutArchive;
import hospicloud.model.archive.TypeEpisode;
import hospicloud.model.reception.Admission;
import hospicloud.repositories.archive.ArchiveDossierRepository;
import hospicloud.repositories.archive.HistoriqueArchivageRepository;
import hospicloud.security.CurrentUserService;
import hospicloud.services.RealtimeNotificationService;
import hospicloud.services.archive.ArchivePdfService;
import hospicloud.services.archive.ArchiveSnapshotService;
import hospicloud.services.archive.ArchiveWorkflowService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class ArchiveWorkflowServiceImpl implements ArchiveWorkflowService {

    private static final Logger log = LoggerFactory.getLogger(ArchiveWorkflowServiceImpl.class);

    private final ArchiveDossierRepository archiveRepository;
    private final HistoriqueArchivageRepository historiqueRepository;
    private final ArchiveAuditHelper auditHelper;
    private final CurrentUserService currentUserService;
    private final JdbcTemplate jdbcTemplate;
    private final RealtimeNotificationService realtimeNotificationService;
    private final ArchiveSnapshotService snapshotService;
    private final ArchivePdfService archivePdfService;

    public ArchiveWorkflowServiceImpl(ArchiveDossierRepository archiveRepository,
                                      HistoriqueArchivageRepository historiqueRepository,
                                      ArchiveAuditHelper auditHelper,
                                      CurrentUserService currentUserService,
                                      JdbcTemplate jdbcTemplate,
                                      RealtimeNotificationService realtimeNotificationService,
                                      ArchiveSnapshotService snapshotService,
                                      ArchivePdfService archivePdfService) {
        this.archiveRepository = archiveRepository;
        this.historiqueRepository = historiqueRepository;
        this.auditHelper = auditHelper;
        this.currentUserService = currentUserService;
        this.jdbcTemplate = jdbcTemplate;
        this.realtimeNotificationService = realtimeNotificationService;
        this.snapshotService = snapshotService;
        this.archivePdfService = archivePdfService;
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public Optional<Long> soumettreApresAutorisationSortie(Integer hopitalId,
                                                           Admission admission,
                                                           Integer idPatient,
                                                           Integer idMedecin,
                                                           Integer idBonSortie) {
        if (hopitalId == null || admission == null || admission.getIdAdmission() == null) {
            return Optional.empty();
        }
        if (!hopitalId.equals(admission.getIdHopital())) {
            log.warn("Refus envoi archive: admission {} n'appartient pas à l'hôpital {}",
                    admission.getIdAdmission(), hopitalId);
            return Optional.empty();
        }
        if (idPatient != null && admission.getIdPatient() != null
                && !idPatient.equals(admission.getIdPatient())) {
            log.warn("Refus envoi archive: patient {} incohérent avec admission {}",
                    idPatient, admission.getIdAdmission());
            return Optional.empty();
        }

        TypeEpisode typeEpisode = resolveTypeEpisode(admission);
        Long episodeId = admission.getIdAdmission().longValue();
        Long patientId = admission.getIdPatient() != null
                ? admission.getIdPatient().longValue()
                : (idPatient != null ? idPatient.longValue() : null);

        if (patientId == null) {
            return Optional.empty();
        }

        return creerOuRetrouverArchive(hopitalId, typeEpisode, episodeId, patientId,
                idMedecin != null ? idMedecin : admission.getIdMedecin(),
                idBonSortie, "Sortie médicale autorisée — dossier prêt à être archivé", true);
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public Optional<Long> soumettreApresSortieOfficielle(Integer hopitalId,
                                                         TypeEpisode typeEpisode,
                                                         Long episodeId,
                                                         Integer idPatient,
                                                         Integer idMedecin,
                                                         Integer idBonSortie,
                                                         String observation) {
        if (hopitalId == null || typeEpisode == null || episodeId == null || idPatient == null) {
            return Optional.empty();
        }
        return creerOuRetrouverArchive(
                hopitalId,
                typeEpisode,
                episodeId,
                idPatient.longValue(),
                idMedecin,
                idBonSortie,
                observation != null ? observation : "Sortie officielle — dossier prêt à être archivé",
                true);
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public Optional<Long> soumettreApresDelivranceSiAbsent(Integer hopitalId,
                                                           Integer idAdmission,
                                                           Integer idPatient,
                                                           Integer idBonSortie) {
        if (hopitalId == null || idAdmission == null) {
            return Optional.empty();
        }

        Admission admission = loadAdmission(hopitalId, idAdmission);
        if (admission == null) {
            return Optional.empty();
        }

        TypeEpisode typeEpisode = resolveTypeEpisode(admission);
        Long episodeId = idAdmission.longValue();
        Optional<ArchiveDossier> existing = archiveRepository.findByEpisode(hopitalId, typeEpisode, episodeId);
        if (existing.isPresent()) {
            return Optional.of(existing.get().getId());
        }

        Long patientId = admission.getIdPatient() != null
                ? admission.getIdPatient().longValue()
                : (idPatient != null ? idPatient.longValue() : null);
        if (patientId == null) {
            return Optional.empty();
        }

        return creerOuRetrouverArchive(hopitalId, typeEpisode, episodeId, patientId,
                admission.getIdMedecin(), idBonSortie,
                "Bon de sortie délivré — dossier prêt à être archivé", true);
    }

    private Optional<Long> creerOuRetrouverArchive(Integer hopitalId,
                                                   TypeEpisode typeEpisode,
                                                   Long episodeId,
                                                   Long patientId,
                                                   Integer idMedecin,
                                                   Integer idBonSortie,
                                                   String observation,
                                                   boolean notifyArchivistes) {
        try {
            Optional<ArchiveDossier> existing = archiveRepository.findByEpisode(hopitalId, typeEpisode, episodeId);
            if (existing.isPresent()) {
                Long archiveId = existing.get().getId();
                log.info("Archive déjà enregistrée pour hôpital {} épisode {} ({})",
                        hopitalId, episodeId, typeEpisode);
                if (notifyArchivistes) {
                    realtimeNotificationService.notifyArchivistesDossierPatientSorti(
                            hopitalId,
                            archiveId,
                            patientId,
                            typeEpisode.name(),
                            currentUserService.getCurrentUtilisateurId());
                }
                return Optional.of(archiveId);
            }

            if (!patientAppartientAHopital(hopitalId, patientId)) {
                log.warn("Refus envoi archive: patient {} hors tenant {}", patientId, hopitalId);
                return Optional.empty();
            }

            ArchiveDossier archive = new ArchiveDossier();
            archive.setHopitalId(hopitalId);
            archive.setPatientId(patientId);
            archive.setTypeEpisode(typeEpisode);
            archive.setEpisodeId(episodeId);
            archive.setIdMedecin(idMedecin);
            archive.setDateFinEpisode(LocalDateTime.now());
            archive.setDateDemandeArchivage(LocalDateTime.now());
            archive.setStatutArchive(StatutArchive.A_VERIFIER);
            archive.setDossierComplet(false);
            archive.setVersion(1);
            archive.setObservation(buildObservation(observation, idBonSortie));

            Long archiveId = archiveRepository.insert(archive);
            if (archiveId == null) {
                log.error("Échec insertion archive pour hôpital {} épisode {}", hopitalId, episodeId);
                return Optional.empty();
            }
            archive.setId(archiveId);

            HistoriqueArchivage historique = new HistoriqueArchivage();
            historique.setHopitalId(hopitalId);
            historique.setArchiveId(archiveId);
            historique.setAncienStatut(null);
            historique.setNouveauStatut(StatutArchive.A_VERIFIER);
            historique.setAction("DOSSIER_ENVOYE_ARCHIVISTE");
            historique.setMotif("Workflow automatique post-sortie");
            historique.setObservation(archive.getObservation());
            historique.setEffectuePar(currentUserService.getCurrentUtilisateurId());

            Long histId = historiqueRepository.insert(historique);
            if (histId == null) {
                throw new IllegalStateException("Historisation obligatoire non enregistrée");
            }

            auditHelper.log("DOSSIER_ENVOYE_ARCHIVISTE", "SUCCESS",
                    "Dossier envoyé à l'archiviste après sortie officielle", archiveId,
                    null, StatutArchive.A_VERIFIER.name(),
                    "bonSortie=" + idBonSortie);

            log.info("Dossier archive {} créé pour hôpital {} patient {} (épisode {})",
                    archiveId, hopitalId, patientId, episodeId);

            try {
                snapshotService.capturerEtPersister(archive);
            } catch (Exception snapEx) {
                log.warn("Snapshot patient non capturé pour archive {}: {}", archiveId, snapEx.getMessage());
            }

            try {
                ArchiveDossier fresh = archiveRepository.findById(hopitalId, archiveId).orElse(archive);
                archivePdfService.genererEtAttacher(fresh);
            } catch (Exception pdfEx) {
                log.warn("PDF dossier non généré pour archive {}: {}", archiveId, pdfEx.getMessage());
            }

            if (notifyArchivistes) {
                realtimeNotificationService.notifyArchivistesDossierPatientSorti(
                        hopitalId,
                        archiveId,
                        patientId,
                        typeEpisode.name(),
                        currentUserService.getCurrentUtilisateurId());
            }

            return Optional.of(archiveId);
        } catch (Exception ex) {
            log.error("Échec envoi automatique vers archiviste (hôpital {}, épisode {}): {}",
                    hopitalId, episodeId, ex.getMessage(), ex);
            auditHelper.log("DOSSIER_ENVOYE_ARCHIVISTE", "ERROR",
                    "Échec envoi automatique vers archiviste", episodeId,
                    null, null, ex.getMessage());
            return Optional.empty();
        }
    }

    private TypeEpisode resolveTypeEpisode(Admission admission) {
        if (admission.getNiveauPriorite() != null && admission.getNiveauPriorite() == 1) {
            return TypeEpisode.URGENCE;
        }
        return TypeEpisode.HOSPITALISATION;
    }

    private boolean patientAppartientAHopital(Integer hopitalId, Long patientId) {
        Integer count = jdbcTemplate.queryForObject("""
                SELECT COUNT(1) FROM patients
                WHERE id_patient = ? AND id_hopital = ?
                """, Integer.class, patientId, hopitalId);
        return count != null && count > 0;
    }

    private Admission loadAdmission(Integer hopitalId, Integer idAdmission) {
        List<Map<String, Object>> rows = jdbcTemplate.queryForList("""
                SELECT id_admission, id_hopital, id_patient, id_medecin, niveau_priorite
                FROM admission
                WHERE id_admission = ? AND id_hopital = ?
                """, idAdmission, hopitalId);
        if (rows.isEmpty()) {
            return null;
        }
        Map<String, Object> row = rows.get(0);
        Admission a = new Admission();
        a.setIdAdmission(((Number) row.get("id_admission")).intValue());
        a.setIdHopital(((Number) row.get("id_hopital")).intValue());
        a.setIdPatient(((Number) row.get("id_patient")).intValue());
        if (row.get("id_medecin") != null) {
            a.setIdMedecin(((Number) row.get("id_medecin")).intValue());
        }
        if (row.get("niveau_priorite") != null) {
            a.setNiveauPriorite(((Number) row.get("niveau_priorite")).intValue());
        }
        return a;
    }

    private String buildObservation(String base, Integer idBonSortie) {
        if (idBonSortie == null) {
            return base;
        }
        return base + " (bon de sortie n°" + idBonSortie + ")";
    }
}
