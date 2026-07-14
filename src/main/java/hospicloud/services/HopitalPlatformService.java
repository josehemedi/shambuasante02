package hospicloud.services;

import hospicloud.dtos.HospitalActivityDTO;
import hospicloud.dtos.HospitalCreateDTO;
import hospicloud.dtos.HospitalDetailDTO;
import hospicloud.dtos.HospitalOverviewDTO;
import hospicloud.dtos.HospitalPlanCatalogDTO;
import hospicloud.dtos.HospitalPlatformStatsDTO;
import hospicloud.dtos.HospitalUpdateDTO;

import java.util.List;

public interface HopitalPlatformService {
    HospitalPlatformStatsDTO getPlatformStats();

    List<HospitalOverviewDTO> listOverview();

    HospitalDetailDTO getHospitalDetail(Integer idHopital);

    List<HospitalActivityDTO> listRecentActivity(int limit);

    List<HospitalPlanCatalogDTO> listPlansCatalog();

    HospitalOverviewDTO createHospital(HospitalCreateDTO dto);

    HospitalDetailDTO updateHospital(Integer idHopital, HospitalUpdateDTO dto);

    HospitalDetailDTO setHospitalStatus(Integer idHopital, boolean active);
}
