package hospicloud.repositories;

import hospicloud.dtos.MrrSeriesPointDTO;
import hospicloud.dtos.PlanDistributionItemDTO;
import hospicloud.dtos.SubscriptionInvoiceDTO;
import hospicloud.dtos.SubscriptionTimelineEventDTO;
import hospicloud.dtos.TenantOverviewDTO;
import hospicloud.dtos.TenantSubscriptionDTO;
import hospicloud.dtos.TenantSubscriptionHistoryDTO;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface AbonnementRepository {
    BigDecimal calculateMrrForPeriod(Integer hopitalId, LocalDate startDate, LocalDate endDate);

    /** MRR total plateforme (abonnements actifs). */
    BigDecimal calculatePlatformMrr();

    /** MRR plateforme à la fin d'un mois donné. */
    BigDecimal calculatePlatformMrrAtEndOfMonth(LocalDate monthEnd);

    List<MrrSeriesPointDTO> getMrrSeriesLastMonths(int months);

    List<PlanDistributionItemDTO> getPlanDistribution();

    List<TenantOverviewDTO> listTenantsOverview();

    void creerAbonnement(Integer idHopital, String planNom, java.math.BigDecimal montantMensuel);

    Optional<TenantSubscriptionDTO> findActiveSubscription(Integer idHopital);

    List<TenantSubscriptionHistoryDTO> findSubscriptionHistory(Integer idHopital, int limit);

    List<TenantSubscriptionHistoryDTO> findAllSubscriptionPayments(Integer idHopital);

    void closeSubscription(Integer idAbonnement);

    void creerAbonnementAvecEcheance(Integer idHopital, String planNom, BigDecimal montantMensuel, LocalDateTime dateFin);

    void updateActiveSubscriptionPlan(Integer idHopital, String planNom, BigDecimal montantMensuel);

    void suspendActiveSubscription(Integer idHopital);

    void reactivateSuspendedSubscription(Integer idHopital);

    long countActiveSubscriptions();

    long countActiveSubscriptionsAt(LocalDateTime pointInTime);

    long countChurnedBetween(LocalDateTime startInclusive, LocalDateTime endExclusive);

    List<SubscriptionInvoiceDTO> listRecentInvoices(int limit);

    List<SubscriptionTimelineEventDTO> listRecentTimeline(int limit);
}
