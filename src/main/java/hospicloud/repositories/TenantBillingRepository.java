package hospicloud.repositories;

import hospicloud.dtos.TenantBillingInvoiceDTO;
import hospicloud.dtos.TenantBillingKpisDTO;
import hospicloud.dtos.TenantBillingRevenuePointDTO;

import java.util.List;

public interface TenantBillingRepository {
    String findHospitalName(Integer idHopital);
    TenantBillingKpisDTO getKpis(Integer idHopital);
    List<TenantBillingInvoiceDTO> listInvoices(Integer idHopital, int limit);
    List<TenantBillingRevenuePointDTO> getRevenueSeries(Integer idHopital, int months);
}
