package hospicloud.controlleurs;

import hospicloud.dtos.AuthResponseDTO;
import hospicloud.dtos.AuthUserDTO;
import hospicloud.dtos.ForgotPasswordRequestDTO;
import hospicloud.dtos.LoginRequestDTO;
import hospicloud.dtos.MessageResponseDTO;
import hospicloud.dtos.ResetPasswordRequestDTO;
import hospicloud.services.AuthService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponseDTO> login(@RequestBody LoginRequestDTO request) {
        return ResponseEntity.ok(authService.login(request));
    }

    @GetMapping("/me")
    public ResponseEntity<AuthUserDTO> me() {
        return ResponseEntity.ok(authService.getCurrentUser());
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout() {
        authService.logout();
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<MessageResponseDTO> forgotPassword(@RequestBody ForgotPasswordRequestDTO request) {
        authService.requestPasswordReset(request.getEmail());
        return ResponseEntity.ok(new MessageResponseDTO(
                "Si un compte actif existe pour cet email, un lien de réinitialisation a été envoyé."));
    }

    @PostMapping("/reset-password")
    public ResponseEntity<MessageResponseDTO> resetPassword(@RequestBody ResetPasswordRequestDTO request) {
        authService.resetPassword(request.getToken(), request.getPassword());
        return ResponseEntity.ok(new MessageResponseDTO("Mot de passe mis à jour avec succès."));
    }
}
