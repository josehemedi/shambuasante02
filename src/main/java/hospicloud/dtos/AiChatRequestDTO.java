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
}
