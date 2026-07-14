package hospicloud.repositoriesImpl;

import hospicloud.dtos.*;
import hospicloud.repositories.HospitalAdminDashboardRepository;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Timestamp;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Repository
public class HospitalAdminDashboardRepositoryImpl implements HospitalAdminDashboardRepository {

    private static final String[] DEPT_COLORS = {
            "var(--color-destructive)",
            "var(--color-chart-1)",
            "var(--color-chart-2)",
            "var(--color-chart-3)",
            "var(--color-chart-4)",
    };

    private static final String[] DAY_KEYS = {"mon", "tue", "wed", "thu", "fri", "sat", "sun"};

    private final JdbcTemplate jdbcTemplate;

    public HospitalAdminDashboardRepositoryImpl(JdbcTemplate jdbcTemplate) {
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
    public long countActivePatients(Integer idHopital) {
        return safeCount(
                "SELECT COUNT(1) FROM patients WHERE id_hopital = ? AND est_actif = TRUE", idHopital);
    }

    @Override
    public long countPatientsRegisteredBefore(Integer idHopital, LocalDate date) {
        return safeCount(
                "SELECT COUNT(1) FROM patients WHERE id_hopital = ? AND est_actif = TRUE AND date_enregistrement < ?",
                idHopital, java.sql.Date.valueOf(date));
    }

    @Override
    public long countActiveConsultations(Integer idHopital) {
        long fromConsultation = safeCount(
                "SELECT COUNT(1) FROM consultation WHERE id_hopital = ? AND statut_consultation IN ('EN_COURS', 'EN_ATTENTE')",
                idHopital);
        long fromAdmission = safeCount(
                "SELECT COUNT(1) FROM admission WHERE id_hopital = ? AND statut IN ('EN_CONSULTATION', 'ENREGISTRE', 'HOSPITALISE')",
                idHopital);
        return Math.max(fromConsultation, fromAdmission);
    }

    @Override
    public long countActiveConsultationsOnDate(Integer idHopital, LocalDate date) {
        LocalDate next = date.plusDays(1);
        long fromConsultation = safeCount(
                "SELECT COUNT(1) FROM consultation WHERE id_hopital = ? AND statut_consultation IN ('EN_COURS', 'EN_ATTENTE') AND date_consultation < ?",
                idHopital, java.sql.Timestamp.valueOf(next.atStartOfDay()));
        long fromRdv = safeCount(
                "SELECT COUNT(1) FROM rendez_vous01 WHERE id_hopital = ? AND DATE(date_heure_rdv) = ? AND statut_rdv NOT IN ('ANNULE', 'ABSENT', 'TERMINE')",
                idHopital, java.sql.Date.valueOf(date));
        return Math.max(fromConsultation, fromRdv);
    }

    @Override
    public BigDecimal sumRevenueBetween(Integer idHopital, LocalDate startInclusive, LocalDate endExclusive) {
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

    @Override
    public long countHospitalized(Integer idHopital) {
        long admission = safeCount(
                "SELECT COUNT(1) FROM admission WHERE id_hopital = ? AND statut = 'HOSPITALISE'", idHopital);
        if (admission > 0) {
            return admission;
        }
        return safeCount(
                "SELECT COUNT(1) FROM rendez_vous01 WHERE id_hopital = ? AND DATE(date_heure_rdv) = CURRENT_DATE AND statut_rdv IN ('CONFIRME', 'EN_COURS')",
                idHopital);
    }

    @Override
    public long countHospitalizedOnDate(Integer idHopital, LocalDate date) {
        return safeCount(
                "SELECT COUNT(1) FROM admission WHERE id_hopital = ? AND statut = 'HOSPITALISE' AND DATE(temps_arrivee) <= ?",
                idHopital, java.sql.Date.valueOf(date));
    }

    @Override
    public List<HospitalAdminRevenuePointDTO> getRevenueSeries(Integer idHopital, int months) {
        int safeMonths = Math.max(1, Math.min(months, 12));
        List<HospitalAdminRevenuePointDTO> series = new ArrayList<>();
        YearMonth current = YearMonth.now();

        for (int i = safeMonths - 1; i >= 0; i--) {
            YearMonth month = current.minusMonths(i);
            LocalDate start = month.atDay(1);
            LocalDate end = month.plusMonths(1).atDay(1);

            BigDecimal total = sumRevenueBetween(idHopital, start, end);
            if (total.compareTo(BigDecimal.ZERO) == 0) {
                total = sumStatRevenue(idHopital, start, end);
            }

            BigDecimal teleShare = teleShareForMonth(idHopital, month);
            BigDecimal tele = total.multiply(teleShare).setScale(2, RoundingMode.HALF_UP);
            BigDecimal inpatientShare = hospitalizationShareForMonth(idHopital, month);
            BigDecimal inpatient = total.multiply(inpatientShare).setScale(2, RoundingMode.HALF_UP);
            BigDecimal outpatient = total.subtract(tele).subtract(inpatient).max(BigDecimal.ZERO);

            series.add(new HospitalAdminRevenuePointDTO(
                    month.getMonthValue(), month.getYear(), inpatient, outpatient, tele));
        }
        return series;
    }

    @Override
    public List<HospitalAdminFlowPointDTO> getPatientFlowLast7Days(Integer idHopital) {
        Map<String, long[]> buckets = new LinkedHashMap<>();
        for (String key : DAY_KEYS) {
            buckets.put(key, new long[]{0L, 0L});
        }

        try {
            jdbcTemplate.query(
                    """
                    SELECT DAYOFWEEK(date_enregistrement) AS dow, COUNT(1) AS total
                    FROM patients
                    WHERE id_hopital = ? AND date_enregistrement >= DATE_SUB(CURRENT_DATE, INTERVAL 6 DAY)
                    GROUP BY DAYOFWEEK(date_enregistrement)
                    """,
                    rs -> {
                        String key = mysqlDayToKey(rs.getInt("dow"));
                        if (key != null) {
                            buckets.get(key)[0] = rs.getLong("total");
                        }
                    },
                    idHopital);
        } catch (Exception ignored) {
        }

        try {
            jdbcTemplate.query(
                    """
                    SELECT DAYOFWEEK(date_sortie) AS dow, COUNT(1) AS total
                    FROM bons_sortie
                    WHERE id_hopital = ? AND date_sortie >= DATE_SUB(CURRENT_DATE, INTERVAL 6 DAY)
                    GROUP BY DAYOFWEEK(date_sortie)
                    """,
                    rs -> {
                        String key = mysqlDayToKey(rs.getInt("dow"));
                        if (key != null) {
                            buckets.get(key)[1] = rs.getLong("total");
                        }
                    },
                    idHopital);
        } catch (Exception ignored) {
        }

        List<HospitalAdminFlowPointDTO> flow = new ArrayList<>();
        for (Map.Entry<String, long[]> entry : buckets.entrySet()) {
            flow.add(new HospitalAdminFlowPointDTO(entry.getKey(), entry.getValue()[0], entry.getValue()[1]));
        }
        return flow;
    }

    @Override
    public List<HospitalAdminDeptLoadDTO> getDepartmentLoad(Integer idHopital) {
        String sql = """
                SELECT COALESCE(NULLIF(TRIM(m.specialite), ''), 'General') AS dept, COUNT(1) AS total
                FROM rendez_vous01 r
                JOIN medecin m ON r.id_medecin = m.id_medecin AND r.id_hopital = m.id_hopital
                WHERE r.id_hopital = ?
                  AND r.date_heure_rdv >= DATE_SUB(CURRENT_DATE, INTERVAL 30 DAY)
                  AND r.statut_rdv NOT IN ('ANNULE', 'ABSENT')
                GROUP BY COALESCE(NULLIF(TRIM(m.specialite), ''), 'General')
                ORDER BY total DESC
                LIMIT 5
                """;
        try {
            List<long[]> rows = new ArrayList<>();
            List<String> names = new ArrayList<>();
            jdbcTemplate.query(sql, rs -> {
                names.add(rs.getString("dept"));
                rows.add(new long[]{rs.getLong("total")});
            }, idHopital);

            if (names.isEmpty()) {
                return defaultDepartmentLoad();
            }

            long max = rows.stream().mapToLong(r -> r[0]).max().orElse(1L);
            List<HospitalAdminDeptLoadDTO> load = new ArrayList<>();
            for (int i = 0; i < names.size(); i++) {
                int pct = (int) Math.round((rows.get(i)[0] * 100.0) / Math.max(1, max));
                load.add(new HospitalAdminDeptLoadDTO(names.get(i), Math.min(100, pct), DEPT_COLORS[i % DEPT_COLORS.length]));
            }
            return load;
        } catch (Exception e) {
            return defaultDepartmentLoad();
        }
    }

    @Override
    public List<HospitalAdminAlertDTO> getEmergencyAlerts(Integer idHopital, int limit) {
        int safeLimit = Math.max(1, Math.min(limit, 10));
        String sql = """
                SELECT a.id_admission, a.niveau_priorite, a.statut, a.temps_arrivee,
                       TRIM(CONCAT(COALESCE(p.prenom, ''), ' ', COALESCE(p.nom, ''))) AS patient_name,
                       TRIM(CONCAT(COALESCE(m.prenom, ''), ' ', COALESCE(m.nom, ''))) AS medecin_name
                FROM admission a
                JOIN patients p ON a.id_patient = p.id_patient AND a.id_hopital = p.id_hopital
                LEFT JOIN medecin m ON a.id_medecin = m.id_medecin AND a.id_hopital = m.id_hopital
                WHERE a.id_hopital = ?
                  AND a.statut IN ('EN_ATTENTE', 'ENREGISTRE', 'EN_CONSULTATION')
                  AND a.niveau_priorite <= 2
                ORDER BY a.niveau_priorite ASC, a.temps_arrivee ASC
                LIMIT ?
                """;
        try {
            return jdbcTemplate.query(sql, (rs, rowNum) -> {
                HospitalAdminAlertDTO alert = new HospitalAdminAlertDTO();
                int priority = rs.getInt("niveau_priorite");
                alert.setId("ADM-" + rs.getInt("id_admission"));
                alert.setLevel(priority == 1 ? "critical" : "warning");
                String patient = rs.getString("patient_name");
                String medecin = rs.getString("medecin_name");
                alert.setTitle("Priority patient waiting — " + patient);
                alert.setTitleFr("Patient prioritaire en attente — " + patient);
                alert.setDept(medecin != null && !medecin.isBlank() ? medecin : "Emergency");
                alert.setTime(formatRelative(rs.getTimestamp("temps_arrivee")));
                return alert;
            }, idHopital, safeLimit);
        } catch (Exception e) {
            return List.of();
        }
    }

    @Override
    public List<HospitalAdminTimelineItemDTO> getActivityTimeline(Integer idHopital, int limit) {
        int safeLimit = Math.max(1, Math.min(limit, 20));
        String sql = """
                SELECT la.id_log, la.action, la.date_activite,
                       TRIM(CONCAT(COALESCE(u.prenom, ''), ' ', COALESCE(u.nom, ''))) AS actor
                FROM logs_activite la
                LEFT JOIN utilisateurs u ON la.id_utilisateur = u.id_utilisateur
                WHERE la.id_hopital = ?
                ORDER BY la.date_activite DESC
                LIMIT ?
                """;
        try {
            return jdbcTemplate.query(sql, (rs, rowNum) -> {
                HospitalAdminTimelineItemDTO item = new HospitalAdminTimelineItemDTO();
                item.setId(rs.getInt("id_log"));
                String action = rs.getString("action");
                item.setText(action);
                item.setTextFr(action);
                item.setActor(rs.getString("actor") != null ? rs.getString("actor") : "System");
                item.setTime(formatRelative(rs.getTimestamp("date_activite")));
                return item;
            }, idHopital, safeLimit);
        } catch (Exception e) {
            return fallbackTimelineFromPatients(idHopital, safeLimit);
        }
    }

    private List<HospitalAdminTimelineItemDTO> fallbackTimelineFromPatients(Integer idHopital, int limit) {
        String sql = """
                SELECT id_patient, nom, prenom, date_enregistrement
                FROM patients
                WHERE id_hopital = ?
                ORDER BY date_enregistrement DESC
                LIMIT ?
                """;
        try {
            return jdbcTemplate.query(sql, (rs, rowNum) -> {
                HospitalAdminTimelineItemDTO item = new HospitalAdminTimelineItemDTO();
                item.setId(rs.getInt("id_patient"));
                String name = rs.getString("prenom") + " " + rs.getString("nom");
                item.setText("New patient registered: " + name);
                item.setTextFr("Nouveau patient enregistré : " + name);
                item.setActor("Reception");
                item.setTime(formatRelative(rs.getTimestamp("date_enregistrement")));
                return item;
            }, idHopital, limit);
        } catch (Exception ex) {
            return List.of();
        }
    }

    private List<HospitalAdminDeptLoadDTO> defaultDepartmentLoad() {
        return List.of(
                new HospitalAdminDeptLoadDTO("Emergency", 0, DEPT_COLORS[0]),
                new HospitalAdminDeptLoadDTO("General", 0, DEPT_COLORS[1]));
    }

    private BigDecimal sumStatRevenue(Integer idHopital, LocalDate start, LocalDate end) {
        try {
            BigDecimal value = jdbcTemplate.queryForObject(
                    """
                    SELECT COALESCE(SUM(chiffre_affaire_journalier), 0)
                    FROM statistiques_frequentation
                    WHERE id_hopital = ? AND date_stat >= ? AND date_stat < ?
                    """,
                    BigDecimal.class,
                    idHopital,
                    java.sql.Date.valueOf(start),
                    java.sql.Date.valueOf(end));
            return value != null ? value : BigDecimal.ZERO;
        } catch (Exception e) {
            return BigDecimal.ZERO;
        }
    }

    private BigDecimal teleShareForMonth(Integer idHopital, YearMonth month) {
        LocalDate start = month.atDay(1);
        LocalDate end = month.plusMonths(1).atDay(1);
        try {
            Long total = jdbcTemplate.queryForObject(
                    """
                    SELECT COUNT(1) FROM rendez_vous01
                    WHERE id_hopital = ? AND date_heure_rdv >= ? AND date_heure_rdv < ?
                    """,
                    Long.class,
                    idHopital,
                    java.sql.Timestamp.valueOf(start.atStartOfDay()),
                    java.sql.Timestamp.valueOf(end.atStartOfDay()));
            Long tele = jdbcTemplate.queryForObject(
                    """
                    SELECT COUNT(1) FROM rendez_vous01
                    WHERE id_hopital = ? AND date_heure_rdv >= ? AND date_heure_rdv < ?
                      AND UPPER(canal) IN ('VISIO', 'TELE', 'TELECONSULTATION', 'VIDEO')
                    """,
                    Long.class,
                    idHopital,
                    java.sql.Timestamp.valueOf(start.atStartOfDay()),
                    java.sql.Timestamp.valueOf(end.atStartOfDay()));
            if (total == null || total == 0 || tele == null) {
                return BigDecimal.ZERO;
            }
            return BigDecimal.valueOf(tele).divide(BigDecimal.valueOf(total), 4, RoundingMode.HALF_UP);
        } catch (Exception e) {
            return BigDecimal.ZERO;
        }
    }

    private BigDecimal hospitalizationShareForMonth(Integer idHopital, YearMonth month) {
        LocalDate start = month.atDay(1);
        LocalDate end = month.plusMonths(1).atDay(1);
        try {
            Long hospitalisations = jdbcTemplate.queryForObject(
                    """
                    SELECT COALESCE(SUM(nombre_hospitalisations), 0)
                    FROM statistiques_frequentation
                    WHERE id_hopital = ? AND date_stat >= ? AND date_stat < ?
                    """,
                    Long.class,
                    idHopital,
                    java.sql.Date.valueOf(start),
                    java.sql.Date.valueOf(end));
            Long consultations = jdbcTemplate.queryForObject(
                    """
                    SELECT COALESCE(SUM(nombre_consultations), 0)
                    FROM statistiques_frequentation
                    WHERE id_hopital = ? AND date_stat >= ? AND date_stat < ?
                    """,
                    Long.class,
                    idHopital,
                    java.sql.Date.valueOf(start),
                    java.sql.Date.valueOf(end));
            long total = (hospitalisations != null ? hospitalisations : 0) + (consultations != null ? consultations : 0);
            if (total == 0) {
                return new BigDecimal("0.35");
            }
            return BigDecimal.valueOf(hospitalisations != null ? hospitalisations : 0)
                    .divide(BigDecimal.valueOf(total), 4, RoundingMode.HALF_UP);
        } catch (Exception e) {
            return new BigDecimal("0.35");
        }
    }

    private long safeCount(String sql, Object... args) {
        try {
            Long value = jdbcTemplate.queryForObject(sql, Long.class, args);
            return value != null ? value : 0L;
        } catch (Exception e) {
            return 0L;
        }
    }

    private String mysqlDayToKey(int dayOfWeek) {
        return switch (dayOfWeek) {
            case 2 -> "mon";
            case 3 -> "tue";
            case 4 -> "wed";
            case 5 -> "thu";
            case 6 -> "fri";
            case 7 -> "sat";
            case 1 -> "sun";
            default -> null;
        };
    }

    private String formatRelative(Timestamp timestamp) {
        if (timestamp == null) {
            return "—";
        }
        LocalDateTime time = timestamp.toLocalDateTime();
        long minutes = ChronoUnit.MINUTES.between(time, LocalDateTime.now());
        if (minutes < 1) {
            return "< 1 min";
        }
        if (minutes < 60) {
            return minutes + " min";
        }
        long hours = Duration.between(time, LocalDateTime.now()).toHours();
        if (hours < 24) {
            return hours + " h";
        }
        long days = ChronoUnit.DAYS.between(time.toLocalDate(), LocalDate.now());
        return days + " d";
    }
}
