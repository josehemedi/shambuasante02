package hospicloud.servicesImpl.archive;

import hospicloud.dtos.archive.DemandeAccesArchiveDto;
import hospicloud.dtos.archive.DemandeAccesRequestDto;
import hospicloud.exceptions.BadRequestException;
import hospicloud.exceptions.ForbiddenException;
import hospicloud.exceptions.ResourceNotFoundException;
import hospicloud.mappers.ArchiveMapper;
import hospicloud.model.Role;
import hospicloud.model.archive.ArchiveDossier;
import hospicloud.model.archive.DemandeAccesArchive;
import hospicloud.model.archive.StatutArchive;
import hospicloud.model.archive.StatutDemandeAccesArchive;
import hospicloud.repositories.archive.ArchiveDossierRepository;
import hospicloud.repositories.archive.DemandeAccesArchiveRepository;
import hospicloud.security.CurrentUserService;
import hospicloud.security.TenantAuthorization;
import hospicloud.security.TenantContext;
import hospicloud.security.archive.ArchivePermissionService;
import hospicloud.services.archive.DemandeAccesArchiveService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class DemandeAccesArchiveServiceImpl implements DemandeAccesArchiveService {

    private final DemandeAccesArchiveRepository demandeRepository;
    private final ArchiveDossierRepository archiveRepository;
    private final ArchivePermissionService permissionService;
    private final CurrentUserService currentUserService;
    private final ArchiveAuditHelper auditHelper;

    public DemandeAccesArchiveServiceImpl(DemandeAccesArchiveRepository demandeRepository,
                                          ArchiveDossierRepository archiveRepository,
                                          ArchivePermissionService permissionService,
                                          CurrentUserService currentUserService,
                                          ArchiveAuditHelper auditHelper) {
        this.demandeRepository = demandeRepository;
        this.archiveRepository = archiveRepository;
        this.permissionService = permissionService;
        this.currentUserService = currentUserService;
        this.auditHelper = auditHelper;
    }

    @Override
    public DemandeAccesArchiveDto creerDemande(Long archiveId, DemandeAccesRequestDto request) {
        if (!StringUtils.hasText(request.getMotif())) {
            throw new BadRequestException("Le motif de la demande est obligatoire.");
        }
        Integer hopitalId = TenantContext.getRequiredHopitalId();
        ArchiveDossier archive = archiveRepository.findById(hopitalId, archiveId)
                .orElseThrow(() -> new ResourceNotFoundException("Archive introuvable."));
        TenantAuthorization.assertSameTenant(archive.getHopitalId());

        if (archive.getStatutArchive() != StatutArchive.ARCHIVE) {
            throw new BadRequestException("Une demande d'accès ne concerne que les dossiers archivés.");
        }

        Role role = currentUserService.getCurrentRole();
        if (role != Role.RECEPTION && role != Role.MEDECIN
                && !permissionService.has(ArchivePermissionService.ARCHIVE_GERER_DEMANDES_ACCES)) {
            throw new ForbiddenException("Rôle non autorisé à créer une demande d'accès.");
        }

        DemandeAccesArchive demande = new DemandeAccesArchive();
        demande.setHopitalId(hopitalId);
        demande.setArchiveId(archiveId);
        demande.setDemandeurId(currentUserService.getCurrentUtilisateurId());
        demande.setMotif(request.getMotif());
        demande.setStatut(StatutDemandeAccesArchive.EN_ATTENTE);
        Long id = demandeRepository.insert(demande);

        auditHelper.log("DEMANDE_ACCES_CREEE", "SUCCESS", "Demande d'accès créée",
                archiveId, null, StatutDemandeAccesArchive.EN_ATTENTE.name(), request.getMotif());

        return demandeRepository.findById(hopitalId, id)
                .map(ArchiveMapper::toDto)
                .orElseThrow(() -> new ResourceNotFoundException("Demande introuvable."));
    }

    @Override
    public DemandeAccesArchiveDto accepter(Long demandeId, String observation) {
        return traiter(demandeId, StatutDemandeAccesArchive.ACCEPTEE, "DEMANDE_ACCES_ACCEPTEE", observation);
    }

    @Override
    public DemandeAccesArchiveDto refuser(Long demandeId, String observation) {
        return traiter(demandeId, StatutDemandeAccesArchive.REFUSEE, "DEMANDE_ACCES_REFUSEE", observation);
    }

    @Override
    @Transactional(readOnly = true)
    public List<DemandeAccesArchiveDto> listerEnAttente() {
        permissionService.require(ArchivePermissionService.ARCHIVE_GERER_DEMANDES_ACCES);
        return demandeRepository.findEnAttente(TenantContext.getRequiredHopitalId()).stream()
                .map(ArchiveMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<DemandeAccesArchiveDto> listerParArchive(Long archiveId) {
        permissionService.require(ArchivePermissionService.ARCHIVE_GERER_DEMANDES_ACCES);
        return demandeRepository.findByArchiveId(TenantContext.getRequiredHopitalId(), archiveId).stream()
                .map(ArchiveMapper::toDto)
                .collect(Collectors.toList());
    }

    private DemandeAccesArchiveDto traiter(Long demandeId, StatutDemandeAccesArchive statut,
                                           String auditAction, String observation) {
        if (currentUserService.getCurrentRole() != Role.ARCHIVISTE
                && currentUserService.getCurrentRole() != Role.TENANT_ADMIN) {
            throw new ForbiddenException("Seul un archiviste ou administrateur peut traiter les demandes.");
        }

        Integer hopitalId = TenantContext.getRequiredHopitalId();
        DemandeAccesArchive demande = demandeRepository.findById(hopitalId, demandeId)
                .orElseThrow(() -> new ResourceNotFoundException("Demande introuvable."));
        TenantAuthorization.assertSameTenant(demande.getHopitalId());

        if (demande.getStatut() != StatutDemandeAccesArchive.EN_ATTENTE) {
            throw new BadRequestException("Cette demande a déjà été traitée.");
        }

        demandeRepository.updateStatut(hopitalId, demandeId, statut,
                currentUserService.getCurrentUtilisateurId(), observation);

        auditHelper.log(auditAction, "SUCCESS", "Demande d'accès traitée",
                demande.getArchiveId(), StatutDemandeAccesArchive.EN_ATTENTE.name(),
                statut.name(), observation);

        return demandeRepository.findById(hopitalId, demandeId)
                .map(ArchiveMapper::toDto)
                .orElseThrow(() -> new ResourceNotFoundException("Demande introuvable."));
    }
}
