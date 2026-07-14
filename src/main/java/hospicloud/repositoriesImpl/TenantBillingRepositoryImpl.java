package hospicloud.repositoriesImpl;

import hospicloud.dtos.TenantBillingInvoiceDTO;
import hospicloud.dtos.TenantBillingKpisDTO;
import hospicloud.dtos.TenantBillingRevenuePointDTO;
import hospicloud.repositories.TenantBillingRepository;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Repository
public class TenantBillingRepositoryImpl implements TenantBillingRepository {

    private static final int OVERDUE_DAYS = 30;

    private final JdbcTemplate jdbcTemplate;

    public TenantBillingRepositoryImpl(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public String findHospitalName(Integer idHopital) {
        try {
            return jdbcTemplate.queryForObject(
                    "SELECT nom FROM hopitaux WHERE id_hopital = ?", String.class, idHopital);
        } catch (Exception e) {
            return "Hospital";
        }
    }

    @Override
    public TenantBillingKpisDTO getKpis(Integer idHopital) {
        LocalDate yearStart = LocalDate.of(LocalDate.now().getYear(), 1, 1);
        LocalDate overdueCutoff = LocalDate.now().minusDays(OVERDUE_DAYS);

        String sql = """
                SELECT
                  COUNT(1) AS invoice_count,
                  COALESCE(SUM(CASE WHEN statut_paiement <> 'ANNULE' THEN montant_total_ttc ELSE 0 END), 0) AS total_ytd,
                  COALESCE(SUM(CASE WHEN statut_paiement = 'PAYE' THEN montant_total_ttc ELSE 0 END), 0) AS total_paid,
                  COALESCE(SUM(CASE WHEN statut_paiement IN ('IMPAYE', 'PARTIEL') THEN montant_total_ttc ELSE 0 END), 0) AS outstanding,
                  COALESCE(SUM(
                    CASE
                      WHEN statut_paiement = 'IMPAYE' AND DATE(date_facture) < ?
                      THEN montant_total_ttc ELSE 0
                    END
                  ), 0) AS overdue
                FROM factures
                WHERE id_hopital = ?
                  AND date_facture >= ?
                """;

        try {
            return jdbcTemplate.queryForObject(sql, (rs, rowNum) -> {
                TenantBillingKpisDTO kpis = new TenantBillingKpisDTO();
                kpis.setInvoiceCount(rs.getLong("invoice_count"));
                kpis.setTotalRevenueYtd(rs.getBigDecimal("total_ytd"));
                kpis.setTotalPaid(rs.getBigDecimal("total_paid"));
                kpis.setOutstanding(rs.getBigDecimal("outstanding"));
                kpis.setOverdue(rs.getBigDecimal("overdue"));
                return kpis;
            }, java.sql.Date.valueOf(overdueCutoff), idHopital, java.sql.Timestamp.valueOf(yearStart.atStartOfDay()));
        } catch (Exception e) {
            return new TenantBillingKpisDTO();
        }
    }

    @Override
    public List<TenantBillingInvoiceDTO> listInvoices(Integer idHopital, int limit) {
        int safeLimit = Math.max(1, Math.min(limit, 500));
        LocalDate overdueCutoff = LocalDate.now().minusDays(OVERDUE_DAYS);

        String sql = """
                SELECT f.id_facture, f.numero_facture, f.date_facture,
                       f.montant_total_ht, f.tva, f.montant_total_ttc, f.statut_paiement,
                       TRIM(CONCAT(COALESCE(p.prenom, ''), ' ', COALESCE(p.nom, ''))) AS patient_name,
                       COALESCE(
                         (SELECT GROUP_CONCAT(fi.designation ORDER BY fi.id_item SEPARATOR ', ')
                          FROM facture_items fi WHERE fi.id_facture = f.id_facture),
                         '—'
                       ) AS service_summary
                FROM factures f
                INNER JOIN patients p ON f.id_patient = p.id_patient AND f.id_hopital = p.id_hopital
                WHERE f.id_hopital = ?
                ORDER BY f.date_facture DESC
                LIMIT ?
                """;

        try {
            return jdbcTemplate.query(sql, (rs, rowNum) -> {
                TenantBillingInvoiceDTO item = new TenantBillingInvoiceDTO();
                item.setIdFacture(rs.getInt("id_facture"));
                item.setNumeroFacture(rs.getString("numero_facture"));
                item.setPatient(blankToDash(rs.getString("patient_name")));
                Timestamp ts = rs.getTimestamp("date_facture");
                LocalDateTime dateFacture = ts != null ? ts.toLocalDateTime() : null;
                item.setDateFacture(dateFacture);
                item.setMontantHt(rs.getBigDecimal("montant_total_ht"));
                item.setTva(rs.getBigDecimal("tva"));
                item.setMontantTtc(rs.getBigDecimal("montant_total_ttc"));
                String statut = rs.getString("statut_paiement");
                item.setStatutPaiement(statut);
                item.setUiStatus(mapUiStatus(statut, dateFacture, overdueCutoff));
                item.setService(blankToDash(rs.getString("service_summary")));
                return item;
            }, idHopital, safeLimit);
        } catch (Exception e) {
            return List.of();
        }
    }

    @Override
    public List<TenantBillingRevenuePointDTO> getRevenueSeries(Integer idHopital, int months) {
        int safeMonths = Math.max(1, Math.min(months, 12));
        List<TenantBillingRevenuePointDTO> series = new ArrayList<>();
        YearMonth current = YearMonth.now();

        for (int i = safeMonths - 1; i >= 0; i--) {
            YearMonth month = current.minusMonths(i);
            LocalDate start = month.atDay(1);
            LocalDate end = month.plusMonths(1).atDay(1);

            BigDecimal revenue = sumPaidRevenue(idHopital, start, end);
            String label = month.getMonth().getDisplayName(TextStyle.SHORT, Locale.FRENCH);
            series.add(new TenantBillingRevenuePointDTO(month.getMonthValue(), month.getYear(), label, revenue));
        }
        return series;
    }

    private BigDecimal sumPaidRevenue(Integer idHopital, LocalDate startInclusive, LocalDate endExclusive) {
        try {
            BigDecimal value = jdbcTemplate.queryForObject(
                    """
                    SELECT COALESCE(SUM(montant_total_ttc), 0)
                    FROM factures
                    WHERE id_hopital = ?
                      AND statut_paiement IN ('PAYE', 'PARTIEL')
                      AND date_facture >= ?
                      AND date_facture < ?
                    """,
                    BigDecimal.class,
                    idHopital,
                    java.sql.Timestamp.valueOf(startInclusive.atStartOfDay()),
                    java.sql.Timestamp.valueOf(endExclusive.atStartOfDay()));
            return value != null ? value : BigDecimal.ZERO;
        } catch (Exception e) {
            return BigDecimal.ZERO;
        }
    }

    private String mapUiStatus(String statut, LocalDateTime dateFacture, LocalDate overdueCutoff) {
        if (statut == null) return "pending";
        return switch (statut) {
            case "PAYE" -> "paid";
            case "PARTIEL" -> "partial";
            case "ANNULE" -> "cancelled";
            case "IMPAYE" -> {
                if (dateFacture != null && dateFacture.toLocalDate().isBefore(overdueCutoff)) {
                    yield "overdue";
                }
                yield "pending";
            }
            default -> "pending";
        };
    }

    private String blankToDash(String value) {
        return value == null || value.isBlank() ? "—" : value.trim();
    }
}
