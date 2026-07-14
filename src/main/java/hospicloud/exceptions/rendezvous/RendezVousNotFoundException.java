package hospicloud.exceptions.rendezvous;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class RendezVousNotFoundException extends RendezVousException {
    public RendezVousNotFoundException(Integer id) {
        super("Aucun rendez-vous trouvé avec l'identifiant : " + id);
    }
}