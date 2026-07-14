package hospicloud.exceptions;

import org.springframework.security.authentication.BadCredentialsException;

public class DisabledAccountException extends BadCredentialsException {
    public DisabledAccountException() {
        super("Compte désactivé. Contactez l'administrateur de votre établissement.");
    }
}
