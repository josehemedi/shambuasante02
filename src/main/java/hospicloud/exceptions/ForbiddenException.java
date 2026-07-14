package hospicloud.exceptions;

public class ForbiddenException extends ApiException {
    public ForbiddenException() { super(); }
    public ForbiddenException(String message) { super(message); }
}
