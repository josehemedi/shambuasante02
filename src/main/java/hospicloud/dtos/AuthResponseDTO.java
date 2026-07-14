package hospicloud.dtos;

public class AuthResponseDTO {

    private String token;
    private String tokenType = "Bearer";
    private AuthUserDTO user;

    public AuthResponseDTO() {}

    public AuthResponseDTO(String token, AuthUserDTO user) {
        this.token = token;
        this.user = user;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getTokenType() {
        return tokenType;
    }

    public void setTokenType(String tokenType) {
        this.tokenType = tokenType;
    }

    public AuthUserDTO getUser() {
        return user;
    }

    public void setUser(AuthUserDTO user) {
        this.user = user;
    }
}
