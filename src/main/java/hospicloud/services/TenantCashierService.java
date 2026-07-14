package hospicloud.services;

import hospicloud.dtos.TenantCashierPaymentRequestDTO;
import hospicloud.dtos.TenantCashierWorkspaceDTO;

import java.util.Map;

public interface TenantCashierService {
    TenantCashierWorkspaceDTO getWorkspace();
    Map<String, Object> collectPayment(TenantCashierPaymentRequestDTO request);
}
