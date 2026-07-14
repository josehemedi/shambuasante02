package hospicloud.servicesImpl;

import hospicloud.dtos.CreatePharmacieMedicamentRequest;
import hospicloud.dtos.PharmacieMedicamentDTO;
import hospicloud.dtos.PharmacieStockAlertDTO;
import hospicloud.dtos.PharmacyDispenseRequestDTO;
import hospicloud.exceptions.BadRequestException;
import hospicloud.exceptions.ForbiddenException;
import hospicloud.model.Role;
import hospicloud.repositories.BillingCompositionRepository;
import hospicloud.repositories.PharmacieMedicamentRepository;
import hospicloud.security.UtilisateurPrincipal;
import hospicloud.services.BillingCompositionService;
import hospicloud.services.TenantPharmacyService;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Transactional
public class TenantPharmacyServiceImpl implements TenantPharmacyService {

    private final PharmacieMedicamentRepository pharmacieMedicamentRepository;
    private final BillingCompositionRepository billingCompositionRepository;
    private final BillingCompositionService billingCompositionService;

    public TenantPharmacyServiceImpl(
            PharmacieMedicamentRepository pharmacieMedicamentRepository,
            BillingCompositionRepository billingCompositionRepository,
            BillingCompositionService billingCompositionService) {
        this.pharmacieMedicamentRepository = pharmacieMedicamentRepository;
        this.billingCompositionRepository = billingCompositionRepository;
        this.billingCompositionService = billingCompositionService;
    }

    @Override
    public List<PharmacieMedicamentDTO> listMedicaments() {
        Integer hopitalId = requireTenantAdminHopitalId();
        pharmacieMedicamentRepository.processStockAlerts(hopitalId);
        return pharmacieMedicamentRepository.listByHopital(hopitalId);
    }

    @Override
    public PharmacieMedicamentDTO createMedicament(CreatePharmacieMedicamentRequest request) {
        UtilisateurPrincipal principal = requireTenantAdminPrincipal();
        Integer hopitalId = principal.getIdHopital();
        if (hopitalId == null) {
            throw new ForbiddenException("Aucun établissement associé à votre compte");
        }
        PharmacieMedicamentDTO created = pharmacieMedicamentRepository.create(hopitalId, principal.getIdUtilisateur(), request);
        pharmacieMedicamentRepository.processStockAlerts(hopitalId);
        return pharmacieMedicamentRepository.findByIdAndHopital(created.getId(), hopitalId).orElse(created);
    }

    @Override
    public List<PharmacieStockAlertDTO> getStockAlerts() {
        Integer hopitalId = requireTenantAdminHopitalId();
        pharmacieMedicamentRepository.processStockAlerts(hopitalId);
        return pharmacieMedicamentRepository.listActiveAlerts(hopitalId);
    }

    @Override
    public Map<String, Object> dispenseToPatient(PharmacyDispenseRequestDTO request) {
        UtilisateurPrincipal principal = requireTenantAdminPrincipal();
        Integer hopitalId = principal.getIdHopital();

        if (request == null || request.getIdPatient() == null) {
            throw new BadRequestException("Le patient est obligatoire");
        }
        if (request.getItems() == null || request.getItems().isEmpty()) {
            throw new BadRequestException("Au moins un médicament est requis");
        }
        if (!billingCompositionRepository.patientBelongsToHospital(request.getIdPatient(), hopitalId)) {
            throw new ForbiddenException("Patient introuvable pour votre établissement");
        }

        int delivered = 0;
        BigDecimal total = BigDecimal.ZERO;
        for (PharmacyDispenseRequestDTO.Item item : request.getItems()) {
            if (item.getMedicamentId() == null) {
                throw new BadRequestException("Identifiant médicament manquant");
            }
            int qty = item.getQuantite() != null ? Math.max(item.getQuantite(), 1) : 1;
            PharmacieMedicamentDTO med = pharmacieMedicamentRepository
                    .findByIdAndHopital(item.getMedicamentId(), hopitalId)
                    .orElseThrow(() -> new BadRequestException("Médicament introuvable"));

            BigDecimal unitPrice = med.getPrixVente() != null ? med.getPrixVente() : BigDecimal.ZERO;
            pharmacieMedicamentRepository.decrementStock(med.getId(), hopitalId, qty);
            billingCompositionRepository.insertPharmacieDelivrance(
                    hopitalId,
                    request.getIdPatient(),
                    med.getId(),
                    qty,
                    unitPrice,
                    med.getNomMedicament(),
                    principal.getIdUtilisateur());
            delivered++;
            total = total.add(unitPrice.multiply(BigDecimal.valueOf(qty)));
        }

        pharmacieMedicamentRepository.processStockAlerts(hopitalId);

        // Taxe automatiquement le prix de vente admin sur la facture patient
        Map<String, Object> billing = billingCompositionService.chargePatientConsumptions(
                hopitalId, request.getIdPatient(), principal.getIdUtilisateur());

        Map<String, Object> result = new HashMap<>();
        result.put("itemsDelivered", delivered);
        result.put("medicamentsTotal", total);
        result.put("billing", billing);
        return result;
    }

    private UtilisateurPrincipal requireTenantAdminPrincipal() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof UtilisateurPrincipal principal)) {
            throw new AccessDeniedException("Authentification requise");
        }
        if (principal.getAppRole() != Role.TENANT_ADMIN) {
            throw new ForbiddenException("Accès réservé aux administrateurs d'hôpital");
        }
        if (principal.getIdHopital() == null) {
            throw new ForbiddenException("Aucun établissement associé à votre compte");
        }
        return principal;
    }

    private Integer requireTenantAdminHopitalId() {
        return requireTenantAdminPrincipal().getIdHopital();
    }
}
