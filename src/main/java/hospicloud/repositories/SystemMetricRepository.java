package hospicloud.repositories;

import hospicloud.model.SystemMetricHistory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Repository pour gérer l'entité SystemMetricHistory en utilisant JdbcTemplate.
 */
@Repository
public class SystemMetricRepository {

    private final JdbcTemplate jdbcTemplate;

    public SystemMetricRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private final RowMapper<SystemMetricHistory> rowMapper = (rs, rowNum) -> {
        SystemMetricHistory metric = new SystemMetricHistory();
        metric.setId(rs.getLong("id"));
        metric.setTenantId(rs.getString("tenant_id"));
        metric.setMetricName(rs.getString("metric_name"));
        metric.setMetricValue(rs.getDouble("metric_value"));
        metric.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
        return metric;
    };

    /**
     * Insère une nouvelle métrique dans la base de données.
     * 
     * @param metric L'objet métrique à sauvegarder.
     */
    public void save(SystemMetricHistory metric) {
        String sql = "INSERT INTO system_metric_history (tenant_id, metric_name, metric_value, created_at) VALUES (?, ?, ?, ?)";
        jdbcTemplate.update(sql, metric.getTenantId(), metric.getMetricName(), metric.getMetricValue(),
                metric.getCreatedAt());
    }

    /**
     * Récupère les métriques pour un tenant et un nom de métrique donnés.
     * 
     * @param tenantId   L'ID du tenant.
     * @param metricName Le nom de la métrique.
     * @return Une liste d'historiques de métriques.
     */
    public List<SystemMetricHistory> findByTenantIdAndMetricName(String tenantId, String metricName) {
        String sql = "SELECT * FROM system_metric_history WHERE tenant_id = ? AND metric_name = ? ORDER BY created_at DESC";
        return jdbcTemplate.query(sql, rowMapper, tenantId, metricName);
    }

    /**
     * Récupère toutes les métriques pour un tenant donné.
     * 
     * @param tenantId L'ID du tenant.
     * @return Une liste d'historiques de métriques.
     */
    public List<SystemMetricHistory> findByTenantId(String tenantId) {
        String sql = "SELECT * FROM system_metric_history WHERE tenant_id = ? ORDER BY created_at DESC";
        return jdbcTemplate.query(sql, rowMapper, tenantId);
    }

    /**
     * Supprime les métriques plus anciennes qu'une date donnée.
     * 
     * @param cutoffDate La date limite.
     * @return Le nombre de lignes supprimées.
     */
    public int deleteOlderThan(LocalDateTime cutoffDate) {
        String sql = "DELETE FROM system_metric_history WHERE created_at < ?";
        return jdbcTemplate.update(sql, cutoffDate);
    }
}
