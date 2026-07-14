package hospicloud.dtos.reporting;

public class ReportNumericChartRowDTO {

    private String label;
    private Double value;

    public ReportNumericChartRowDTO() {
    }

    public ReportNumericChartRowDTO(String label, Double value) {
        this.label = label;
        this.value = value;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public Double getValue() {
        return value;
    }

    public void setValue(Double value) {
        this.value = value;
    }
}
