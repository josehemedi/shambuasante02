package hospicloud.repositoriesImpl;

import hospicloud.dtos.DoctorConsultationActiveDTO;
import hospicloud.dtos.DoctorFilePatientDTO;
import hospicloud.dtos.DoctorPendingNoteDTO;
import hospicloud.repositories.DoctorDashboardRepository;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public class DoctorDashboardRepositoryImpl implements DoctorDashboardRepository {

    private final JdbcTemplate jdbcTemplate;

    public DoctorDashboardRepositoryImpl(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public List<DoctorFilePatientDTO> findFilePatients(Integer medecinId, Integer hopitalId) {
        String sql = """
            SELECT r.id_rdv,
                   TRIM(CONCAT(COALESCE(p.prenom, ''), ' ', COALESCE(p.nom, ''))) AS patient_name,
                   r.date_heure_rdv,
                   r.statut_rdv,
                   COALESCE(r.canal, 'PHYSIQUE') AS room_name
            FROM rendez_vous01 r
            INNER JOIN patients p ON r.id_patient = p.id_patient AND r.id_hopital = p.id_hopital
            WHERE r.id_medecin = ? AND r.id_hopital = ?
              AND DATE(r.date_heure_rdv) = CURRENT_DATE
              AND r.statut_rdv IN ('CONFIRME', 'PROGRAMME')
            ORDER BY r.date_heure_rdv ASC
            """;

        return jdbcTemplate.query(sql, (rs, rowNum) -> mapFilePatient(rs), medecinId, hopitalId);
    }

    @Override
    public List<DoctorConsultationActiveDTO> findActiveConsultations(Integer medecinId, Integer hopitalId) {
        try {
            String sql = """
                SELECT c.id_consultation,
                       CONCAT(COALESCE(p.prenom, ''), ' ', COALESCE(p.nom, '')) AS patient_name,
                       c.motif_visite,
                       COALESCE(r.canal, 'PHYSIQUE') AS canal,
                       c.date_consultation
                FROM consultations_medicales c
                JOIN patients p ON c.id_patient = p.id_patient AND c.id_hopital = p.id_hopital
                LEFT JOIN rendez_vous01 r ON c.id_rdv = r.id_rdv AND c.id_hopital = r.id_hopital
                WHERE c.id_medecin = ? AND c.id_hopital = ?
                  AND DATE(c.date_consultation) = CURRENT_DATE
                  AND (c.diagnostic IS NULL OR TRIM(c.diagnostic) = '')
                ORDER BY c.date_consultation DESC
                """;

            return jdbcTemplate.query(sql, (rs, rowNum) -> {
                DoctorConsultationActiveDTO dto = new DoctorConsultationActiveDTO();
                dto.setId(rs.getLong("id_consultation"));
                dto.setPatientName(rs.getString("patient_name").trim());
                dto.setMotif(rs.getString("motif_visite"));
                dto.setCanal(rs.getString("canal"));
                dto.setStartedAt(toLocalDateTime(rs.getTimestamp("date_consultation")));
                return dto;
            }, medecinId, hopitalId);
        } catch (DataAccessException ex) {
            return List.of();
        }
    }

    @Override
    public List<DoctorPendingNoteDTO> findPendingNotes(Integer medecinId, Integer hopitalId) {
        try {
            String sql = """
                SELECT c.id_consultation,
                       CONCAT(COALESCE(p.prenom, ''), ' ', COALESCE(p.nom, '')) AS patient_name,
                       c.motif_visite,
                       c.date_consultation
                FROM consultations_medicales c
                JOIN patients p ON c.id_patient = p.id_patient AND c.id_hopital = p.id_hopital
                WHERE c.id_medecin = ? AND c.id_hopital = ?
                  AND (
                    c.observations IS NULL OR TRIM(c.observations) = ''
                    OR c.diagnostic IS NULL OR TRIM(c.diagnostic) = ''
                  )
                  AND c.date_consultation >= DATE_SUB(CURRENT_DATE, INTERVAL 7 DAY)
                ORDER BY c.date_consultation DESC
                LIMIT 12
                """;

            return jdbcTemplate.query(sql, (rs, rowNum) -> {
                DoctorPendingNoteDTO dto = new DoctorPendingNoteDTO();
                dto.setId(rs.getLong("id_consultation"));
                dto.setPatientName(rs.getString("patient_name").trim());
                dto.setMotif(rs.getString("motif_visite"));
                dto.setConsultationDate(toLocalDateTime(rs.getTimestamp("date_consultation")));
                return dto;
            }, medecinId, hopitalId);
        } catch (DataAccessException ex) {
            return List.of();
        }
    }

    private DoctorFilePatientDTO mapFilePatient(ResultSet rs) throws SQLException {
        LocalDateTime appointmentTime = toLocalDateTime(rs.getTimestamp("date_heure_rdv"));
        DoctorFilePatientDTO dto = new DoctorFilePatientDTO();
        dto.setId(rs.getInt("id_rdv"));
        dto.setPatientName(rs.getString("patient_name").trim());
        dto.setAppointmentTime(appointmentTime);
        dto.setRoom(rs.getString("room_name"));
        dto.setWaited(formatWaited(appointmentTime));
        dto.setPriority(resolvePriority(appointmentTime, rs.getString("statut_rdv")));
        return dto;
    }

    private String formatWaited(LocalDateTime appointmentTime) {
        if (appointmentTime == null) {
            return "—";
        }
        Duration duration = Duration.between(appointmentTime, LocalDateTime.now());
        long minutes = Math.max(0, duration.toMinutes());
        if (minutes < 60) {
            return minutes + " min";
        }
        return (minutes / 60) + " h " + (minutes % 60) + " min";
    }

    private String resolvePriority(LocalDateTime appointmentTime, String status) {
        if (appointmentTime == null) {
            return "normal";
        }
        long minutesLate = Duration.between(appointmentTime, LocalDateTime.now()).toMinutes();
        if (minutesLate >= 20) {
            return "high";
        }
        if ("CONFIRME".equalsIgnoreCase(status)) {
            return "normal";
        }
        return "low";
    }

    private LocalDateTime toLocalDateTime(Timestamp timestamp) {
        return timestamp != null ? timestamp.toLocalDateTime() : null;
    }
}
