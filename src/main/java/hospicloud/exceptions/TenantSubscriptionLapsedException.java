package hospicloud.exceptions;

import org.springframework.security.authentication.BadCredentialsException;

public class TenantSubscriptionLapsedException extends BadCredentialsException {

    public TenantSubscriptionLapsedException() {
        super("L'abonnement de votre établissement a expiré. Contactez l'administrateur de l'hôpital.");
    }
}
