package hospicloud.repositories;

import hospicloud.async.AsyncJobMessage;
import hospicloud.async.AsyncJobResponse;
import hospicloud.async.AsyncJobStatus;
import hospicloud.async.AsyncJobType;

import java.util.Map;
import java.util.Optional;

public interface AsyncJobRepository {
    void insert(String jobId, AsyncJobType type, AsyncJobStatus status, Integer idHopital,
                Integer actorUserId, Long entityId, String payloadJson);

    void updateStatus(String jobId, AsyncJobStatus status, String errorMessage);

    void markSucceeded(String jobId, String resultJson, String resultPath);

    Optional<AsyncJobResponse> findById(String jobId);

    void ensureSchema();
}
