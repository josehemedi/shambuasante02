package hospicloud.security;

import hospicloud.model.Role;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * Contexte pour gérer l'utilisateur connecté.
 */
public class CurrentUserContext {

    private static final ThreadLocal<Integer> CURRENT_MEDECIN_ID = new ThreadLocal<>();
    private static final ThreadLocal<Long> CURRENT_PATIENT_ID = new ThreadLocal<>();

    private CurrentUserContext() {}

    public static void setMedecinId(Integer idMedecin) {
        CURRENT_MEDECIN_ID.set(idMedecin);
    }

    public static void setPatientId(Long idPatient) {
        CURRENT_PATIENT_ID.set(idPatient);
    }

    public static String getUsername() {
        UtilisateurPrincipal principal = getPrincipal();
        return principal != null ? principal.getUsername() : null;
    }

    public static Integer getMedecinId() {
        Integer id = CURRENT_MEDECIN_ID.get();
        if (id != null) {
            return id;
        }

        UtilisateurPrincipal principal = getPrincipal();
        if (principal != null && principal.getIdMedecin() != null) {
            return principal.getIdMedecin();
        }

        return null;
    }

    public static Integer getPatientId() {
        Long id = CURRENT_PATIENT_ID.get();
        if (id != null) {
            return id.intValue();
        }

        UtilisateurPrincipal principal = getPrincipal();
        if (principal != null && principal.getIdPatient() != null) {
            return principal.getIdPatient().intValue();
        }

        return null;
    }

    public static Role getRole() {
        UtilisateurPrincipal principal = getPrincipal();
        return principal != null ? principal.getAppRole() : null;
    }

    public static Integer getHopitalId() {
        UtilisateurPrincipal principal = getPrincipal();
        return principal != null ? principal.getIdHopital() : null;
    }

    public static void clear() {
        CURRENT_MEDECIN_ID.remove();
        CURRENT_PATIENT_ID.remove();
    }

    private static UtilisateurPrincipal getPrincipal() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof UtilisateurPrincipal principal) {
            return principal;
        }
        return null;
    }
}
