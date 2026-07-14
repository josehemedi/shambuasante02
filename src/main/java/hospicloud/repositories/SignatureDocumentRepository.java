package hospicloud.repositories;

import hospicloud.model.SignatureDocument;
import hospicloud.model.enums.TypeDocument;

import java.util.Optional;

public interface SignatureDocumentRepository {

    SignatureDocument save(SignatureDocument signature);

    Optional<SignatureDocument> findActiveByDocument(TypeDocument typeDocument, Long documentId, Integer hopitalId);

    void ensureSchema();
}
