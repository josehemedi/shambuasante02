package hospicloud.exceptions;

/**
 * Thrown when the client sends a bad request (validation failed, missing data, etc.).
 */
public class BadRequestException extends ApiException {
    public BadRequestException() { super(); }
    public BadRequestException(String message) { super(message); }
}
