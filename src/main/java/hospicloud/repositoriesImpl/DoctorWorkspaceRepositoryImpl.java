package hospicloud.repositoriesImpl;

import hospicloud.dtos.DoctorWorkspaceActivityDTO;
import hospicloud.repositories.DoctorWorkspaceRepository;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Repository
public class DoctorWorkspaceRepositoryImpl implements DoctorWorkspaceRepository {

    private final JdbcTemplate jdbcTemplate;

    public DoctorWorkspaceRepositoryImpl(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public List<DoctorWorkspaceActivityDTO> findRecentActivities(Integer medecinId, Integer hopitalId) {
        List<DoctorWorkspaceActivityDTO> activities = new ArrayList<>();

        activities.addAll(findUnreadNotifications(medecinId, hopitalId));
        activities.addAll(findPendingLabResults(medecinId, hopitalId));

        activities.sort(Comparator.comparing(
                DoctorWorkspaceActivityDTO::getOccurredAt,
                Comparator.nullsLast(Comparator.reverseOrder())));

        return activities.size() > 8 ? activities.subList(0, 8) : activities;
    }

    private List<DoctorWorkspaceActivityDTO> findUnreadNotifications(Integer medecinId, Integer hopitalId) {
        try {
            String sql = """
                SELECT id_notification, message, date_creation
                FROM notifications
                WHERE id_medecin = ? AND id_hopital = ? AND est_lu = 0
                ORDER BY date_creation DESC
                LIMIT 5
                """;

            return jdbcTemplate.query(sql, (rs, rowNum) -> {
                DoctorWorkspaceActivityDTO dto = new DoctorWorkspaceActivityDTO();
                dto.setId(rs.getLong("id_notification"));
                dto.setType("NOTIFICATION");
                dto.setPatientName(null);
                dto.setDetail(rs.getString("message"));
                dto.setOccurredAt(toLocalDateTime(rs.getTimestamp("date_creation")));
                return dto;
            }, medecinId, hopitalId);
        } catch (DataAccessException ex) {
            return List.of();
        }
    }

    private List<DoctorWorkspaceActivityDTO> findPendingLabResults(Integer medecinId, Integer hopitalId) {
        try {
            String sql = """
                SELECT a.id_analyse,
                       TRIM(CONCAT(COALESCE(p.prenom, ''), ' ', COALESCE(p.nom, ''))) AS patient_name,
                       COALESCE(ta.nom_analyse, 'Analyse') AS analyse_name,
                       a.date_demande
                FROM analyses_laboratoire a
                INNER JOIN patients p ON a.id_patient = p.id_patient AND a.id_hopital = p.id_hopital
                LEFT JOIN types_analyses ta ON a.id_type_analyse = ta.id_type_analyse
                WHERE a.id_hopital = ? AND a.id_medecin = ? AND a.statut = 'EN_ATTENTE'
                ORDER BY a.date_demande DESC
                LIMIT 5
                """;

            return jdbcTemplate.query(sql, (rs, rowNum) -> {
                DoctorWorkspaceActivityDTO dto = new DoctorWorkspaceActivityDTO();
                dto.setId(rs.getLong("id_analyse"));
                dto.setType("LAB");
                dto.setPatientName(rs.getString("patient_name"));
                dto.setDetail(rs.getString("analyse_name"));
                dto.setOccurredAt(toLocalDateTime(rs.getTimestamp("date_demande")));
                return dto;
            }, hopitalId, medecinId);
        } catch (DataAccessException ex) {
            return List.of();
        }
    }

    private LocalDateTime toLocalDateTime(Timestamp timestamp) {
        return timestamp != null ? timestamp.toLocalDateTime() : null;
    }
}
