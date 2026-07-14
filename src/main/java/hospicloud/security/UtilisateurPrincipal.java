package hospicloud.security;

import hospicloud.model.Role;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

public class UtilisateurPrincipal implements UserDetails {

    private final Integer idUtilisateur;
    private final String email;
    private final String password;
    private final Role role;
    private final Integer idHopital;
    private final Integer idMedecin;
    private final Long idPatient;
    private final boolean active;

    public UtilisateurPrincipal(Integer idUtilisateur, String email, String password, Role role,
                                Integer idHopital, Integer idMedecin, Long idPatient, boolean active) {
        this.idUtilisateur = idUtilisateur;
        this.email = email;
        this.password = password;
        this.role = role;
        this.idHopital = idHopital;
        this.idMedecin = idMedecin;
        this.idPatient = idPatient;
        this.active = active;
    }

    public UtilisateurPrincipal(String email, String password, Role role,
                                Integer idHopital, Integer idMedecin, Long idPatient) {
        this(null, email, password, role, idHopital, idMedecin, idPatient, true);
    }

    public Integer getIdUtilisateur() {
        return idUtilisateur;
    }

    public Role getAppRole() {
        return role;
    }

    public Integer getIdHopital() {
        return idHopital;
    }

    public Integer getIdMedecin() {
        return idMedecin;
    }

    public Long getIdPatient() {
        return idPatient;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + role.name()));
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return email;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return active;
    }
}
