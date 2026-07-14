package hospicloud.async;

import java.time.LocalDateTime;
import java.util.Map;

public class AsyncJobResponse {
    private String jobId;
    private AsyncJobType type;
    private AsyncJobStatus status;
    private String statusUrl;
    private String downloadUrl;
    private String resultRef;
    private String errorMessage;
    private Map<String, Object> result;
    private LocalDateTime createdAt;
    private LocalDateTime completedAt;

    public String getJobId() { return jobId; }
    public void setJobId(String jobId) { this.jobId = jobId; }

    public AsyncJobType getType() { return type; }
    public void setType(AsyncJobType type) { this.type = type; }

    public AsyncJobStatus getStatus() { return status; }
    public void setStatus(AsyncJobStatus status) { this.status = status; }

    public String getStatusUrl() { return statusUrl; }
    public void setStatusUrl(String statusUrl) { this.statusUrl = statusUrl; }

    public String getDownloadUrl() { return downloadUrl; }
    public void setDownloadUrl(String downloadUrl) { this.downloadUrl = downloadUrl; }

    public String getResultRef() { return resultRef; }
    public void setResultRef(String resultRef) { this.resultRef = resultRef; }

    public String getErrorMessage() { return errorMessage; }
    public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }

    public Map<String, Object> getResult() { return result; }
    public void setResult(Map<String, Object> result) { this.result = result; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getCompletedAt() { return completedAt; }
    public void setCompletedAt(LocalDateTime completedAt) { this.completedAt = completedAt; }
}
