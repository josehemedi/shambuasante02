package hospicloud.servicesImpl;

import hospicloud.dtos.TenantReportsOverviewDTO;
import hospicloud.exceptions.BadRequestException;
import hospicloud.exceptions.ForbiddenException;
import hospicloud.model.Role;
import hospicloud.repositories.TenantReportsRepository;
import hospicloud.security.UtilisateurPrincipal;
import hospicloud.services.TenantReportsService;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;

@Service
@Transactional(readOnly = true)
public class TenantReportsServiceImpl implements TenantReportsService {

    private final TenantReportsRepository tenantReportsRepository;

    public TenantReportsServiceImpl(TenantReportsRepository tenantReportsRepository) {
        this.tenantReportsRepository = tenantReportsRepository;
    }

    @Override
    public TenantReportsOverviewDTO getOverview(LocalDate from, LocalDate to) {
        Integer hopitalId = requireTenantAdminHopitalId();
        DateRange range = resolveRange(from, to);

        TenantReportsOverviewDTO overview = new TenantReportsOverviewDTO();
        overview.setHopitalId(hopitalId);
        overview.setHospitalName(tenantReportsRepository.findHospitalName(hopitalId));
        overview.setDateFrom(range.from().toString());
        overview.setDateTo(range.toInclusive().toString());
        overview.setTotalPatients(tenantReportsRepository.countActivePatients(hopitalId));
        overview.setTotalAppointments(tenantReportsRepository.countAppointmentsBetween(
                hopitalId, range.from(), range.toExclusive()));
        overview.setTotalRevenue(tenantReportsRepository.sumRevenueBetween(
                hopitalId, range.from(), range.toExclusive()));
        overview.setTotalInvoices(tenantReportsRepository.countInvoicesBetween(
                hopitalId, range.from(), range.toExclusive()));
        overview.setMonthlyAppointments(tenantReportsRepository.getMonthlyAppointments(
                hopitalId, range.from(), range.toExclusive()));
        overview.setRevenueSeries(tenantReportsRepository.getMonthlyRevenue(
                hopitalId, range.from(), range.toExclusive()));
        overview.setPatientDemographics(tenantReportsRepository.getPatientDemographics(hopitalId));
        return overview;
    }

    private DateRange resolveRange(LocalDate from, LocalDate to) {
        LocalDate end = to != null ? to : LocalDate.now();
        LocalDate start = from != null ? from : end.minusMonths(5).withDayOfMonth(1);

        if (start.isAfter(end)) {
            throw new BadRequestException("La date de début doit être antérieure à la date de fin.");
        }

        LocalDate maxSpanStart = end.minusMonths(24);
        if (start.isBefore(maxSpanStart)) {
            start = maxSpanStart;
        }

        return new DateRange(start, end);
    }

    private Integer requireTenantAdminHopitalId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof UtilisateurPrincipal principal)) {
            throw new ForbiddenException("Authentification requise.");
        }
        if (principal.getAppRole() != Role.TENANT_ADMIN) {
            throw new ForbiddenException("Accès réservé aux administrateurs d'hôpital.");
        }
        if (principal.getIdHopital() == null) {
            throw new ForbiddenException("Aucun établissement associé à votre compte.");
        }
        return principal.getIdHopital();
    }

    private record DateRange(LocalDate from, LocalDate toInclusive) {
        LocalDate toExclusive() {
            return toInclusive.plusDays(1);
        }
    }
}
