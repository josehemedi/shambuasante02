package hospicloud.repositoriesImpl;

import hospicloud.dtos.patient.DashboardStatsDTO;
import hospicloud.dtos.patient.UpcomingAppointmentDTO;
import hospicloud.dtos.patient.RecentActivityDTO;
import hospicloud.repositories.PatientDashboardRepository;
import hospicloud.security.TenantContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

@Repository
public class PatientDashboardRepositoryImpl implements PatientDashboardRepository {

    private static final Logger logger = LoggerFactory.getLogger(PatientDashboardRepositoryImpl.class);

    private final JdbcTemplate jdbcTemplate;

    public PatientDashboardRepositoryImpl(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private LocalDateTime toLocalDateTime(Timestamp ts) {
        return ts != null ? ts.toLocalDateTime() : null;
    }

    private Long safeCount(String sql, Object... args) {
        try {
            Long count = jdbcTemplate.queryForObject(sql, Long.class, args);
            return count != null ? count : 0L;
        } catch (Exception ex) {
            logger.debug("Statistique patient indisponible: {}", ex.getMessage());
            return 0L;
        }
    }

    private Double safeSum(String sql, Object... args) {
        try {
            Double sum = jdbcTemplate.queryForObject(sql, Double.class, args);
            return sum != null ? sum : 0.0;
        } catch (Exception ex) {
            logger.debug("Solde patient indisponible: {}", ex.getMessage());
            return 0.0;
        }
    }

    @Override
    public DashboardStatsDTO getPatientDashboardStats(Integer idPatient) {
        Integer hopitalId = TenantContext.getRequiredHopitalId();

        Long rdvCount = safeCount(
                "SELECT COUNT(id_rdv) FROM rendez_vous01 WHERE id_patient = ? AND id_hopital = ?",
                idPatient, hopitalId);

        Long ordonnanceCount = safeCount(
                "SELECT COUNT(id_ordonnance) FROM ordonnances_medicales WHERE id_patient = ? AND hospital_id = ?",
                idPatient, hopitalId);

        Long rapportCount = safeCount(
                "SELECT COUNT(pd.id_document) FROM patients_documents pd " +
                        "INNER JOIN patients p ON pd.id_patient = p.id_patient " +
                        "WHERE pd.id_patient = ? AND p.id_hopital = ?",
                idPatient, hopitalId);

        Double solde = safeSum(
                "SELECT COALESCE(SUM(montant_total_ttc), 0) FROM factures " +
                        "WHERE id_patient = ? AND id_hopital = ? AND statut_paiement IN ('IMPAYE', 'PARTIEL')",
                idPatient, hopitalId);

        return new DashboardStatsDTO(rdvCount, ordonnanceCount, rapportCount, solde);
    }

    @Override
    public List<UpcomingAppointmentDTO> getUpcomingAppointments(Integer idPatient) {
        Integer hopitalId = TenantContext.getRequiredHopitalId();

        String sql = """
                SELECT r.id_rdv, r.id_hopital, r.date_heure_rdv, r.motif_visite, r.canal, r.statut_rdv,
                       TRIM(CONCAT(COALESCE(m.prenom, ''), ' ', COALESCE(m.nom, ''))) AS nom_medecin
                FROM rendez_vous01 r
                LEFT JOIN medecin m ON r.id_medecin = m.id_medecin AND r.id_hopital = m.id_hopital
                WHERE r.id_patient = ? AND r.id_hopital = ?
                  AND r.statut_rdv IN ('PROGRAMME', 'CONFIRME', 'EN_COURS')
                  AND (
                    r.date_heure_rdv >= NOW()
                    OR (
                      UPPER(r.canal) = 'TELECONSULTATION'
                      AND NOW() >= DATE_SUB(r.date_heure_rdv, INTERVAL 15 MINUTE)
                      AND NOW() <= DATE_ADD(r.date_heure_rdv, INTERVAL COALESCE(r.duree_estimee, 30) + 30 MINUTE)
                    )
                  )
                ORDER BY r.date_heure_rdv ASC
                LIMIT 10
                """;

        return jdbcTemplate.query(sql, (rs, rowNum) -> new UpcomingAppointmentDTO(
                rs.getInt("id_rdv"),
                rs.getInt("id_hopital"),
                toLocalDateTime(rs.getTimestamp("date_heure_rdv")),
                rs.getString("motif_visite"),
                rs.getString("nom_medecin"),
                rs.getString("canal"),
                rs.getString("statut_rdv")
        ), idPatient, hopitalId);
    }

    @Override
    public List<RecentActivityDTO> getRecentActivities(Integer idPatient) {
        Integer hopitalId = TenantContext.getRequiredHopitalId();

        String sql = """
                SELECT type_activite, description, date_heure, statut FROM (
                    SELECT 'Rendez-vous' AS type_activite,
                           motif_visite AS description,
                           date_heure_rdv AS date_heure,
                           statut_rdv AS statut
                    FROM rendez_vous01
                    WHERE id_patient = ? AND id_hopital = ?
                    UNION ALL
                    SELECT 'Ordonnance' AS type_activite,
                           COALESCE(contenu_ordonnance, 'Ordonnance') AS description,
                           COALESCE(date_prescription, date_expiration) AS date_heure,
                           COALESCE(statut, 'ACTIVE') AS statut
                    FROM ordonnances_medicales
                    WHERE id_patient = ? AND hospital_id = ?
                    UNION ALL
                    SELECT 'Facture' AS type_activite,
                           numero_facture AS description,
                           date_facture AS date_heure,
                           statut_paiement AS statut
                    FROM factures
                    WHERE id_patient = ? AND id_hopital = ?
                ) activities
                ORDER BY date_heure DESC
                LIMIT 10
                """;

        try {
            return jdbcTemplate.query(sql, (rs, rowNum) -> new RecentActivityDTO(
                    rs.getString("type_activite"),
                    rs.getString("description"),
                    toLocalDateTime(rs.getTimestamp("date_heure")),
                    rs.getString("statut")
            ), idPatient, hopitalId, idPatient, hopitalId, idPatient, hopitalId);
        } catch (Exception ex) {
            logger.warn("Activités récentes patient indisponibles: {}", ex.getMessage());
            return Collections.emptyList();
        }
    }

    @Override
    public List<UpcomingAppointmentDTO> getTeleconsultations(Integer idPatient) {
        return getUpcomingAppointments(idPatient).stream()
                .filter(a -> a.getCanal() != null && "TELECONSULTATION".equalsIgnoreCase(a.getCanal()))
                .toList();
    }
}
