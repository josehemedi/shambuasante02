package hospicloud.security;

import hospicloud.model.Medecin;
import hospicloud.model.Role;
import hospicloud.model.Utilisateur;
import hospicloud.repositories.MedecinRepository;
import hospicloud.repositories.UtilisateurRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Accès au profil de l'utilisateur connecté.
 * Pour les médecins, résout aussi {@code id_medecin} via JWT, utilisateurs ou table medecin.
 */
@Component
public class CurrentUserService {

    private static final Logger log = LoggerFactory.getLogger(CurrentUserService.class);

    private final UtilisateurRepository utilisateurRepository;
    private final MedecinRepository medecinRepository;

    public CurrentUserService(UtilisateurRepository utilisateurRepository,
                              MedecinRepository medecinRepository) {
        this.utilisateurRepository = utilisateurRepository;
        this.medecinRepository = medecinRepository;
    }

    public Integer getCurrentMedecinId() {
        Integer id = CurrentUserContext.getMedecinId();
        if (id != null) {
            return id;
        }

        if (getCurrentRole() != Role.MEDECIN) {
            return null;
        }

        return resolveAndCacheMedecinId();
    }

    /**
     * Résout l'id médecin manquant (comptes créés sans lien) et le mémorise pour la requête.
     */
    private Integer resolveAndCacheMedecinId() {
        String email = getCurrentUsername();
        if (email == null || email.isBlank()) {
            return null;
        }

        Integer hopitalId = null;
        try {
            hopitalId = TenantContext.getHopitalId();
        } catch (Exception ignored) {
            hopitalId = null;
        }
        if (hopitalId == null) {
            UtilisateurPrincipal principal = currentPrincipal();
            if (principal != null) {
                hopitalId = principal.getIdHopital();
            }
        }

        Utilisateur user = utilisateurRepository.findByEmail(email)
                .or(() -> utilisateurRepository.findByEmailAnyStatus(email))
                .orElse(null);

        if (user != null && user.getIdMedecin() != null) {
            CurrentUserContext.setMedecinId(user.getIdMedecin());
            return user.getIdMedecin();
        }

        if (hopitalId == null) {
            return null;
        }

        Integer previous = TenantContext.getHopitalId();
        try {
            TenantContext.setHopitalId(hopitalId);
            Integer medecinId = medecinRepository.trouverParEmail(email)
                    .map(Medecin::getIdMedecin)
                    .orElse(null);
            if (medecinId == null) {
                return null;
            }

            if (user != null && user.getIdUtilisateur() != null) {
                try {
                    utilisateurRepository.updateMedecinLink(user.getIdUtilisateur(), hopitalId, medecinId);
                } catch (Exception ex) {
                    log.warn("Impossible de lier utilisateurs.id_medecin={} pour {}: {}",
                            medecinId, email, ex.getMessage());
                }
            }

            CurrentUserContext.setMedecinId(medecinId);
            return medecinId;
        } finally {
            if (previous != null) {
                TenantContext.setHopitalId(previous);
            } else if (TenantContext.getHopitalId() != null && hopitalId.equals(TenantContext.getHopitalId())) {
                // conserver le tenant du filtre HTTP
            }
        }
    }

    public Integer getCurrentHopitalId() {
        return TenantContext.getRequiredHopitalId();
    }

    public String getCurrentUsername() {
        return CurrentUserContext.getUsername();
    }

    public boolean isMedecinConnected() {
        return getCurrentMedecinId() != null;
    }

    public Integer getCurrentUtilisateurId() {
        UtilisateurPrincipal principal = currentPrincipal();
        return principal != null ? principal.getIdUtilisateur() : null;
    }

    public Role getCurrentRole() {
        UtilisateurPrincipal principal = currentPrincipal();
        return principal != null ? principal.getAppRole() : null;
    }

    public boolean isMedecin() {
        return Role.MEDECIN == getCurrentRole();
    }

    /** Filtre optionnel « mes créations » : retourne l'id utilisateur courant si mine=true. */
    public Integer resolveCreatorFilter(Boolean mine) {
        if (mine == null || !mine) {
            return null;
        }
        return getCurrentUtilisateurId();
    }

    private UtilisateurPrincipal currentPrincipal() {
        org.springframework.security.core.Authentication auth =
                org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof UtilisateurPrincipal principal) {
            return principal;
        }
        return null;
    }
}
