package hospicloud.security;

import hospicloud.exceptions.ForbiddenException;
import hospicloud.model.Role;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class RoleRequestParserTest {

    @Test
    void parseTenantAssignableRole_acceptsRolePrefix() {
        assertThat(RoleRequestParser.parseTenantAssignableRole("ROLE_MEDECIN")).isEqualTo(Role.MEDECIN);
        assertThat(RoleRequestParser.parseTenantAssignableRole("MEDECIN")).isEqualTo(Role.MEDECIN);
    }

    @Test
    void parseTenantAssignableRole_rejectsSuperAdmin() {
        assertThatThrownBy(() -> RoleRequestParser.parseTenantAssignableRole("ROLE_SUPER_ADMIN"))
                .isInstanceOf(ForbiddenException.class);
    }

    @Test
    void parseTenantAssignableRole_rejectsTenantAdmin() {
        assertThatThrownBy(() -> RoleRequestParser.parseTenantAssignableRole("ROLE_TENANT_ADMIN"))
                .isInstanceOf(ForbiddenException.class);
    }

    @Test
    void toApiRole_formatsWithPrefix() {
        assertThat(RoleRequestParser.toApiRole(Role.PATIENT)).isEqualTo("ROLE_PATIENT");
    }
}
