package hospicloud.services;

import hospicloud.dtos.AuthResponseDTO;
import hospicloud.dtos.AuthUserDTO;
import hospicloud.dtos.LoginRequestDTO;

public interface AuthService {

    AuthResponseDTO login(LoginRequestDTO request);

    AuthUserDTO getCurrentUser();

    void logout();

    void requestPasswordReset(String email);

    void resetPassword(String rawToken, String newPassword);
}
