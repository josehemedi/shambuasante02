package hospicloud.repositories;

import java.time.LocalDate;

public interface LogsActiviteRepository {
    Long countActiveUsersInPeriod(Integer hopitalId, LocalDate startDate, LocalDate endDate);
}
