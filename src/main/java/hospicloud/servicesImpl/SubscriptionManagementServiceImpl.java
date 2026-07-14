package hospicloud.servicesImpl;

import hospicloud.dtos.HospitalPlanCatalogDTO;
import hospicloud.dtos.SubscriptionInvoiceDTO;
import hospicloud.dtos.SubscriptionKpiMetricDTO;
import hospicloud.dtos.SubscriptionKpisDTO;
import hospicloud.dtos.SubscriptionTimelineEventDTO;
import hospicloud.repositories.AbonnementRepository;
import hospicloud.services.HopitalPlatformService;
import hospicloud.services.SubscriptionManagementService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class SubscriptionManagementServiceImpl implements SubscriptionManagementService {

    private final AbonnementRepository abonnementRepository;
    private final HopitalPlatformService hopitalPlatformService;

    public SubscriptionManagementServiceImpl(AbonnementRepository abonnementRepository,
                                             HopitalPlatformService hopitalPlatformService) {
        this.abonnementRepository = abonnementRepository;
        this.hopitalPlatformService = hopitalPlatformService;
    }

    @Override
    public SubscriptionKpisDTO getKpis() {
        YearMonth currentMonth = YearMonth.now();
        LocalDateTime startOfMonth = currentMonth.atDay(1).atStartOfDay();
        LocalDateTime startOfPreviousMonth = currentMonth.minusMonths(1).atDay(1).atStartOfDay();
        LocalDate previousMonthEnd = currentMonth.minusMonths(1).atEndOfMonth();

        long activeNow = abonnementRepository.countActiveSubscriptions();
        long activePrevious = abonnementRepository.countActiveSubscriptionsAt(previousMonthEnd.plusDays(1).atStartOfDay());

        BigDecimal mrrNow = abonnementRepository.calculatePlatformMrr();
        BigDecimal mrrPrevious = abonnementRepository.calculatePlatformMrrAtEndOfMonth(previousMonthEnd);

        BigDecimal arpuNow = calculateArpu(mrrNow, activeNow);
        BigDecimal arpuPrevious = calculateArpu(mrrPrevious, activePrevious);

        long churnedThisMonth = abonnementRepository.countChurnedBetween(startOfMonth, startOfMonth.plusMonths(1));
        long activeAtMonthStart = abonnementRepository.countActiveSubscriptionsAt(startOfMonth);
        BigDecimal churnNow = calculateChurnRate(churnedThisMonth, activeAtMonthStart);

        long churnedPreviousMonth = abonnementRepository.countChurnedBetween(startOfPreviousMonth, startOfMonth);
        long activeAtPreviousMonthStart = abonnementRepository.countActiveSubscriptionsAt(startOfPreviousMonth);
        BigDecimal churnPrevious = calculateChurnRate(churnedPreviousMonth, activeAtPreviousMonthStart);

        SubscriptionKpisDTO kpis = new SubscriptionKpisDTO();
        kpis.setActiveSubscriptions(metric(activeNow, activePrevious));
        kpis.setMrr(metric(mrrNow, mrrPrevious));
        kpis.setArpu(metric(arpuNow, arpuPrevious));
        kpis.setChurnRate(metric(churnNow, churnPrevious));
        return kpis;
    }

    @Override
    public List<HospitalPlanCatalogDTO> getPlans() {
        return hopitalPlatformService.listPlansCatalog();
    }

    @Override
    public List<SubscriptionInvoiceDTO> getInvoices(int limit) {
        return abonnementRepository.listRecentInvoices(limit);
    }

    @Override
    public List<SubscriptionTimelineEventDTO> getTimeline(int limit) {
        return abonnementRepository.listRecentTimeline(limit);
    }

    private BigDecimal calculateArpu(BigDecimal mrr, long activeSubscriptions) {
        if (activeSubscriptions <= 0) {
            return BigDecimal.ZERO;
        }
        return mrr.divide(BigDecimal.valueOf(activeSubscriptions), 2, RoundingMode.HALF_UP);
    }

    private BigDecimal calculateChurnRate(long churned, long activeAtStart) {
        if (activeAtStart <= 0) {
            return BigDecimal.ZERO;
        }
        return BigDecimal.valueOf(churned)
                .multiply(BigDecimal.valueOf(100))
                .divide(BigDecimal.valueOf(activeAtStart), 2, RoundingMode.HALF_UP);
    }

    private SubscriptionKpiMetricDTO metric(long current, long previous) {
        return new SubscriptionKpiMetricDTO(
                BigDecimal.valueOf(current),
                calculatePercentage(previous, current));
    }

    private SubscriptionKpiMetricDTO metric(BigDecimal current, BigDecimal previous) {
        return new SubscriptionKpiMetricDTO(current, calculatePercentage(previous, current));
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
