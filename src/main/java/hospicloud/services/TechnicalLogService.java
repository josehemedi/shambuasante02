package hospicloud.services;

import hospicloud.dtos.TechnicalLogDTO;
import hospicloud.dtos.TechnicalLogKpisDTO;
import hospicloud.dtos.events.TechnicalLogEvent;

import java.time.LocalDateTime;
import java.util.List;

public interface TechnicalLogService {

    void record(TechnicalLogEvent event);

    void recordApiError(String module, String action, String endpoint, String httpMethod,
                        int httpStatus, String message, String errorDetails,
                        String ipAddress, String userAgent);

    void recordAuthEvent(String action, String message, String status,
                         Integer hopitalId, Integer userId, String userEmail, String userRole,
                         String ipAddress, String userAgent);

    List<TechnicalLogDTO> search(Integer hopitalId, Long userId, String userEmail, String module,
                                 String action, String status, String requestId, String endpoint,
                                 String search, LocalDateTime dateFrom, LocalDateTime dateTo, int limit);

    TechnicalLogKpisDTO getKpis();

    List<String> listModules();

    List<String> listActions();
}
