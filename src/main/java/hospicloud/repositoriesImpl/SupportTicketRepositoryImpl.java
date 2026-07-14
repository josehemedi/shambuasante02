package hospicloud.repositoriesImpl;

import hospicloud.dtos.SupportTicketDTO;
import hospicloud.dtos.SupportTicketStatusUpdateDTO;
import hospicloud.repositories.SupportTicketRepository;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Repository
public class SupportTicketRepositoryImpl implements SupportTicketRepository {

    private final JdbcTemplate jdbcTemplate;

    private static final RowMapper<SupportTicketDTO> ROW_MAPPER = (rs, rowNum) -> {
        SupportTicketDTO dto = new SupportTicketDTO();
        dto.setId(rs.getLong("id"));
        dto.setHopitalId(rs.getInt("hopital_id"));
        dto.setHopitalNom(rs.getString("hopital_nom"));
        dto.setCreatedByUserId(rs.getObject("created_by_user_id", Integer.class));
        dto.setCreatedByEmail(rs.getString("created_by_email"));
        dto.setCreatedByRole(rs.getString("created_by_role"));
        dto.setSubject(rs.getString("subject"));
        dto.setDescription(rs.getString("description"));
        dto.setModule(rs.getString("module"));
        dto.setPriority(rs.getString("priority"));
        dto.setStatus(rs.getString("status"));
        dto.setRequestId(rs.getString("request_id"));
        dto.setAssignedTo(rs.getString("assigned_to"));
        dto.setResolutionNotes(rs.getString("resolution_notes"));
        Timestamp created = rs.getTimestamp("created_at");
        if (created != null) {
            dto.setCreatedAt(created.toLocalDateTime());
        }
        Timestamp updated = rs.getTimestamp("updated_at");
        if (updated != null) {
            dto.setUpdatedAt(updated.toLocalDateTime());
        }
        return dto;
    };

    public SupportTicketRepositoryImpl(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public Long insert(Integer hopitalId, Integer createdByUserId, String createdByEmail, String createdByRole,
                       String subject, String description, String module, String priority, String requestId) {
        String sql = """
                INSERT INTO support_tickets
                (hopital_id, created_by_user_id, created_by_email, created_by_role,
                 subject, description, module, priority, request_id)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
                """;
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(con -> {
            PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setInt(1, hopitalId);
            if (createdByUserId != null) {
                ps.setInt(2, createdByUserId);
            } else {
                ps.setNull(2, java.sql.Types.INTEGER);
            }
            ps.setString(3, createdByEmail);
            ps.setString(4, createdByRole);
            ps.setString(5, subject);
            ps.setString(6, description);
            ps.setString(7, module);
            ps.setString(8, priority != null ? priority : "MEDIUM");
            ps.setString(9, requestId);
            return ps;
        }, keyHolder);
        Number key = keyHolder.getKey();
        return key != null ? key.longValue() : null;
    }

    @Override
    public List<SupportTicketDTO> search(Integer hopitalId, String status, String module, String priority,
                                         String requestId, String search, int limit) {
        StringBuilder sql = new StringBuilder("""
                SELECT st.id, st.hopital_id, h.nom AS hopital_nom,
                       st.created_by_user_id, st.created_by_email, st.created_by_role,
                       st.subject, st.description, st.module, st.priority, st.status,
                       st.request_id, st.assigned_to, st.resolution_notes,
                       st.created_at, st.updated_at
                FROM support_tickets st
                INNER JOIN hopitaux h ON h.id_hopital = st.hopital_id
                WHERE 1=1
                """);
        List<Object> params = new ArrayList<>();

        if (hopitalId != null) {
            sql.append(" AND st.hopital_id = ?");
            params.add(hopitalId);
        }
        if (status != null && !status.isBlank()) {
            sql.append(" AND st.status = ?");
            params.add(status.trim());
        }
        if (module != null && !module.isBlank()) {
            sql.append(" AND st.module = ?");
            params.add(module.trim());
        }
        if (priority != null && !priority.isBlank()) {
            sql.append(" AND st.priority = ?");
            params.add(priority.trim());
        }
        if (requestId != null && !requestId.isBlank()) {
            sql.append(" AND st.request_id LIKE ?");
            params.add("%" + requestId.trim() + "%");
        }
        if (search != null && !search.isBlank()) {
            sql.append(" AND (st.subject LIKE ? OR st.description LIKE ? OR st.created_by_email LIKE ?)");
            String pattern = "%" + search.trim() + "%";
            params.add(pattern);
            params.add(pattern);
            params.add(pattern);
        }

        sql.append(" ORDER BY st.created_at DESC LIMIT ?");
        params.add(Math.min(Math.max(limit, 1), 200));

        return jdbcTemplate.query(sql.toString(), ROW_MAPPER, params.toArray());
    }

    @Override
    public Optional<SupportTicketDTO> findById(Long id) {
        String sql = """
                SELECT st.id, st.hopital_id, h.nom AS hopital_nom,
                       st.created_by_user_id, st.created_by_email, st.created_by_role,
                       st.subject, st.description, st.module, st.priority, st.status,
                       st.request_id, st.assigned_to, st.resolution_notes,
                       st.created_at, st.updated_at
                FROM support_tickets st
                INNER JOIN hopitaux h ON h.id_hopital = st.hopital_id
                WHERE st.id = ?
                """;
        List<SupportTicketDTO> list = jdbcTemplate.query(sql, ROW_MAPPER, id);
        return list.isEmpty() ? Optional.empty() : Optional.of(list.get(0));
    }

    @Override
    public boolean updateStatus(Long id, SupportTicketStatusUpdateDTO update) {
        String sql = """
                UPDATE support_tickets
                SET status = COALESCE(?, status),
                    assigned_to = COALESCE(?, assigned_to),
                    resolution_notes = COALESCE(?, resolution_notes)
                WHERE id = ?
                """;
        int rows = jdbcTemplate.update(sql,
                update.getStatus(),
                update.getAssignedTo(),
                update.getResolutionNotes(),
                id);
        return rows > 0;
    }

    @Override
    public long countOpenTickets() {
        Long count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM support_tickets WHERE status IN ('OPEN','IN_PROGRESS')",
                Long.class);
        return count != null ? count : 0L;
    }
}
