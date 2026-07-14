package hospicloud.repositoriesImpl;

import hospicloud.dtos.TenantReportsAppointmentMonthDTO;
import hospicloud.dtos.TenantReportsDemographicDTO;
import hospicloud.dtos.TenantReportsRevenueMonthDTO;
import hospicloud.repositories.TenantReportsRepository;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Repository
public class TenantReportsRepositoryImpl implements TenantReportsRepository {

    private static final String[] DEMO_COLORS = {"0EA5E9", "8B5CF6", "10B981", "F97316"};
    private static final String[][] AGE_GROUPS = {
            {"0-18 years", "0-18 ans", "0-18"},
            {"19-45 years", "19-45 ans", "19-45"},
            {"46-65 years", "46-65 ans", "46-65"},
            {"65+ years", "65+ ans", "65+"},
    };

    private final JdbcTemplate jdbcTemplate;

    public TenantReportsRepositoryImpl(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public String findHospitalName(Integer idHopital) {
        try {
            return jdbcTemplate.queryForObject(
                    "SELECT COALESCE(NULLIF(TRIM(nom_commercial), ''), nom) FROM hopitaux WHERE id_hopital = ?",
                    String.class,
                    idHopital);
        } catch (Exception e) {
            return "Établissement #" + idHopital;
        }
    }

    @Override
    public long countActivePatients(Integer idHopital) {
        Long count = jdbcTemplate.queryForObject(
                "SELECT COUNT(1) FROM patients WHERE id_hopital = ? AND est_actif = TRUE",
                Long.class,
                idHopital);
        return count != null ? count : 0L;
    }

    @Override
    public long countAppointmentsBetween(Integer idHopital, LocalDate from, LocalDate toExclusive) {
        Long count = jdbcTemplate.queryForObject(
                """
                SELECT COUNT(1) FROM rendez_vous01
                WHERE id_hopital = ? AND date_heure_rdv >= ? AND date_heure_rdv < ?
                  AND UPPER(statut_rdv) NOT IN ('ANNULE', 'ABSENT')
                """,
                Long.class,
                idHopital,
                java.sql.Timestamp.valueOf(from.atStartOfDay()),
                java.sql.Timestamp.valueOf(toExclusive.atStartOfDay()));
        return count != null ? count : 0L;
    }

    @Override
    public long countInvoicesBetween(Integer idHopital, LocalDate from, LocalDate toExclusive) {
        try {
            Long count = jdbcTemplate.queryForObject(
                    """
                    SELECT COUNT(1) FROM factures
                    WHERE id_hopital = ? AND date_facture >= ? AND date_facture < ?
                    """,
                    Long.class,
                    idHopital,
                    java.sql.Date.valueOf(from),
                    java.sql.Date.valueOf(toExclusive));
            return count != null ? count : 0L;
        } catch (Exception e) {
            return 0L;
        }
    }

    @Override
    public BigDecimal sumRevenueBetween(Integer idHopital, LocalDate from, LocalDate toExclusive) {
        try {
            BigDecimal sum = jdbcTemplate.queryForObject(
                    """
                    SELECT COALESCE(SUM(montant_total_ttc), 0) FROM factures
                    WHERE id_hopital = ? AND date_facture >= ? AND date_facture < ?
                      AND UPPER(statut_paiement) IN ('PAYE', 'PARTIEL')
                    """,
                    BigDecimal.class,
                    idHopital,
                    java.sql.Date.valueOf(from),
                    java.sql.Date.valueOf(toExclusive));
            return sum != null ? sum : BigDecimal.ZERO;
        } catch (Exception e) {
            return BigDecimal.ZERO;
        }
    }

    @Override
    public List<TenantReportsAppointmentMonthDTO> getMonthlyAppointments(
            Integer idHopital, LocalDate from, LocalDate toExclusive) {
        Map<String, TenantReportsAppointmentMonthDTO> buckets = initMonthlyBuckets(from, toExclusive);
        try {
            jdbcTemplate.query(
                    """
                    SELECT YEAR(date_heure_rdv) AS y, MONTH(date_heure_rdv) AS m,
                           COUNT(1) AS total,
                           SUM(CASE WHEN UPPER(canal) = 'TELECONSULTATION' THEN 1 ELSE 0 END) AS follow_up,
                           SUM(CASE WHEN UPPER(canal) <> 'TELECONSULTATION' OR canal IS NULL THEN 1 ELSE 0 END) AS consultation
                    FROM rendez_vous01
                    WHERE id_hopital = ? AND date_heure_rdv >= ? AND date_heure_rdv < ?
                      AND UPPER(statut_rdv) NOT IN ('ANNULE', 'ABSENT')
                    GROUP BY YEAR(date_heure_rdv), MONTH(date_heure_rdv)
                    ORDER BY y, m
                    """,
                    rs -> {
                        int year = rs.getInt("y");
                        int month = rs.getInt("m");
                        String key = year + "-" + month;
                        TenantReportsAppointmentMonthDTO dto = buckets.get(key);
                        if (dto != null) {
                            dto.setTotal(rs.getLong("total"));
                            dto.setConsultation(rs.getLong("consultation"));
                            dto.setFollowUp(rs.getLong("follow_up"));
                        }
                    },
                    idHopital,
                    java.sql.Timestamp.valueOf(from.atStartOfDay()),
                    java.sql.Timestamp.valueOf(toExclusive.atStartOfDay()));
        } catch (Exception ignored) {
        }
        return new ArrayList<>(buckets.values());
    }

    @Override
    public List<TenantReportsRevenueMonthDTO> getMonthlyRevenue(
            Integer idHopital, LocalDate from, LocalDate toExclusive) {
        Map<String, TenantReportsRevenueMonthDTO> buckets = new LinkedHashMap<>();
        YearMonth cursor = YearMonth.from(from);
        YearMonth end = YearMonth.from(toExclusive.minusDays(1));
        while (!cursor.isAfter(end)) {
            TenantReportsRevenueMonthDTO dto = new TenantReportsRevenueMonthDTO();
            dto.setMonth(cursor.getMonthValue());
            dto.setYear(cursor.getYear());
            dto.setName(shortMonthLabel(cursor));
            dto.setRevenue(BigDecimal.ZERO);
            buckets.put(cursor.getYear() + "-" + cursor.getMonthValue(), dto);
            cursor = cursor.plusMonths(1);
        }

        try {
            jdbcTemplate.query(
                    """
                    SELECT YEAR(date_facture) AS y, MONTH(date_facture) AS m,
                           COALESCE(SUM(montant_total_ttc), 0) AS revenue
                    FROM factures
                    WHERE id_hopital = ? AND date_facture >= ? AND date_facture < ?
                      AND UPPER(statut_paiement) IN ('PAYE', 'PARTIEL')
                    GROUP BY YEAR(date_facture), MONTH(date_facture)
                    ORDER BY y, m
                    """,
                    rs -> {
                        String key = rs.getInt("y") + "-" + rs.getInt("m");
                        TenantReportsRevenueMonthDTO dto = buckets.get(key);
                        if (dto != null) {
                            dto.setRevenue(rs.getBigDecimal("revenue"));
                        }
                    },
                    idHopital,
                    java.sql.Date.valueOf(from),
                    java.sql.Date.valueOf(toExclusive));
        } catch (Exception ignored) {
        }

        return new ArrayList<>(buckets.values());
    }

    @Override
    public List<TenantReportsDemographicDTO> getPatientDemographics(Integer idHopital) {
        Map<String, Long> counts = new LinkedHashMap<>();
        for (String[] group : AGE_GROUPS) {
            counts.put(group[2], 0L);
        }

        try {
            jdbcTemplate.query(
                    """
                    SELECT
                      CASE
                        WHEN date_naissance IS NULL THEN 'unknown'
                        WHEN TIMESTAMPDIFF(YEAR, date_naissance, CURDATE()) < 19 THEN '0-18'
                        WHEN TIMESTAMPDIFF(YEAR, date_naissance, CURDATE()) < 46 THEN '19-45'
                        WHEN TIMESTAMPDIFF(YEAR, date_naissance, CURDATE()) < 66 THEN '46-65'
                        ELSE '65+'
                      END AS age_group,
                      COUNT(1) AS total
                    FROM patients
                    WHERE id_hopital = ? AND est_actif = TRUE
                    GROUP BY age_group
                    """,
                    rs -> {
                        String key = rs.getString("age_group");
                        if (counts.containsKey(key)) {
                            counts.put(key, rs.getLong("total"));
                        }
                    },
                    idHopital);
        } catch (Exception ignored) {
        }

        List<TenantReportsDemographicDTO> result = new ArrayList<>();
        for (int i = 0; i < AGE_GROUPS.length; i++) {
            TenantReportsDemographicDTO dto = new TenantReportsDemographicDTO();
            dto.setName(AGE_GROUPS[i][0]);
            dto.setNameFr(AGE_GROUPS[i][1]);
            dto.setValue(counts.getOrDefault(AGE_GROUPS[i][2], 0L));
            dto.setColor(DEMO_COLORS[i % DEMO_COLORS.length]);
            result.add(dto);
        }
        return result;
    }

    private Map<String, TenantReportsAppointmentMonthDTO> initMonthlyBuckets(LocalDate from, LocalDate toExclusive) {
        Map<String, TenantReportsAppointmentMonthDTO> buckets = new LinkedHashMap<>();
        YearMonth cursor = YearMonth.from(from);
        YearMonth end = YearMonth.from(toExclusive.minusDays(1));
        while (!cursor.isAfter(end)) {
            TenantReportsAppointmentMonthDTO dto = new TenantReportsAppointmentMonthDTO();
            dto.setMonth(cursor.getMonthValue());
            dto.setYear(cursor.getYear());
            dto.setName(shortMonthLabel(cursor));
            dto.setTotal(0);
            dto.setConsultation(0);
            dto.setFollowUp(0);
            buckets.put(cursor.getYear() + "-" + cursor.getMonthValue(), dto);
            cursor = cursor.plusMonths(1);
        }
        return buckets;
    }

    private String shortMonthLabel(YearMonth month) {
        return month.getMonth().getDisplayName(TextStyle.SHORT, Locale.ENGLISH);
    }
}
