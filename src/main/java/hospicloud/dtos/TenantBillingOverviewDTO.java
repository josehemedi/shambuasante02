package hospicloud.dtos;

import java.util.ArrayList;
import java.util.List;

public class TenantBillingOverviewDTO {
    private String hospitalName = "Hospital";
    private Integer hopitalId;
    private TenantBillingKpisDTO kpis = new TenantBillingKpisDTO();
    private List<TenantBillingInvoiceDTO> invoices = new ArrayList<>();
    private List<TenantBillingRevenuePointDTO> revenueSeries = new ArrayList<>();

    public String getHospitalName() { return hospitalName; }
    public void setHospitalName(String hospitalName) { this.hospitalName = hospitalName; }

    public Integer getHopitalId() { return hopitalId; }
    public void setHopitalId(Integer hopitalId) { this.hopitalId = hopitalId; }

    public TenantBillingKpisDTO getKpis() { return kpis; }
    public void setKpis(TenantBillingKpisDTO kpis) { this.kpis = kpis; }

    public List<TenantBillingInvoiceDTO> getInvoices() { return invoices; }
    public void setInvoices(List<TenantBillingInvoiceDTO> invoices) { this.invoices = invoices; }

    public List<TenantBillingRevenuePointDTO> getRevenueSeries() { return revenueSeries; }
    public void setRevenueSeries(List<TenantBillingRevenuePointDTO> revenueSeries) { this.revenueSeries = revenueSeries; }
}
