package hospicloud.exceptions;

/**
 * Thrown when a requested resource could not be found.
 */
public class ResourceNotFoundException extends ApiException {
    public ResourceNotFoundException() { super(); }
    public ResourceNotFoundException(String message) { super(message); }
}
