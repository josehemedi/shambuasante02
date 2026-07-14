package hospicloud.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import hospicloud.exceptions.ApiError;
import hospicloud.model.Role;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserPresenceService userPresenceService;
    private final UserSessionService userSessionService;
    private final ObjectMapper objectMapper;

    public JwtAuthenticationFilter(JwtService jwtService,
                                   UserPresenceService userPresenceService,
                                   UserSessionService userSessionService,
                                   ObjectMapper objectMapper) {
        this.jwtService = jwtService;
        this.userPresenceService = userPresenceService;
        this.userSessionService = userSessionService;
        this.objectMapper = objectMapper;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = authHeader.substring(7);
        if (!jwtService.isTokenValid(token)) {
            filterChain.doFilter(request, response);
            return;
        }

        UtilisateurPrincipal principal = jwtService.toPrincipal(token);
        if (principal.getAppRole() == Role.MEDECIN) {
            String jti = jwtService.extractJti(token);
            if (jti == null || !userSessionService.isSessionActive(jti)) {
                writeUnauthorized(response, request, "SESSION_REVOKED",
                        "Votre session a expiré ou a été ouverte sur un autre appareil.");
                return;
            }
            userSessionService.touchSession(jti);
        }

        try {
            if (principal.getIdMedecin() != null) {
                CurrentUserContext.setMedecinId(principal.getIdMedecin());
            }
            if (principal.getIdPatient() != null) {
                CurrentUserContext.setPatientId(principal.getIdPatient());
            }

            userPresenceService.markPresent(principal.getIdUtilisateur());
            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities());
            authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
            SecurityContextHolder.getContext().setAuthentication(authentication);

            filterChain.doFilter(request, response);
        } finally {
            CurrentUserContext.clear();
        }
    }

    private void writeUnauthorized(HttpServletResponse response,
                                   HttpServletRequest request,
                                   String code,
                                   String message) throws IOException {
        ApiError error = new ApiError(
                HttpStatus.UNAUTHORIZED.value(),
                code,
                message,
                request.getRequestURI(),
                code);
        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        objectMapper.writeValue(response.getOutputStream(), error);
    }
}
