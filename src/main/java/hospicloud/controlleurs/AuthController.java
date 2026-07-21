package hospicloud.controlleurs;

import hospicloud.dtos.ActivateAccountRequestDTO;
import hospicloud.dtos.AuthResponseDTO;
import hospicloud.dtos.AuthUserDTO;
import hospicloud.dtos.ForgotPasswordRequestDTO;
import hospicloud.dtos.LoginRequestDTO;
import hospicloud.dtos.MessageResponseDTO;
import hospicloud.dtos.ResetPasswordRequestDTO;
import hospicloud.services.AccountInvitationService;
import hospicloud.services.AuthService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;
    private final AccountInvitationService accountInvitationService;

    public AuthController(AuthService authService, AccountInvitationService accountInvitationService) {
        this.authService = authService;
        this.accountInvitationService = accountInvitationService;
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

    /** Confirme l'email, définit le mot de passe et active le compte (admin hôpital invité). */
    @PostMapping("/activate")
    public ResponseEntity<MessageResponseDTO> activateAccount(@RequestBody ActivateAccountRequestDTO request) {
        accountInvitationService.activateAccount(request.getToken(), request.getPassword());
        return ResponseEntity.ok(new MessageResponseDTO(
                "Compte activé avec succès. Vous pouvez maintenant vous connecter."));
    }

    /** Renvoie l'email d'activation (réponse neutre pour ne pas divulguer l'existence du compte). */
    @PostMapping("/resend-activation")
    public ResponseEntity<MessageResponseDTO> resendActivation(@RequestBody ForgotPasswordRequestDTO request) {
        accountInvitationService.resendActivation(request.getEmail());
        return ResponseEntity.ok(new MessageResponseDTO(
                "Si un compte en attente d'activation existe pour cet email, un nouveau lien a été envoyé."));
    }
}
