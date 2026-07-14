package hospicloud.servicesImpl;

import hospicloud.dtos.TenantSubscriptionDTO;
import hospicloud.model.Hopital;
import hospicloud.repositories.AbonnementRepository;
import hospicloud.repositories.HopitalRepository;
import hospicloud.services.TenantSubscriptionAccessService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class TenantSubscriptionAccessServiceImpl implements TenantSubscriptionAccessService {

    private final AbonnementRepository abonnementRepository;
    private final HopitalRepository hopitalRepository;

    public TenantSubscriptionAccessServiceImpl(AbonnementRepository abonnementRepository,
                                               HopitalRepository hopitalRepository) {
        this.abonnementRepository = abonnementRepository;
        this.hopitalRepository = hopitalRepository;
    }

    @Override
    public boolean isPlatformAccessRestricted(Integer hopitalId) {
        if (hopitalId == null) {
            return false;
        }
        String uiStatus = abonnementRepository.findActiveSubscription(hopitalId)
                .map(TenantSubscriptionDTO::getUiStatus)
                .orElseGet(() -> resolveFallbackUiStatus(hopitalId));
        return "expired".equals(uiStatus) || "suspended".equals(uiStatus);
    }

    private String resolveFallbackUiStatus(Integer hopitalId) {
        Hopital hopital = hopitalRepository.rechercherhopitalParId(hopitalId.longValue());
        if (hopital == null) {
            return "active";
        }
        return hopital.isEstActif() ? "trial" : "suspended";
    }
}
