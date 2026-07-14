package hospicloud.servicesImpl;

import hospicloud.dtos.LaboratoryOverviewDTO;
import hospicloud.exceptions.ForbiddenException;
import hospicloud.model.Role;
import hospicloud.repositories.LaboratoryRepository;
import hospicloud.security.UtilisateurPrincipal;
import hospicloud.services.TenantLaboratoryService;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class TenantLaboratoryServiceImpl implements TenantLaboratoryService {

    private final LaboratoryRepository laboratoryRepository;

    public TenantLaboratoryServiceImpl(LaboratoryRepository laboratoryRepository) {
        this.laboratoryRepository = laboratoryRepository;
    }

    @Override
    public LaboratoryOverviewDTO getOverview() {
        Integer hopitalId = requireTenantAdminHopitalId();
        LaboratoryOverviewDTO overview = new LaboratoryOverviewDTO();
        overview.setKpis(laboratoryRepository.getKpis(hopitalId));
        overview.setTests(laboratoryRepository.listTests(hopitalId, 200));
        return overview;
    }

    private Integer requireTenantAdminHopitalId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof UtilisateurPrincipal principal)) {
            throw new AccessDeniedException("Authentification requise");
        }
        if (principal.getAppRole() != Role.TENANT_ADMIN) {
            throw new ForbiddenException("Accès réservé aux administrateurs d'hôpital");
        }
        Integer hopitalId = principal.getIdHopital();
        if (hopitalId == null) {
            throw new ForbiddenException("Aucun établissement associé à votre compte");
        }
        return hopitalId;
    }
}
