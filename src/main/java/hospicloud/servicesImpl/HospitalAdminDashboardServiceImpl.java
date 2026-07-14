package hospicloud.servicesImpl;

import hospicloud.dtos.*;
import hospicloud.exceptions.ForbiddenException;
import hospicloud.model.Role;
import hospicloud.repositories.HospitalAdminDashboardRepository;
import hospicloud.security.UtilisateurPrincipal;
import hospicloud.services.HospitalAdminDashboardService;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class HospitalAdminDashboardServiceImpl implements HospitalAdminDashboardService {

    private final HospitalAdminDashboardRepository dashboardRepository;

    public HospitalAdminDashboardServiceImpl(HospitalAdminDashboardRepository dashboardRepository) {
        this.dashboardRepository = dashboardRepository;
    }

    @Override
    public HospitalAdminDashboardDTO getDashboard() {
        Integer hopitalId = requireTenantAdminHopitalId();

        HospitalAdminDashboardDTO dashboard = new HospitalAdminDashboardDTO();
        dashboard.setHospitalName(dashboardRepository.findHospitalName(hopitalId));
        dashboard.setKpis(buildKpis(hopitalId));
        dashboard.setRevenueSeries(dashboardRepository.getRevenueSeries(hopitalId, 8));
        dashboard.setPatientFlow(dashboardRepository.getPatientFlowLast7Days(hopitalId));
        dashboard.setDepartmentLoad(dashboardRepository.getDepartmentLoad(hopitalId));
        dashboard.setEmergencyAlerts(dashboardRepository.getEmergencyAlerts(hopitalId, 5));
        dashboard.setActivityTimeline(dashboardRepository.getActivityTimeline(hopitalId, 8));
        dashboard.setAiInsights(buildInsights(dashboard.getKpis(), dashboard.getEmergencyAlerts().size()));
        return dashboard;
    }

    private HospitalAdminKpisDTO buildKpis(Integer hopitalId) {
        LocalDate today = LocalDate.now();
        YearMonth currentMonth = YearMonth.now();
        YearMonth previousMonth = currentMonth.minusMonths(1);

        long totalPatients = dashboardRepository.countActivePatients(hopitalId);
        long patientsStartOfMonth = dashboardRepository.countPatientsRegisteredBefore(hopitalId, currentMonth.atDay(1));
        double deltaPatients = calculatePercentage(patientsStartOfMonth, totalPatients);

        long activeConsultations = dashboardRepository.countActiveConsultations(hopitalId);
        long consultationsYesterday = dashboardRepository.countActiveConsultationsOnDate(hopitalId, today.minusDays(1));
        double deltaConsultations = calculatePercentage(consultationsYesterday, activeConsultations);

        BigDecimal revenueMtd = dashboardRepository.sumRevenueBetween(
                hopitalId, currentMonth.atDay(1), currentMonth.plusMonths(1).atDay(1));
        BigDecimal revenuePrevMtd = dashboardRepository.sumRevenueBetween(
                hopitalId, previousMonth.atDay(1), currentMonth.atDay(1));
        double deltaRevenue = calculatePercentage(revenuePrevMtd, revenueMtd);

        long hospitalized = dashboardRepository.countHospitalized(hopitalId);
        long capacityBase = Math.max(10, totalPatients);
        double occupancy = Math.min(100.0, (hospitalized * 100.0) / capacityBase);
        long hospitalizedYesterday = dashboardRepository.countHospitalizedOnDate(hopitalId, today.minusDays(1));
        double occupancyYesterday = Math.min(100.0, (hospitalizedYesterday * 100.0) / capacityBase);
        double deltaOccupancy = round(occupancy - occupancyYesterday);

        HospitalAdminKpisDTO kpis = new HospitalAdminKpisDTO();
        kpis.setTotalPatients(totalPatients);
        kpis.setDeltaTotalPatients(deltaPatients);
        kpis.setActiveConsultations(activeConsultations);
        kpis.setDeltaActiveConsultations(deltaConsultations);
        kpis.setRevenueMtd(revenueMtd);
        kpis.setDeltaRevenueMtd(deltaRevenue);
        kpis.setOccupancy(round(occupancy));
        kpis.setDeltaOccupancy(deltaOccupancy);
        return kpis;
    }

    private List<HospitalAdminInsightDTO> buildInsights(HospitalAdminKpisDTO kpis, int alertCount) {
        List<HospitalAdminInsightDTO> insights = new ArrayList<>();

        if (kpis.getOccupancy() >= 85) {
            insights.add(insight(1, "High bed occupancy",
                    "Forte occupation des lits",
                    "Current occupancy is " + kpis.getOccupancy() + "%. Consider prioritizing discharges.",
                    "L'occupation actuelle est de " + kpis.getOccupancy() + " %. Priorisez les sorties.",
                    "warning"));
        }

        if (alertCount > 0) {
            insights.add(insight(2, "Priority patients in queue",
                    "Patients prioritaires en file",
                    alertCount + " high-priority admission(s) require attention.",
                    alertCount + " admission(s) prioritaire(s) nécessitent une attention.",
                    "warning"));
        }

        if (kpis.getDeltaRevenueMtd() < -5) {
            insights.add(insight(3, "Revenue below last month",
                    "Revenus en baisse",
                    "Month-to-date revenue is down compared to the previous month.",
                    "Le chiffre d'affaires du mois est inférieur au mois précédent.",
                    "secondary"));
        } else if (kpis.getDeltaTotalPatients() > 5) {
            insights.add(insight(4, "Patient base growing",
                    "Croissance du parc patients",
                    "Your active patient registry grew this month.",
                    "Votre registre de patients actifs a progressé ce mois-ci.",
                    "primary"));
        }

        if (insights.isEmpty()) {
            insights.add(insight(5, "Operations stable",
                    "Activité stable",
                    "No critical anomalies detected for your hospital today.",
                    "Aucune anomalie critique détectée pour votre établissement aujourd'hui.",
                    "primary"));
        }

        return insights.size() > 3 ? insights.subList(0, 3) : insights;
    }

    private HospitalAdminInsightDTO insight(int id, String title, String titleFr, String detail, String detailFr, String tone) {
        HospitalAdminInsightDTO dto = new HospitalAdminInsightDTO();
        dto.setId(id);
        dto.setTitle(title);
        dto.setTitleFr(titleFr);
        dto.setDetail(detail);
        dto.setDetailFr(detailFr);
        dto.setTone(tone);
        return dto;
    }

    private double calculatePercentage(long previous, long current) {
        return calculatePercentage(BigDecimal.valueOf(previous), BigDecimal.valueOf(current));
    }

    private double calculatePercentage(BigDecimal previous, BigDecimal current) {
        if (previous == null || previous.compareTo(BigDecimal.ZERO) == 0) {
            return current != null && current.compareTo(BigDecimal.ZERO) > 0 ? 100.0 : 0.0;
        }
        return current.subtract(previous)
                .multiply(BigDecimal.valueOf(100))
                .divide(previous, 2, RoundingMode.HALF_UP)
                .doubleValue();
    }

    private double round(double value) {
        return BigDecimal.valueOf(value).setScale(1, RoundingMode.HALF_UP).doubleValue();
    }

    private Integer requireTenantAdminHopitalId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof UtilisateurPrincipal principal)) {
            throw new AccessDeniedException("Authentification requise");
        }
        if (principal.getAppRole() != Role.TENANT_ADMIN) {
            throw new ForbiddenException("Accès réservé aux administrateurs d'hôpital");
        }
        Integer hopitalId = principal.getIdHopital();
        if (hopitalId == null) {
            throw new ForbiddenException("Aucun établissement associé à votre compte");
        }
        return hopitalId;
    }
}
