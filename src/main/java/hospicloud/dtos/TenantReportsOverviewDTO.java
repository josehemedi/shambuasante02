package hospicloud.dtos;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class TenantReportsOverviewDTO {

    private Integer hopitalId;
    private String hospitalName;
    private String dateFrom;
    private String dateTo;
    private long totalPatients;
    private long totalAppointments;
    private BigDecimal totalRevenue;
    private long totalInvoices;
    private List<TenantReportsAppointmentMonthDTO> monthlyAppointments = new ArrayList<>();
    private List<TenantReportsRevenueMonthDTO> revenueSeries = new ArrayList<>();
    private List<TenantReportsDemographicDTO> patientDemographics = new ArrayList<>();

    public Integer getHopitalId() {
        return hopitalId;
    }

    public void setHopitalId(Integer hopitalId) {
        this.hopitalId = hopitalId;
    }

    public String getHospitalName() {
        return hospitalName;
    }

    public void setHospitalName(String hospitalName) {
        this.hospitalName = hospitalName;
    }

    public String getDateFrom() {
        return dateFrom;
    }

    public void setDateFrom(String dateFrom) {
        this.dateFrom = dateFrom;
    }

    public String getDateTo() {
        return dateTo;
    }

    public void setDateTo(String dateTo) {
        this.dateTo = dateTo;
    }

    public long getTotalPatients() {
        return totalPatients;
    }

    public void setTotalPatients(long totalPatients) {
        this.totalPatients = totalPatients;
    }

    public long getTotalAppointments() {
        return totalAppointments;
    }

    public void setTotalAppointments(long totalAppointments) {
        this.totalAppointments = totalAppointments;
    }

    public BigDecimal getTotalRevenue() {
        return totalRevenue;
    }

    public void setTotalRevenue(BigDecimal totalRevenue) {
        this.totalRevenue = totalRevenue;
    }

    public long getTotalInvoices() {
        return totalInvoices;
    }

    public void setTotalInvoices(long totalInvoices) {
        this.totalInvoices = totalInvoices;
    }

    public List<TenantReportsAppointmentMonthDTO> getMonthlyAppointments() {
        return monthlyAppointments;
    }

    public void setMonthlyAppointments(List<TenantReportsAppointmentMonthDTO> monthlyAppointments) {
        this.monthlyAppointments = monthlyAppointments != null ? monthlyAppointments : new ArrayList<>();
    }

    public List<TenantReportsRevenueMonthDTO> getRevenueSeries() {
        return revenueSeries;
    }

    public void setRevenueSeries(List<TenantReportsRevenueMonthDTO> revenueSeries) {
        this.revenueSeries = revenueSeries != null ? revenueSeries : new ArrayList<>();
    }

    public List<TenantReportsDemographicDTO> getPatientDemographics() {
        return patientDemographics;
    }

    public void setPatientDemographics(List<TenantReportsDemographicDTO> patientDemographics) {
        this.patientDemographics = patientDemographics != null ? patientDemographics : new ArrayList<>();
    }
}
