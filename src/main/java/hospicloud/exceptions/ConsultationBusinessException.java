package hospicloud.exceptions;

import org.springframework.http.HttpStatus;

public class ConsultationBusinessException extends ApiException {
    private final String code;
    private final HttpStatus status;

    public ConsultationBusinessException(String code, String message, HttpStatus status) {
        super(message);
        this.code = code;
        this.status = status != null ? status : HttpStatus.BAD_REQUEST;
    }

    public ConsultationBusinessException(String code, String message) {
        this(code, message, HttpStatus.BAD_REQUEST);
    }

    public String getCode() {
        return code;
    }

    public HttpStatus getStatus() {
        return status;
    }
}
