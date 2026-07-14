package hospicloud.exceptions.patient;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND) // Renvoie automatiquement une erreur 404
public class PatientNotFoundException extends PatientException {
    public PatientNotFoundException(Integer id) {
        super("Le patient avec l'ID " + id + " n'existe pas dans cet hôpital.");
    }
    
    public PatientNotFoundException(String critere) {
        super("Aucun patient trouvé pour : " + critere);
    }
}