package hospicloud.security.archive;

import hospicloud.exceptions.ForbiddenException;
import hospicloud.model.Role;
import hospicloud.security.CurrentUserService;
import hospicloud.security.TenantContext;
import org.springframework.stereotype.Component;

@Component
public class ArchivePermissionService {

    public static final String ARCHIVE_VOIR = "ARCHIVE_VOIR";
    public static final String ARCHIVE_RECHERCHER = "ARCHIVE_RECHERCHER";
    public static final String ARCHIVE_VERIFIER = "ARCHIVE_VERIFIER";
    public static final String ARCHIVE_ARCHIVER = "ARCHIVE_ARCHIVER";
    public static final String ARCHIVE_RESTAURER = "ARCHIVE_RESTAURER";
    public static final String ARCHIVE_VOIR_HISTORIQUE = "ARCHIVE_VOIR_HISTORIQUE";
    public static final String ARCHIVE_GERER_DEMANDES_ACCES = "ARCHIVE_GERER_DEMANDES_ACCES";
    public static final String ARCHIVE_VOIR_STATISTIQUES = "ARCHIVE_VOIR_STATISTIQUES";
    public static final String ARCHIVE_CLASSER = "ARCHIVE_CLASSER";

    private final CurrentUserService currentUserService;

    public ArchivePermissionService(CurrentUserService currentUserService) {
        this.currentUserService = currentUserService;
    }

    public void require(String permission) {
        if (!has(permission)) {
            throw new ForbiddenException("Permission archivage requise: " + permission);
        }
    }

    public boolean has(String permission) {
        Role role = currentUserService.getCurrentRole();
        if (role == null) {
            return false;
        }
        // Super Admin SaaS : stats d'un hôpital uniquement (X-Hopital-Id), jamais le contenu médical.
        if (role == Role.SUPER_ADMIN) {
            return TenantContext.getHopitalId() != null
                    && ARCHIVE_VOIR_STATISTIQUES.equals(permission);
        }
        return switch (permission) {
            case ARCHIVE_VOIR, ARCHIVE_RECHERCHER -> hasViewAccess(role);
            case ARCHIVE_VERIFIER -> role == Role.ARCHIVISTE || role == Role.TENANT_ADMIN;
            case ARCHIVE_ARCHIVER -> role == Role.ARCHIVISTE;
            case ARCHIVE_RESTAURER -> role == Role.ARCHIVISTE || role == Role.TENANT_ADMIN;
            case ARCHIVE_VOIR_HISTORIQUE -> hasViewAccess(role) || role == Role.TENANT_ADMIN;
            case ARCHIVE_GERER_DEMANDES_ACCES -> role == Role.ARCHIVISTE
                    || role == Role.TENANT_ADMIN || role == Role.RECEPTION;
            case ARCHIVE_VOIR_STATISTIQUES -> role == Role.ARCHIVISTE || role == Role.TENANT_ADMIN
                    || role == Role.MEDECIN || role == Role.RECEPTION;
            case ARCHIVE_CLASSER -> role == Role.ARCHIVISTE || role == Role.TENANT_ADMIN;
            default -> false;
        };
    }

    /**
     * Contenu médical (PDF, export, snapshot détaillé) : jamais pour SUPER_ADMIN,
     * même en impersonation tenant.
     */
    public boolean canViewMedicalContent() {
        Role role = currentUserService.getCurrentRole();
        if (role == null) return false;
        return role != Role.RECEPTION && role != Role.SUPER_ADMIN;
    }

    /**
     * SUPER_ADMIN reste « technique only » pour le contenu médical,
     * mais peut consulter les stats / listes métadonnées d'un hôpital via X-Hopital-Id.
     */
    public boolean isSuperAdminTechnicalOnly() {
        return currentUserService.getCurrentRole() == Role.SUPER_ADMIN;
    }

    private boolean hasViewAccess(Role role) {
        return role == Role.ARCHIVISTE || role == Role.TENANT_ADMIN
                || role == Role.MEDECIN || role == Role.RECEPTION;
    }
}
