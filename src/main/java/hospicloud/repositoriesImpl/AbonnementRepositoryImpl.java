package hospicloud.repositoriesImpl;

import hospicloud.dtos.MrrSeriesPointDTO;
import hospicloud.dtos.PlanDistributionItemDTO;
import hospicloud.dtos.SubscriptionInvoiceDTO;
import hospicloud.dtos.SubscriptionTimelineEventDTO;
import hospicloud.dtos.TenantOverviewDTO;
import hospicloud.dtos.TenantSubscriptionDTO;
import hospicloud.dtos.TenantSubscriptionHistoryDTO;
import hospicloud.repositories.AbonnementRepository;
import hospicloud.saas.SaasPlanRegistry;
import hospicloud.security.TenantContext;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.TextStyle;
import java.time.temporal.ChronoUnit;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

@Repository
public class AbonnementRepositoryImpl implements AbonnementRepository {

    private static final String ACTIVE_SUBSCRIPTION_JOIN = """
            LEFT JOIN (
                SELECT a.id_hopital, a.plan_nom, a.montant_mensuel, a.statut
                FROM abonnements a
                INNER JOIN (
                    SELECT id_hopital, MAX(id_abonnement) AS max_id
                    FROM abonnements
                    WHERE statut = 'actif'
                    GROUP BY id_hopital
                ) latest ON latest.max_id = a.id_abonnement
            ) abo ON abo.id_hopital = h.id_hopital
            """;

    private final JdbcTemplate jdbcTemplate;

    public AbonnementRepositoryImpl(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public BigDecimal calculateMrrForPeriod(Integer hopitalId, LocalDate startDate, LocalDate endDate) {
        Integer tenantId = hopitalId != null ? hopitalId : TenantContext.getRequiredHopitalId();
        String sql = """
                SELECT COALESCE(SUM(montant_mensuel), 0)
                FROM abonnements
                WHERE id_hopital = ? AND statut = 'actif'
                """;
        return jdbcTemplate.queryForObject(sql, BigDecimal.class, tenantId);
    }

    @Override
    public BigDecimal calculatePlatformMrr() {
        String sql = """
                SELECT COALESCE(SUM(montant_mensuel), 0)
                FROM abonnements
                WHERE statut = 'actif'
                """;
        return jdbcTemplate.queryForObject(sql, BigDecimal.class);
    }

    @Override
    public BigDecimal calculatePlatformMrrAtEndOfMonth(LocalDate monthEnd) {
        LocalDateTime endExclusive = monthEnd.plusDays(1).atStartOfDay();
        LocalDateTime startInclusive = monthEnd.withDayOfMonth(1).atStartOfDay();
        String sql = """
                SELECT COALESCE(SUM(montant_mensuel), 0)
                FROM abonnements
                WHERE statut = 'actif'
                  AND date_debut < ?
                  AND (date_fin IS NULL OR date_fin >= ?)
                """;
        return jdbcTemplate.queryForObject(sql, BigDecimal.class, endExclusive, startInclusive);
    }

    @Override
    public List<MrrSeriesPointDTO> getMrrSeriesLastMonths(int months) {
        List<MrrSeriesPointDTO> series = new ArrayList<>();
        YearMonth current = YearMonth.now();
        Locale locale = Locale.ENGLISH;
        for (int i = months - 1; i >= 0; i--) {
            YearMonth month = current.minusMonths(i);
            BigDecimal mrr = calculatePlatformMrrAtEndOfMonth(month.atEndOfMonth());
            String label = month.getMonth().getDisplayName(TextStyle.SHORT, locale);
            series.add(new MrrSeriesPointDTO(label, mrr));
        }
        return series;
    }

    @Override
    public List<PlanDistributionItemDTO> getPlanDistribution() {
        String sql = """
                SELECT COALESCE(plan_nom, 'Starter') AS plan_nom, COUNT(DISTINCT id_hopital) AS cnt
                FROM abonnements
                WHERE statut = 'actif'
                GROUP BY COALESCE(plan_nom, 'Starter')
                ORDER BY cnt DESC
                """;
        return jdbcTemplate.query(sql, (rs, rowNum) ->
                new PlanDistributionItemDTO(rs.getString("plan_nom"), rs.getLong("cnt")));
    }

    @Override
    public List<TenantOverviewDTO> listTenantsOverview() {
        String sql = """
                SELECT h.id_hopital, h.nom, h.pays, h.est_actif, h.date_creation,
                       abo.plan_nom, abo.montant_mensuel, abo.statut AS abo_statut,
                       (SELECT COUNT(1) FROM utilisateurs u
                        WHERE u.id_hopital = h.id_hopital AND u.est_actif = TRUE) AS users
                FROM hopitaux h
                """ + ACTIVE_SUBSCRIPTION_JOIN + """
                ORDER BY h.nom ASC
                """;
        return jdbcTemplate.query(sql, (rs, rowNum) -> {
            boolean hopitalActif = rs.getBoolean("est_actif");
            String aboStatut = rs.getString("abo_statut");
            String plan = rs.getString("plan_nom");
            BigDecimal mrr = rs.getBigDecimal("montant_mensuel");
            if (mrr == null) {
                mrr = BigDecimal.ZERO;
            }
            if (plan == null || plan.isBlank()) {
                plan = "Starter";
            }
            String status = resolveTenantStatus(hopitalActif, aboStatut, plan, mrr, rs.getTimestamp("date_creation"));
            return new TenantOverviewDTO(
                    "T-" + String.format("%03d", rs.getInt("id_hopital")),
                    rs.getString("nom"),
                    blankToDash(rs.getString("pays")),
                    plan,
                    rs.getLong("users"),
                    mrr,
                    status
            );
        });
    }

    private String resolveTenantStatus(boolean hopitalActif, String aboStatut, String plan,
                                       BigDecimal mrr, java.sql.Timestamp dateCreation) {
        if (!hopitalActif) {
            return "suspended";
        }
        if ("suspendu".equalsIgnoreCase(aboStatut) || "annule".equalsIgnoreCase(aboStatut)) {
            return "suspended";
        }
        if (aboStatut == null && mrr.compareTo(BigDecimal.ZERO) == 0) {
            if (dateCreation != null) {
                LocalDate created = dateCreation.toLocalDateTime().toLocalDate();
                if (created.isAfter(LocalDate.now().minusDays(30))) {
                    return "trial";
                }
            }
            return "trial";
        }
        return "active";
    }

    private String blankToDash(String value) {
        return value == null || value.isBlank() ? "—" : value;
    }

    @Override
    public void creerAbonnement(Integer idHopital, String planNom, BigDecimal montantMensuel) {
        jdbcTemplate.update(
                "INSERT INTO abonnements (id_hopital, plan_nom, montant_mensuel, statut, date_debut) VALUES (?, ?, ?, 'actif', CURRENT_TIMESTAMP)",
                idHopital, planNom, montantMensuel);
    }

    @Override
    public Optional<TenantSubscriptionDTO> findActiveSubscription(Integer idHopital) {
        String sql = """
                SELECT a.id_abonnement, a.id_hopital, h.nom AS hospital_name,
                       a.plan_nom, a.montant_mensuel, a.statut, a.date_debut, a.date_fin,
                       h.est_actif, h.date_creation
                FROM abonnements a
                INNER JOIN hopitaux h ON h.id_hopital = a.id_hopital
                INNER JOIN (
                    SELECT id_hopital, MAX(id_abonnement) AS max_id
                    FROM abonnements
                    WHERE statut = 'actif' AND id_hopital = ?
                    GROUP BY id_hopital
                ) latest ON latest.max_id = a.id_abonnement
                """;
        List<TenantSubscriptionDTO> rows = jdbcTemplate.query(sql, (rs, rowNum) -> {
            TenantSubscriptionDTO dto = new TenantSubscriptionDTO();
            dto.setIdAbonnement(rs.getInt("id_abonnement"));
            dto.setIdHopital(rs.getInt("id_hopital"));
            dto.setHospitalName(rs.getString("hospital_name"));
            String plan = rs.getString("plan_nom");
            dto.setPlanNom(plan == null || plan.isBlank() ? "Starter" : plan);
            BigDecimal mrr = rs.getBigDecimal("montant_mensuel");
            dto.setMontantMensuel(mrr != null ? mrr : BigDecimal.ZERO);
            dto.setStatut(rs.getString("statut"));
            Timestamp dateDebut = rs.getTimestamp("date_debut");
            Timestamp dateFin = rs.getTimestamp("date_fin");
            if (dateDebut != null) {
                dto.setDateDebut(dateDebut.toLocalDateTime());
            }
            if (dateFin != null) {
                dto.setDateFin(dateFin.toLocalDateTime());
            }
            boolean hopitalActif = rs.getBoolean("est_actif");
            String uiStatus = resolveUiStatus(hopitalActif, dto.getStatut(), dto.getMontantMensuel(),
                    rs.getTimestamp("date_creation"), dto.getDateFin());
            dto.setUiStatus(uiStatus);
            dto.setDaysUntilDue(calculateDaysUntilDue(dto.getDateFin()));
            dto.setNeedsPayment(computeNeedsPayment(uiStatus, dto.getDateFin()));
            return dto;
        }, idHopital);
        return rows.isEmpty() ? Optional.empty() : Optional.of(rows.get(0));
    }

    @Override
    public List<TenantSubscriptionHistoryDTO> findSubscriptionHistory(Integer idHopital, int limit) {
        int safeLimit = Math.max(1, Math.min(limit, 50));
        String sql = """
                SELECT id_abonnement, plan_nom, montant_mensuel, statut, date_debut, date_fin
                FROM abonnements
                WHERE id_hopital = ?
                ORDER BY id_abonnement DESC
                LIMIT ?
                """;
        return jdbcTemplate.query(sql, (rs, rowNum) -> {
            TenantSubscriptionHistoryDTO item = new TenantSubscriptionHistoryDTO();
            item.setIdAbonnement(rs.getInt("id_abonnement"));
            String plan = rs.getString("plan_nom");
            item.setPlanNom(plan == null || plan.isBlank() ? "Starter" : plan);
            BigDecimal amount = rs.getBigDecimal("montant_mensuel");
            item.setMontantMensuel(amount != null ? amount : BigDecimal.ZERO);
            item.setStatut(rs.getString("statut"));
            Timestamp dateDebut = rs.getTimestamp("date_debut");
            Timestamp dateFin = rs.getTimestamp("date_fin");
            if (dateDebut != null) {
                item.setDateDebut(dateDebut.toLocalDateTime());
            }
            if (dateFin != null) {
                item.setDateFin(dateFin.toLocalDateTime());
            }
            item.setAction(resolveHistoryAction(item.getStatut(), rowNum == 0));
            return item;
        }, idHopital, safeLimit);
    }

    @Override
    public List<TenantSubscriptionHistoryDTO> findAllSubscriptionPayments(Integer idHopital) {
        String sql = """
                SELECT id_abonnement, plan_nom, montant_mensuel, statut, date_debut, date_fin
                FROM abonnements
                WHERE id_hopital = ?
                ORDER BY date_debut DESC, id_abonnement DESC
                LIMIT 500
                """;
        List<TenantSubscriptionHistoryDTO> rows = jdbcTemplate.query(sql, (rs, rowNum) -> {
            TenantSubscriptionHistoryDTO item = new TenantSubscriptionHistoryDTO();
            item.setIdAbonnement(rs.getInt("id_abonnement"));
            String plan = rs.getString("plan_nom");
            item.setPlanNom(plan == null || plan.isBlank() ? "Starter" : plan);
            BigDecimal amount = rs.getBigDecimal("montant_mensuel");
            item.setMontantMensuel(amount != null ? amount : BigDecimal.ZERO);
            item.setStatut(rs.getString("statut"));
            Timestamp dateDebut = rs.getTimestamp("date_debut");
            Timestamp dateFin = rs.getTimestamp("date_fin");
            if (dateDebut != null) {
                item.setDateDebut(dateDebut.toLocalDateTime());
            }
            if (dateFin != null) {
                item.setDateFin(dateFin.toLocalDateTime());
            }
            return item;
        }, idHopital);

        for (int i = 0; i < rows.size(); i++) {
            rows.get(i).setAction(resolveHistoryAction(rows.get(i).getStatut(), i == 0));
        }
        return rows;
    }

    @Override
    public void closeSubscription(Integer idAbonnement) {
        jdbcTemplate.update(
                "UPDATE abonnements SET statut = 'annule', date_fin = COALESCE(date_fin, CURRENT_TIMESTAMP) WHERE id_abonnement = ?",
                idAbonnement);
    }

    @Override
    public void creerAbonnementAvecEcheance(Integer idHopital, String planNom, BigDecimal montantMensuel, LocalDateTime dateFin) {
        jdbcTemplate.update(
                "INSERT INTO abonnements (id_hopital, plan_nom, montant_mensuel, statut, date_debut, date_fin) VALUES (?, ?, ?, 'actif', CURRENT_TIMESTAMP, ?)",
                idHopital, planNom, montantMensuel, Timestamp.valueOf(dateFin));
    }

    @Override
    public void updateActiveSubscriptionPlan(Integer idHopital, String planNom, BigDecimal montantMensuel) {
        int updated = jdbcTemplate.update(
                """
                UPDATE abonnements a
                INNER JOIN (
                    SELECT id_hopital, MAX(id_abonnement) AS max_id
                    FROM abonnements
                    WHERE statut = 'actif' AND id_hopital = ?
                    GROUP BY id_hopital
                ) latest ON latest.max_id = a.id_abonnement AND a.id_hopital = ?
                SET a.plan_nom = ?, a.montant_mensuel = ?
                """,
                idHopital, idHopital, planNom, montantMensuel);
        if (updated == 0) {
            creerAbonnement(idHopital, planNom, montantMensuel);
        }
    }

    @Override
    public void suspendActiveSubscription(Integer idHopital) {
        jdbcTemplate.update(
                """
                UPDATE abonnements
                SET statut = 'suspendu', date_fin = COALESCE(date_fin, CURRENT_TIMESTAMP)
                WHERE id_hopital = ? AND statut = 'actif'
                """,
                idHopital);
    }

    @Override
    public void reactivateSuspendedSubscription(Integer idHopital) {
        jdbcTemplate.update(
                """
                UPDATE abonnements a
                INNER JOIN (
                    SELECT MAX(id_abonnement) AS max_id
                    FROM abonnements
                    WHERE id_hopital = ? AND statut = 'suspendu'
                ) latest ON latest.max_id = a.id_abonnement
                SET a.statut = 'actif', a.date_fin = NULL
                """,
                idHopital);
    }

    @Override
    public long countActiveSubscriptions() {
        Long count = jdbcTemplate.queryForObject(
                "SELECT COUNT(DISTINCT id_hopital) FROM abonnements WHERE statut = 'actif'",
                Long.class);
        return count != null ? count : 0L;
    }

    @Override
    public long countActiveSubscriptionsAt(LocalDateTime pointInTime) {
        Timestamp point = Timestamp.valueOf(pointInTime);
        Long count = jdbcTemplate.queryForObject(
                """
                SELECT COUNT(DISTINCT id_hopital)
                FROM abonnements
                WHERE statut = 'actif'
                  AND date_debut < ?
                  AND (date_fin IS NULL OR date_fin >= ?)
                """,
                Long.class,
                point,
                point);
        return count != null ? count : 0L;
    }

    @Override
    public long countChurnedBetween(LocalDateTime startInclusive, LocalDateTime endExclusive) {
        Long count = jdbcTemplate.queryForObject(
                """
                SELECT COUNT(DISTINCT id_hopital)
                FROM abonnements
                WHERE statut IN ('annule', 'suspendu')
                  AND date_fin IS NOT NULL
                  AND date_fin >= ?
                  AND date_fin < ?
                """,
                Long.class,
                Timestamp.valueOf(startInclusive),
                Timestamp.valueOf(endExclusive));
        return count != null ? count : 0L;
    }

    @Override
    public List<SubscriptionInvoiceDTO> listRecentInvoices(int limit) {
        int safeLimit = Math.max(1, Math.min(limit, 100));
        String sql = """
                SELECT a.id_abonnement, h.nom AS tenant, a.plan_nom, a.montant_mensuel,
                       a.statut, a.date_debut, a.date_fin, h.est_actif, h.date_creation
                FROM abonnements a
                INNER JOIN hopitaux h ON h.id_hopital = a.id_hopital
                INNER JOIN (
                    SELECT id_hopital, MAX(id_abonnement) AS max_id
                    FROM abonnements
                    GROUP BY id_hopital
                ) latest ON latest.max_id = a.id_abonnement
                ORDER BY a.date_debut DESC
                LIMIT ?
                """;
        return jdbcTemplate.query(sql, (rs, rowNum) -> {
            SubscriptionInvoiceDTO invoice = new SubscriptionInvoiceDTO();
            int idAbonnement = rs.getInt("id_abonnement");
            invoice.setId("INV-" + String.format("%04d", idAbonnement));
            invoice.setTenant(rs.getString("tenant"));
            String plan = rs.getString("plan_nom");
            invoice.setPlan(plan == null || plan.isBlank() ? "Starter" : plan);
            BigDecimal amount = rs.getBigDecimal("montant_mensuel");
            invoice.setAmount(amount != null ? amount : BigDecimal.ZERO);
            Timestamp dateDebut = rs.getTimestamp("date_debut");
            Timestamp dateFin = rs.getTimestamp("date_fin");
            invoice.setDate(formatDate(dateDebut));
            invoice.setDueDate(formatDueDate(dateDebut, dateFin));
            invoice.setStatus(resolveInvoiceStatus(
                    rs.getBoolean("est_actif"),
                    rs.getString("statut"),
                    invoice.getAmount(),
                    rs.getTimestamp("date_creation"),
                    dateFin));
            return invoice;
        }, safeLimit);
    }

    @Override
    public List<SubscriptionTimelineEventDTO> listRecentTimeline(int limit) {
        int safeLimit = Math.max(1, Math.min(limit, 100));
        String sql = """
                SELECT a.id_abonnement, a.id_hopital, h.nom AS tenant, a.plan_nom, a.montant_mensuel,
                       a.statut, a.date_debut
                FROM abonnements a
                INNER JOIN hopitaux h ON h.id_hopital = a.id_hopital
                ORDER BY a.date_debut DESC, a.id_abonnement DESC
                LIMIT ?
                """;
        List<SubscriptionTimelineEventDTO> events = jdbcTemplate.query(sql, (rs, rowNum) -> {
            SubscriptionTimelineEventDTO event = new SubscriptionTimelineEventDTO();
            event.setId(rs.getInt("id_abonnement"));
            event.setTenant(rs.getString("tenant"));
            String plan = rs.getString("plan_nom");
            event.setPlan(plan == null || plan.isBlank() ? "Starter" : plan);
            BigDecimal amount = rs.getBigDecimal("montant_mensuel");
            event.setAmount(amount != null ? amount : BigDecimal.ZERO);
            event.setDate(formatDate(rs.getTimestamp("date_debut")));
            return event;
        }, safeLimit);

        Map<Integer, String> previousPlanByHospital = new HashMap<>();
        String historySql = """
                SELECT id_abonnement, id_hopital, plan_nom, montant_mensuel, statut
                FROM abonnements
                ORDER BY id_hopital ASC, id_abonnement ASC
                """;
        Map<Integer, String> actionById = new HashMap<>();
        jdbcTemplate.query(historySql, rs -> {
            int idAbonnement = rs.getInt("id_abonnement");
            int idHopital = rs.getInt("id_hopital");
            String plan = rs.getString("plan_nom");
            String normalizedPlan = plan == null || plan.isBlank() ? "Starter" : plan;
            BigDecimal amount = rs.getBigDecimal("montant_mensuel");
            String statut = rs.getString("statut");
            String previousPlan = previousPlanByHospital.get(idHopital);
            actionById.put(idAbonnement, resolveTimelineAction(previousPlan, normalizedPlan, amount, statut));
            previousPlanByHospital.put(idHopital, normalizedPlan);
        });

        for (SubscriptionTimelineEventDTO event : events) {
            event.setAction(actionById.getOrDefault((int) event.getId(), "renewed"));
        }
        return events;
    }

    private String formatDate(Timestamp timestamp) {
        if (timestamp == null) {
            return LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE);
        }
        return timestamp.toLocalDateTime().toLocalDate().format(DateTimeFormatter.ISO_LOCAL_DATE);
    }

    private String formatDueDate(Timestamp dateDebut, Timestamp dateFin) {
        if (dateFin != null) {
            return dateFin.toLocalDateTime().toLocalDate().format(DateTimeFormatter.ISO_LOCAL_DATE);
        }
        if (dateDebut != null) {
            return dateDebut.toLocalDateTime().toLocalDate().plusDays(30).format(DateTimeFormatter.ISO_LOCAL_DATE);
        }
        return LocalDate.now().plusDays(30).format(DateTimeFormatter.ISO_LOCAL_DATE);
    }

    private String resolveInvoiceStatus(boolean hopitalActif, String aboStatut, BigDecimal amount,
                                        Timestamp dateCreation, Timestamp dateFin) {
        if (!hopitalActif || "suspendu".equalsIgnoreCase(aboStatut)) {
            return "overdue";
        }
        if ("annule".equalsIgnoreCase(aboStatut)) {
            return "overdue";
        }
        if (dateFin != null && dateFin.toLocalDateTime().isBefore(LocalDateTime.now())) {
            return "overdue";
        }
        if (amount.compareTo(BigDecimal.ZERO) == 0) {
            if (dateCreation != null) {
                LocalDate created = dateCreation.toLocalDateTime().toLocalDate();
                if (created.isAfter(LocalDate.now().minusDays(30))) {
                    return "pending";
                }
            }
            return "pending";
        }
        if (dateFin != null && !dateFin.toLocalDateTime().isAfter(LocalDateTime.now().plusDays(7))) {
            return "pending";
        }
        return "paid";
    }

    private String resolveTimelineAction(String previousPlan, String currentPlan, BigDecimal amount, String statut) {
        if ("annule".equalsIgnoreCase(statut) || "suspendu".equalsIgnoreCase(statut)) {
            return "downgraded";
        }
        if (previousPlan == null) {
            return amount.compareTo(BigDecimal.ZERO) == 0 ? "trial" : "renewed";
        }
        int previousRank = planRank(previousPlan);
        int currentRank = planRank(currentPlan);
        if (currentRank > previousRank) {
            return "upgraded";
        }
        if (currentRank < previousRank) {
            return "downgraded";
        }
        return "renewed";
    }

    private int planRank(String plan) {
        return SaasPlanRegistry.planRank(plan);
    }

    private String resolveUiStatus(boolean hopitalActif, String aboStatut, BigDecimal mrr,
                                   Timestamp dateCreation, LocalDateTime dateFin) {
        if (!hopitalActif || "suspendu".equalsIgnoreCase(aboStatut)) {
            return "suspended";
        }
        if ("annule".equalsIgnoreCase(aboStatut)) {
            return "expired";
        }
        if (dateFin != null && dateFin.isBefore(LocalDateTime.now())) {
            return "expired";
        }
        if (mrr.compareTo(BigDecimal.ZERO) == 0) {
            if (dateCreation != null) {
                LocalDate created = dateCreation.toLocalDateTime().toLocalDate();
                if (created.isAfter(LocalDate.now().minusDays(30))) {
                    return "trial";
                }
            }
            return "trial";
        }
        if (dateFin != null && !dateFin.isAfter(LocalDateTime.now().plusDays(7))) {
            return "due_soon";
        }
        return "active";
    }

    private Integer calculateDaysUntilDue(LocalDateTime dateFin) {
        if (dateFin == null) {
            return null;
        }
        long days = ChronoUnit.DAYS.between(LocalDate.now(), dateFin.toLocalDate());
        return (int) days;
    }

    private boolean computeNeedsPayment(String uiStatus, LocalDateTime dateFin) {
        if ("suspended".equals(uiStatus) || "expired".equals(uiStatus) || "due_soon".equals(uiStatus) || "trial".equals(uiStatus)) {
            return true;
        }
        return dateFin == null;
    }

    private String resolveHistoryAction(String statut, boolean isLatest) {
        if (isLatest && "actif".equalsIgnoreCase(statut)) {
            return "active";
        }
        if ("annule".equalsIgnoreCase(statut)) {
            return "renewed";
        }
        if ("suspendu".equalsIgnoreCase(statut)) {
            return "suspended";
        }
        return "closed";
    }
}
