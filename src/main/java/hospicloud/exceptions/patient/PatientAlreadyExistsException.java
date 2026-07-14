package hospicloud.exceptions.patient;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.CONFLICT) // Renvoie une erreur 409
public class PatientAlreadyExistsException extends PatientException {
    public PatientAlreadyExistsException(String message) {
        super(message);
    }
}