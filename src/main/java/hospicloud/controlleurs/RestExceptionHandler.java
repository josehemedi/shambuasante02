package hospicloud.controlleurs;

import hospicloud.exceptions.ForbiddenException;
import hospicloud.exceptions.ResourceNotFoundException;
import hospicloud.exceptions.rendezvous.RendezVousConflictException;
import hospicloud.exceptions.rendezvous.RendezVousException;
import hospicloud.security.TenantContext;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.util.stream.Collectors;

@ControllerAdvice
public class RestExceptionHandler extends ResponseEntityExceptionHandler {

    // 1. CORRECTION : On utilise @Override pour la validation
    // (MethodArgumentNotValidException)
    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(
            MethodArgumentNotValidException ex,
            HttpHeaders headers,
            HttpStatusCode status,
            WebRequest request) {

        String message = ex.getBindingResult().getFieldErrors().stream()
                .map(fe -> fe.getField() + ": " + fe.getDefaultMessage())
                .collect(Collectors.joining(", "));

        ApiError apiError = new ApiError(HttpStatus.BAD_REQUEST.value(), "Validation Error", message);
        return new ResponseEntity<>(apiError, HttpStatus.BAD_REQUEST);
    }

    // 2. CORRECTION : On utilise @Override pour le JSON mal formé
    // (HttpMessageNotReadableException)
    @Override
    protected ResponseEntity<Object> handleHttpMessageNotReadable(
            HttpMessageNotReadableException ex,
            HttpHeaders headers,
            HttpStatusCode status,
            WebRequest request) {

        ApiError apiError = new ApiError(HttpStatus.BAD_REQUEST.value(), "Malformed JSON request", ex.getMessage());
        return new ResponseEntity<>(apiError, HttpStatus.BAD_REQUEST);
    }

    // 3. OK : On garde @ExceptionHandler car ce n'est pas géré par la classe
    // parente
    @ExceptionHandler(IllegalArgumentException.class)
    protected ResponseEntity<Object> handleIllegalArgument(IllegalArgumentException ex) {
        ApiError apiError = new ApiError(HttpStatus.BAD_REQUEST.value(), "Bad Request", ex.getMessage());
        return new ResponseEntity<>(apiError, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(BadCredentialsException.class)
    protected ResponseEntity<Object> handleBadCredentials(BadCredentialsException ex) {
        ApiError apiError = new ApiError(HttpStatus.UNAUTHORIZED.value(), "Unauthorized", ex.getMessage());
        return new ResponseEntity<>(apiError, HttpStatus.UNAUTHORIZED);
    }

    // 4. Gestion d'un DuplicateKeyException (conflit sur clé unique)
    @ExceptionHandler(DuplicateKeyException.class)
    protected ResponseEntity<Object> handleDuplicateKey(DuplicateKeyException ex) {
        ApiError apiError = new ApiError(HttpStatus.CONFLICT.value(), "Conflict",
                "Un patient avec le même code existe déjà : " + ex.getMostSpecificCause().getMessage());
        return new ResponseEntity<>(apiError, HttpStatus.CONFLICT);
    }

    // 5. Gestion d'IllegalStateException (utilisée quand la génération du code
    // échoue après plusieurs tentatives)
    @ExceptionHandler(IllegalStateException.class)
    protected ResponseEntity<Object> handleIllegalState(IllegalStateException ex) {
        if (ex.getMessage() != null && ex.getMessage().contains("Tenant context not initialized")) {
            ApiError apiError = new ApiError(HttpStatus.BAD_REQUEST.value(), "Bad Request",
                    "Le tenant courant est introuvable. Staff : reconnectez-vous (JWT idHopital). SUPER_ADMIN : fournissez X-Hopital-Id.");
            return new ResponseEntity<>(apiError, HttpStatus.BAD_REQUEST);
        }

        ApiError apiError = new ApiError(HttpStatus.CONFLICT.value(), "Conflict", ex.getMessage());
        return new ResponseEntity<>(apiError, HttpStatus.CONFLICT);
    }

    @ExceptionHandler(ForbiddenException.class)
    protected ResponseEntity<Object> handleForbidden(ForbiddenException ex) {
        ApiError apiError = new ApiError(HttpStatus.FORBIDDEN.value(), "Forbidden", ex.getMessage());
        return new ResponseEntity<>(apiError, HttpStatus.FORBIDDEN);
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    protected ResponseEntity<Object> handleNotFound(ResourceNotFoundException ex) {
        ApiError apiError = new ApiError(HttpStatus.NOT_FOUND.value(), "Not Found", ex.getMessage());
        return new ResponseEntity<>(apiError, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(RendezVousConflictException.class)
    protected ResponseEntity<Object> handleRendezVousConflict(RendezVousConflictException ex) {
        ApiError apiError = new ApiError(
                HttpStatus.CONFLICT.value(),
                "Conflict",
                ex.getMessage());
        return new ResponseEntity<>(apiError, HttpStatus.CONFLICT);
    }

    @ExceptionHandler(RendezVousException.class)
    protected ResponseEntity<Object> handleRendezVousException(RendezVousException ex) {
        ApiError apiError = new ApiError(HttpStatus.BAD_REQUEST.value(), "Bad Request", ex.getMessage());
        return new ResponseEntity<>(apiError, HttpStatus.BAD_REQUEST);
    }

    // 6. ATTENTION : Pour l'exception générale, il vaut mieux utiliser une autre
    // approche
    // mais pour l'instant, celle-ci ne bloque pas le démarrage.
    @ExceptionHandler(Exception.class)
    protected ResponseEntity<Object> handleAll(Exception ex) {
        ApiError apiError = new ApiError(HttpStatus.INTERNAL_SERVER_ERROR.value(), "Internal Server Error",
                ex.getMessage());
        return new ResponseEntity<>(apiError, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}