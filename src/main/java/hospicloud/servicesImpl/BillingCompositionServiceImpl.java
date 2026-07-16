package hospicloud.servicesImpl;

import hospicloud.dtos.BillingAdvanceRequestDTO;
import hospicloud.dtos.BillingComposeRequestDTO;
import hospicloud.dtos.BillingDraftLineDTO;
import hospicloud.exceptions.BadRequestException;
import hospicloud.exceptions.ForbiddenException;
import hospicloud.model.Role;
import hospicloud.repositories.BillingCompositionRepository;
import hospicloud.repositories.TenantCashierRepository;
import hospicloud.security.TenantAccessSupport;
import hospicloud.security.UtilisateurPrincipal;
import hospicloud.services.BillingCompositionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class BillingCompositionServiceImpl implements BillingCompositionService {

    private static final Logger log = LoggerFactory.getLogger(BillingCompositionServiceImpl.class);

    private final BillingCompositionRepository billingRepository;
    private final TenantCashierRepository tenantCashierRepository;

    public BillingCompositionServiceImpl(
            BillingCompositionRepository billingRepository,
            TenantCashierRepository tenantCashierRepository) {
        this.billingRepository = billingRepository;
        this.tenantCashierRepository = tenantCashierRepository;
    }

    @Override
    @Transactional
    public Map<String, Object> composeInvoice(BillingComposeRequestDTO request) {
        UtilisateurPrincipal principal = TenantAccessSupport.requirePrincipal(Role.CAISSIER, Role.TENANT_ADMIN);
        Integer hopitalId = TenantAccessSupport.resolveHopitalId(principal);
        Integer idPatient = resolvePatientId(request, hopitalId);
        return doCompose(
                hopitalId,
                idPatient,
                request.getIdFacture(),
                request.getMontantRemise(),
                request.getTauxAssuranceOverride(),
                request.isRebuildExistingLines(),
                principal.getIdUtilisateur());
    }

    @Override
    @Transactional
    public Map<String, Object> chargePatientConsumptions(Integer hopitalId, Integer idPatient, Integer actorUserId) {
        if (hopitalId == null || idPatient == null) {
            return Map.of("skipped", true);
        }
        try {
            return doCompose(hopitalId, idPatient, null, null, null, false, actorUserId);
        } catch (RuntimeException e) {
            log.warn("Auto-facturation patient {} hopital {}: {}", idPatient, hopitalId, e.getMessage());
            return Map.of("error", e.getMessage());
        }
    }

    private Map<String, Object> doCompose(
            Integer hopitalId,
            Integer idPatient,
            Integer idFacture,
            BigDecimal montantRemiseRequest,
            BigDecimal tauxAssuranceOverride,
            boolean rebuildExistingLines,
            Integer actorUserId) {

        if (!billingRepository.patientBelongsToHospital(idPatient, hopitalId)) {
            throw new ForbiddenException("Patient introuvable pour votre établissement");
        }

        if (idFacture == null) {
            idFacture = billingRepository.findOpenFactureId(idPatient, hopitalId).orElse(null);
        } else {
            Integer owner = billingRepository.findFacturePatientId(idFacture, hopitalId)
                    .orElseThrow(() -> new ForbiddenException("Facture introuvable"));
            if (!owner.equals(idPatient)) {
                throw new BadRequestException("La facture ne correspond pas au patient");
            }
        }

        boolean created = false;
        if (idFacture == null) {
            String numero = billingRepository.nextFactureNumber(hopitalId);
            idFacture = billingRepository.insertFacture(
                    idPatient,
                    hopitalId,
                    numero,
                    BigDecimal.ZERO,
                    BigDecimal.ZERO,
                    BigDecimal.ZERO,
                    actorUserId);
            created = true;
        } else if (rebuildExistingLines) {
            billingRepository.deleteAutoLines(idFacture, hopitalId);
        }

        List<BillingDraftLineDTO> newLines = collectUnbilledLines(idPatient, hopitalId);
        for (BillingDraftLineDTO line : newLines) {
            billingRepository.insertFactureItem(idFacture, hopitalId, line);
        }

        BigDecimal sousTotal = billingRepository.sumFactureItems(idFacture, hopitalId);
        BigDecimal tauxAssurance = tauxAssuranceOverride != null
                ? tauxAssuranceOverride
                : billingRepository.findPatientInsuranceRate(idPatient, hopitalId);
        if (tauxAssurance.compareTo(BigDecimal.ZERO) < 0) tauxAssurance = BigDecimal.ZERO;
        if (tauxAssurance.compareTo(BigDecimal.valueOf(100)) > 0) tauxAssurance = BigDecimal.valueOf(100);

        BigDecimal montantAssurance = sousTotal
                .multiply(tauxAssurance)
                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);

        BigDecimal montantRemise = montantRemiseRequest != null
                ? montantRemiseRequest.max(BigDecimal.ZERO)
                : billingRepository.findFactureRemise(idFacture, hopitalId).orElse(BigDecimal.ZERO);

        BigDecimal unappliedAdvances = billingRepository.sumUnappliedAdvances(idPatient, hopitalId);
        if (unappliedAdvances.compareTo(BigDecimal.ZERO) > 0) {
            billingRepository.applyAdvancesToFacture(idPatient, hopitalId, idFacture);
        }
        BigDecimal avances = billingRepository.sumAdvancesForFacture(idFacture, hopitalId);

        BigDecimal netPatient = sousTotal
                .subtract(montantAssurance)
                .subtract(montantRemise)
                .subtract(avances)
                .max(BigDecimal.ZERO)
                .setScale(2, RoundingMode.HALF_UP);

        BigDecimal alreadyPaid = tenantCashierRepository.sumPaidForFacture(idFacture, hopitalId);
        String statut;
        if (netPatient.compareTo(BigDecimal.ZERO) <= 0 || alreadyPaid.compareTo(netPatient) >= 0) {
            statut = "PAYE";
        } else if (alreadyPaid.compareTo(BigDecimal.ZERO) > 0) {
            statut = "PARTIEL";
        } else {
            statut = "IMPAYE";
        }

        billingRepository.updateFactureComposition(
                idFacture,
                hopitalId,
                sousTotal,
                tauxAssurance,
                montantAssurance,
                montantRemise,
                avances,
                sousTotal,
                netPatient,
                statut,
                actorUserId);

        Map<String, Object> result = new HashMap<>();
        result.put("idFacture", idFacture);
        result.put("created", created);
        result.put("linesAdded", newLines.size());
        result.put("sousTotalSoins", sousTotal);
        result.put("tauxAssurance", tauxAssurance);
        result.put("montantAssurance", montantAssurance);
        result.put("montantRemise", montantRemise);
        result.put("montantAvances", avances);
        result.put("montantPatient", netPatient);
        result.put("alreadyPaid", alreadyPaid);
        result.put("balanceDue", netPatient.subtract(alreadyPaid).max(BigDecimal.ZERO));
        result.put("statut", statut);
        result.put("breakdown", Map.of(
                "consultation", countByCat(newLines, "CONSULTATION"),
                "examens", countByCat(newLines, "EXAMEN"),
                "medicaments", countByCat(newLines, "MEDICAMENT"),
                "hospitalisation", countByCat(newLines, "HOSPITALISATION"),
                "actes", countByCat(newLines, "ACTE_MEDICAL"),
                "autres", countByCat(newLines, "AUTRE")
        ));
        return result;
    }

    @Override
    @Transactional
    public Map<String, Object> recordAdvance(BillingAdvanceRequestDTO request) {
        UtilisateurPrincipal principal = TenantAccessSupport.requirePrincipal(Role.CAISSIER, Role.TENANT_ADMIN);
        Integer hopitalId = TenantAccessSupport.resolveHopitalId(principal);

        if (request.getIdPatient() == null) {
            throw new BadRequestException("Le patient est obligatoire");
        }
        if (request.getMontant() == null || request.getMontant().compareTo(BigDecimal.ZERO) <= 0) {
            throw new BadRequestException("Montant d'avance invalide");
        }
        if (!billingRepository.patientBelongsToHospital(request.getIdPatient(), hopitalId)) {
            throw new ForbiddenException("Patient introuvable pour votre établissement");
        }

        int id = billingRepository.insertAdvance(
                hopitalId,
                request.getIdPatient(),
                request.getMontant().setScale(2, RoundingMode.HALF_UP),
                request.getMethod(),
                request.getReference(),
                request.getNotes());

        chargePatientConsumptions(hopitalId, request.getIdPatient(), principal.getIdUtilisateur());

        Map<String, Object> result = new HashMap<>();
        result.put("idAvance", id);
        result.put("idPatient", request.getIdPatient());
        result.put("montant", request.getMontant());
        result.put("unappliedTotal", billingRepository.sumUnappliedAdvances(request.getIdPatient(), hopitalId));
        return result;
    }

    @Override
    @Transactional
    public Map<String, Object> refreshOpenInvoices() {
        UtilisateurPrincipal principal = TenantAccessSupport.requirePrincipal(Role.CAISSIER, Role.TENANT_ADMIN);
        Integer hopitalId = TenantAccessSupport.resolveHopitalId(principal);

        List<Integer> patients = billingRepository.listPatientsWithOpenFactures(hopitalId);
        int refreshed = 0;
        for (Integer idPatient : patients) {
            doCompose(hopitalId, idPatient, null, null, null, false, principal.getIdUtilisateur());
            refreshed++;
        }

        Map<String, Object> result = new HashMap<>();
        result.put("refreshed", refreshed);
        return result;
    }

    private Integer resolvePatientId(BillingComposeRequestDTO request, Integer hopitalId) {
        if (request.getIdPatient() != null) {
            return request.getIdPatient();
        }
        if (request.getIdFacture() != null) {
            return billingRepository.findFacturePatientId(request.getIdFacture(), hopitalId)
                    .orElseThrow(() -> new ForbiddenException("Facture introuvable"));
        }
        throw new BadRequestException("Le patient ou la facture est obligatoire");
    }

    private List<BillingDraftLineDTO> collectUnbilledLines(Integer idPatient, Integer idHopital) {
        List<BillingDraftLineDTO> lines = new ArrayList<>();
        lines.addAll(billingRepository.collectUnbilledConsultations(idPatient, idHopital));
        lines.addAll(billingRepository.collectUnbilledAnalyses(idPatient, idHopital));
        lines.addAll(billingRepository.collectUnbilledPharmacy(idPatient, idHopital));
        lines.addAll(billingRepository.collectUnbilledHospitalization(idPatient, idHopital));
        lines.addAll(billingRepository.collectUnbilledActes(idPatient, idHopital));
        return lines;
    }

    private int countByCat(List<BillingDraftLineDTO> lines, String cat) {
        return (int) lines.stream().filter(l -> cat.equals(l.getCategorie())).count();
    }
}
