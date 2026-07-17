package hospicloud.dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.ArrayList;
import java.util.List;

public class AiChatRequestDTO {

    @NotBlank
    @Size(max = 4000)
    private String message;

    private String analysisType;

    @Size(max = 12)
    private List<AiChatMessageDTO> history = new ArrayList<>();

    private Long patientId;

    /** Scope hint: MEDECIN | ADMIN | SUPER_ADMIN (optional, derived from role if absent). */
    private String ragScope;

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getAnalysisType() {
        return analysisType;
    }

    public void setAnalysisType(String analysisType) {
        this.analysisType = analysisType;
    }

    public List<AiChatMessageDTO> getHistory() {
        return history;
    }

    public void setHistory(List<AiChatMessageDTO> history) {
        this.history = history != null ? history : new ArrayList<>();
    }

    public Long getPatientId() {
        return patientId;
    }

    public void setPatientId(Long patientId) {
        this.patientId = patientId;
    }

    public String getRagScope() {
        return ragScope;
    }

    public void setRagScope(String ragScope) {
        this.ragScope = ragScope;
    }
}
