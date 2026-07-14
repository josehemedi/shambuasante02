package hospicloud.servicesImpl.archive;

import hospicloud.dtos.archive.*;
import hospicloud.exceptions.BadRequestException;
import hospicloud.exceptions.ForbiddenException;
import hospicloud.exceptions.ResourceNotFoundException;
import hospicloud.mappers.ArchiveMapper;
import hospicloud.model.Role;
import hospicloud.model.archive.*;
import hospicloud.repositories.archive.ArchiveDossierRepository;
import hospicloud.repositories.archive.HistoriqueArchivageRepository;
import hospicloud.security.CurrentUserService;
import hospicloud.security.TenantContext;
import hospicloud.security.archive.ArchivePermissionService;
import hospicloud.services.archive.ArchivageService;
import hospicloud.services.archive.VerificationDossierService;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Transactional
public class ArchivageServiceImpl implements ArchivageService {

    private final ArchiveDossierRepository archiveRepository;
    private final HistoriqueArchivageRepository historiqueRepository;
    private final VerificationDossierService verificationService;
    private final ArchivePermissionService permissionService;
    private final CurrentUserService currentUserService;
    private final ArchiveAuditHelper auditHelper;
    private final JdbcTemplate jdbcTemplate;

    public ArchivageServiceImpl(ArchiveDossierRepository archiveRepository,
                                HistoriqueArchivageRepository historiqueRepository,
                                VerificationDossierService verificationService,
                                ArchivePermissionService permissionService,
                                CurrentUserService currentUserService,
                                ArchiveAuditHelper auditHelper,
                                JdbcTemplate jdbcTemplate) {
        this.archiveRepository = archiveRepository;
        this.historiqueRepository = historiqueRepository;
        this.verificationService = verificationService;
        this.permissionService = permissionService;
        this.currentUserService = currentUserService;
        this.auditHelper = auditHelper;
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    @Transactional(readOnly = true)
    public ArchivePageResponseDto rechercher(ArchiveSearchFilter filter) {
        permissionService.require(ArchivePermissionService.ARCHIVE_RECHERCHER);
        if (permissionService.isSuperAdminTechnicalOnly()) {
            throw new ForbiddenException("Le super administrateur n'a pas accès au contenu médical des archives.");
        }
        Integer hopitalId = TenantContext.getRequiredHopitalId();
        applyMedecinScope(filter);
        List<ArchiveDossierResponseDto> items = archiveRepository.search(hopitalId, filter).stream()
                .map(a -> ArchiveMapper.toDto(a, permissionService))
                .collect(Collectors.toList());
        long total = archiveRepository.count(hopitalId, filter);
        return new ArchivePageResponseDto(items, total, filter.getPage(), filter.getSize());
    }

    @Override
    @Transactional(readOnly = true)
    public ArchiveDossierResponseDto consulter(Long id) {
        permissionService.require(ArchivePermissionService.ARCHIVE_VOIR);
        if (permissionService.isSuperAdminTechnicalOnly()) {
            throw new ForbiddenException("Le super administrateur n'a pas accès au contenu médical des archives.");
        }
        ArchiveDossier archive = loadArchiveScoped(id);
        auditHelper.log("ARCHIVE_CONSULTEE", "SUCCESS",
                "Consultation d'une archive", id,
                archive.getStatutArchive().name(), archive.getStatutArchive().name(), null);
        return ArchiveMapper.toDto(archive, permissionService);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ArchiveDossierResponseDto> listerParPatient(Long patientId) {
        permissionService.require(ArchivePermissionService.ARCHIVE_VOIR);
        ArchiveSearchFilter filter = new ArchiveSearchFilter();
        filter.setPatientId(patientId);
        filter.setSize(100);
        applyMedecinScope(filter);
        Integer hopitalId = TenantContext.getRequiredHopitalId();
        return archiveRepository.search(hopitalId, filter).stream()
                .map(a -> ArchiveMapper.toDto(a, permissionService))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public ArchiveStatistiquesDto statistiques() {
        permissionService.require(ArchivePermissionService.ARCHIVE_VOIR_STATISTIQUES);
        return archiveRepository.computeStatistiques(TenantContext.getRequiredHopitalId());
    }

    @Override
    @Transactional(readOnly = true)
    public VerificationDossierResultDto verifierDossier(VerifierDossierRequestDto request) {
        permissionService.require(ArchivePermissionService.ARCHIVE_VERIFIER);
        Integer hopitalId = TenantContext.getRequiredHopitalId();
        ReglesArchivageHopital regles = archiveRepository.findOrCreateRegles(hopitalId);
        VerificationDossierResultDto result = verificationService.verifierAvecRegles(
                hopitalId, request.getTypeEpisode(), request.getEpisodeId(),
                request.getPatientId(), regles);
        auditHelper.log("DOSSIER_VERIFIE", result.isComplet() ? "SUCCESS" : "INCOMPLET",
                "Vérification de dossier", request.getEpisodeId(),
                null, null, null);
        return result;
    }

    @Override
    public ArchiveDossierResponseDto enregistrerEpisode(EnregistrerEpisodeRequestDto request) {
        permissionService.require(ArchivePermissionService.ARCHIVE_VERIFIER);
        Integer hopitalId = TenantContext.getRequiredHopitalId();

        archiveRepository.findByEpisode(hopitalId, request.getTypeEpisode(), request.getEpisodeId())
                .ifPresent(a -> {
                    throw new BadRequestException("Un dossier d'archivage existe déjà pour cet épisode.");
                });

        EpisodeMetadata meta = resolveEpisodeMetadata(hopitalId, request.getTypeEpisode(),
                request.getEpisodeId(), request.getPatientId());

        ArchiveDossier archive = new ArchiveDossier();
        archive.setHopitalId(hopitalId);
        archive.setPatientId(meta.patientId());
        archive.setTypeEpisode(request.getTypeEpisode());
        archive.setEpisodeId(request.getEpisodeId());
        archive.setIdMedecin(meta.medecinId());
        archive.setDateFinEpisode(meta.dateFin());
        archive.setDateDemandeArchivage(LocalDateTime.now());
        archive.setStatutArchive(StatutArchive.A_VERIFIER);
        archive.setVersion(1);

        Long id = archiveRepository.insert(archive);
        archive.setId(id);

        enregistrerHistorique(archive, null, StatutArchive.A_VERIFIER,
                "ENREGISTREMENT_EPISODE", null, "Épisode enregistré pour archivage");

        return ArchiveMapper.toDto(loadArchive(id), permissionService);
    }

    @Override
    public ArchiveDossierResponseDto marquerCommeIncomplet(Long id, TransitionArchiveRequestDto request) {
        permissionService.require(ArchivePermissionService.ARCHIVE_VERIFIER);
        ArchiveDossier archive = loadArchiveScoped(id);
        assertTransition(archive.getStatutArchive(), StatutArchive.A_VERIFIER, StatutArchive.INCOMPLET);

        StatutArchive ancien = archive.getStatutArchive();
        archive.setStatutArchive(StatutArchive.INCOMPLET);
        archive.setDossierComplet(false);
        archive.setObservation(request.getObservation());
        applyUpdate(archive, ancien, "DOSSIER_MARQUE_INCOMPLET", request.getMotif());
        auditHelper.log("DOSSIER_MARQUE_INCOMPLET", "SUCCESS", "Dossier marqué incomplet",
                id, ancien.name(), StatutArchive.INCOMPLET.name(), request.getMotif());
        return ArchiveMapper.toDto(loadArchive(id), permissionService);
    }

    @Override
    public ArchiveDossierResponseDto marquerCommePretAArchiver(Long id, TransitionArchiveRequestDto request) {
        permissionService.require(ArchivePermissionService.ARCHIVE_VERIFIER);
        ArchiveDossier archive = loadArchiveScoped(id);
        assertTransition(archive.getStatutArchive(),
                StatutArchive.A_VERIFIER, StatutArchive.INCOMPLET);

        ReglesArchivageHopital regles = archiveRepository.findOrCreateRegles(archive.getHopitalId());
        VerificationDossierResultDto verification = verificationService.verifierAvecRegles(
                archive.getHopitalId(), archive.getTypeEpisode(), archive.getEpisodeId(),
                archive.getPatientId(), regles);

        if (!verification.isPeutArchiver()) {
            throw new BadRequestException("Le dossier est incomplet: "
                    + String.join(", ", verification.getManquants()));
        }

        StatutArchive ancien = archive.getStatutArchive();
        archive.setStatutArchive(StatutArchive.PRET_A_ARCHIVER);
        archive.setDossierComplet(true);
        archive.setVerifiePar(currentUserService.getCurrentUtilisateurId());
        archive.setObservation(request.getObservation());
        applyUpdate(archive, ancien, "DOSSIER_PRET_A_ARCHIVER", request.getMotif());
        auditHelper.log("DOSSIER_PRET_A_ARCHIVER", "SUCCESS", "Dossier prêt à archiver",
                id, ancien.name(), StatutArchive.PRET_A_ARCHIVER.name(), request.getMotif());
        return ArchiveMapper.toDto(loadArchive(id), permissionService);
    }

    @Override
    public ArchiveDossierResponseDto archiverEpisode(Long id, TransitionArchiveRequestDto request) {
        permissionService.require(ArchivePermissionService.ARCHIVE_ARCHIVER);
        ArchiveDossier archive = loadArchiveScoped(id);

        if (archive.getStatutArchive() != StatutArchive.PRET_A_ARCHIVER) {
            throw new BadRequestException("Seuls les dossiers prêts à archiver peuvent être archivés.");
        }

        ReglesArchivageHopital regles = archiveRepository.findOrCreateRegles(archive.getHopitalId());
        VerificationDossierResultDto verification = verificationService.verifierAvecRegles(
                archive.getHopitalId(), archive.getTypeEpisode(), archive.getEpisodeId(),
                archive.getPatientId(), regles);
        if (!verification.isPeutArchiver()) {
            throw new BadRequestException("Archivage refusé — dossier incomplet.");
        }

        StatutArchive ancien = archive.getStatutArchive();
        archive.setStatutArchive(StatutArchive.ARCHIVE);
        archive.setDateArchivage(LocalDateTime.now());
        archive.setArchivePar(currentUserService.getCurrentUtilisateurId());
        archive.setMotifArchivage(request.getMotif());
        archive.setEmplacementPhysique(request.getEmplacementPhysique());
        archive.setNumeroBoiteArchive(request.getNumeroBoiteArchive());
        archive.setNumeroRayon(request.getNumeroRayon());
        archive.setObservation(request.getObservation());
        applyUpdate(archive, ancien, "DOSSIER_ARCHIVE", request.getMotif());
        auditHelper.log("DOSSIER_ARCHIVE", "SUCCESS", "Épisode archivé",
                id, ancien.name(), StatutArchive.ARCHIVE.name(), request.getMotif());
        return ArchiveMapper.toDto(loadArchive(id), permissionService);
    }

    @Override
    public ArchiveDossierResponseDto restaurerArchive(Long id, TransitionArchiveRequestDto request) {
        permissionService.require(ArchivePermissionService.ARCHIVE_RESTAURER);
        if (!StringUtils.hasText(request.getMotif())) {
            throw new BadRequestException("Un motif est obligatoire pour la restauration.");
        }

        ArchiveDossier archive = loadArchiveScoped(id);
        if (archive.getStatutArchive() != StatutArchive.ARCHIVE) {
            throw new BadRequestException("Seules les archives peuvent être restaurées.");
        }

        StatutArchive ancien = archive.getStatutArchive();
        archive.setStatutArchive(StatutArchive.RESTAURE);
        archive.setDateRestauration(LocalDateTime.now());
        archive.setRestaurePar(currentUserService.getCurrentUtilisateurId());
        archive.setMotifRestauration(request.getMotif());
        archive.setObservation(request.getObservation());
        applyUpdate(archive, ancien, "ARCHIVE_RESTAUREE", request.getMotif());
        auditHelper.log("ARCHIVE_RESTAUREE", "SUCCESS", "Archive restaurée exceptionnellement",
                id, ancien.name(), StatutArchive.RESTAURE.name(), request.getMotif());
        return ArchiveMapper.toDto(loadArchive(id), permissionService);
    }

    @Override
    @Transactional(readOnly = true)
    public List<HistoriqueArchivageDto> historique(Long id) {
        permissionService.require(ArchivePermissionService.ARCHIVE_VOIR_HISTORIQUE);
        loadArchiveScoped(id);
        Integer hopitalId = TenantContext.getRequiredHopitalId();
        return historiqueRepository.findByArchiveId(hopitalId, id).stream()
                .map(ArchiveMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public ReglesArchivageHopital getRegles() {
        permissionService.require(ArchivePermissionService.ARCHIVE_VOIR_STATISTIQUES);
        return archiveRepository.findOrCreateRegles(TenantContext.getRequiredHopitalId());
    }

    @Override
    public ReglesArchivageHopital updateRegles(ReglesArchivageHopital regles) {
        if (currentUserService.getCurrentRole() != Role.TENANT_ADMIN) {
            throw new ForbiddenException("Seul l'administrateur de l'hôpital peut modifier les règles d'archivage.");
        }
        regles.setHopitalId(TenantContext.getRequiredHopitalId());
        archiveRepository.updateRegles(regles);
        return archiveRepository.findOrCreateRegles(regles.getHopitalId());
    }

    private void applyUpdate(ArchiveDossier archive, StatutArchive ancien,
                             String action, String motif) {
        boolean updated = archiveRepository.updateStatut(archive);
        if (!updated) {
            throw new BadRequestException("Conflit de version — veuillez recharger le dossier.");
        }
        enregistrerHistorique(archive, ancien, archive.getStatutArchive(), action, motif, null);
    }

    private void enregistrerHistorique(ArchiveDossier archive, StatutArchive ancien,
                                       StatutArchive nouveau, String action,
                                       String motif, String observation) {
        HistoriqueArchivage h = new HistoriqueArchivage();
        h.setHopitalId(archive.getHopitalId());
        h.setArchiveId(archive.getId());
        h.setAncienStatut(ancien);
        h.setNouveauStatut(nouveau);
        h.setAction(action);
        h.setMotif(motif);
        h.setObservation(observation);
        h.setEffectuePar(currentUserService.getCurrentUtilisateurId());
        Long histId = historiqueRepository.insert(h);
        if (histId == null) {
            throw new BadRequestException("Échec de l'historisation — opération annulée.");
        }
    }

    private ArchiveDossier loadArchive(Long id) {
        return archiveRepository.findById(TenantContext.getRequiredHopitalId(), id)
                .orElseThrow(() -> new ResourceNotFoundException("Archive introuvable: " + id));
    }

    private ArchiveDossier loadArchiveScoped(Long id) {
        ArchiveDossier archive = loadArchive(id);
        Role role = currentUserService.getCurrentRole();
        if (role == Role.MEDECIN) {
            Integer medecinId = currentUserService.getCurrentMedecinId();
            if (medecinId == null || !medecinId.equals(archive.getIdMedecin())) {
                throw new ForbiddenException("Accès refusé aux archives d'un autre médecin.");
            }
        }
        return archive;
    }

    private void applyMedecinScope(ArchiveSearchFilter filter) {
        if (currentUserService.getCurrentRole() == Role.MEDECIN) {
            filter.setIdMedecin(currentUserService.getCurrentMedecinId());
        }
    }

    private void assertTransition(StatutArchive current, StatutArchive... allowed) {
        for (StatutArchive s : allowed) {
            if (current == s) return;
        }
        throw new BadRequestException("Transition non autorisée depuis le statut " + current);
    }

    private EpisodeMetadata resolveEpisodeMetadata(Integer hopitalId, TypeEpisode type,
                                                     Long episodeId, Long patientId) {
        return switch (type) {
            case CONSULTATION -> fromConsultation(hopitalId, episodeId, patientId);
            case HOSPITALISATION, URGENCE, ADMINISTRATIF -> fromAdmission(hopitalId, episodeId, patientId);
        };
    }

    private EpisodeMetadata fromConsultation(Integer hopitalId, Long episodeId, Long patientId) {
        List<Map<String, Object>> rows = jdbcTemplate.queryForList("""
                SELECT id_patient, id_medecin, date_signature, date_consultation
                FROM consultations_medicales
                WHERE id_consultation = ? AND id_hopital = ?
                """, episodeId, hopitalId);
        if (rows.isEmpty()) {
            throw new ResourceNotFoundException("Consultation introuvable.");
        }
        Map<String, Object> row = rows.get(0);
        Long pid = ((Number) row.get("id_patient")).longValue();
        if (patientId != null && !patientId.equals(pid)) {
            throw new BadRequestException("Patient incohérent avec la consultation.");
        }
        Integer medecinId = row.get("id_medecin") != null
                ? ((Number) row.get("id_medecin")).intValue() : null;
        LocalDateTime fin = row.get("date_signature") != null
                ? ((java.sql.Timestamp) row.get("date_signature")).toLocalDateTime()
                : row.get("date_consultation") != null
                ? ((java.sql.Timestamp) row.get("date_consultation")).toLocalDateTime()
                : LocalDateTime.now();
        return new EpisodeMetadata(pid, medecinId, fin);
    }

    private EpisodeMetadata fromAdmission(Integer hopitalId, Long episodeId, Long patientId) {
        List<Map<String, Object>> rows = jdbcTemplate.queryForList("""
                SELECT id_patient, id_medecin, temps_arrivee
                FROM admission WHERE id_admission = ? AND id_hopital = ?
                """, episodeId.intValue(), hopitalId);
        if (rows.isEmpty()) {
            throw new ResourceNotFoundException("Admission introuvable.");
        }
        Map<String, Object> row = rows.get(0);
        Long pid = ((Number) row.get("id_patient")).longValue();
        if (patientId != null && !patientId.equals(pid)) {
            throw new BadRequestException("Patient incohérent avec l'admission.");
        }
        Integer medecinId = row.get("id_medecin") != null
                ? ((Number) row.get("id_medecin")).intValue() : null;
        LocalDateTime fin = row.get("temps_arrivee") != null
                ? ((java.sql.Timestamp) row.get("temps_arrivee")).toLocalDateTime()
                : LocalDateTime.now();
        return new EpisodeMetadata(pid, medecinId, fin);
    }

    private record EpisodeMetadata(Long patientId, Integer medecinId, LocalDateTime dateFin) {}
}
