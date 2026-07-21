package hospicloud.services;

import hospicloud.model.Utilisateur;

/**
 * Invitation + activation des comptes administrateur d'hôpital (TENANT_ADMIN).
 * Flux : SUPER_ADMIN crée le compte inactif → email avec lien → l'admin définit son mot de passe → compte activé.
 */
public interface AccountInvitationService {

    /**
     * Crée un TENANT_ADMIN inactif pour l'hôpital et envoie l'email d'activation.
     */
    Utilisateur inviteHospitalAdmin(Integer idHopital,
                                    String prenom,
                                    String nom,
                                    String email,
                                    String telephone);

    /**
     * Confirme l'email, définit le mot de passe et active le compte.
     */
    void activateAccount(String rawToken, String newPassword);

    /**
     * Renvoie un email d'activation si le compte existe et n'est pas encore actif.
     */
    void resendActivation(String email);
}
