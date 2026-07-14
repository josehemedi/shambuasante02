package hospicloud.security;

import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class UserPresenceService {

    private static final Duration PRESENCE_TIMEOUT = Duration.ofMinutes(2);

    private final ConcurrentHashMap<Integer, Instant> lastSeenByUserId = new ConcurrentHashMap<>();

    public void markPresent(Integer userId) {
        if (userId != null) {
            lastSeenByUserId.put(userId, Instant.now());
        }
    }

    public void markAbsent(Integer userId) {
        if (userId != null) {
            lastSeenByUserId.remove(userId);
        }
    }

    public boolean isPresent(Integer userId) {
        if (userId == null) {
            return false;
        }

        Instant lastSeen = lastSeenByUserId.get(userId);
        if (lastSeen == null) {
            return false;
        }

        if (lastSeen.plus(PRESENCE_TIMEOUT).isBefore(Instant.now())) {
            lastSeenByUserId.remove(userId, lastSeen);
            return false;
        }

        return true;
    }

    public Optional<Instant> getLastSeen(Integer userId) {
        if (!isPresent(userId)) {
            return Optional.empty();
        }
        return Optional.ofNullable(lastSeenByUserId.get(userId));
    }
}
