package hospicloud.security;

import hospicloud.exceptions.ForbiddenException;
import hospicloud.exceptions.ResourceNotFoundException;
import hospicloud.model.Role;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessageDeliveryException;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.regex.Matcher;

@Component
public class StompSecurityChannelInterceptor implements ChannelInterceptor {

    private final JwtService jwtService;
    private final TeleconsultationChatGuard teleconsultationChatGuard;

    public StompSecurityChannelInterceptor(JwtService jwtService,
                                           TeleconsultationChatGuard teleconsultationChatGuard) {
        this.jwtService = jwtService;
        this.teleconsultationChatGuard = teleconsultationChatGuard;
    }

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
        if (accessor == null) {
            return message;
        }

        StompCommand command = accessor.getCommand();
        if (StompCommand.CONNECT.equals(command)) {
            authenticateConnect(accessor);
        } else if (StompCommand.SUBSCRIBE.equals(command)) {
            bindSessionContext(accessor);
            assertSubscribeAllowed(accessor);
        } else if (StompCommand.SEND.equals(command)) {
            bindSessionContext(accessor);
        }

        return message;
    }

    private void bindSessionContext(StompHeaderAccessor accessor) {
        Authentication authentication = resolveAuthentication(accessor);
        if (authentication == null || !(authentication.getPrincipal() instanceof UtilisateurPrincipal principal)) {
            throw new MessageDeliveryException("UNAUTHORIZED");
        }

        SecurityContextHolder.getContext().setAuthentication(authentication);
        Integer tenantId = resolveTenantId(principal, accessor);
        if (tenantId != null) {
            TenantContext.setHopitalId(tenantId);
        }
    }

    private void authenticateConnect(StompHeaderAccessor accessor) {
        String authHeader = firstHeader(accessor, "Authorization", "authorization");
        if (!hasText(authHeader) || !authHeader.startsWith("Bearer ")) {
            throw new MessageDeliveryException("UNAUTHORIZED");
        }

        String token = authHeader.substring(7).trim();
        if (!jwtService.isTokenValid(token)) {
            throw new MessageDeliveryException("UNAUTHORIZED");
        }

        UtilisateurPrincipal principal = jwtService.toPrincipal(token);
        Authentication authentication = new UsernamePasswordAuthenticationToken(
                principal, null, principal.getAuthorities());
        accessor.setUser(authentication);
        SecurityContextHolder.getContext().setAuthentication(authentication);

        Integer tenantId = resolveTenantId(principal, accessor);
        if (tenantId != null) {
            TenantContext.setHopitalId(tenantId);
        }
    }

    private void assertSubscribeAllowed(StompHeaderAccessor accessor) {
        String destination = accessor.getDestination();
        if (destination == null) {
            return;
        }

        Matcher notificationMatcher = RealtimeNotificationTopics.USER_TOPIC.matcher(destination);
        if (notificationMatcher.matches()) {
            assertNotificationSubscribeAllowed(accessor, notificationMatcher);
            return;
        }

        Matcher receptionMatcher = ReceptionLiveTopics.RECEPTION_TOPIC.matcher(destination);
        if (receptionMatcher.matches()) {
            assertReceptionSubscribeAllowed(accessor, receptionMatcher);
            return;
        }

        Matcher waitingRoomMatcher = WaitingRoomTopics.WAITING_ROOM_TOPIC.matcher(destination);
        if (waitingRoomMatcher.matches()) {
            assertWaitingRoomSubscribeAllowed(accessor, waitingRoomMatcher);
            return;
        }

        Matcher medecinQueueMatcher = MedecinQueueTopics.MEDECIN_QUEUE_TOPIC.matcher(destination);
        if (medecinQueueMatcher.matches()) {
            assertMedecinQueueSubscribeAllowed(accessor, medecinQueueMatcher);
            return;
        }

        Matcher chatMatcher = TeleconsultationChatTopics.CHAT_TOPIC.matcher(destination);
        if (chatMatcher.matches()) {
            assertTeleconsultationChatSubscribeAllowed(accessor, chatMatcher);
        }
    }

    private void assertNotificationSubscribeAllowed(StompHeaderAccessor accessor, Matcher matcher) {
        Authentication authentication = resolveAuthentication(accessor);
        if (authentication == null || !(authentication.getPrincipal() instanceof UtilisateurPrincipal principal)) {
            throw new MessageDeliveryException("UNAUTHORIZED");
        }

        Integer requestedTenantId = Integer.parseInt(matcher.group(1));
        Integer requestedUserId = Integer.parseInt(matcher.group(2));

        if (principal.getIdUtilisateur() == null || !principal.getIdUtilisateur().equals(requestedUserId)) {
            throw new MessageDeliveryException("ACCESS_DENIED");
        }

        Integer principalTenantId = principal.getIdHopital();
        if (principal.getAppRole() == Role.SUPER_ADMIN) {
            principalTenantId = resolveTenantId(principal, accessor);
        }

        if (principalTenantId == null || !principalTenantId.equals(requestedTenantId)) {
            throw new MessageDeliveryException("ACCESS_DENIED");
        }

        TenantContext.setHopitalId(requestedTenantId);
    }

    private void assertReceptionSubscribeAllowed(StompHeaderAccessor accessor, Matcher matcher) {
        Authentication authentication = resolveAuthentication(accessor);
        if (authentication == null || !(authentication.getPrincipal() instanceof UtilisateurPrincipal principal)) {
            throw new MessageDeliveryException("UNAUTHORIZED");
        }

        Integer requestedTenantId = Integer.parseInt(matcher.group(1));
        Role role = principal.getAppRole();
        if (role != Role.RECEPTION && role != Role.TENANT_ADMIN && role != Role.MEDECIN) {
            throw new MessageDeliveryException("ACCESS_DENIED");
        }

        Integer principalTenantId = principal.getIdHopital();
        if (principal.getAppRole() == Role.SUPER_ADMIN) {
            principalTenantId = resolveTenantId(principal, accessor);
        }

        if (principalTenantId == null || !principalTenantId.equals(requestedTenantId)) {
            throw new MessageDeliveryException("ACCESS_DENIED");
        }

        TenantContext.setHopitalId(requestedTenantId);
    }

    private void assertWaitingRoomSubscribeAllowed(StompHeaderAccessor accessor, Matcher matcher) {
        Authentication authentication = resolveAuthentication(accessor);
        if (authentication == null || !(authentication.getPrincipal() instanceof UtilisateurPrincipal principal)) {
            throw new MessageDeliveryException("UNAUTHORIZED");
        }

        Integer requestedTenantId = Integer.parseInt(matcher.group(1));
        Role role = principal.getAppRole();
        if (role != Role.RECEPTION && role != Role.TENANT_ADMIN && role != Role.MEDECIN) {
            throw new MessageDeliveryException("ACCESS_DENIED");
        }

        Integer principalTenantId = principal.getIdHopital();
        if (principal.getAppRole() == Role.SUPER_ADMIN) {
            principalTenantId = resolveTenantId(principal, accessor);
        }

        if (principalTenantId == null || !principalTenantId.equals(requestedTenantId)) {
            throw new MessageDeliveryException("ACCESS_DENIED");
        }

        TenantContext.setHopitalId(requestedTenantId);
    }

    private void assertMedecinQueueSubscribeAllowed(StompHeaderAccessor accessor, Matcher matcher) {
        Authentication authentication = resolveAuthentication(accessor);
        if (authentication == null || !(authentication.getPrincipal() instanceof UtilisateurPrincipal principal)) {
            throw new MessageDeliveryException("UNAUTHORIZED");
        }

        Integer requestedTenantId = Integer.parseInt(matcher.group(1));
        Integer requestedMedecinId = Integer.parseInt(matcher.group(2));

        Role role = principal.getAppRole();
        if (role != Role.MEDECIN && role != Role.TENANT_ADMIN && role != Role.RECEPTION) {
            throw new MessageDeliveryException("ACCESS_DENIED");
        }

        Integer principalTenantId = principal.getIdHopital();
        if (principal.getAppRole() == Role.SUPER_ADMIN) {
            principalTenantId = resolveTenantId(principal, accessor);
        }

        if (principalTenantId == null || !principalTenantId.equals(requestedTenantId)) {
            throw new MessageDeliveryException("ACCESS_DENIED");
        }

        if (role == Role.MEDECIN) {
            Integer ownMedecinId = principal.getIdMedecin();
            // Si le JWT n'a pas encore id_medecin, on autorise le topic du tenant ;
            // le client ne s'abonne qu'à son propre id.
            if (ownMedecinId != null && !ownMedecinId.equals(requestedMedecinId)) {
                throw new MessageDeliveryException("ACCESS_DENIED");
            }
        }

        TenantContext.setHopitalId(requestedTenantId);
    }

    private void assertTeleconsultationChatSubscribeAllowed(StompHeaderAccessor accessor, Matcher matcher) {
        Authentication authentication = resolveAuthentication(accessor);
        if (authentication == null || !(authentication.getPrincipal() instanceof UtilisateurPrincipal principal)) {
            throw new MessageDeliveryException("UNAUTHORIZED");
        }

        Integer requestedTenantId = Integer.parseInt(matcher.group(1));
        Integer requestedRdvId = Integer.parseInt(matcher.group(2));

        Integer principalTenantId = principal.getIdHopital();
        if (principal.getAppRole() == Role.SUPER_ADMIN) {
            principalTenantId = resolveTenantId(principal, accessor);
        }

        if (principalTenantId == null || !principalTenantId.equals(requestedTenantId)) {
            throw new MessageDeliveryException("ACCESS_DENIED");
        }

        TenantContext.setHopitalId(requestedTenantId);
        try {
            teleconsultationChatGuard.requireParticipant(requestedRdvId);
        } catch (ForbiddenException | ResourceNotFoundException ex) {
            throw new MessageDeliveryException("ACCESS_DENIED");
        }
    }

    /** SaaS : X-Hopital-Id uniquement pour SUPER_ADMIN (aligné TenantResolverFilter). */
    private Integer resolveTenantId(UtilisateurPrincipal principal, StompHeaderAccessor accessor) {
        if (principal.getAppRole() == Role.SUPER_ADMIN) {
            String headerId = firstHeader(accessor, "X-Hopital-Id", "x-hopital-id");
            if (hasText(headerId)) {
                try {
                    return Integer.parseInt(headerId.trim());
                } catch (NumberFormatException e) {
                    throw new MessageDeliveryException("Invalid X-Hopital-Id header");
                }
            }
            return null;
        }

        return principal.getIdHopital();
    }

    private Authentication resolveAuthentication(StompHeaderAccessor accessor) {
        if (accessor.getUser() instanceof Authentication authentication) {
            return authentication;
        }
        return SecurityContextHolder.getContext().getAuthentication();
    }

    private String firstHeader(StompHeaderAccessor accessor, String... names) {
        for (String name : names) {
            String value = accessor.getFirstNativeHeader(name);
            if (hasText(value)) {
                return value;
            }
        }
        return null;
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}
