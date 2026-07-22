package hospicloud.servicesImpl.archive;

import hospicloud.dtos.archive.*;
import hospicloud.exceptions.BadRequestException;
import hospicloud.exceptions.ResourceNotFoundException;
import hospicloud.mappers.ArchiveMapper;
import hospicloud.model.archive.ArchiveDossier;
import hospicloud.model.archive.ArchiveDossierVirtuel;
import hospicloud.repositories.archive.ArchiveDossierRepository;
import hospicloud.repositories.archive.ArchiveDossierVirtuelRepository;
import hospicloud.security.CurrentUserService;
import hospicloud.security.TenantAuthorization;
import hospicloud.security.TenantContext;
import hospicloud.security.archive.ArchivePermissionService;
import hospicloud.services.archive.ArchiveClassementService;
import hospicloud.services.archive.ArchivePdfService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ArchiveClassementServiceImpl implements ArchiveClassementService {

    private final ArchiveDossierVirtuelRepository folderRepository;
    private final ArchiveDossierRepository archiveRepository;
    private final ArchivePermissionService permissionService;
    private final CurrentUserService currentUserService;
    private final ArchivePdfService archivePdfService;

    public ArchiveClassementServiceImpl(ArchiveDossierVirtuelRepository folderRepository,
                                        ArchiveDossierRepository archiveRepository,
                                        ArchivePermissionService permissionService,
                                        CurrentUserService currentUserService,
                                        ArchivePdfService archivePdfService) {
        this.folderRepository = folderRepository;
        this.archiveRepository = archiveRepository;
        this.permissionService = permissionService;
        this.currentUserService = currentUserService;
        this.archivePdfService = archivePdfService;
    }

    @Override
    @Transactional(readOnly = true)
    public ArchiveExplorerContentDto explorer(Long folderId) {
        permissionService.require(ArchivePermissionService.ARCHIVE_VOIR);
        Integer hopitalId = TenantContext.getRequiredHopitalId();

        ArchiveExplorerContentDto content = new ArchiveExplorerContentDto();
        content.setCurrentFolderId(folderId);

        if (folderId == null) {
            content.setCurrentFolderName("Racine");
            content.getBreadcrumb().add(new ArchiveExplorerContentDto.BreadcrumbItem(null, "Racine"));
        } else {
            ArchiveDossierVirtuel folder = folderRepository.findById(hopitalId, folderId)
                    .orElseThrow(() -> new ResourceNotFoundException("Dossier virtuel introuvable"));
            content.setCurrentFolderName(folder.getNom());
            content.setBreadcrumb(buildBreadcrumb(hopitalId, folder));
        }

        content.setFolders(folderRepository.listChildren(hopitalId, folderId).stream()
                .map(this::toFolderDto)
                .toList());
        List<ArchiveDossierResponseDto> files = archiveRepository.listByDossierVirtuel(hopitalId, folderId).stream()
                .map(a -> ArchiveMapper.toDto(a, permissionService))
                .collect(Collectors.toList());
        Map<Long, List<ArchiveFichierDto>> pdfByArchive = archivePdfService.listerParArchives(
                hopitalId,
                files.stream().map(ArchiveDossierResponseDto::getId).collect(Collectors.toList()));
        for (ArchiveDossierResponseDto file : files) {
            ArchiveMapper.attachFichiers(file, pdfByArchive.getOrDefault(file.getId(), List.of()));
        }
        content.setFiles(files);
        return content;
    }

    @Override
    @Transactional(readOnly = true)
    public List<ArchiveDossierVirtuelDto> arbre() {
        permissionService.require(ArchivePermissionService.ARCHIVE_VOIR);
        Integer hopitalId = TenantContext.getRequiredHopitalId();
        List<ArchiveDossierVirtuel> all = folderRepository.listAll(hopitalId);
        Map<Long, ArchiveDossierVirtuelDto> byId = new HashMap<>();
        List<ArchiveDossierVirtuelDto> roots = new ArrayList<>();

        for (ArchiveDossierVirtuel f : all) {
            byId.put(f.getId(), toFolderDto(f));
        }
        for (ArchiveDossierVirtuel f : all) {
            ArchiveDossierVirtuelDto dto = byId.get(f.getId());
            if (f.getParentId() == null) {
                roots.add(dto);
            } else {
                ArchiveDossierVirtuelDto parent = byId.get(f.getParentId());
                if (parent != null) parent.getChildren().add(dto);
                else roots.add(dto);
            }
        }
        return roots;
    }

    @Override
    @Transactional
    public ArchiveDossierVirtuelDto creerDossier(CreateDossierVirtuelRequest request) {
        permissionService.require(ArchivePermissionService.ARCHIVE_CLASSER);
        Integer hopitalId = TenantContext.getRequiredHopitalId();
        String nom = normalizeNom(request.getNom());
        Long parentId = request.getParentId();

        if (parentId != null) {
            folderRepository.findById(hopitalId, parentId)
                    .orElseThrow(() -> new ResourceNotFoundException("Dossier parent introuvable"));
        }
        if (folderRepository.existsByNom(hopitalId, parentId, nom, null)) {
            throw new BadRequestException("Un dossier portant ce nom existe déjà ici.");
        }

        ArchiveDossierVirtuel folder = new ArchiveDossierVirtuel();
        folder.setHopitalId(hopitalId);
        folder.setParentId(parentId);
        folder.setNom(nom);
        folder.setCreatedBy(currentUserService.getCurrentUtilisateurId());
        Long id = folderRepository.insert(folder);
        return toFolderDto(folderRepository.findById(hopitalId, id)
                .orElseThrow(() -> new ResourceNotFoundException("Dossier créé introuvable")));
    }

    @Override
    @Transactional
    public ArchiveDossierVirtuelDto renommer(Long folderId, RenameDossierVirtuelRequest request) {
        permissionService.require(ArchivePermissionService.ARCHIVE_CLASSER);
        Integer hopitalId = TenantContext.getRequiredHopitalId();
        ArchiveDossierVirtuel folder = folderRepository.findById(hopitalId, folderId)
                .orElseThrow(() -> new ResourceNotFoundException("Dossier virtuel introuvable"));
        String nom = normalizeNom(request.getNom());
        if (folderRepository.existsByNom(hopitalId, folder.getParentId(), nom, folderId)) {
            throw new BadRequestException("Un dossier portant ce nom existe déjà ici.");
        }
        folderRepository.updateNom(hopitalId, folderId, nom);
        return toFolderDto(folderRepository.findById(hopitalId, folderId).orElseThrow());
    }

    @Override
    @Transactional
    public ArchiveDossierVirtuelDto deplacerDossierVirtuel(Long folderId, MoveDossierVirtuelRequest request) {
        permissionService.require(ArchivePermissionService.ARCHIVE_CLASSER);
        Integer hopitalId = TenantContext.getRequiredHopitalId();
        ArchiveDossierVirtuel folder = folderRepository.findById(hopitalId, folderId)
                .orElseThrow(() -> new ResourceNotFoundException("Dossier virtuel introuvable"));
        Long newParentId = request.getParentId();

        if (folderId.equals(newParentId)) {
            throw new BadRequestException("Impossible de déplacer un dossier dans lui-même.");
        }
        if (newParentId != null) {
            folderRepository.findById(hopitalId, newParentId)
                    .orElseThrow(() -> new ResourceNotFoundException("Dossier destination introuvable"));
            if (folderRepository.isDescendantOf(hopitalId, newParentId, folderId)) {
                throw new BadRequestException("Impossible de déplacer un dossier dans un de ses sous-dossiers.");
            }
        }
        if (folderRepository.existsByNom(hopitalId, newParentId, folder.getNom(), folderId)) {
            throw new BadRequestException("Un dossier portant ce nom existe déjà à la destination.");
        }
        folderRepository.updateParent(hopitalId, folderId, newParentId);
        return toFolderDto(folderRepository.findById(hopitalId, folderId).orElseThrow());
    }

    @Override
    @Transactional
    public void supprimerDossier(Long folderId) {
        permissionService.require(ArchivePermissionService.ARCHIVE_CLASSER);
        Integer hopitalId = TenantContext.getRequiredHopitalId();
        folderRepository.findById(hopitalId, folderId)
                .orElseThrow(() -> new ResourceNotFoundException("Dossier virtuel introuvable"));
        if (!folderRepository.deleteIfEmpty(hopitalId, folderId)) {
            throw new BadRequestException("Le dossier n'est pas vide. Déplacez ou supprimez son contenu d'abord.");
        }
    }

    @Override
    @Transactional
    public ArchiveDossierResponseDto deplacerArchive(Long archiveId, MoveArchiveDossierRequest request) {
        permissionService.require(ArchivePermissionService.ARCHIVE_CLASSER);
        Integer hopitalId = TenantContext.getRequiredHopitalId();
        ArchiveDossier archive = archiveRepository.findById(hopitalId, archiveId)
                .orElseThrow(() -> new ResourceNotFoundException("Dossier d'archivage introuvable"));
        TenantAuthorization.assertSameTenant(archive.getHopitalId());
        Long targetFolderId = request.getDossierVirtuelId();
        if (targetFolderId != null) {
            ArchiveDossierVirtuel folder = folderRepository.findById(hopitalId, targetFolderId)
                    .orElseThrow(() -> new ResourceNotFoundException("Dossier destination introuvable"));
            TenantAuthorization.assertSameTenant(folder.getHopitalId());
        }
        archiveRepository.updateDossierVirtuelId(hopitalId, archiveId, targetFolderId);
        return ArchiveMapper.toDto(
                archiveRepository.findById(hopitalId, archiveId).orElse(archive),
                permissionService);
    }

    private List<ArchiveExplorerContentDto.BreadcrumbItem> buildBreadcrumb(Integer hopitalId,
                                                                           ArchiveDossierVirtuel folder) {
        List<ArchiveExplorerContentDto.BreadcrumbItem> items = new ArrayList<>();
        items.add(new ArchiveExplorerContentDto.BreadcrumbItem(null, "Racine"));
        List<ArchiveDossierVirtuel> chain = new ArrayList<>();
        ArchiveDossierVirtuel current = folder;
        int guard = 0;
        while (current != null && guard++ < 64) {
            chain.add(0, current);
            if (current.getParentId() == null) break;
            current = folderRepository.findById(hopitalId, current.getParentId()).orElse(null);
        }
        for (ArchiveDossierVirtuel f : chain) {
            items.add(new ArchiveExplorerContentDto.BreadcrumbItem(f.getId(), f.getNom()));
        }
        return items;
    }

    private ArchiveDossierVirtuelDto toFolderDto(ArchiveDossierVirtuel f) {
        ArchiveDossierVirtuelDto dto = new ArchiveDossierVirtuelDto();
        dto.setId(f.getId());
        dto.setParentId(f.getParentId());
        dto.setNom(f.getNom());
        dto.setCreatedBy(f.getCreatedBy());
        dto.setCreatedAt(f.getCreatedAt());
        dto.setEnfantsCount(f.getEnfantsCount());
        dto.setDossiersCount(f.getDossiersCount());
        return dto;
    }

    private String normalizeNom(String nom) {
        if (nom == null || nom.isBlank()) {
            throw new BadRequestException("Le nom du dossier est obligatoire.");
        }
        String cleaned = nom.trim().replaceAll("[\\\\/:*?\"<>|]", "-");
        if (cleaned.isBlank() || cleaned.length() > 180) {
            throw new BadRequestException("Nom de dossier invalide.");
        }
        return cleaned;
    }
}
