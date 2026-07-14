package hospicloud.repositories;

import hospicloud.dtos.TechnicalLogDTO;
import hospicloud.dtos.TechnicalLogKpisDTO;
import hospicloud.dtos.events.TechnicalLogEvent;

import java.time.LocalDateTime;
import java.util.List;

public interface TechnicalLogRepository {

    void insert(TechnicalLogEvent event);

    List<TechnicalLogDTO> search(Integer hopitalId, Long userId, String userEmail, String module,
                                 String action, String status, String requestId, String endpoint,
                                 String search, LocalDateTime dateFrom, LocalDateTime dateTo, int limit);

    TechnicalLogKpisDTO getKpis(LocalDateTime since);

    List<String> listDistinctModules();

    List<String> listDistinctActions();
}
