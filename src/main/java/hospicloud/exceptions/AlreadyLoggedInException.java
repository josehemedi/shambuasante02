package hospicloud.exceptions;

public class AlreadyLoggedInException extends ConflictException {
    public AlreadyLoggedInException() {
        super("Une session est déjà active sur un autre appareil. Déconnectez-vous d'abord sur l'autre machine.");
    }
}
