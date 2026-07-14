package hospicloud.repositories;

import hospicloud.dtos.DoctorConsultationActiveDTO;
import hospicloud.dtos.DoctorFilePatientDTO;
import hospicloud.dtos.DoctorPendingNoteDTO;

import java.util.List;

public interface DoctorDashboardRepository {

    List<DoctorFilePatientDTO> findFilePatients(Integer medecinId, Integer hopitalId);

    List<DoctorConsultationActiveDTO> findActiveConsultations(Integer medecinId, Integer hopitalId);

    List<DoctorPendingNoteDTO> findPendingNotes(Integer medecinId, Integer hopitalId);
}
