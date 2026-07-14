package hospicloud.exceptions;

public class RendezVousModificationNotAllowedException extends RuntimeException {

    public RendezVousModificationNotAllowedException(String message) {
        super(message);
    }

    public RendezVousModificationNotAllowedException() {
        super("La modification du rendez-vous n'est plus autorisée 48 heures avant la consultation.");
    }
}