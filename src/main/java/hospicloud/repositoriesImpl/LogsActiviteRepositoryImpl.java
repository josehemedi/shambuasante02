package hospicloud.repositoriesImpl;

import hospicloud.repositories.LogsActiviteRepository;
import hospicloud.security.TenantContext;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;

@Repository
public class LogsActiviteRepositoryImpl implements LogsActiviteRepository {

    private final JdbcTemplate jdbcTemplate;

    public LogsActiviteRepositoryImpl(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public Long countActiveUsersInPeriod(Integer hopitalId, LocalDate startDate, LocalDate endDate) {
        Integer tenantId = hopitalId != null ? hopitalId : TenantContext.getRequiredHopitalId();
        String sql = "SELECT COUNT(DISTINCT id_utilisateur) FROM logs_activite WHERE id_hopital = ? AND date_activite >= ? AND date_activite < ?";
        return jdbcTemplate.queryForObject(sql, Long.class, tenantId, startDate.atStartOfDay(), endDate.plusDays(1).atStartOfDay());
    }
}
