package hospicloud.exceptions.rendezvous;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class InvalidRendezVousStateException extends RendezVousException {
    public InvalidRendezVousStateException(String statutActuel, String action) {
        super("Impossible de " + action + " car le rendez-vous est déjà " + statutActuel);
    }
}