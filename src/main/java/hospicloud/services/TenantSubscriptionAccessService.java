package hospicloud.services;

public interface TenantSubscriptionAccessService {

    /**
     * Vrai lorsque l'établissement ne peut plus utiliser la plateforme
     * (abonnement expiré ou établissement suspendu pour impayé).
     */
    boolean isPlatformAccessRestricted(Integer hopitalId);
}
