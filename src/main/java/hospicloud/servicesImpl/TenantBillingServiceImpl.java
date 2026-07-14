package hospicloud.servicesImpl;

import hospicloud.dtos.TenantBillingOverviewDTO;
import hospicloud.model.Role;
import hospicloud.repositories.TenantBillingRepository;
import hospicloud.security.TenantAccessSupport;
import hospicloud.services.TenantBillingService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class TenantBillingServiceImpl implements TenantBillingService {

    private final TenantBillingRepository tenantBillingRepository;

    public TenantBillingServiceImpl(TenantBillingRepository tenantBillingRepository) {
        this.tenantBillingRepository = tenantBillingRepository;
    }

    @Override
    public TenantBillingOverviewDTO getOverview() {
        Integer hopitalId = TenantAccessSupport.requireHopitalId(Role.TENANT_ADMIN);
        TenantBillingOverviewDTO overview = new TenantBillingOverviewDTO();
        overview.setHopitalId(hopitalId);
        overview.setHospitalName(tenantBillingRepository.findHospitalName(hopitalId));
        overview.setKpis(tenantBillingRepository.getKpis(hopitalId));
        overview.setInvoices(tenantBillingRepository.listInvoices(hopitalId, 500));
        overview.setRevenueSeries(tenantBillingRepository.getRevenueSeries(hopitalId, 6));
        return overview;
    }
}
