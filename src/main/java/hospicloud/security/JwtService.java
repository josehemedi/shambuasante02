package hospicloud.security;

import hospicloud.model.Role;
import hospicloud.model.Utilisateur;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
public class JwtService {

    private final JwtProperties jwtProperties;
    private final SecretKey secretKey;

    public JwtService(JwtProperties jwtProperties) {
        this.jwtProperties = jwtProperties;
        this.secretKey = Keys.hmacShaKeyFor(jwtProperties.getSecret().getBytes(StandardCharsets.UTF_8));
    }

    public String generateToken(Utilisateur utilisateur) {
        return generateToken(utilisateur, null);
    }

    public String generateToken(Utilisateur utilisateur, String jti) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("role", utilisateur.getRole().name());
        claims.put("idUtilisateur", utilisateur.getIdUtilisateur());
        claims.put("idHopital", utilisateur.getIdHopital());
        claims.put("idMedecin", utilisateur.getIdMedecin());
        claims.put("idPatient", utilisateur.getIdPatient());
        claims.put("prenom", utilisateur.getPrenom());
        claims.put("nom", utilisateur.getNom());
        if (jti != null && !jti.isBlank()) {
            claims.put("jti", jti);
        }

        Date now = new Date();
        Date expiry = new Date(now.getTime() + jwtProperties.getExpirationMs());

        var builder = Jwts.builder()
                .claims(claims)
                .subject(utilisateur.getEmail())
                .issuedAt(now)
                .expiration(expiry);
        if (jti != null && !jti.isBlank()) {
            builder.id(jti);
        }
        return builder.signWith(secretKey).compact();
    }

    public String newSessionId() {
        return UUID.randomUUID().toString();
    }

    public String extractJti(String token) {
        try {
            Claims claims = parseClaims(token);
            String jti = claims.getId();
            if (jti != null && !jti.isBlank()) {
                return jti;
            }
            return claims.get("jti", String.class);
        } catch (Exception ex) {
            return null;
        }
    }

    public Claims parseClaims(String token) {
        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public boolean isTokenValid(String token) {
        try {
            Claims claims = parseClaims(token);
            return claims.getExpiration().after(new Date());
        } catch (Exception ex) {
            return false;
        }
    }

    public UtilisateurPrincipal toPrincipal(String token) {
        Claims claims = parseClaims(token);
        Role role = Role.valueOf(claims.get("role", String.class));

        Integer idHopital = claims.get("idHopital", Integer.class);
        Integer idMedecin = claims.get("idMedecin", Integer.class);
        Long idPatient = claims.get("idPatient", Long.class);
        if (idPatient == null) {
            Integer legacyPatientId = claims.get("idPatient", Integer.class);
            if (legacyPatientId != null) {
                idPatient = legacyPatientId.longValue();
            }
        }

        return new UtilisateurPrincipal(
                claims.get("idUtilisateur", Integer.class),
                claims.getSubject(),
                null,
                role,
                idHopital,
                idMedecin,
                idPatient,
                true
        );
    }
}
