package hospicloud.async;

import java.time.LocalDateTime;
import java.util.Map;

public class AsyncJobMessage {
    private String jobId;
    private AsyncJobType type;
    private Integer idHopital;
    private Integer actorUserId;
    private Long entityId;
    private Map<String, Object> payload;
    private LocalDateTime requestedAt;

    public String getJobId() { return jobId; }
    public void setJobId(String jobId) { this.jobId = jobId; }

    public AsyncJobType getType() { return type; }
    public void setType(AsyncJobType type) { this.type = type; }

    public Integer getIdHopital() { return idHopital; }
    public void setIdHopital(Integer idHopital) { this.idHopital = idHopital; }

    public Integer getActorUserId() { return actorUserId; }
    public void setActorUserId(Integer actorUserId) { this.actorUserId = actorUserId; }

    public Long getEntityId() { return entityId; }
    public void setEntityId(Long entityId) { this.entityId = entityId; }

    public Map<String, Object> getPayload() { return payload; }
    public void setPayload(Map<String, Object> payload) { this.payload = payload; }

    public LocalDateTime getRequestedAt() { return requestedAt; }
    public void setRequestedAt(LocalDateTime requestedAt) { this.requestedAt = requestedAt; }
}
