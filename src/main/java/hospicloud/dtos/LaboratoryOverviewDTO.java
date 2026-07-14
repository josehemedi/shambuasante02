package hospicloud.dtos;

import java.util.ArrayList;
import java.util.List;

public class LaboratoryOverviewDTO {
    private LaboratoryKpisDTO kpis = new LaboratoryKpisDTO();
    private List<LaboratoryTestItemDTO> tests = new ArrayList<>();

    public LaboratoryKpisDTO getKpis() { return kpis; }
    public void setKpis(LaboratoryKpisDTO kpis) { this.kpis = kpis; }

    public List<LaboratoryTestItemDTO> getTests() { return tests; }
    public void setTests(List<LaboratoryTestItemDTO> tests) { this.tests = tests; }
}
