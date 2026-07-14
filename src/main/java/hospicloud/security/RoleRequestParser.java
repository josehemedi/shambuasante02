package hospicloud.security;

import hospicloud.exceptions.BadRequestException;
import hospicloud.exceptions.ForbiddenException;
import hospicloud.model.Role;

import java.util.EnumSet;
import java.util.Set;

public final class RoleRequestParser {

    private static final Set<Role> TENANT_ADMIN_CREATABLE = EnumSet.of(
            Role.MEDECIN,
            Role.RECEPTION,
            Role.PATIENT,
            Role.USER,
            Role.LABORANTIN,
            Role.CAISSIER,
            Role.ARCHIVISTE
    );

    private RoleRequestParser() {}

    public static Role parseTenantAssignableRole(String rawRole) {
        if (rawRole == null || rawRole.isBlank()) {
            throw new BadRequestException("Le rôle est obligatoire");
        }

        String normalized = rawRole.trim().toUpperCase();
        if (normalized.startsWith("ROLE_")) {
            normalized = normalized.substring("ROLE_".length());
        }

        Role role;
        try {
            role = Role.valueOf(normalized);
        } catch (IllegalArgumentException ex) {
            throw new BadRequestException("Rôle inconnu : " + rawRole);
        }

        if (role == Role.SUPER_ADMIN || role == Role.TENANT_ADMIN) {
            throw new ForbiddenException("Vous n'êtes pas autorisé à attribuer le rôle " + toApiRole(role));
        }

        if (!TENANT_ADMIN_CREATABLE.contains(role)) {
            throw new ForbiddenException("Rôle non autorisé pour un administrateur d'hôpital : " + toApiRole(role));
        }

        return role;
    }

    public static String toApiRole(Role role) {
        return role == null ? null : "ROLE_" + role.name();
    }
}
