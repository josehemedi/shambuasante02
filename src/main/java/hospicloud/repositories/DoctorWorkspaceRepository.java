package hospicloud.repositories;

import hospicloud.dtos.DoctorWorkspaceActivityDTO;

import java.util.List;

public interface DoctorWorkspaceRepository {

    List<DoctorWorkspaceActivityDTO> findRecentActivities(Integer medecinId, Integer hopitalId);
}
