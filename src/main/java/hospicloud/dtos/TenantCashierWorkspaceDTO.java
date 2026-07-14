package hospicloud.dtos;

import java.util.ArrayList;
import java.util.List;

public class TenantCashierWorkspaceDTO {
    private Integer hopitalId;
    private String hospitalName = "Hospital";
    private TenantCashierKpisDTO kpis = new TenantCashierKpisDTO();
    private List<TenantCashierQueueItemDTO> queue = new ArrayList<>();
    private List<TenantCashierHistoryItemDTO> history = new ArrayList<>();

    public Integer getHopitalId() { return hopitalId; }
    public void setHopitalId(Integer hopitalId) { this.hopitalId = hopitalId; }

    public String getHospitalName() { return hospitalName; }
    public void setHospitalName(String hospitalName) { this.hospitalName = hospitalName; }

    public TenantCashierKpisDTO getKpis() { return kpis; }
    public void setKpis(TenantCashierKpisDTO kpis) { this.kpis = kpis; }

    public List<TenantCashierQueueItemDTO> getQueue() { return queue; }
    public void setQueue(List<TenantCashierQueueItemDTO> queue) { this.queue = queue; }

    public List<TenantCashierHistoryItemDTO> getHistory() { return history; }
    public void setHistory(List<TenantCashierHistoryItemDTO> history) { this.history = history; }
}
