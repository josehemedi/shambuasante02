package hospicloud.servicesImpl;



import hospicloud.dtos.ChangeSubscriptionPlanRequest;

import hospicloud.dtos.HospitalPlanCatalogDTO;

import hospicloud.dtos.TenantSubscriptionDTO;

import hospicloud.dtos.TenantSubscriptionHistoryDTO;

import hospicloud.exceptions.BadRequestException;

import hospicloud.exceptions.ForbiddenException;

import hospicloud.exceptions.ResourceNotFoundException;

import hospicloud.model.Hopital;

import hospicloud.model.Role;

import hospicloud.repositories.AbonnementRepository;

import hospicloud.repositories.HopitalRepository;

import hospicloud.saas.SaasPlanRegistry;

import hospicloud.security.UtilisateurPrincipal;

import hospicloud.services.SaasPlanService;

import hospicloud.services.TenantSubscriptionService;

import org.springframework.security.access.AccessDeniedException;

import org.springframework.security.core.Authentication;

import org.springframework.security.core.context.SecurityContextHolder;

import org.springframework.stereotype.Service;

import org.springframework.transaction.annotation.Transactional;



import java.math.BigDecimal;

import java.time.LocalDateTime;

import java.util.List;

import java.util.Map;

import java.util.Optional;



@Service

@Transactional

public class TenantSubscriptionServiceImpl implements TenantSubscriptionService {



    private static final int BILLING_CYCLE_DAYS = 30;



    private final AbonnementRepository abonnementRepository;

    private final HopitalRepository hopitalRepository;

    private final SaasPlanService saasPlanService;



    public TenantSubscriptionServiceImpl(AbonnementRepository abonnementRepository,

                                         HopitalRepository hopitalRepository,

                                         SaasPlanService saasPlanService) {

        this.abonnementRepository = abonnementRepository;

        this.hopitalRepository = hopitalRepository;

        this.saasPlanService = saasPlanService;

    }



    @Override

    @Transactional(readOnly = true)

    public TenantSubscriptionDTO getCurrentSubscription() {

        Integer hopitalId = requireTenantAdminHopitalId();

        TenantSubscriptionDTO dto = abonnementRepository.findActiveSubscription(hopitalId)

                .orElseGet(() -> buildFallbackSubscription(hopitalId));

        return saasPlanService.enrichSubscription(dto);

    }



    @Override

    @Transactional(readOnly = true)

    public List<HospitalPlanCatalogDTO> listAvailablePlans() {

        requireTenantAdminHopitalId();

        Map<String, Long> subscribers = new java.util.HashMap<>();
        for (hospicloud.dtos.PlanDistributionItemDTO item : abonnementRepository.getPlanDistribution()) {
            String normalized = saasPlanService.normalizePlanName(item.getName());
            subscribers.merge(normalized, item.getValue(), Long::sum);
        }

        return saasPlanService.buildCatalogPlans(

                subscribers.getOrDefault(SaasPlanRegistry.BASIC, 0L),

                subscribers.getOrDefault(SaasPlanRegistry.PROFESSIONNEL, 0L),

                subscribers.getOrDefault(SaasPlanRegistry.ENTREPRISE, 0L));

    }



    @Override

    @Transactional(readOnly = true)

    public List<TenantSubscriptionHistoryDTO> getHistory(int limit) {

        Integer hopitalId = requireTenantAdminHopitalId();

        return abonnementRepository.findSubscriptionHistory(hopitalId, limit);

    }



    @Override

    public TenantSubscriptionDTO repaySubscription() {

        Integer hopitalId = requireTenantAdminHopitalId();

        Optional<TenantSubscriptionDTO> current = abonnementRepository.findActiveSubscription(hopitalId);

        String planNom = current.map(TenantSubscriptionDTO::getPlanNom).orElse(SaasPlanRegistry.BASIC);

        BigDecimal price = resolvePlanPrice(planNom);

        return saasPlanService.enrichSubscription(

                activateNewSubscription(hopitalId, current, planNom, price, "renew"));

    }



    @Override

    public TenantSubscriptionDTO changePlan(ChangeSubscriptionPlanRequest request) {

        Integer hopitalId = requireTenantAdminHopitalId();

        String planNom = normalizePlan(request.getPlanNom());

        BigDecimal price = resolvePlanPrice(planNom);

        Optional<TenantSubscriptionDTO> current = abonnementRepository.findActiveSubscription(hopitalId);

        if (current.isPresent() && planNom.equalsIgnoreCase(saasPlanService.normalizePlanName(current.get().getPlanNom()))) {

            throw new BadRequestException("Vous êtes déjà abonné au forfait " + planNom);

        }

        return saasPlanService.enrichSubscription(

                activateNewSubscription(hopitalId, current, planNom, price, "change_plan"));

    }



    private TenantSubscriptionDTO activateNewSubscription(Integer hopitalId,

                                                          Optional<TenantSubscriptionDTO> current,

                                                          String planNom,

                                                          BigDecimal price,

                                                          String action) {

        current.ifPresent(sub -> abonnementRepository.closeSubscription(sub.getIdAbonnement()));



        LocalDateTime nextDue = LocalDateTime.now().plusDays(BILLING_CYCLE_DAYS);

        abonnementRepository.creerAbonnementAvecEcheance(hopitalId, planNom, price, nextDue);



        Hopital hopital = hopitalRepository.rechercherhopitalParId(hopitalId.longValue());

        if (hopital != null && !hopital.isEstActif()) {

            hopital.setEstActif(true);

            hopitalRepository.modifier(hopital);

        }



        return abonnementRepository.findActiveSubscription(hopitalId)

                .orElseThrow(() -> new IllegalStateException("Impossible d'activer l'abonnement (" + action + ")"));

    }



    private TenantSubscriptionDTO buildFallbackSubscription(Integer hopitalId) {

        Hopital hopital = hopitalRepository.rechercherhopitalParId(hopitalId.longValue());

        if (hopital == null) {

            throw new ResourceNotFoundException("Établissement introuvable");

        }

        TenantSubscriptionDTO dto = new TenantSubscriptionDTO();

        dto.setIdHopital(hopitalId);

        dto.setHospitalName(hopital.getNom());

        dto.setPlanNom(SaasPlanRegistry.BASIC);

        dto.setMontantMensuel(BigDecimal.ZERO);

        dto.setStatut("actif");

        dto.setUiStatus(hopital.isEstActif() ? "trial" : "suspended");

        dto.setNeedsPayment(true);

        dto.setDaysUntilDue(0);



        abonnementRepository.findSubscriptionHistory(hopitalId, 1).stream()

                .findFirst()

                .ifPresent(last -> {

                    if (last.getPlanNom() != null && !last.getPlanNom().isBlank()) {

                        dto.setPlanNom(saasPlanService.normalizePlanName(last.getPlanNom()));

                    }

                    if (last.getMontantMensuel() != null && last.getMontantMensuel().compareTo(BigDecimal.ZERO) > 0) {

                        dto.setMontantMensuel(last.getMontantMensuel());

                    }

                });



        return dto;

    }



    private String normalizePlan(String planNom) {

        if (planNom == null || planNom.isBlank()) {

            throw new BadRequestException("Le forfait est requis");

        }

        String normalized = saasPlanService.normalizePlanName(planNom);

        if (!SaasPlanRegistry.allowedPlanNames().contains(normalized)) {

            throw new BadRequestException("Forfait inconnu : " + planNom);

        }

        return normalized;

    }



    private BigDecimal resolvePlanPrice(String planNom) {

        return saasPlanService.getDefinition(planNom).monthlyPrice();

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

