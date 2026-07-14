package hospicloud.repositoriesImpl;

import hospicloud.dtos.SubscriptionKpiMetricDTO;
import hospicloud.dtos.TechnicalLogDTO;
import hospicloud.dtos.TechnicalLogKpisDTO;
import hospicloud.dtos.events.TechnicalLogEvent;
import hospicloud.repositories.TechnicalLogRepository;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Repository
public class TechnicalLogRepositoryImpl implements TechnicalLogRepository {

    private final JdbcTemplate jdbcTemplate;

    private static final RowMapper<TechnicalLogDTO> ROW_MAPPER = (rs, rowNum) -> {
        TechnicalLogDTO dto = new TechnicalLogDTO();
        dto.setId(rs.getLong("id"));
        dto.setHopitalId(rs.getObject("hopital_id", Integer.class));
        dto.setHopitalNom(rs.getString("hopital_nom"));
        dto.setUserId(rs.getObject("user_id", Long.class));
        dto.setUserEmail(rs.getString("user_email"));
        dto.setUserRole(rs.getString("user_role"));
        dto.setModule(rs.getString("module"));
        dto.setAction(rs.getString("action"));
        dto.setEndpoint(rs.getString("endpoint"));
        dto.setHttpMethod(rs.getString("http_method"));
        dto.setStatus(rs.getString("status"));
        dto.setMessage(rs.getString("message"));
        dto.setErrorDetails(rs.getString("error_details"));
        dto.setRequestId(rs.getString("request_id"));
        dto.setIpAddress(rs.getString("ip_address"));
        dto.setUserAgent(rs.getString("user_agent"));
        Timestamp ts = rs.getTimestamp("created_at");
        if (ts != null) {
            dto.setCreatedAt(ts.toLocalDateTime());
        }
        return dto;
    };

    public TechnicalLogRepositoryImpl(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void insert(TechnicalLogEvent event) {
        String sql = """
                INSERT INTO technical_logs
                (hopital_id, user_id, user_email, user_role, module, action, endpoint, http_method,
                 status, message, error_details, request_id, ip_address, user_agent)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                """;
        jdbcTemplate.update(sql,
                event.getHopitalId(),
                event.getUserId(),
                event.getUserEmail(),
                event.getUserRole(),
                event.getModule(),
                event.getAction(),
                event.getEndpoint(),
                event.getHttpMethod(),
                event.getStatus(),
                event.getMessage(),
                truncate(event.getErrorDetails(), 65000),
                event.getRequestId(),
                event.getIpAddress(),
                truncate(event.getUserAgent(), 65000));
    }

    @Override
    public List<TechnicalLogDTO> search(Integer hopitalId, Long userId, String userEmail, String module,
                                        String action, String status, String requestId, String endpoint,
                                        String search, LocalDateTime dateFrom, LocalDateTime dateTo, int limit) {
        StringBuilder sql = new StringBuilder("""
                SELECT tl.id, tl.hopital_id, h.nom AS hopital_nom,
                       tl.user_id, tl.user_email, tl.user_role, tl.module, tl.action,
                       tl.endpoint, tl.http_method, tl.status, tl.message, tl.error_details,
                       tl.request_id, tl.ip_address, tl.user_agent, tl.created_at
                FROM technical_logs tl
                LEFT JOIN hopitaux h ON h.id_hopital = tl.hopital_id
                WHERE 1=1
                """);
        List<Object> params = new ArrayList<>();

        appendFilter(sql, params, "tl.hopital_id = ?", hopitalId);
        appendFilter(sql, params, "tl.user_id = ?", userId);
        if (userEmail != null && !userEmail.isBlank()) {
            sql.append(" AND tl.user_email LIKE ?");
            params.add("%" + userEmail.trim() + "%");
        }
        appendFilter(sql, params, "tl.module = ?", module);
        appendFilter(sql, params, "tl.action = ?", action);
        appendFilter(sql, params, "tl.status = ?", status);
        if (requestId != null && !requestId.isBlank()) {
            sql.append(" AND tl.request_id LIKE ?");
            params.add("%" + requestId.trim() + "%");
        }
        if (endpoint != null && !endpoint.isBlank()) {
            sql.append(" AND tl.endpoint LIKE ?");
            params.add("%" + endpoint.trim() + "%");
        }
        if (search != null && !search.isBlank()) {
            sql.append(" AND (tl.message LIKE ? OR tl.error_details LIKE ? OR tl.user_email LIKE ? OR tl.request_id LIKE ?)");
            String pattern = "%" + search.trim() + "%";
            params.add(pattern);
            params.add(pattern);
            params.add(pattern);
            params.add(pattern);
        }
        if (dateFrom != null) {
            sql.append(" AND tl.created_at >= ?");
            params.add(Timestamp.valueOf(dateFrom));
        }
        if (dateTo != null) {
            sql.append(" AND tl.created_at <= ?");
            params.add(Timestamp.valueOf(dateTo));
        }

        sql.append(" ORDER BY tl.created_at DESC LIMIT ?");
        params.add(Math.min(Math.max(limit, 1), 500));

        return jdbcTemplate.query(sql.toString(), ROW_MAPPER, params.toArray());
    }

    @Override
    public TechnicalLogKpisDTO getKpis(LocalDateTime since) {
        String sql = """
                SELECT
                    COUNT(*) AS total_events,
                    SUM(CASE WHEN status = 'ERROR' THEN 1 ELSE 0 END) AS error_count,
                    SUM(CASE WHEN status = 'WARNING' THEN 1 ELSE 0 END) AS warning_count
                FROM technical_logs
                WHERE created_at >= ?
                """;
        var row = jdbcTemplate.queryForMap(sql, Timestamp.valueOf(since));
        long total = ((Number) row.get("total_events")).longValue();
        long errors = ((Number) row.get("error_count")).longValue();
        long warnings = ((Number) row.get("warning_count")).longValue();

        int compliance = total == 0 ? 100 : (int) Math.max(0, Math.min(100, 100 - (errors * 100.0 / total)));

        TechnicalLogKpisDTO kpis = new TechnicalLogKpisDTO();
        kpis.setTotalEvents(metric(total, 0));
        kpis.setSecurityAlerts(metric(errors, 0));
        kpis.setDataChanges(metric(warnings, 0));
        kpis.setComplianceScore(metric(compliance, 0));
        return kpis;
    }

    @Override
    public List<String> listDistinctModules() {
        return jdbcTemplate.queryForList(
                "SELECT DISTINCT module FROM technical_logs ORDER BY module", String.class);
    }

    @Override
    public List<String> listDistinctActions() {
        return jdbcTemplate.queryForList(
                "SELECT DISTINCT action FROM technical_logs WHERE action IS NOT NULL ORDER BY action", String.class);
    }

    private static void appendFilter(StringBuilder sql, List<Object> params, String clause, Object value) {
        if (value == null) {
            return;
        }
        if (value instanceof String s && s.isBlank()) {
            return;
        }
        sql.append(" AND ").append(clause);
        params.add(value);
    }

    private static SubscriptionKpiMetricDTO metric(long value, double delta) {
        SubscriptionKpiMetricDTO m = new SubscriptionKpiMetricDTO();
        m.setValue(BigDecimal.valueOf(value));
        m.setDelta(BigDecimal.valueOf(delta));
        return m;
    }

    private static String truncate(String value, int max) {
        if (value == null) {
            return null;
        }
        return value.length() <= max ? value : value.substring(0, max);
    }
}
