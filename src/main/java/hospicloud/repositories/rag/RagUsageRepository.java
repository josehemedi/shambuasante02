package hospicloud.repositories.rag;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public interface RagUsageRepository {
    void insert(Integer hopitalId, Integer userId, String roleCode, String scopeCode, Long patientId,
                String modelName, String analysisType, int promptChars, int contextChars, int responseChars,
                String sourcesJson, boolean success, String errorMessage, BigDecimal estimatedCostUsd);

    Map<String, Object> statsForHopital(Integer hopitalId);
    Map<String, Object> statsPlatform();
    List<Map<String, Object>> recentErrors(Integer hopitalId, int limit);
    List<Map<String, Object>> usageByDay(Integer hopitalId, int days);
}
