package hospicloud.dtos;

public class HospitalAdminDeptLoadDTO {
    private String name;
    private int value;
    private String color;

    public HospitalAdminDeptLoadDTO() {}

    public HospitalAdminDeptLoadDTO(String name, int value, String color) {
        this.name = name;
        this.value = value;
        this.color = color;
    }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public int getValue() { return value; }
    public void setValue(int value) { this.value = value; }

    public String getColor() { return color; }
    public void setColor(String color) { this.color = color; }
}
