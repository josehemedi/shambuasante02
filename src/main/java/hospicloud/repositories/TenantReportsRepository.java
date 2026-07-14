package hospicloud.repositories;

import hospicloud.dtos.TenantReportsAppointmentMonthDTO;
import hospicloud.dtos.TenantReportsDemographicDTO;
import hospicloud.dtos.TenantReportsRevenueMonthDTO;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public interface TenantReportsRepository {

    String findHospitalName(Integer idHopital);

    long countActivePatients(Integer idHopital);

    long countAppointmentsBetween(Integer idHopital, LocalDate from, LocalDate toExclusive);

    long countInvoicesBetween(Integer idHopital, LocalDate from, LocalDate toExclusive);

    BigDecimal sumRevenueBetween(Integer idHopital, LocalDate from, LocalDate toExclusive);

    List<TenantReportsAppointmentMonthDTO> getMonthlyAppointments(Integer idHopital, LocalDate from, LocalDate toExclusive);

    List<TenantReportsRevenueMonthDTO> getMonthlyRevenue(Integer idHopital, LocalDate from, LocalDate toExclusive);

    List<TenantReportsDemographicDTO> getPatientDemographics(Integer idHopital);
}
