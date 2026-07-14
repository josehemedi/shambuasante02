package hospicloud.dtos;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class HospitalAdminDashboardDTO {
    private String hospitalName;
    private HospitalAdminKpisDTO kpis = new HospitalAdminKpisDTO();
    private List<HospitalAdminRevenuePointDTO> revenueSeries = new ArrayList<>();
    private List<HospitalAdminFlowPointDTO> patientFlow = new ArrayList<>();
    private List<HospitalAdminDeptLoadDTO> departmentLoad = new ArrayList<>();
    private List<HospitalAdminAlertDTO> emergencyAlerts = new ArrayList<>();
    private List<HospitalAdminInsightDTO> aiInsights = new ArrayList<>();
    private List<HospitalAdminTimelineItemDTO> activityTimeline = new ArrayList<>();

    public String getHospitalName() { return hospitalName; }
    public void setHospitalName(String hospitalName) { this.hospitalName = hospitalName; }

    public HospitalAdminKpisDTO getKpis() { return kpis; }
    public void setKpis(HospitalAdminKpisDTO kpis) { this.kpis = kpis; }

    public List<HospitalAdminRevenuePointDTO> getRevenueSeries() { return revenueSeries; }
    public void setRevenueSeries(List<HospitalAdminRevenuePointDTO> revenueSeries) { this.revenueSeries = revenueSeries; }

    public List<HospitalAdminFlowPointDTO> getPatientFlow() { return patientFlow; }
    public void setPatientFlow(List<HospitalAdminFlowPointDTO> patientFlow) { this.patientFlow = patientFlow; }

    public List<HospitalAdminDeptLoadDTO> getDepartmentLoad() { return departmentLoad; }
    public void setDepartmentLoad(List<HospitalAdminDeptLoadDTO> departmentLoad) { this.departmentLoad = departmentLoad; }

    public List<HospitalAdminAlertDTO> getEmergencyAlerts() { return emergencyAlerts; }
    public void setEmergencyAlerts(List<HospitalAdminAlertDTO> emergencyAlerts) { this.emergencyAlerts = emergencyAlerts; }

    public List<HospitalAdminInsightDTO> getAiInsights() { return aiInsights; }
    public void setAiInsights(List<HospitalAdminInsightDTO> aiInsights) { this.aiInsights = aiInsights; }

    public List<HospitalAdminTimelineItemDTO> getActivityTimeline() { return activityTimeline; }
    public void setActivityTimeline(List<HospitalAdminTimelineItemDTO> activityTimeline) { this.activityTimeline = activityTimeline; }
}
