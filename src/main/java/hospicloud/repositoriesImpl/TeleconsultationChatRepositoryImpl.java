package hospicloud.repositoriesImpl;

import hospicloud.dtos.TeleconsultationChatMessageDTO;
import hospicloud.repositories.TeleconsultationChatRepository;
import hospicloud.security.ChatMessageCryptoService;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Repository
public class TeleconsultationChatRepositoryImpl implements TeleconsultationChatRepository {

    private static final DateTimeFormatter ISO = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    private final JdbcTemplate jdbcTemplate;
    private final ChatMessageCryptoService cryptoService;
    private volatile boolean schemaEnsured = false;

    public TeleconsultationChatRepositoryImpl(JdbcTemplate jdbcTemplate,
                                              ChatMessageCryptoService cryptoService) {
        this.jdbcTemplate = jdbcTemplate;
        this.cryptoService = cryptoService;
    }

    @Override
    public void ensureSchema() {
        jdbcTemplate.execute("""
            CREATE TABLE IF NOT EXISTS teleconsultation_chat_messages (
                id BIGINT NOT NULL AUTO_INCREMENT,
                id_hopital INT NOT NULL,
                id_rdv INT NOT NULL,
                id_emetteur INT NOT NULL,
                sender_role VARCHAR(20) NOT NULL,
                contenu TEXT NOT NULL,
                created_at TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP,
                read_by_doctor_at TIMESTAMP NULL DEFAULT NULL,
                read_by_patient_at TIMESTAMP NULL DEFAULT NULL,
                PRIMARY KEY (id),
                KEY idx_tele_chat_tenant_rdv (id_hopital, id_rdv, created_at)
            ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4
            """);

        addColumnIfMissing("teleconsultation_chat_messages", "read_by_doctor_at", "TIMESTAMP NULL DEFAULT NULL");
        addColumnIfMissing("teleconsultation_chat_messages", "read_by_patient_at", "TIMESTAMP NULL DEFAULT NULL");
        schemaEnsured = true;
    }

    private void addColumnIfMissing(String table, String column, String definition) {
        Integer count = jdbcTemplate.queryForObject(
                """
                SELECT COUNT(*) FROM information_schema.columns
                WHERE table_schema = DATABASE() AND table_name = ? AND column_name = ?
                """,
                Integer.class,
                table,
                column
        );
        if (count == null || count == 0) {
            jdbcTemplate.execute("ALTER TABLE " + table + " ADD COLUMN " + column + " " + definition);
        }
    }

    private void ensureSchemaOnce() {
        if (!schemaEnsured) {
            synchronized (this) {
                if (!schemaEnsured) {
                    ensureSchema();
                }
            }
        }
    }

    @Override
    public TeleconsultationChatMessageDTO save(TeleconsultationChatMessageDTO message) {
        ensureSchemaOnce();

        String sql = """
            INSERT INTO teleconsultation_chat_messages
            (id_hopital, id_rdv, id_emetteur, sender_role, contenu, created_at)
            VALUES (?, ?, ?, ?, ?, ?)
            """;

        LocalDateTime createdAt = message.getCreatedAt() != null
                ? LocalDateTime.parse(message.getCreatedAt(), ISO)
                : LocalDateTime.now();

        String encryptedContent = cryptoService.encrypt(message.getContent());

        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setInt(1, message.getIdHopital());
            ps.setInt(2, message.getIdRdv());
            ps.setInt(3, message.getIdEmetteur());
            ps.setString(4, message.getSenderRole());
            ps.setString(5, encryptedContent);
            ps.setTimestamp(6, Timestamp.valueOf(createdAt));
            return ps;
        }, keyHolder);

        Number key = keyHolder.getKey();
        if (key != null) {
            message.setId(key.longValue());
        }
        message.setCreatedAt(createdAt.format(ISO));
        message.setReadByRecipient(false);
        return message;
    }

    @Override
    public List<TeleconsultationChatMessageDTO> findByRdv(Integer idHopital, Integer idRdv) {
        ensureSchemaOnce();

        String sql = """
            SELECT id, id_hopital, id_rdv, id_emetteur, sender_role, contenu, created_at,
                   read_by_doctor_at, read_by_patient_at
            FROM teleconsultation_chat_messages
            WHERE id_hopital = ? AND id_rdv = ?
            ORDER BY created_at ASC, id ASC
            """;

        return jdbcTemplate.query(sql, this::mapRow, idHopital, idRdv);
    }

    @Override
    public List<Long> markAsReadByRecipient(Integer idHopital, Integer idRdv, boolean readerIsDoctor) {
        ensureSchemaOnce();

        String selectUnread;
        String updateSql;
        if (readerIsDoctor) {
            selectUnread = """
                SELECT id FROM teleconsultation_chat_messages
                WHERE id_hopital = ? AND id_rdv = ? AND sender_role = 'patient'
                  AND read_by_doctor_at IS NULL
                """;
            updateSql = """
                UPDATE teleconsultation_chat_messages
                SET read_by_doctor_at = CURRENT_TIMESTAMP
                WHERE id_hopital = ? AND id_rdv = ? AND sender_role = 'patient'
                  AND read_by_doctor_at IS NULL
                """;
        } else {
            selectUnread = """
                SELECT id FROM teleconsultation_chat_messages
                WHERE id_hopital = ? AND id_rdv = ? AND sender_role = 'doctor'
                  AND read_by_patient_at IS NULL
                """;
            updateSql = """
                UPDATE teleconsultation_chat_messages
                SET read_by_patient_at = CURRENT_TIMESTAMP
                WHERE id_hopital = ? AND id_rdv = ? AND sender_role = 'doctor'
                  AND read_by_patient_at IS NULL
                """;
        }

        List<Long> unreadIds = jdbcTemplate.query(
                selectUnread, (rs, rowNum) -> rs.getLong("id"), idHopital, idRdv);
        if (unreadIds == null || unreadIds.isEmpty()) {
            return List.of();
        }

        jdbcTemplate.update(updateSql, idHopital, idRdv);
        return unreadIds;
    }

    private TeleconsultationChatMessageDTO mapRow(ResultSet rs, int rowNum) throws SQLException {
        TeleconsultationChatMessageDTO dto = new TeleconsultationChatMessageDTO();
        dto.setId(rs.getLong("id"));
        dto.setIdHopital(rs.getInt("id_hopital"));
        dto.setIdRdv(rs.getInt("id_rdv"));
        dto.setIdEmetteur(rs.getInt("id_emetteur"));
        dto.setSenderRole(rs.getString("sender_role"));
        dto.setContent(cryptoService.decrypt(rs.getString("contenu")));
        Timestamp createdAt = rs.getTimestamp("created_at");
        if (createdAt != null) {
            dto.setCreatedAt(createdAt.toLocalDateTime().format(ISO));
        }

        Timestamp readByDoctor = rs.getTimestamp("read_by_doctor_at");
        Timestamp readByPatient = rs.getTimestamp("read_by_patient_at");
        if (readByDoctor != null) {
            dto.setReadByDoctorAt(readByDoctor.toLocalDateTime().format(ISO));
        }
        if (readByPatient != null) {
            dto.setReadByPatientAt(readByPatient.toLocalDateTime().format(ISO));
        }
        return dto;
    }
}
