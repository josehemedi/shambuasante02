package hospicloud.services;

import hospicloud.async.AsyncJobResponse;
import hospicloud.async.AsyncJobType;

import java.util.Map;

public interface AsyncJobService {
    AsyncJobResponse enqueueReport(AsyncJobType type, Integer idHopital, Integer actorUserId,
                                   Long entityId, Map<String, Object> payload);

    AsyncJobResponse enqueueEnregistrement(AsyncJobType type, Integer idHopital, Integer actorUserId,
                                           Map<String, Object> payload);

    AsyncJobResponse getJob(String jobId);

    byte[] loadReportBytes(String jobId);
}
