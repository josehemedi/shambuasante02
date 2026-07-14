package hospicloud.security;

import hospicloud.exceptions.ForbiddenException;
import hospicloud.exceptions.ResourceNotFoundException;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessageDeliveryException;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

@Component
public class ConsultationChannelInterceptor implements ChannelInterceptor {

    private final TeleconsultationChatGuard teleconsultationChatGuard;

    public ConsultationChannelInterceptor(TeleconsultationChatGuard teleconsultationChatGuard) {
        this.teleconsultationChatGuard = teleconsultationChatGuard;
    }

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(message);
        String destination = accessor.getDestination();

        if (destination == null) {
            return message;
        }

        if (destination.startsWith("/app/teleconsultation/")) {
            assertAuthenticated(accessor);
            Integer idRdv = parseRdvId(destination);
            try {
                teleconsultationChatGuard.requireParticipant(idRdv);
            } catch (ForbiddenException | ResourceNotFoundException ex) {
                throw new MessageDeliveryException("ACCESS_DENIED");
            }
        }

        return message;
    }

    private void assertAuthenticated(StompHeaderAccessor accessor) {
        Authentication authentication = resolveAuthentication(accessor);
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new MessageDeliveryException("UNAUTHORIZED");
        }
    }

    private Authentication resolveAuthentication(StompHeaderAccessor accessor) {
        if (accessor.getUser() instanceof Authentication authentication) {
            return authentication;
        }
        return null;
    }

    private Integer parseRdvId(String destination) {
        try {
            String[] parts = destination.split("/");
            return Integer.parseInt(parts[parts.length - 2]);
        } catch (NumberFormatException | ArrayIndexOutOfBoundsException e) {
            throw new MessageDeliveryException("Invalid destination format");
        }
    }
}
