package hospicloud.servicesImpl;

import hospicloud.dtos.TenantCashierPaymentContextDTO;
import hospicloud.dtos.TenantCashierPaymentRequestDTO;
import hospicloud.dtos.TenantCashierWorkspaceDTO;
import hospicloud.exceptions.BadRequestException;
import hospicloud.exceptions.ForbiddenException;
import hospicloud.model.Role;
import hospicloud.repositories.TenantCashierRepository;
import hospicloud.security.TenantAccessSupport;
import hospicloud.security.UtilisateurPrincipal;
import hospicloud.services.RealtimeNotificationService;
import hospicloud.services.TenantCashierService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Service
public class TenantCashierServiceImpl implements TenantCashierService {

    private static final Logger log = LoggerFactory.getLogger(TenantCashierServiceImpl.class);

    private final TenantCashierRepository tenantCashierRepository;
    private final RealtimeNotificationService realtimeNotificationService;

    public TenantCashierServiceImpl(
            TenantCashierRepository tenantCashierRepository,
            RealtimeNotificationService realtimeNotificationService) {
        this.tenantCashierRepository = tenantCashierRepository;
        this.realtimeNotificationService = realtimeNotificationService;
    }

    @Override
    @Transactional(readOnly = true)
    public TenantCashierWorkspaceDTO getWorkspace() {
        Integer hopitalId = TenantAccessSupport.requireHopitalId(Role.CAISSIER, Role.TENANT_ADMIN);
        TenantCashierWorkspaceDTO workspace = new TenantCashierWorkspaceDTO();
        workspace.setHopitalId(hopitalId);
        workspace.setHospitalName(tenantCashierRepository.findHospitalName(hopitalId));
        workspace.setKpis(tenantCashierRepository.getKpis(hopitalId));
        workspace.setQueue(tenantCashierRepository.listQueue(hopitalId, 200));
        workspace.setHistory(tenantCashierRepository.listHistory(hopitalId, 50));
        return workspace;
    }

    @Override
    @Transactional
    public Map<String, Object> collectPayment(TenantCashierPaymentRequestDTO request) {
        UtilisateurPrincipal principal = TenantAccessSupport.requirePrincipal(Role.CAISSIER, Role.TENANT_ADMIN);
        Integer hopitalId = TenantAccessSupport.resolveHopitalId(principal);

        if (request.getIdFacture() == null) {
            throw new BadRequestException("La facture est obligatoire");
        }
        BigDecimal amount = request.getAmount();
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BadRequestException("Montant invalide");
        }

        BigDecimal totalTtc = tenantCashierRepository.findFactureTotalTtc(request.getIdFacture(), hopitalId)
                .orElseThrow(() -> new ForbiddenException("Facture introuvable pour votre établissement"));

        BigDecimal alreadyPaid = tenantCashierRepository.sumPaidForFacture(request.getIdFacture(), hopitalId);
        BigDecimal balance = totalTtc.subtract(alreadyPaid);
        if (amount.compareTo(balance) > 0) {
            throw new BadRequestException("Le montant dépasse le solde à payer");
        }

        Integer modeId = tenantCashierRepository.resolveModePaiementId(hopitalId, request.getMethod());
        tenantCashierRepository.insertPaiement(
                request.getIdFacture(),
                modeId,
                amount,
                request.getReference());

        BigDecimal newPaid = alreadyPaid.add(amount);
        String newStatut = newPaid.compareTo(totalTtc) >= 0 ? "PAYE" : "PARTIEL";
        tenantCashierRepository.updateFactureStatut(
                request.getIdFacture(),
                hopitalId,
                newStatut,
                principal.getIdUtilisateur());

        notifyHospitalAdmins(
                hopitalId,
                request,
                amount,
                newStatut,
                principal);

        BigDecimal balanceAfter = totalTtc.subtract(newPaid).max(BigDecimal.ZERO);
        boolean invoiceRemoved = balanceAfter.compareTo(BigDecimal.ZERO) <= 0;

        Map<String, Object> receipt = new HashMap<>();
        receipt.put("receiptNumber", "REC-" + System.currentTimeMillis());
        receipt.put("invoiceNumber", request.getIdFacture());
        receipt.put("paidAt", LocalDateTime.now().toString());
        receipt.put("amount", amount);
        receipt.put("paymentType", request.getPaymentType());
        receipt.put("method", request.getMethod());
        receipt.put("balanceAfter", balanceAfter);

        Map<String, Object> result = new HashMap<>();
        result.put("receipt", receipt);
        result.put("invoiceRemoved", invoiceRemoved);
        return result;
    }

    private void notifyHospitalAdmins(
            Integer hopitalId,
            TenantCashierPaymentRequestDTO request,
            BigDecimal amount,
            String newStatut,
            UtilisateurPrincipal principal) {
        try {
            TenantCashierPaymentContextDTO context = tenantCashierRepository
                    .findFacturePaymentContext(request.getIdFacture(), hopitalId)
                    .orElse(new TenantCashierPaymentContextDTO(
                            String.valueOf(request.getIdFacture()),
                            "Patient"));

            String cashierLabel = StringUtils.hasText(principal.getUsername())
                    ? principal.getUsername()
                    : "Caisse";

            realtimeNotificationService.notifyPaymentRecorded(
                    hopitalId,
                    request.getIdFacture(),
                    amount,
                    context.getInvoiceNumber(),
                    context.getPatientName(),
                    cashierLabel,
                    request.getMethod(),
                    newStatut,
                    principal.getIdUtilisateur());
        } catch (RuntimeException e) {
            log.warn("Notification encaissement non envoyée pour facture {}: {}", request.getIdFacture(), e.getMessage());
        }
    }
}
