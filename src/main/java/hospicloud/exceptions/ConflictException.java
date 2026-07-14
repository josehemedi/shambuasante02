package hospicloud.exceptions;

/**
 * Thrown when an operation would cause a conflict (duplicate key, concurrency issue...).
 */
public class ConflictException extends ApiException {
    public ConflictException() { super(); }
    public ConflictException(String message) { super(message); }
}
