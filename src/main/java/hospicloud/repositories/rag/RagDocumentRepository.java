package hospicloud.repositories.rag;

import hospicloud.model.rag.RagDocument;

import java.util.List;
import java.util.Optional;

public interface RagDocumentRepository {
    List<RagDocument> listForAudience(Integer hopitalId, String audience, boolean includeExpired);
    List<RagDocument> listByHopital(Integer hopitalId);
    Optional<RagDocument> findById(Long id);
    Long insert(RagDocument doc);
    void update(RagDocument doc);
    void delete(Long id, Integer hopitalId);
}
