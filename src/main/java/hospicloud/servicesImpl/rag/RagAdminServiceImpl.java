package hospicloud.servicesImpl.rag;

import hospicloud.dtos.rag.RagDocumentDto;
import hospicloud.exceptions.BadRequestException;
import hospicloud.exceptions.ForbiddenException;
import hospicloud.exceptions.ResourceNotFoundException;
import hospicloud.model.Role;
import hospicloud.model.rag.RagDocument;
import hospicloud.repositories.rag.RagDocumentRepository;
import hospicloud.repositories.rag.RagUsageRepository;
import hospicloud.security.CurrentUserContext;
import hospicloud.security.CurrentUserService;
import hospicloud.security.TenantContext;
import hospicloud.services.rag.RagAdminService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class RagAdminServiceImpl implements RagAdminService {

    private final RagDocumentRepository documentRepository;
    private final RagUsageRepository usageRepository;
    private final CurrentUserService currentUserService;
    private final String model;

    public RagAdminServiceImpl(RagDocumentRepository documentRepository,
                               RagUsageRepository usageRepository,
                               CurrentUserService currentUserService,
                               @Value("${spring.ai.openai.chat.options.model:gpt-4o-mini}") String model) {
        this.documentRepository = documentRepository;
        this.usageRepository = usageRepository;
        this.currentUserService = currentUserService;
        this.model = model;
    }

    @Override
    public List<RagDocumentDto> listDocuments() {
        Role role = CurrentUserContext.getRole();
        Integer hopitalId = resolveHopitalForAdmin(role);
        return documentRepository.listByHopital(hopitalId).stream().map(this::toDto).toList();
    }

    @Override
    public RagDocumentDto createDocument(RagDocumentDto request) {
        Role role = CurrentUserContext.getRole();
        Integer hopitalId = resolveHopitalForWrite(role);
        if (request.getTitre() == null || request.getTitre().isBlank()
                || request.getContenu() == null || request.getContenu().isBlank()) {
            throw new BadRequestException("Titre et contenu obligatoires.");
        }
        RagDocument doc = new RagDocument();
        doc.setHopitalId(hopitalId);
        doc.setCategorie(request.getCategorie() != null ? request.getCategorie() : "RECOMMANDATION");
        doc.setTitre(request.getTitre().trim());
        doc.setContenu(request.getContenu().trim());
        doc.setVersionLabel(request.getVersionLabel() != null ? request.getVersionLabel() : "1.0");
        doc.setStatut(request.getStatut() != null ? request.getStatut() : "ACTIF");
        doc.setAudience(request.getAudience() != null ? request.getAudience() : defaultAudience(role));
        doc.setTags(request.getTags());
        doc.setExpireAt(request.getExpireAt());
        Integer userId = currentUserService.getCurrentUtilisateurId();
        doc.setCreatedBy(userId);
        doc.setUpdatedBy(userId);
        Long id = documentRepository.insert(doc);
        doc.setId(id);
        return toDto(doc);
    }

    @Override
    public RagDocumentDto updateDocument(Long id, RagDocumentDto request) {
        Role role = CurrentUserContext.getRole();
        Integer hopitalId = resolveHopitalForWrite(role);
        RagDocument existing = documentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Document RAG introuvable"));
        assertCanMutate(existing, hopitalId, role);

        existing.setCategorie(request.getCategorie() != null ? request.getCategorie() : existing.getCategorie());
        existing.setTitre(request.getTitre() != null ? request.getTitre() : existing.getTitre());
        existing.setContenu(request.getContenu() != null ? request.getContenu() : existing.getContenu());
        existing.setVersionLabel(request.getVersionLabel() != null ? request.getVersionLabel() : existing.getVersionLabel());
        existing.setStatut(request.getStatut() != null ? request.getStatut() : existing.getStatut());
        existing.setAudience(request.getAudience() != null ? request.getAudience() : existing.getAudience());
        existing.setTags(request.getTags() != null ? request.getTags() : existing.getTags());
        existing.setExpireAt(request.getExpireAt());
        existing.setUpdatedBy(currentUserService.getCurrentUtilisateurId());
        existing.setHopitalId(existing.getHopitalId());
        documentRepository.update(existing);
        return toDto(existing);
    }

    @Override
    public void deleteDocument(Long id) {
        Role role = CurrentUserContext.getRole();
        Integer hopitalId = resolveHopitalForWrite(role);
        RagDocument existing = documentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Document RAG introuvable"));
        assertCanMutate(existing, hopitalId, role);
        documentRepository.delete(id, existing.getHopitalId());
    }

    @Override
    public Map<String, Object> analytics() {
        Role role = CurrentUserContext.getRole();
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("model", model);
        out.put("role", role != null ? role.name() : null);

        if (role == Role.SUPER_ADMIN) {
            out.put("scope", "PLATFORM");
            out.putAll(usageRepository.statsPlatform());
            out.put("usageByDay", usageRepository.usageByDay(null, 30));
            out.put("recentErrors", usageRepository.recentErrors(null, 20));
            out.put("monthlyTokenQuota", 5_000_000);
            out.put("security", Map.of(
                    "multiTenantIsolation", true,
                    "patientPhiHiddenFromSuperAdmin", true,
                    "crossHospitalForbidden", true
            ));
            return out;
        }

        Integer hopitalId = TenantContext.getRequiredHopitalId();
        out.put("scope", "HOSPITAL");
        out.put("hopitalId", hopitalId);
        out.putAll(usageRepository.statsForHopital(hopitalId));
        out.put("usageByDay", usageRepository.usageByDay(hopitalId, 30));
        out.put("recentErrors", usageRepository.recentErrors(hopitalId, 20));
        out.put("documents", documentRepository.listByHopital(hopitalId).size());
        out.put("monthlyTokenQuota", 500_000);
        out.put("forbiddenForAdmin", List.of(
                "diagnostic_complet_patient",
                "notes_confidentielles_medecin",
                "resultats_medicaux_detailles",
                "conversations_privees_medecin_assistant",
                "dossiers_autre_etablissement"
        ));
        return out;
    }

    @Override
    public List<String> categories() {
        return List.of(
                "PROTOCOLE", "GUIDE", "PROCEDURE_LABO", "RECOMMANDATION",
                "MEDICAMENT", "PRESCRIPTION", "URGENCE", "ADMISSION_SORTIE",
                "IMAGERIE", "ADMIN", "PLATEFORME"
        );
    }

    private Integer resolveHopitalForAdmin(Role role) {
        if (role == Role.SUPER_ADMIN) return null;
        if (role == Role.TENANT_ADMIN) return TenantContext.getRequiredHopitalId();
        throw new ForbiddenException("Accès administration RAG réservé aux administrateurs.");
    }

    private Integer resolveHopitalForWrite(Role role) {
        if (role == Role.SUPER_ADMIN) return null;
        if (role == Role.TENANT_ADMIN) return TenantContext.getRequiredHopitalId();
        throw new ForbiddenException("Écriture RAG réservée aux administrateurs.");
    }

    private void assertCanMutate(RagDocument existing, Integer hopitalId, Role role) {
        if (role == Role.SUPER_ADMIN) {
            if (existing.getHopitalId() != null) {
                throw new ForbiddenException("Le super-admin ne modifie que les documents plateforme.");
            }
            return;
        }
        if (existing.getHopitalId() == null) {
            throw new ForbiddenException("Les documents plateforme ne sont pas modifiables par l'hôpital.");
        }
        if (!existing.getHopitalId().equals(hopitalId)) {
            throw new ForbiddenException("Document hors établissement.");
        }
    }

    private String defaultAudience(Role role) {
        if (role == Role.SUPER_ADMIN) return "SUPER_ADMIN";
        if (role == Role.TENANT_ADMIN) return "ADMIN";
        return "MEDECIN";
    }

    private RagDocumentDto toDto(RagDocument d) {
        RagDocumentDto dto = new RagDocumentDto();
        dto.setId(d.getId());
        dto.setHopitalId(d.getHopitalId());
        dto.setCategorie(d.getCategorie());
        dto.setTitre(d.getTitre());
        dto.setContenu(d.getContenu());
        dto.setVersionLabel(d.getVersionLabel());
        dto.setStatut(d.getStatut());
        dto.setAudience(d.getAudience());
        dto.setTags(d.getTags());
        dto.setExpireAt(d.getExpireAt());
        dto.setUpdatedAt(d.getUpdatedAt());
        return dto;
    }
}
