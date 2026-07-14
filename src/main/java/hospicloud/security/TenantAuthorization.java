package hospicloud.security;

import hospicloud.exceptions.ForbiddenException;
import hospicloud.model.Role;

/**
 * Contrôles d'accès multi-tenant au niveau service.
 */
public final class TenantAuthorization {

    private TenantAuthorization() {}

    public static void assertStaffRole() {
        Role role = CurrentUserContext.getRole();
        if (role == Role.PATIENT) {
            throw new ForbiddenException("Accès refusé pour votre rôle.");
        }
    }

    public static void assertPatientOwns(Integer resourcePatientId) {
        Role role = CurrentUserContext.getRole();
        if (role != Role.PATIENT) {
            return;
        }
        Integer currentPatientId = CurrentUserContext.getPatientId();
        if (currentPatientId == null
                || resourcePatientId == null
                || !currentPatientId.equals(resourcePatientId)) {
            throw new ForbiddenException("Accès refusé : cette ressource ne vous appartient pas.");
        }
    }

    /**
     * Vérifie qu'une ressource appartient à l'établissement courant (JWT / TenantContext).
     * Ne jamais faire confiance à un id_hopital envoyé par le client.
     */
    public static void assertSameTenant(Integer resourceHopitalId) {
        Integer tenantId = TenantContext.getRequiredHopitalId();
        if (resourceHopitalId == null || !tenantId.equals(resourceHopitalId)) {
            throw new ForbiddenException(
                    "Violation de périmètre SaaS : cette ressource n'appartient pas à votre établissement.");
        }
    }

    /**
     * Vérifie que le médecin connecté peut agir dans le périmètre clinique de l'établissement.
     * Tout praticien du même tenant peut consulter ou poursuivre un dossier (continuité des soins).
     * Le contrôle multi-tenant (id_hopital) est effectué en amont sur la ressource.
     */
    public static void assertMedecinScope(Integer resourceMedecinId) {
        Role role = CurrentUserContext.getRole();
        if (role != Role.MEDECIN) {
            return;
        }
        if (CurrentUserContext.getMedecinId() == null) {
            throw new ForbiddenException("Accès refusé : profil médecin incomplet.");
        }
    }
}
