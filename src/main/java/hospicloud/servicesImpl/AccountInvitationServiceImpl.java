package hospicloud.servicesImpl;

import hospicloud.exceptions.BadRequestException;
import hospicloud.exceptions.ConflictException;
import hospicloud.exceptions.ResourceNotFoundException;
import hospicloud.model.Hopital;
import hospicloud.model.PasswordResetToken;
import hospicloud.model.Role;
import hospicloud.model.Utilisateur;
import hospicloud.repositories.HopitalRepository;
import hospicloud.repositories.PasswordResetTokenRepository;
import hospicloud.repositories.UtilisateurRepository;
import hospicloud.security.PasswordResetTokenSupport;
import hospicloud.services.AccountInvitationService;
import hospicloud.services.TechnicalLogService;
import hospicloud.utils.EmailService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class AccountInvitationServiceImpl implements AccountInvitationService {

    private static final Logger logger = LoggerFactory.getLogger(AccountInvitationServiceImpl.class);

    private final UtilisateurRepository utilisateurRepository;
    private final HopitalRepository hopitalRepository;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;
    private final TechnicalLogService technicalLogService;
    private final String frontendBaseUrl;
    private final int activationExpirationMinutes;

    public AccountInvitationServiceImpl(UtilisateurRepository utilisateurRepository,
                                        HopitalRepository hopitalRepository,
                                        PasswordResetTokenRepository passwordResetTokenRepository,
                                        PasswordEncoder passwordEncoder,
                                        EmailService emailService,
                                        TechnicalLogService technicalLogService,
                                        @Value("${app.frontend.base-url:http://localhost:5173}") String frontendBaseUrl,
                                        @Value("${app.account-activation.expiration-minutes:1440}") int activationExpirationMinutes) {
        this.utilisateurRepository = utilisateurRepository;
        this.hopitalRepository = hopitalRepository;
        this.passwordResetTokenRepository = passwordResetTokenRepository;
        this.passwordEncoder = passwordEncoder;
        this.emailService = emailService;
        this.technicalLogService = technicalLogService;
        this.frontendBaseUrl = frontendBaseUrl.endsWith("/")
                ? frontendBaseUrl.substring(0, frontendBaseUrl.length() - 1)
                : frontendBaseUrl;
        this.activationExpirationMinutes = activationExpirationMinutes > 0 ? activationExpirationMinutes : 1440;
    }

    @Override
    @Transactional
    public Utilisateur inviteHospitalAdmin(Integer idHopital,
                                           String prenom,
                                           String nom,
                                           String email,
                                           String telephone) {
        if (idHopital == null) {
            throw new BadRequestException("Hôpital requis.");
        }
        Hopital hopital = hopitalRepository.rechercherhopitalParId(idHopital.longValue());
        if (hopital == null) {
            throw new ResourceNotFoundException("Hôpital introuvable.");
        }

        String normalizedEmail = normalizeEmail(email);
        if (prenom == null || prenom.isBlank() || nom == null || nom.isBlank()) {
            throw new BadRequestException("Le prénom et le nom de l'administrateur sont requis.");
        }
        if (normalizedEmail.isBlank()) {
            throw new BadRequestException("L'email de l'administrateur est requis.");
        }
        if (utilisateurRepository.existsByEmail(normalizedEmail)) {
            throw new ConflictException("Un utilisateur avec cet email existe déjà.");
        }

        Utilisateur utilisateur = new Utilisateur();
        utilisateur.setPrenom(prenom.trim());
        utilisateur.setNom(nom.trim());
        utilisateur.setEmail(normalizedEmail);
        utilisateur.setTelephone(telephone != null && !telephone.isBlank() ? telephone.trim() : null);
        // Mot de passe provisoire illisible — remplacé à l'activation.
        utilisateur.setMotDePasse(passwordEncoder.encode("pending-activation-" + UUID.randomUUID()));
        utilisateur.setRole(Role.TENANT_ADMIN);
        utilisateur.setIdHopital(idHopital);
        utilisateur.setEstActif(false);
        utilisateur.setDateCreation(LocalDateTime.now());

        Utilisateur saved = utilisateurRepository.insert(utilisateur);
        sendActivationEmail(saved, hopital.getNom());

        technicalLogService.recordAuthEvent(
                "HOSPITAL_ADMIN_INVITED",
                "Invitation administrateur hôpital envoyée",
                "INFO",
                idHopital,
                saved.getIdUtilisateur(),
                saved.getEmail(),
                Role.TENANT_ADMIN.name(),
                null,
                null);

        return saved;
    }

    @Override
    @Transactional
    public void activateAccount(String rawToken, String newPassword) {
        validateNewPassword(newPassword);
        if (rawToken == null || rawToken.isBlank()) {
            throw new BadRequestException("Lien d'activation invalide ou expiré.");
        }

        String tokenHash = PasswordResetTokenSupport.hashToken(rawToken.trim());
        PasswordResetToken token = passwordResetTokenRepository.findValidByHash(tokenHash)
                .orElseThrow(() -> new BadRequestException("Lien d'activation invalide ou expiré."));

        Utilisateur utilisateur = utilisateurRepository.findById(token.getIdUtilisateur())
                .orElseThrow(() -> new BadRequestException("Lien d'activation invalide ou expiré."));

        if (utilisateur.isEstActif()) {
            throw new BadRequestException("Ce compte est déjà activé. Connectez-vous ou réinitialisez votre mot de passe.");
        }

        utilisateurRepository.updatePassword(utilisateur.getIdUtilisateur(), passwordEncoder.encode(newPassword));
        boolean activated = utilisateurRepository.setActive(
                utilisateur.getIdUtilisateur(),
                utilisateur.getIdHopital(),
                true);
        if (!activated) {
            // Fallback si id_hopital null (ne devrait pas arriver pour TENANT_ADMIN)
            utilisateurRepository.setActiveById(utilisateur.getIdUtilisateur(), true);
        }
        passwordResetTokenRepository.markUsed(token.getId());

        technicalLogService.recordAuthEvent(
                "ACCOUNT_ACTIVATED",
                "Compte administrateur activé",
                "INFO",
                utilisateur.getIdHopital(),
                utilisateur.getIdUtilisateur(),
                utilisateur.getEmail(),
                utilisateur.getRole() != null ? utilisateur.getRole().name() : null,
                null,
                null);
    }

    @Override
    @Transactional
    public void resendActivation(String email) {
        String normalized = normalizeEmail(email);
        if (normalized.isBlank()) {
            return;
        }
        utilisateurRepository.findByEmailAnyStatus(normalized).ifPresent(utilisateur -> {
            if (utilisateur.isEstActif()) {
                return;
            }
            if (utilisateur.getRole() != Role.TENANT_ADMIN) {
                return;
            }
            String hopitalName = null;
            if (utilisateur.getIdHopital() != null) {
                Hopital h = hopitalRepository.rechercherhopitalParId(utilisateur.getIdHopital().longValue());
                if (h != null) hopitalName = h.getNom();
            }
            sendActivationEmail(utilisateur, hopitalName);
        });
    }

    private void sendActivationEmail(Utilisateur utilisateur, String hopitalName) {
        passwordResetTokenRepository.invalidateAllForUser(utilisateur.getIdUtilisateur());

        String rawToken = PasswordResetTokenSupport.generateRawToken();
        PasswordResetToken token = new PasswordResetToken();
        token.setIdUtilisateur(utilisateur.getIdUtilisateur());
        token.setIdHopital(utilisateur.getIdHopital());
        token.setTokenHash(PasswordResetTokenSupport.hashToken(rawToken));
        token.setExpiresAt(LocalDateTime.now().plusMinutes(activationExpirationMinutes));
        token.setCreatedAt(LocalDateTime.now());
        passwordResetTokenRepository.save(token);

        String activateUrl = frontendBaseUrl + "/activate?token=" + rawToken;
        String fullName = buildDisplayName(utilisateur);
        String hospitalLabel = hopitalName != null && !hopitalName.isBlank() ? hopitalName : "votre établissement";
        String subject = "Activez votre compte administrateur — ShambuaSanté";
        String html = buildActivationEmailHtml(fullName, hospitalLabel, activateUrl, activationExpirationMinutes);

        try {
            emailService.envoyerEmailHtml(utilisateur.getEmail(), subject, html);
        } catch (Exception ex) {
            logger.warn("Échec envoi email activation pour {} : {}", utilisateur.getEmail(), ex.getMessage());
            logger.info("Lien d'activation (secours) pour {} : {}", utilisateur.getEmail(), activateUrl);
        }
    }

    private void validateNewPassword(String newPassword) {
        if (newPassword == null || newPassword.length() < 8) {
            throw new BadRequestException("Le mot de passe doit comporter au moins 8 caractères.");
        }
    }

    private static String normalizeEmail(String email) {
        return email == null ? "" : email.trim().toLowerCase();
    }

    private static String buildDisplayName(Utilisateur utilisateur) {
        String fullName = ((utilisateur.getPrenom() != null ? utilisateur.getPrenom() : "")
                + " "
                + (utilisateur.getNom() != null ? utilisateur.getNom() : "")).trim();
        return fullName.isBlank() ? utilisateur.getEmail() : fullName;
    }

    private static String buildActivationEmailHtml(String fullName, String hopitalName, String activateUrl, int expirationMinutes) {
        int hours = Math.max(1, expirationMinutes / 60);
        return """
            <div style="font-family:Arial,sans-serif;line-height:1.5;color:#111827;max-width:560px">
              <h2 style="color:#0f766e;margin-bottom:8px">ShambuaSanté</h2>
              <p>Bonjour <strong>%s</strong>,</p>
              <p>Vous avez été désigné(e) comme <strong>administrateur</strong> de l'établissement
                 <strong>%s</strong> sur la plateforme ShambuaSanté.</p>
              <p>Pour confirmer votre adresse e-mail, créer votre mot de passe et activer votre compte, cliquez ci-dessous :</p>
              <p style="margin:24px 0">
                <a href="%s" style="display:inline-block;background:#0f766e;color:#fff;padding:14px 22px;border-radius:10px;text-decoration:none;font-weight:700">
                  Activer mon compte
                </a>
              </p>
              <p style="font-size:13px;color:#6b7280">Ce lien expire dans %d heure(s). Si vous n'êtes pas concerné(e), ignorez cet e-mail.</p>
              <p style="font-size:12px;color:#9ca3af;word-break:break-all">Lien direct : %s</p>
            </div>
            """.formatted(fullName, hopitalName, activateUrl, hours, activateUrl);
    }
}
