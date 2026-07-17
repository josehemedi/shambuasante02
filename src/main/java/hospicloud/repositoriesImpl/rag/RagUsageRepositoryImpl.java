package hospicloud.repositoriesImpl.rag;

import hospicloud.repositories.rag.RagUsageRepository;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository
public class RagUsageRepositoryImpl implements RagUsageRepository {

    private final JdbcTemplate jdbcTemplate;

    public RagUsageRepositoryImpl(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void insert(Integer hopitalId, Integer userId, String roleCode, String scopeCode, Long patientId,
                       String modelName, String analysisType, int promptChars, int contextChars, int responseChars,
                       String sourcesJson, boolean success, String errorMessage, BigDecimal estimatedCostUsd) {
        jdbcTemplate.update("""
                INSERT INTO rag_usage_logs
                (hopital_id, user_id, role_code, scope_code, patient_id, model_name, analysis_type,
                 prompt_chars, context_chars, response_chars, sources_json, success, error_message, estimated_cost_usd)
                VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?)
                """,
                hopitalId, userId, roleCode, scopeCode, patientId, modelName, analysisType,
                promptChars, contextChars, responseChars, sourcesJson, success ? 1 : 0, errorMessage, estimatedCostUsd);
    }

    @Override
    public Map<String, Object> statsForHopital(Integer hopitalId) {
        Map<String, Object> row = jdbcTemplate.queryForMap("""
                SELECT
                  COUNT(*) AS total_calls,
                  SUM(CASE WHEN success = 1 THEN 1 ELSE 0 END) AS success_calls,
                  SUM(CASE WHEN success = 0 THEN 1 ELSE 0 END) AS error_calls,
                  COALESCE(SUM(estimated_cost_usd), 0) AS total_cost_usd,
                  COALESCE(SUM(prompt_chars + context_chars + response_chars), 0) AS total_chars
                FROM rag_usage_logs
                WHERE hopital_id = ?
                """, hopitalId);
        return new HashMap<>(row);
    }

    @Override
    public Map<String, Object> statsPlatform() {
        Map<String, Object> row = jdbcTemplate.queryForMap("""
                SELECT
                  COUNT(*) AS total_calls,
                  COUNT(DISTINCT hopital_id) AS hospitals_active,
                  SUM(CASE WHEN success = 0 THEN 1 ELSE 0 END) AS error_calls,
                  COALESCE(SUM(estimated_cost_usd), 0) AS total_cost_usd,
                  COALESCE(SUM(prompt_chars + context_chars + response_chars), 0) AS total_chars
                FROM rag_usage_logs
                """);
        return new HashMap<>(row);
    }

    @Override
    public List<Map<String, Object>> recentErrors(Integer hopitalId, int limit) {
        if (hopitalId == null) {
            return jdbcTemplate.queryForList("""
                    SELECT id, hopital_id, role_code, scope_code, model_name, error_message, created_at
                    FROM rag_usage_logs
                    WHERE success = 0
                    ORDER BY created_at DESC
                    LIMIT ?
                    """, limit);
        }
        return jdbcTemplate.queryForList("""
                SELECT id, hopital_id, role_code, scope_code, model_name, error_message, created_at
                FROM rag_usage_logs
                WHERE success = 0 AND hopital_id = ?
                ORDER BY created_at DESC
                LIMIT ?
                """, hopitalId, limit);
    }

    @Override
    public List<Map<String, Object>> usageByDay(Integer hopitalId, int days) {
        if (hopitalId == null) {
            return jdbcTemplate.queryForList("""
                    SELECT DATE(created_at) AS day, COUNT(*) AS calls,
                           COALESCE(SUM(estimated_cost_usd), 0) AS cost_usd
                    FROM rag_usage_logs
                    WHERE created_at >= DATE_SUB(CURDATE(), INTERVAL ? DAY)
                    GROUP BY DATE(created_at)
                    ORDER BY day
                    """, days);
        }
        return jdbcTemplate.queryForList("""
                SELECT DATE(created_at) AS day, COUNT(*) AS calls,
                       COALESCE(SUM(estimated_cost_usd), 0) AS cost_usd
                FROM rag_usage_logs
                WHERE hopital_id = ? AND created_at >= DATE_SUB(CURDATE(), INTERVAL ? DAY)
                GROUP BY DATE(created_at)
                ORDER BY day
                """, hopitalId, days);
    }
}
