package hospicloud.exceptions;

import java.util.stream.Collectors;

import hospicloud.exceptions.DisabledAccountException;
import hospicloud.exceptions.AlreadyLoggedInException;
import hospicloud.exceptions.TenantSubscriptionLapsedException;
import hospicloud.exceptions.ForbiddenException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;

/**
 * Global exception handler that maps ApiException and other exceptions to HTTP responses.
 */
@ControllerAdvice
public class GlobalExceptionHandler {

    private String extractPath(WebRequest req) {
        if (req == null) return null;
        String desc = req.getDescription(false); // typically "uri=/path"
        if (desc != null && desc.startsWith("uri=")) return desc.substring(4);
        return desc;
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> handleValidation(MethodArgumentNotValidException ex, WebRequest req) {
        String details = ex.getBindingResult().getFieldErrors().stream()
                .map(fe -> fe.getField() + ": " + fe.getDefaultMessage())
                .collect(Collectors.joining("; "));
        ApiError err = new ApiError(HttpStatus.BAD_REQUEST.value(), "Validation Failed", details, extractPath(req));
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(err);
    }

    @ExceptionHandler(ConsultationBusinessException.class)
    public ResponseEntity<ApiError> handleConsultationBusiness(ConsultationBusinessException ex, WebRequest req) {
        ApiError err = new ApiError(
                ex.getStatus().value(),
                ex.getCode(),
                ex.getMessage(),
                extractPath(req),
                ex.getCode());
        return ResponseEntity.status(ex.getStatus()).body(err);
    }

    @ExceptionHandler(ForbiddenException.class)
    public ResponseEntity<ApiError> handleForbidden(ForbiddenException ex, WebRequest req) {
        ApiError err = new ApiError(HttpStatus.FORBIDDEN.value(), "Forbidden", ex.getMessage(), extractPath(req));
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(err);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiError> handleAccessDenied(AccessDeniedException ex, WebRequest req) {
        ApiError err = new ApiError(HttpStatus.FORBIDDEN.value(), "Forbidden",
                ex.getMessage() != null ? ex.getMessage() : "Accès refusé", extractPath(req));
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(err);
    }

    @ExceptionHandler(DisabledAccountException.class)
    public ResponseEntity<ApiError> handleDisabledAccount(DisabledAccountException ex, WebRequest req) {
        ApiError err = new ApiError(HttpStatus.UNAUTHORIZED.value(), "ACCOUNT_DISABLED", ex.getMessage(), extractPath(req));
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(err);
    }

    @ExceptionHandler(TenantSubscriptionLapsedException.class)
    public ResponseEntity<ApiError> handleTenantSubscriptionLapsed(TenantSubscriptionLapsedException ex, WebRequest req) {
        ApiError err = new ApiError(
                HttpStatus.UNAUTHORIZED.value(),
                "SUBSCRIPTION_LAPSED",
                ex.getMessage(),
                extractPath(req),
                "SUBSCRIPTION_LAPSED");
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(err);
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ApiError> handleBadCredentials(BadCredentialsException ex, WebRequest req) {
        ApiError err = new ApiError(HttpStatus.UNAUTHORIZED.value(), "Unauthorized", ex.getMessage(), extractPath(req));
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(err);
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiError> handleNotFound(ResourceNotFoundException ex, WebRequest req) {
        ApiError err = new ApiError(HttpStatus.NOT_FOUND.value(), "Not Found", ex.getMessage(), extractPath(req));
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(err);
    }

    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<ApiError> handleBadRequest(BadRequestException ex, WebRequest req) {
        ApiError err = new ApiError(HttpStatus.BAD_REQUEST.value(), "Bad Request", ex.getMessage(), extractPath(req));
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(err);
    }

    @ExceptionHandler(AlreadyLoggedInException.class)
    public ResponseEntity<ApiError> handleAlreadyLoggedIn(AlreadyLoggedInException ex, WebRequest req) {
        ApiError err = new ApiError(
                HttpStatus.CONFLICT.value(),
                "ALREADY_LOGGED_IN",
                ex.getMessage(),
                extractPath(req),
                "ALREADY_LOGGED_IN");
        return ResponseEntity.status(HttpStatus.CONFLICT).body(err);
    }

    @ExceptionHandler(ConflictException.class)
    public ResponseEntity<ApiError> handleConflict(ConflictException ex, WebRequest req) {
        ApiError err = new ApiError(HttpStatus.CONFLICT.value(), "Conflict", ex.getMessage(), extractPath(req));
        return ResponseEntity.status(HttpStatus.CONFLICT).body(err);
    }

    @ExceptionHandler(ApiException.class)
    public ResponseEntity<ApiError> handleApiException(ApiException ex, WebRequest req) {
        ApiError err = new ApiError(HttpStatus.BAD_REQUEST.value(), "API Error", ex.getMessage(), extractPath(req));
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(err);
    }

    @ExceptionHandler(SecurityException.class)
    public ResponseEntity<ApiError> handleSecurity(SecurityException ex, WebRequest req) {
        ApiError err = new ApiError(HttpStatus.FORBIDDEN.value(), "Forbidden",
                ex.getMessage() != null ? ex.getMessage() : "Accès refusé", extractPath(req));
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(err);
    }

    @ExceptionHandler(DataAccessException.class)
    public ResponseEntity<ApiError> handleDataAccess(DataAccessException ex, WebRequest req) {
        String details = resolveDatabaseMessage(ex);
        ApiError err = new ApiError(HttpStatus.INTERNAL_SERVER_ERROR.value(), "Database Error", details, extractPath(req));
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(err);
    }

    private String resolveDatabaseMessage(DataAccessException ex) {
        Throwable cause = ex.getMostSpecificCause();
        if (cause != null && cause.getMessage() != null) {
            String raw = cause.getMessage().toLowerCase();
            if (raw.contains("unknown column") || raw.contains("bad sql grammar")) {
                return "Schéma de base de données incomplet pour les consultations médicales. "
                        + "Redémarrez le serveur Hospicloud pour appliquer la migration automatique.";
            }
        }
        return "Erreur lors de l'accès à la base de données. Contactez l'administrateur si le problème persiste.";
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> handleOther(Exception ex, WebRequest req) {
        ApiError err = new ApiError(HttpStatus.INTERNAL_SERVER_ERROR.value(), "Internal Error", ex.getMessage(), extractPath(req));
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(err);
    }
}