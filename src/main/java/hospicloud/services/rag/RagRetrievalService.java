package hospicloud.services.rag;

import hospicloud.dtos.rag.RagContextBundle;

public interface RagRetrievalService {
    RagContextBundle buildContext(Long patientId, String analysisType, String userMessage);
}
