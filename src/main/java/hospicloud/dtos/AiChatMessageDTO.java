package hospicloud.dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class AiChatMessageDTO {

    @NotBlank
    @Pattern(regexp = "user|assistant|system", message = "Rôle invalide")
    private String role;

    @NotBlank
    @Size(max = 8000)
    private String content;

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
}
