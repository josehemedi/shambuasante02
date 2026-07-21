package hospicloud.servicesImpl;

import hospicloud.dtos.HospitalActivityDTO;
import hospicloud.dtos.HospitalCreateDTO;
import hospicloud.dtos.HospitalDetailDTO;
import hospicloud.dtos.HospitalOverviewDTO;
import hospicloud.dtos.HospitalPlanCatalogDTO;
import hospicloud.dtos.HospitalPlatformStatsDTO;
import hospicloud.dtos.HospitalUpdateDTO;
import hospicloud.model.Hopital;
import hospicloud.repositories.AbonnementRepository;
import hospicloud.repositories.HopitalPlatformRepository;
import hospicloud.repositories.HopitalRepository;
import hospicloud.repositories.UtilisateurRepository;
import hospicloud.services.AccountInvitationService;
import hospicloud.services.HopitalPlatformService;
import hospicloud.services.HospitalService;
import hospicloud.services.SaasPlanService;
import hospicloud.dtos.InviteHospitalAdminRequest;
import hospicloud.model.Utilisateur;
import hospicloud.saas.SaasPlanRegistry;
import hospicloud.exceptions.ResourceNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.Normalizer;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class HopitalPlatformServiceImpl implements HopitalPlatformService {

    private static final Set<String> ALLOWED_TYPES = Set.of(
            "CLINIQUE", "HOPITAL_GENERAL", "CENTRE_MEDICAL", "MATERNITE", "LABORATOIRE");

    private final HopitalPlatformRepository platformRepository;
    private final HopitalRepository hopitalRepository;
    private final UtilisateurRepository utilisateurRepository;
    private final AbonnementRepository abonnementRepository;
    private final HospitalService hospitalService;
    private final SaasPlanService saasPlanService;
    private final AccountInvitationService accountInvitationService;

    public HopitalPlatformServiceImpl(HopitalPlatformRepository platformRepository,
                                      HopitalRepository hopitalRepository,
                                      UtilisateurRepository utilisateurRepository,
                                      AbonnementRepository abonnementRepository,
                                      HospitalService hospitalService,
                                      SaasPlanService saasPlanService,
                                      AccountInvitationService accountInvitationService) {
        this.platformRepository = platformRepository;
        this.hopitalRepository = hopitalRepository;
        this.utilisateurRepository = utilisateurRepository;
        this.abonnementRepository = abonnementRepository;
        this.hospitalService = hospitalService;
        this.saasPlanService = saasPlanService;
        this.accountInvitationService = accountInvitationService;
    }

    @Override
    public HospitalPlatformStatsDTO getPlatformStats() {
        List<HospitalOverviewDTO> overview = platformRepository.listOverview();
        long total = platformRepository.countTotal();
        long active = overview.stream().filter(h -> "active".equals(h.getStatus())).count();
        long trial = overview.stream().filter(h -> "trial".equals(h.getStatus())).count();
        long suspended = overview.stream().filter(h -> "suspended".equals(h.getStatus())).count();
        long totalUsers = overview.stream().mapToLong(HospitalOverviewDTO::getUsers).sum();
        BigDecimal totalMrr = overview.stream()
                .map(HospitalOverviewDTO::getMrr)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        LocalDate firstDayCurrentMonth = YearMonth.now().atDay(1);
        long prevTotal = hopitalRepository.countAllActifsExistingBefore(firstDayCurrentMonth)
                + countSuspendedBefore(overview, firstDayCurrentMonth);
        long prevUsers = utilisateurRepository.countAllActiveExistingBefore(firstDayCurrentMonth);
        BigDecimal prevMrr = abonnementRepository.calculatePlatformMrrAtEndOfMonth(
                YearMonth.now().minusMonths(1).atEndOfMonth());

        HospitalPlatformStatsDTO stats = new HospitalPlatformStatsDTO();
        stats.setTotal(total);
        stats.setActive(active);
        stats.setTrial(trial);
        stats.setSuspended(suspended);
        stats.setTotalUsers(totalUsers);
        stats.setTotalMrr(totalMrr);
        stats.setDeltaTotal(calculatePercentage(prevTotal, total));
        stats.setDeltaActive(calculatePercentage(
                overview.stream().filter(h -> "active".equals(h.getStatus())
                        && h.getJoined() != null && h.getJoined().toLocalDate().isBefore(firstDayCurrentMonth)).count(),
                active));
        stats.setDeltaTrial(calculatePercentage(0, trial));
        stats.setDeltaSuspended(calculatePercentage(0, suspended));
        stats.setDeltaTotalUsers(calculatePercentage(prevUsers, totalUsers));
        stats.setDeltaTotalMrr(calculatePercentage(prevMrr, totalMrr));
        return stats;
    }

    @Override
    public List<HospitalOverviewDTO> listOverview() {
        return platformRepository.listOverview();
    }

    @Override
    public HospitalDetailDTO getHospitalDetail(Integer idHopital) {
        return platformRepository.findDetailById(idHopital)
                .orElseThrow(() -> new ResourceNotFoundException("Hôpital introuvable pour id=" + idHopital));
    }

    @Override
    public List<HospitalActivityDTO> listRecentActivity(int limit) {
        return platformRepository.listRecentActivity(limit);
    }

    @Override
    public List<HospitalPlanCatalogDTO> listPlansCatalog() {
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
    @Transactional(readOnly = false)
    public HospitalOverviewDTO createHospital(HospitalCreateDTO dto) {
        String nom = dto.getNom() != null ? dto.getNom().trim() : "";
        if (nom.isEmpty()) {
            throw new IllegalArgumentException("Le nom de l'hôpital est requis.");
        }
        if (dto.getPays() == null || dto.getPays().isBlank()) {
            throw new IllegalArgumentException("Le pays est requis.");
        }
        if (dto.getVille() == null || dto.getVille().isBlank()) {
            throw new IllegalArgumentException("La ville est requise.");
        }
        if (dto.getEmail() == null || dto.getEmail().isBlank()) {
            throw new IllegalArgumentException("L'email est requis.");
        }
        if (hopitalRepository.existsByEmail(dto.getEmail())) {
            throw new IllegalStateException("Un hôpital avec cet email existe déjà.");
        }

        String type = normalizeType(dto.getType());
        String planNom = normalizePlan(dto.getPlanNom());
        BigDecimal planPrice = saasPlanService.getDefinition(planNom).monthlyPrice();

        String sousDomaine = normalizeSousDomaine(dto.getSousDomaine(), dto.getNom());
        if (hopitalRepository.existsBySousDomaine(sousDomaine)) {
            throw new IllegalStateException("Ce sous-domaine est déjà utilisé.");
        }

        String nomCommercial = dto.getNomCommercial() != null && !dto.getNomCommercial().isBlank()
                ? dto.getNomCommercial().trim() : nom;

        Hopital hopital = new Hopital();
        hopital.setNom(nom);
        hopital.setPays(dto.getPays().trim());
        hopital.setVille(dto.getVille().trim());
        hopital.setEmail(dto.getEmail().trim());
        hopital.setTelephone(dto.getTelephone() != null && !dto.getTelephone().isBlank()
                ? dto.getTelephone().trim() : null);
        hopital.setAdresse(dto.getAdresse() != null && !dto.getAdresse().isBlank()
                ? dto.getAdresse().trim() : null);
        hopital.setAdresseComplete(dto.getAdresseComplete() != null && !dto.getAdresseComplete().isBlank()
                ? dto.getAdresseComplete().trim()
                : hopital.getAdresse());
        hopital.setLogoUrl(dto.getLogoUrl() != null && !dto.getLogoUrl().isBlank()
                ? dto.getLogoUrl().trim() : null);
        hopital.setNomCommercial(nomCommercial);
        hopital.setType(type);
        hopital.setEstActif(dto.isEstActif());
        hopital.setSousDomaine(sousDomaine);

        hospitalService.enresgitrerHopital(hopital);
        if (hopital.getIdHopital() == null) {
            throw new IllegalStateException("Impossible de créer l'hôpital.");
        }

        abonnementRepository.creerAbonnement(hopital.getIdHopital(), planNom, planPrice);

        // Invite l'administrateur d'hôpital (compte inactif + email d'activation).
        accountInvitationService.inviteHospitalAdmin(
                hopital.getIdHopital(),
                dto.getAdminPrenom(),
                dto.getAdminNom(),
                dto.getAdminEmail(),
                dto.getAdminTelephone());

        return platformRepository.listOverview().stream()
                .filter(h -> hopital.getIdHopital().equals(h.getIdHopital()))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Hôpital créé mais introuvable dans la liste."));
    }

    @Override
    @Transactional(readOnly = false)
    public Utilisateur inviteHospitalAdmin(Integer idHopital, InviteHospitalAdminRequest request) {
        if (hopitalRepository.rechercherhopitalParId(idHopital.longValue()) == null) {
            throw new ResourceNotFoundException("Hôpital introuvable pour id=" + idHopital);
        }
        return accountInvitationService.inviteHospitalAdmin(
                idHopital,
                request.getAdminPrenom(),
                request.getAdminNom(),
                request.getAdminEmail(),
                request.getAdminTelephone());
    }

    @Override
    @Transactional(readOnly = false)
    public HospitalDetailDTO updateHospital(Integer idHopital, HospitalUpdateDTO dto) {
        Hopital existing = hopitalRepository.rechercherhopitalParId(idHopital.longValue());
        if (existing == null) {
            throw new ResourceNotFoundException("Hôpital introuvable pour id=" + idHopital);
        }

        String nom = dto.getNom() != null ? dto.getNom().trim() : "";
        if (nom.isEmpty()) {
            throw new IllegalArgumentException("Le nom de l'hôpital est requis.");
        }
        if (dto.getPays() == null || dto.getPays().isBlank()) {
            throw new IllegalArgumentException("Le pays est requis.");
        }
        if (dto.getVille() == null || dto.getVille().isBlank()) {
            throw new IllegalArgumentException("La ville est requise.");
        }
        if (dto.getEmail() == null || dto.getEmail().isBlank()) {
            throw new IllegalArgumentException("L'email est requis.");
        }

        String sousDomaine = normalizeSousDomaine(dto.getSousDomaine(), dto.getNom());
        if (hopitalRepository.existsBySousDomaineExcludingId(sousDomaine, idHopital)) {
            throw new IllegalStateException("Ce sous-domaine est déjà utilisé.");
        }
        if (hopitalRepository.existsByEmailExcludingId(dto.getEmail().trim(), idHopital)) {
            throw new IllegalStateException("Un hôpital avec cet email existe déjà.");
        }

        String type = normalizeType(dto.getType());
        String planNom = normalizePlan(dto.getPlanNom());
        BigDecimal planPrice = saasPlanService.getDefinition(planNom).monthlyPrice();
        String nomCommercial = dto.getNomCommercial() != null && !dto.getNomCommercial().isBlank()
                ? dto.getNomCommercial().trim() : nom;

        existing.setNom(nom);
        existing.setNomCommercial(nomCommercial);
        existing.setSousDomaine(sousDomaine);
        existing.setType(type);
        existing.setEstActif(dto.isEstActif());
        existing.setAdresse(dto.getAdresse() != null && !dto.getAdresse().isBlank() ? dto.getAdresse().trim() : null);
        existing.setAdresseComplete(dto.getAdresseComplete() != null && !dto.getAdresseComplete().isBlank()
                ? dto.getAdresseComplete().trim()
                : existing.getAdresse());
        existing.setVille(dto.getVille().trim());
        existing.setPays(dto.getPays().trim());
        existing.setTelephone(dto.getTelephone() != null && !dto.getTelephone().isBlank()
                ? dto.getTelephone().trim() : null);
        existing.setEmail(dto.getEmail().trim());
        existing.setLogoUrl(dto.getLogoUrl() != null && !dto.getLogoUrl().isBlank()
                ? dto.getLogoUrl().trim() : null);
        existing.setDateModification(java.time.LocalDateTime.now());

        hospitalService.modifier(existing);
        abonnementRepository.updateActiveSubscriptionPlan(idHopital, planNom, planPrice);

        return getHospitalDetail(idHopital);
    }

    @Override
    @Transactional(readOnly = false)
    public HospitalDetailDTO setHospitalStatus(Integer idHopital, boolean active) {
        Hopital existing = hopitalRepository.rechercherhopitalParId(idHopital.longValue());
        if (existing == null) {
            throw new ResourceNotFoundException("Hôpital introuvable pour id=" + idHopital);
        }
        if (existing.isEstActif() == active) {
            return getHospitalDetail(idHopital);
        }

        existing.setEstActif(active);
        existing.setDateModification(java.time.LocalDateTime.now());
        hospitalService.modifier(existing);

        if (active) {
            abonnementRepository.reactivateSuspendedSubscription(idHopital);
        } else {
            abonnementRepository.suspendActiveSubscription(idHopital);
        }

        return getHospitalDetail(idHopital);
    }

    private String normalizeType(String type) {
        String normalized = type != null ? type.trim().toUpperCase() : "CLINIQUE";
        if (!ALLOWED_TYPES.contains(normalized)) {
            throw new IllegalArgumentException("Type d'établissement invalide.");
        }
        return normalized;
    }

    private String normalizePlan(String plan) {
        String normalized = saasPlanService.normalizePlanName(plan);
        if (!SaasPlanRegistry.allowedPlanNames().contains(normalized)) {
            return SaasPlanRegistry.BASIC;
        }
        return normalized;
    }

    private String normalizeSousDomaine(String sousDomaine, String nom) {
        String value = sousDomaine != null && !sousDomaine.isBlank()
                ? sousDomaine.trim().toLowerCase()
                : slugify(nom);
        if (!value.matches("^[a-z0-9-]{3,63}$")) {
            throw new IllegalArgumentException("Le sous-domaine doit contenir 3 à 63 caractères (a-z, 0-9, tirets).");
        }
        return value;
    }

    private String slugify(String nom) {
        String base = Normalizer.normalize(nom != null ? nom : "", Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .toLowerCase()
                .replaceAll("[^a-z0-9]+", "-")
                .replaceAll("^-+|-+$", "");
        if (base.length() < 3) {
            base = "hopital";
        }
        return base.length() > 63 ? base.substring(0, 63) : base;
    }

    private String generateUniqueSousDomaine(String nom) {
        String base = slugify(nom);
        String candidate = base;
        int suffix = 1;
        while (hopitalRepository.existsBySousDomaine(candidate)) {
            candidate = base + "-" + suffix++;
        }
        return candidate;
    }

    private HospitalPlanCatalogDTO buildPlan(String name, BigDecimal price, long subscribers,
                                             boolean popular, List<String> features) {
        return new HospitalPlanCatalogDTO(name, price, subscribers, popular, features);
    }

    private long countSuspendedBefore(List<HospitalOverviewDTO> overview, LocalDate date) {
        return overview.stream()
                .filter(h -> "suspended".equals(h.getStatus())
                        && h.getJoined() != null
                        && h.getJoined().toLocalDate().isBefore(date))
                .count();
    }

    private BigDecimal calculatePercentage(long previous, long current) {
        return calculatePercentage(BigDecimal.valueOf(previous), BigDecimal.valueOf(current));
    }

    private BigDecimal calculatePercentage(BigDecimal previous, BigDecimal current) {
        if (previous == null || previous.compareTo(BigDecimal.ZERO) == 0) {
            return current != null && current.compareTo(BigDecimal.ZERO) > 0
                    ? BigDecimal.valueOf(100) : BigDecimal.ZERO;
        }
        return current.subtract(previous)
                .multiply(BigDecimal.valueOf(100))
                .divide(previous, 2, RoundingMode.HALF_UP);
    }
}
