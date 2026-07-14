package hospicloud.servicesImpl;

import hospicloud.dtos.AuthResponseDTO;
import hospicloud.dtos.AuthUserDTO;
import hospicloud.dtos.LoginRequestDTO;
import hospicloud.exceptions.AlreadyLoggedInException;
import hospicloud.exceptions.BadRequestException;
import hospicloud.exceptions.DisabledAccountException;
import hospicloud.exceptions.TenantSubscriptionLapsedException;
import hospicloud.model.Hopital;
import hospicloud.model.Medecin;
import hospicloud.model.PasswordResetToken;
import hospicloud.model.Role;
import hospicloud.model.Utilisateur;
import hospicloud.repositories.HopitalRepository;
import hospicloud.repositories.MedecinRepository;
import hospicloud.repositories.PasswordResetTokenRepository;
import hospicloud.repositories.UtilisateurRepository;
import hospicloud.security.JwtService;
import hospicloud.security.PasswordResetTokenSupport;
import hospicloud.security.RoleMapper;
import hospicloud.security.TenantContext;
import hospicloud.security.UserPresenceService;
import hospicloud.security.UserSessionService;
import hospicloud.security.UtilisateurPrincipal;
import hospicloud.services.AuthService;
import hospicloud.services.TechnicalLogService;
import hospicloud.services.TenantSubscriptionAccessService;
import hospicloud.utils.EmailService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import jakarta.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class AuthServiceImpl implements AuthService {

    private static final Logger logger = LoggerFactory.getLogger(AuthServiceImpl.class);

    private final UtilisateurRepository utilisateurRepository;
    private final HopitalRepository hopitalRepository;
    private final MedecinRepository medecinRepository;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final UserPresenceService userPresenceService;
    private final UserSessionService userSessionService;
    private final TechnicalLogService technicalLogService;
    private final EmailService emailService;
    private final TenantSubscriptionAccessService tenantSubscriptionAccessService;
    private final String frontendBaseUrl;
    private final int resetExpirationMinutes;

    public AuthServiceImpl(UtilisateurRepository utilisateurRepository,
                           HopitalRepository hopitalRepository,
                           MedecinRepository medecinRepository,
                           PasswordResetTokenRepository passwordResetTokenRepository,
                           PasswordEncoder passwordEncoder,
                           JwtService jwtService,
                           UserPresenceService userPresenceService,
                           UserSessionService userSessionService,
                           TechnicalLogService technicalLogService,
                           EmailService emailService,
                           TenantSubscriptionAccessService tenantSubscriptionAccessService,
                           @Value("${app.frontend.base-url:http://localhost:5173}") String frontendBaseUrl,
                           @Value("${app.password-reset.expiration-minutes:60}") int resetExpirationMinutes) {
        this.utilisateurRepository = utilisateurRepository;
        this.hopitalRepository = hopitalRepository;
        this.medecinRepository = medecinRepository;
        this.passwordResetTokenRepository = passwordResetTokenRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.userPresenceService = userPresenceService;
        this.userSessionService = userSessionService;
        this.technicalLogService = technicalLogService;
        this.emailService = emailService;
        this.tenantSubscriptionAccessService = tenantSubscriptionAccessService;
        this.frontendBaseUrl = frontendBaseUrl.endsWith("/")
                ? frontendBaseUrl.substring(0, frontendBaseUrl.length() - 1)
                : frontendBaseUrl;
        this.resetExpirationMinutes = resetExpirationMinutes;
    }

    @Override
    public AuthResponseDTO login(LoginRequestDTO request) {
        if (request.getEmail() == null || request.getEmail().isBlank()) {
            throw new BadCredentialsException("Email requis");
        }

        Utilisateur utilisateur = utilisateurRepository.findByEmailAnyStatus(request.getEmail().trim().toLowerCase())
                .orElseThrow(() -> {
                    logAuthFailure(null, null, request.getEmail(), "LOGIN_FAILED", "Identifiants invalides");
                    return new BadCredentialsException("Identifiants invalides");
                });

        if (!utilisateur.isEstActif()) {
            logAuthFailure(utilisateur.getIdHopital(), utilisateur.getIdUtilisateur(), utilisateur.getEmail(),
                    "LOGIN_BLOCKED", "Compte désactivé");
            throw new DisabledAccountException();
        }

        if (!passwordEncoder.matches(request.getPassword(), utilisateur.getMotDePasse())) {
            logAuthFailure(utilisateur.getIdHopital(), utilisateur.getIdUtilisateur(), utilisateur.getEmail(),
                    "LOGIN_FAILED", "Mot de passe incorrect");
            throw new BadCredentialsException("Identifiants invalides");
        }

        if (utilisateur.getIdHopital() != null
                && utilisateur.getRole() != Role.SUPER_ADMIN
                && utilisateur.getRole() != Role.TENANT_ADMIN
                && tenantSubscriptionAccessService.isPlatformAccessRestricted(utilisateur.getIdHopital())) {
            logAuthFailure(utilisateur.getIdHopital(), utilisateur.getIdUtilisateur(), utilisateur.getEmail(),
                    "LOGIN_BLOCKED", "Abonnement établissement expiré");
            throw new TenantSubscriptionLapsedException();
        }

        // Lie / crée le profil métier médecin avant d'émettre le JWT (sinon idMedecin reste null).
        ensureMedecinProfileLinked(utilisateur);

        String token;
        if (utilisateur.getRole() == Role.MEDECIN) {
            userSessionService.purgeExpired();
            userSessionService.purgeInactiveSessions();
            if (userSessionService.hasRecentActiveSession(utilisateur.getIdUtilisateur())
                    && userPresenceService.isPresent(utilisateur.getIdUtilisateur())) {
                logAuthFailure(utilisateur.getIdHopital(), utilisateur.getIdUtilisateur(), utilisateur.getEmail(),
                        "LOGIN_BLOCKED", "Session déjà active sur un autre appareil");
                throw new AlreadyLoggedInException();
            }
            userSessionService.invalidateAllForUser(utilisateur.getIdUtilisateur());
            String jti = jwtService.newSessionId();
            token = jwtService.generateToken(utilisateur, jti);
            userSessionService.registerSession(utilisateur.getIdUtilisateur(), jti, clientIp());
        } else {
            token = jwtService.generateToken(utilisateur);
        }

        userPresenceService.markPresent(utilisateur.getIdUtilisateur());
        logAuthSuccess(utilisateur);
        return new AuthResponseDTO(token, toAuthUserDTO(utilisateur));
    }

    @Override
    public void logout() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof UtilisateurPrincipal principal) {
            if (principal.getAppRole() == Role.MEDECIN) {
                String bearer = currentBearerToken();
                if (bearer != null) {
                    String jti = jwtService.extractJti(bearer);
                    userSessionService.invalidateSession(jti);
                } else {
                    userSessionService.invalidateAllForUser(principal.getIdUtilisateur());
                }
            }
            userPresenceService.markAbsent(principal.getIdUtilisateur());
            technicalLogService.recordAuthEvent(
                    "LOGOUT",
                    "Déconnexion utilisateur",
                    "INFO",
                    principal.getIdHopital(),
                    principal.getIdUtilisateur(),
                    principal.getUsername(),
                    principal.getAppRole() != null ? principal.getAppRole().name() : null,
                    clientIp(),
                    userAgent());
        }
    }

    @Override
    public AuthUserDTO getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof UtilisateurPrincipal principal)) {
            throw new BadCredentialsException("Non authentifié");
        }

        return utilisateurRepository.findByEmail(principal.getUsername())
                .map(u -> {
                    ensureMedecinProfileLinked(u);
                    return toAuthUserDTO(u);
                })
                .orElseGet(() -> {
                    AuthUserDTO dto = new AuthUserDTO();
                    dto.setEmail(principal.getUsername());
                    dto.setRole(principal.getAppRole().name());
                    dto.setFrontendRole(RoleMapper.toFrontendRole(principal.getAppRole()));
                    dto.setIdHopital(principal.getIdHopital());
                    dto.setIdMedecin(principal.getIdMedecin());
                    dto.setIdPatient(principal.getIdPatient());
                    return dto;
                });
    }

    /**
     * Garantit utilisateurs.id_medecin pour les comptes ROLE=MEDECIN du tenant.
     * Être dans la table {@code medecin} ne suffit pas : le compte login doit pointer vers ce profil.
     */
    private void ensureMedecinProfileLinked(Utilisateur utilisateur) {
        if (utilisateur == null || utilisateur.getRole() != Role.MEDECIN || utilisateur.getIdHopital() == null) {
            return;
        }

        Integer previousTenant = TenantContext.getHopitalId();
        try {
            TenantContext.setHopitalId(utilisateur.getIdHopital());

            if (utilisateur.getIdMedecin() != null) {
                repairIncompleteMedecinProfile(utilisateur);
                return;
            }

            Integer medecinId = medecinRepository.trouverParEmail(utilisateur.getEmail())
                    .map(Medecin::getIdMedecin)
                    .orElse(null);

            if (medecinId == null) {
                Medecin created = new Medecin();
                created.setIdHopital(utilisateur.getIdHopital());
                created.setNom(utilisateur.getNom());
                created.setPrenom(utilisateur.getPrenom());
                created.setEmail(utilisateur.getEmail());
                created.setSpecialite("Médecine générale");
                created.setDisponibiliteStatus(Boolean.TRUE);
                medecinId = medecinRepository.creerEtRetournerId(created);
                logger.info(
                        "Profil medecin auto-créé id={} pour utilisateur {} (tenant {})",
                        medecinId,
                        utilisateur.getEmail(),
                        utilisateur.getIdHopital());
            }

            if (medecinId != null) {
                utilisateurRepository.updateMedecinLink(
                        utilisateur.getIdUtilisateur(),
                        utilisateur.getIdHopital(),
                        medecinId);
                utilisateur.setIdMedecin(medecinId);
            }
        } catch (Exception ex) {
            logger.warn(
                    "Impossible de lier le profil médecin pour {} : {}",
                    utilisateur.getEmail(),
                    ex.getMessage());
        } finally {
            if (previousTenant != null) {
                TenantContext.setHopitalId(previousTenant);
            } else {
                TenantContext.clear();
            }
        }
    }

    private void repairIncompleteMedecinProfile(Utilisateur utilisateur) {
        try {
            medecinRepository.trouverParId(utilisateur.getIdMedecin()).ifPresent(profile -> {
                boolean dirty = false;
                if ((profile.getEmail() == null || profile.getEmail().isBlank())
                        && utilisateur.getEmail() != null) {
                    profile.setEmail(utilisateur.getEmail());
                    dirty = true;
                }
                if ((profile.getNom() == null || profile.getNom().isBlank())
                        && utilisateur.getNom() != null) {
                    profile.setNom(utilisateur.getNom());
                    dirty = true;
                }
                if ((profile.getPrenom() == null || profile.getPrenom().isBlank())
                        && utilisateur.getPrenom() != null) {
                    profile.setPrenom(utilisateur.getPrenom());
                    dirty = true;
                }
                if (dirty) {
                    medecinRepository.mettreAJour(profile);
                }
            });
        } catch (Exception ex) {
            logger.debug("Réparation profil médecin ignorée: {}", ex.getMessage());
        }
    }

    @Override
    public void requestPasswordReset(String email) {
        if (email == null || email.isBlank()) {
            return;
        }

        String normalizedEmail = email.trim().toLowerCase();
        utilisateurRepository.findByEmail(normalizedEmail).ifPresent(this::createAndSendResetToken);
    }

    @Override
    public void resetPassword(String rawToken, String newPassword) {
        validateNewPassword(newPassword);

        if (rawToken == null || rawToken.isBlank()) {
            throw new BadRequestException("Lien de réinitialisation invalide ou expiré.");
        }

        String tokenHash = PasswordResetTokenSupport.hashToken(rawToken.trim());
        PasswordResetToken resetToken = passwordResetTokenRepository.findValidByHash(tokenHash)
                .orElseThrow(() -> new BadRequestException("Lien de réinitialisation invalide ou expiré."));

        Utilisateur utilisateur = utilisateurRepository.findById(resetToken.getIdUtilisateur())
                .orElseThrow(() -> new BadRequestException("Lien de réinitialisation invalide ou expiré."));

        if (!utilisateur.isEstActif()) {
            throw new BadRequestException("Ce compte est désactivé. Contactez l'administrateur de votre établissement.");
        }

        utilisateurRepository.updatePassword(utilisateur.getIdUtilisateur(), passwordEncoder.encode(newPassword));
        passwordResetTokenRepository.markUsed(resetToken.getId());

        technicalLogService.recordAuthEvent(
                "PASSWORD_RESET",
                "Mot de passe réinitialisé",
                "INFO",
                utilisateur.getIdHopital(),
                utilisateur.getIdUtilisateur(),
                utilisateur.getEmail(),
                utilisateur.getRole() != null ? utilisateur.getRole().name() : null,
                clientIp(),
                userAgent());
    }

    private void createAndSendResetToken(Utilisateur utilisateur) {
        passwordResetTokenRepository.invalidateAllForUser(utilisateur.getIdUtilisateur());

        String rawToken = PasswordResetTokenSupport.generateRawToken();
        PasswordResetToken token = new PasswordResetToken();
        token.setIdUtilisateur(utilisateur.getIdUtilisateur());
        token.setIdHopital(utilisateur.getIdHopital());
        token.setTokenHash(PasswordResetTokenSupport.hashToken(rawToken));
        token.setExpiresAt(LocalDateTime.now().plusMinutes(resetExpirationMinutes));
        token.setCreatedAt(LocalDateTime.now());
        passwordResetTokenRepository.save(token);

        String resetUrl = frontendBaseUrl + "/reset-password?token=" + rawToken;
        String fullName = buildDisplayName(utilisateur);
        String subject = "Réinitialisation de votre mot de passe — ShambuaSanté";
        String html = buildResetEmailHtml(fullName, resetUrl, resetExpirationMinutes);

        try {
            emailService.envoyerEmailHtml(utilisateur.getEmail(), subject, html);
        } catch (Exception ex) {
            logger.warn("Échec envoi email reset pour {} : {}", utilisateur.getEmail(), ex.getMessage());
            logger.info("Lien de réinitialisation (secours dev) pour {} : {}", utilisateur.getEmail(), resetUrl);
        }

        technicalLogService.recordAuthEvent(
                "PASSWORD_RESET_REQUEST",
                "Demande de réinitialisation du mot de passe",
                "INFO",
                utilisateur.getIdHopital(),
                utilisateur.getIdUtilisateur(),
                utilisateur.getEmail(),
                utilisateur.getRole() != null ? utilisateur.getRole().name() : null,
                clientIp(),
                userAgent());
    }

    private void validateNewPassword(String newPassword) {
        if (newPassword == null || newPassword.length() < 8) {
            throw new BadRequestException("Le mot de passe doit comporter au moins 8 caractères.");
        }
    }

    private String buildDisplayName(Utilisateur utilisateur) {
        String fullName = ((utilisateur.getPrenom() != null ? utilisateur.getPrenom() : "")
                + " "
                + (utilisateur.getNom() != null ? utilisateur.getNom() : "")).trim();
        return fullName.isBlank() ? utilisateur.getEmail() : fullName;
    }

    private String buildResetEmailHtml(String fullName, String resetUrl, int expirationMinutes) {
        return """
            <div style="font-family:Arial,sans-serif;line-height:1.5;color:#111827;max-width:560px">
              <h2 style="color:#0f766e">ShambuaSanté</h2>
              <p>Bonjour %s,</p>
              <p>Vous avez demandé la réinitialisation de votre mot de passe.</p>
              <p>
                <a href="%s" style="display:inline-block;background:#0f766e;color:#fff;padding:12px 18px;border-radius:8px;text-decoration:none;font-weight:600">
                  Définir un nouveau mot de passe
                </a>
              </p>
              <p>Ce lien expire dans %d minutes. Si vous n'êtes pas à l'origine de cette demande, ignorez cet email.</p>
              <p style="font-size:12px;color:#6b7280">Lien direct : %s</p>
            </div>
            """.formatted(fullName, resetUrl, expirationMinutes, resetUrl);
    }

    private AuthUserDTO toAuthUserDTO(Utilisateur utilisateur) {
        AuthUserDTO dto = new AuthUserDTO();
        dto.setIdUtilisateur(utilisateur.getIdUtilisateur());
        dto.setEmail(utilisateur.getEmail());
        dto.setNom(utilisateur.getNom());
        dto.setPrenom(utilisateur.getPrenom());
        dto.setRole(utilisateur.getRole().name());
        dto.setFrontendRole(RoleMapper.toFrontendRole(utilisateur.getRole()));
        dto.setIdHopital(utilisateur.getIdHopital());
        dto.setIdMedecin(utilisateur.getIdMedecin());
        dto.setIdPatient(utilisateur.getIdPatient());

        if (utilisateur.getIdHopital() != null) {
            Hopital hopital = hopitalRepository.rechercherhopitalParId(utilisateur.getIdHopital().longValue());
            if (hopital != null) {
                dto.setTenantLabel(hopital.getNom());
            }
            dto.setTenantAccessRestricted(tenantSubscriptionAccessService.isPlatformAccessRestricted(
                    utilisateur.getIdHopital()));
        } else if (utilisateur.getRole() == hospicloud.model.Role.SUPER_ADMIN) {
            dto.setTenantLabel("All Tenants");
        }

        return dto;
    }

    private void logAuthSuccess(Utilisateur utilisateur) {
        technicalLogService.recordAuthEvent(
                "LOGIN_SUCCESS",
                "Connexion réussie",
                "INFO",
                utilisateur.getIdHopital(),
                utilisateur.getIdUtilisateur(),
                utilisateur.getEmail(),
                utilisateur.getRole() != null ? utilisateur.getRole().name() : null,
                clientIp(),
                userAgent());
    }

    private void logAuthFailure(Integer hopitalId, Integer userId, String email, String action, String message) {
        technicalLogService.recordAuthEvent(
                action,
                message,
                "WARNING",
                hopitalId,
                userId,
                email,
                null,
                clientIp(),
                userAgent());
    }

    private static String clientIp() {
        ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attrs == null) {
            return null;
        }
        HttpServletRequest request = attrs.getRequest();
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) {
            return forwarded.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }

    private static String userAgent() {
        ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attrs == null) {
            return null;
        }
        return attrs.getRequest().getHeader("User-Agent");
    }

    private static String currentBearerToken() {
        ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attrs == null) {
            return null;
        }
        String authHeader = attrs.getRequest().getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return null;
        }
        return authHeader.substring(7);
    }
}
