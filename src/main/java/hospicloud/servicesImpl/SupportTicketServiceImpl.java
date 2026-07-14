package hospicloud.servicesImpl;

import hospicloud.dtos.SupportTicketCreateDTO;
import hospicloud.dtos.SupportTicketDTO;
import hospicloud.dtos.SupportTicketStatusUpdateDTO;
import hospicloud.model.Role;
import hospicloud.repositories.SupportTicketRepository;
import hospicloud.security.TenantContext;
import hospicloud.security.UtilisateurPrincipal;
import hospicloud.services.SupportTicketService;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SupportTicketServiceImpl implements SupportTicketService {

    private final SupportTicketRepository supportTicketRepository;

    public SupportTicketServiceImpl(SupportTicketRepository supportTicketRepository) {
        this.supportTicketRepository = supportTicketRepository;
    }

    @Override
    public SupportTicketDTO create(SupportTicketCreateDTO request) {
        if (request.getSubject() == null || request.getSubject().isBlank()) {
            throw new IllegalArgumentException("Le sujet est requis");
        }
        if (request.getDescription() == null || request.getDescription().isBlank()) {
            throw new IllegalArgumentException("La description est requise");
        }

        Integer hopitalId = TenantContext.getRequiredHopitalId();
        Integer userId = null;
        String email = null;
        String role = null;

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof UtilisateurPrincipal principal) {
            userId = principal.getIdUtilisateur();
            email = principal.getUsername();
            if (principal.getAppRole() != null) {
                role = principal.getAppRole().name();
            }
        }

        Long id = supportTicketRepository.insert(
                hopitalId,
                userId,
                email,
                role,
                request.getSubject().trim(),
                request.getDescription().trim(),
                request.getModule(),
                request.getPriority(),
                request.getRequestId());

        return supportTicketRepository.findById(id)
                .orElseThrow(() -> new IllegalStateException("Ticket créé introuvable"));
    }

    @Override
    public List<SupportTicketDTO> search(Integer hopitalId, String status, String module,
                                         String priority, String requestId, String search, int limit) {
        Integer scopedHopitalId = resolveHopitalScope(hopitalId);
        return supportTicketRepository.search(scopedHopitalId, status, module, priority, requestId, search, limit);
    }

    @Override
    public SupportTicketDTO updateStatus(Long id, SupportTicketStatusUpdateDTO update) {
        if (!supportTicketRepository.updateStatus(id, update)) {
            throw new IllegalArgumentException("Ticket introuvable: " + id);
        }
        return supportTicketRepository.findById(id)
                .orElseThrow(() -> new IllegalStateException("Ticket introuvable après mise à jour"));
    }

    private Integer resolveHopitalScope(Integer requestedHopitalId) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof UtilisateurPrincipal principal
                && principal.getAppRole() == Role.SUPER_ADMIN) {
            return requestedHopitalId;
        }
        return TenantContext.getRequiredHopitalId();
    }
}
