package hospicloud.exceptions;

/**
 * Base runtime exception for application-specific errors.
 * Extend this class for business exceptions so they can be handled uniformly.
 */
public abstract class ApiException extends RuntimeException {

    public ApiException() {
        super();
    }

    public ApiException(String message) {
        super(message);
    }

    public ApiException(String message, Throwable cause) {
        super(message, cause);
    }

    public ApiException(Throwable cause) {
        super(cause);
    }
}
