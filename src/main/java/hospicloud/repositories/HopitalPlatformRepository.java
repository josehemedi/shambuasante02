package hospicloud.repositories;

import hospicloud.dtos.HospitalActivityDTO;
import hospicloud.dtos.HospitalDetailDTO;
import hospicloud.dtos.HospitalOverviewDTO;

import java.util.List;
import java.util.Optional;

public interface HopitalPlatformRepository {
    List<HospitalOverviewDTO> listOverview();

    Optional<HospitalDetailDTO> findDetailById(Integer idHopital);

    long countByStatus(String status);

    long countTotal();

    List<HospitalActivityDTO> listRecentActivity(int limit);
}
