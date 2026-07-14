package hospicloud.repositoriesImpl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import hospicloud.async.AsyncJobResponse;
import hospicloud.async.AsyncJobStatus;
import hospicloud.async.AsyncJobType;
import hospicloud.repositories.AsyncJobRepository;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.util.Map;
import java.util.Optional;

@Repository
public class AsyncJobRepositoryImpl implements AsyncJobRepository {

    private final JdbcTemplate jdbcTemplate;
    private final ObjectMapper objectMapper;

    public AsyncJobRepositoryImpl(JdbcTemplate jdbcTemplate, ObjectMapper objectMapper) {
        this.jdbcTemplate = jdbcTemplate;
        this.objectMapper = objectMapper;
    }

    @Override
    public void ensureSchema() {
        // handled by AsyncJobsSchemaMigration
    }

    @Override
    public void insert(String jobId, AsyncJobType type, AsyncJobStatus status, Integer idHopital,
                       Integer actorUserId, Long entityId, String payloadJson) {
        jdbcTemplate.update(
                """
                INSERT INTO async_jobs (
                    job_id, job_type, status, id_hopital, actor_user_id, entity_id, payload_json
                ) VALUES (?, ?, ?, ?, ?, ?, ?)
                """,
                jobId,
                type.name(),
                status.name(),
                idHopital,
                actorUserId,
                entityId,
                payloadJson);
    }

    @Override
    public void updateStatus(String jobId, AsyncJobStatus status, String errorMessage) {
        jdbcTemplate.update(
                """
                UPDATE async_jobs
                SET status = ?, error_message = ?, updated_at = CURRENT_TIMESTAMP,
                    completed_at = CASE WHEN ? IN ('SUCCEEDED','FAILED') THEN CURRENT_TIMESTAMP ELSE completed_at END
                WHERE job_id = ?
                """,
                status.name(),
                errorMessage,
                status.name(),
                jobId);
    }

    @Override
    public void markSucceeded(String jobId, String resultJson, String resultPath) {
        jdbcTemplate.update(
                """
                UPDATE async_jobs
                SET status = 'SUCCEEDED',
                    result_json = ?,
                    result_path = ?,
                    error_message = NULL,
                    updated_at = CURRENT_TIMESTAMP,
                    completed_at = CURRENT_TIMESTAMP
                WHERE job_id = ?
                """,
                resultJson,
                resultPath,
                jobId);
    }

    @Override
    public Optional<AsyncJobResponse> findById(String jobId) {
        try {
            return jdbcTemplate.query(
                    """
                    SELECT job_id, job_type, status, result_json, result_path, error_message,
                           created_at, completed_at
                    FROM async_jobs WHERE job_id = ?
                    """,
                    rs -> {
                        if (!rs.next()) return Optional.<AsyncJobResponse>empty();
                        AsyncJobResponse r = new AsyncJobResponse();
                        r.setJobId(rs.getString("job_id"));
                        r.setType(AsyncJobType.valueOf(rs.getString("job_type")));
                        r.setStatus(AsyncJobStatus.valueOf(rs.getString("status")));
                        r.setResultRef(rs.getString("result_path"));
                        r.setErrorMessage(rs.getString("error_message"));
                        String json = rs.getString("result_json");
                        if (json != null && !json.isBlank()) {
                            try {
                                r.setResult(objectMapper.readValue(json, new TypeReference<Map<String, Object>>() {}));
                            } catch (Exception ignored) {
                            }
                        }
                        Timestamp created = rs.getTimestamp("created_at");
                        Timestamp completed = rs.getTimestamp("completed_at");
                        if (created != null) r.setCreatedAt(created.toLocalDateTime());
                        if (completed != null) r.setCompletedAt(completed.toLocalDateTime());
                        return Optional.of(r);
                    },
                    jobId);
        } catch (Exception e) {
            return Optional.empty();
        }
    }
}
