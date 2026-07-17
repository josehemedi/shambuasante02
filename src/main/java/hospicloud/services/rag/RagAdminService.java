package hospicloud.services.rag;

import hospicloud.dtos.rag.RagDocumentDto;

import java.util.List;
import java.util.Map;

public interface RagAdminService {
    List<RagDocumentDto> listDocuments();
    RagDocumentDto createDocument(RagDocumentDto request);
    RagDocumentDto updateDocument(Long id, RagDocumentDto request);
    void deleteDocument(Long id);
    Map<String, Object> analytics();
    List<String> categories();
}
