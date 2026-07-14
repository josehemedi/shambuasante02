package hospicloud.exceptions.rendezvous;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.CONFLICT)
public class RendezVousConflictException extends RendezVousException {

    public static final String MESSAGE =
            "Le médecin est déjà occupé sur ce créneau. Veuillez choisir une autre heure.";

    public RendezVousConflictException() {
        super(MESSAGE);
    }

    public RendezVousConflictException(String message) {
        super(message != null && !message.isBlank() ? message : MESSAGE);
    }
}
