package hospicloud.repositories;

import hospicloud.model.PasswordResetToken;

import java.util.Optional;

public interface PasswordResetTokenRepository {

    void ensureSchema();

    void invalidateAllForUser(Integer idUtilisateur);

    void save(PasswordResetToken token);

    Optional<PasswordResetToken> findValidByHash(String tokenHash);

    void markUsed(Long id);
}
