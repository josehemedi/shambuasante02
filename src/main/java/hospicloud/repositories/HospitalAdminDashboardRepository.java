package hospicloud.repositories;

import hospicloud.dtos.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public interface HospitalAdminDashboardRepository {
    String findHospitalName(Integer idHopital);

    long countActivePatients(Integer idHopital);

    long countPatientsRegisteredBefore(Integer idHopital, LocalDate date);

    long countActiveConsultations(Integer idHopital);

    long countActiveConsultationsOnDate(Integer idHopital, LocalDate date);

    BigDecimal sumRevenueBetween(Integer idHopital, LocalDate startInclusive, LocalDate endExclusive);

    long countHospitalized(Integer idHopital);

    long countHospitalizedOnDate(Integer idHopital, LocalDate date);

    List<HospitalAdminRevenuePointDTO> getRevenueSeries(Integer idHopital, int months);

    List<HospitalAdminFlowPointDTO> getPatientFlowLast7Days(Integer idHopital);

    List<HospitalAdminDeptLoadDTO> getDepartmentLoad(Integer idHopital);

    List<HospitalAdminAlertDTO> getEmergencyAlerts(Integer idHopital, int limit);

    List<HospitalAdminTimelineItemDTO> getActivityTimeline(Integer idHopital, int limit);
}
