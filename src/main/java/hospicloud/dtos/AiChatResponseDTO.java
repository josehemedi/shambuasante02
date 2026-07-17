package hospicloud.dtos;

import java.util.ArrayList;
import java.util.List;

public class AiChatResponseDTO {

    private String role = "assistant";
    private String content;
    private String model;
    private boolean configured;
    private List<String> sources = new ArrayList<>();
    private Integer confidence;
    private String ragScope;
    private List<String> warnings = new ArrayList<>();
    private List<String> missingFields = new ArrayList<>();

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public boolean isConfigured() {
        return configured;
    }

    public void setConfigured(boolean configured) {
        this.configured = configured;
    }

    public List<String> getSources() {
        return sources;
    }

    public void setSources(List<String> sources) {
        this.sources = sources != null ? sources : new ArrayList<>();
    }

    public Integer getConfidence() {
        return confidence;
    }

    public void setConfidence(Integer confidence) {
        this.confidence = confidence;
    }

    public String getRagScope() {
        return ragScope;
    }

    public void setRagScope(String ragScope) {
        this.ragScope = ragScope;
    }

    public List<String> getWarnings() {
        return warnings;
    }

    public void setWarnings(List<String> warnings) {
        this.warnings = warnings != null ? warnings : new ArrayList<>();
    }

    public List<String> getMissingFields() {
        return missingFields;
    }

    public void setMissingFields(List<String> missingFields) {
        this.missingFields = missingFields != null ? missingFields : new ArrayList<>();
    }
}
