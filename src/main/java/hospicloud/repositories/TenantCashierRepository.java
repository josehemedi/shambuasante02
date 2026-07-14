package hospicloud.repositories;

import hospicloud.dtos.CashierInvoiceDetailDTO;
import hospicloud.dtos.TenantCashierHistoryItemDTO;
import hospicloud.dtos.TenantCashierKpisDTO;
import hospicloud.dtos.TenantCashierPaymentContextDTO;
import hospicloud.dtos.TenantCashierQueueItemDTO;
import hospicloud.dtos.reporting.CashierInvoiceLineRowDTO;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface TenantCashierRepository {
    String findHospitalName(Integer idHopital);
    TenantCashierKpisDTO getKpis(Integer idHopital);
    List<TenantCashierQueueItemDTO> listQueue(Integer idHopital, int limit);
    List<TenantCashierHistoryItemDTO> listHistory(Integer idHopital, int limit);
    Optional<BigDecimal> findFactureTotalTtc(Integer idFacture, Integer idHopital);
    BigDecimal sumPaidForFacture(Integer idFacture, Integer idHopital);
    Integer resolveModePaiementId(Integer idHopital, String method);
    int insertPaiement(Integer idFacture, Integer idModePaiement, BigDecimal amount, String reference);
    boolean updateFactureStatut(Integer idFacture, Integer idHopital, String statut, Integer idCaissier);
    Optional<TenantCashierPaymentContextDTO> findFacturePaymentContext(Integer idFacture, Integer idHopital);
    Optional<CashierInvoiceDetailDTO> findInvoiceDetail(Integer idFacture, Integer idHopital);
    List<CashierInvoiceLineRowDTO> findInvoiceLines(Integer idFacture, Integer idHopital);
}
