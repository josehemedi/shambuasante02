package hospicloud.dtos;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class TenantCashierQueueItemDTO {
    private String id;
    private Integer idFacture;
    private String patientId;
    private String patientName;
    private Integer age;
    private String sex;
    private LocalDateTime visitDate;
    private String department = "—";
    private String doctorName = "—";
    private String invoiceNumber;
    private String status;
    private BigDecimal totalAmount = BigDecimal.ZERO;
    private BigDecimal paidAmount = BigDecimal.ZERO;
    private BigDecimal balanceDue = BigDecimal.ZERO;
    private BigDecimal sousTotalSoins = BigDecimal.ZERO;
    private BigDecimal montantAssurance = BigDecimal.ZERO;
    private BigDecimal montantRemise = BigDecimal.ZERO;
    private BigDecimal montantAvances = BigDecimal.ZERO;
    private BigDecimal tauxAssurance = BigDecimal.ZERO;
    private Integer idPatientDb;
    private String priority = "normal";
    private boolean awaitingAdminDischarge;
    private boolean adminDischargeValidated;
    private List<TenantCashierFeeLineDTO> consultationFees = new ArrayList<>();
    private List<TenantCashierFeeLineDTO> laboratoryFees = new ArrayList<>();
    private List<TenantCashierFeeLineDTO> pharmacyItems = new ArrayList<>();
    private List<TenantCashierFeeLineDTO> hospitalizationFees = new ArrayList<>();
    private List<TenantCashierFeeLineDTO> medicalActFees = new ArrayList<>();
    private List<TenantCashierFeeLineDTO> otherFees = new ArrayList<>();

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public Integer getIdFacture() { return idFacture; }
    public void setIdFacture(Integer idFacture) { this.idFacture = idFacture; }

    public String getPatientId() { return patientId; }
    public void setPatientId(String patientId) { this.patientId = patientId; }

    public String getPatientName() { return patientName; }
    public void setPatientName(String patientName) { this.patientName = patientName; }

    public Integer getAge() { return age; }
    public void setAge(Integer age) { this.age = age; }

    public String getSex() { return sex; }
    public void setSex(String sex) { this.sex = sex; }

    public LocalDateTime getVisitDate() { return visitDate; }
    public void setVisitDate(LocalDateTime visitDate) { this.visitDate = visitDate; }

    public String getDepartment() { return department; }
    public void setDepartment(String department) { this.department = department; }

    public String getDoctorName() { return doctorName; }
    public void setDoctorName(String doctorName) { this.doctorName = doctorName; }

    public String getInvoiceNumber() { return invoiceNumber; }
    public void setInvoiceNumber(String invoiceNumber) { this.invoiceNumber = invoiceNumber; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public BigDecimal getTotalAmount() { return totalAmount; }
    public void setTotalAmount(BigDecimal totalAmount) { this.totalAmount = totalAmount; }

    public BigDecimal getPaidAmount() { return paidAmount; }
    public void setPaidAmount(BigDecimal paidAmount) { this.paidAmount = paidAmount; }

    public BigDecimal getBalanceDue() { return balanceDue; }
    public void setBalanceDue(BigDecimal balanceDue) { this.balanceDue = balanceDue; }

    public BigDecimal getSousTotalSoins() { return sousTotalSoins; }
    public void setSousTotalSoins(BigDecimal sousTotalSoins) { this.sousTotalSoins = sousTotalSoins; }

    public BigDecimal getMontantAssurance() { return montantAssurance; }
    public void setMontantAssurance(BigDecimal montantAssurance) { this.montantAssurance = montantAssurance; }

    public BigDecimal getMontantRemise() { return montantRemise; }
    public void setMontantRemise(BigDecimal montantRemise) { this.montantRemise = montantRemise; }

    public BigDecimal getMontantAvances() { return montantAvances; }
    public void setMontantAvances(BigDecimal montantAvances) { this.montantAvances = montantAvances; }

    public BigDecimal getTauxAssurance() { return tauxAssurance; }
    public void setTauxAssurance(BigDecimal tauxAssurance) { this.tauxAssurance = tauxAssurance; }

    public Integer getIdPatientDb() { return idPatientDb; }
    public void setIdPatientDb(Integer idPatientDb) { this.idPatientDb = idPatientDb; }

    public String getPriority() { return priority; }
    public void setPriority(String priority) { this.priority = priority; }

    public boolean isAwaitingAdminDischarge() { return awaitingAdminDischarge; }
    public void setAwaitingAdminDischarge(boolean awaitingAdminDischarge) { this.awaitingAdminDischarge = awaitingAdminDischarge; }

    public boolean isAdminDischargeValidated() { return adminDischargeValidated; }
    public void setAdminDischargeValidated(boolean adminDischargeValidated) { this.adminDischargeValidated = adminDischargeValidated; }

    public List<TenantCashierFeeLineDTO> getConsultationFees() { return consultationFees; }
    public void setConsultationFees(List<TenantCashierFeeLineDTO> consultationFees) { this.consultationFees = consultationFees; }

    public List<TenantCashierFeeLineDTO> getLaboratoryFees() { return laboratoryFees; }
    public void setLaboratoryFees(List<TenantCashierFeeLineDTO> laboratoryFees) { this.laboratoryFees = laboratoryFees; }

    public List<TenantCashierFeeLineDTO> getPharmacyItems() { return pharmacyItems; }
    public void setPharmacyItems(List<TenantCashierFeeLineDTO> pharmacyItems) { this.pharmacyItems = pharmacyItems; }

    public List<TenantCashierFeeLineDTO> getHospitalizationFees() { return hospitalizationFees; }
    public void setHospitalizationFees(List<TenantCashierFeeLineDTO> hospitalizationFees) { this.hospitalizationFees = hospitalizationFees; }

    public List<TenantCashierFeeLineDTO> getMedicalActFees() { return medicalActFees; }
    public void setMedicalActFees(List<TenantCashierFeeLineDTO> medicalActFees) { this.medicalActFees = medicalActFees; }

    public List<TenantCashierFeeLineDTO> getOtherFees() { return otherFees; }
    public void setOtherFees(List<TenantCashierFeeLineDTO> otherFees) { this.otherFees = otherFees; }
}

