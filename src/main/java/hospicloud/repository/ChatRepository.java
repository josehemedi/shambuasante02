package hospicloud.repository;

import hospicloud.model.ChatMessage;
import hospicloud.security.TenantContext;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

@Repository
public class ChatRepository {

    private final JdbcTemplate jdbcTemplate;

    public ChatRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public ChatMessage saveMessage(ChatMessage msg) {
        assertConsultationInTenant(msg.getConsultationId());

        String sql = "INSERT INTO messages (id_consultation, sender_id, content, created_at) VALUES (?, ?, ?, ?)";
        jdbcTemplate.update(sql, msg.getConsultationId(), msg.getSenderId(), msg.getContent(), msg.getCreatedAt());
        return msg;
    }

    public List<ChatMessage> findByConsultationId(Long consultationId) {
        assertConsultationInTenant(consultationId);

        Integer hopitalId = TenantContext.getRequiredHopitalId();
        String sql = "SELECT m.id, m.id_consultation, m.sender_id, m.content, m.created_at "
                + "FROM messages m "
                + "INNER JOIN consultations_medicales c ON m.id_consultation = c.id_consultation "
                + "WHERE m.id_consultation = ? AND c.id_hopital = ? "
                + "ORDER BY m.created_at ASC";
        return jdbcTemplate.query(sql, new Object[] { consultationId, hopitalId }, this::mapRowToChatMessage);
    }

    private void assertConsultationInTenant(Long consultationId) {
        Integer hopitalId = TenantContext.getRequiredHopitalId();
        String sql = "SELECT COUNT(*) FROM consultations_medicales WHERE id_consultation = ? AND id_hopital = ?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, consultationId, hopitalId);
        if (count == null || count == 0) {
            throw new EmptyResultDataAccessException("Consultation introuvable pour cet établissement", 1);
        }
    }

    private ChatMessage mapRowToChatMessage(ResultSet rs, int rowNum) throws SQLException {
        ChatMessage msg = new ChatMessage();
        msg.setId(rs.getLong("id"));
        msg.setConsultationId(rs.getLong("id_consultation"));
        msg.setSenderId(rs.getString("sender_id"));
        msg.setContent(rs.getString("content"));
        msg.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
        return msg;
    }
}
