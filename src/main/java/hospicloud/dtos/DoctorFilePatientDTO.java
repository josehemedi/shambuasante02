package hospicloud.dtos;

import java.time.LocalDateTime;

public class DoctorFilePatientDTO {
    private Integer id;
    private String patientName;
    private String waited;
    private String priority;
    private String room;
    private LocalDateTime appointmentTime;

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public String getPatientName() { return patientName; }
    public void setPatientName(String patientName) { this.patientName = patientName; }

    public String getWaited() { return waited; }
    public void setWaited(String waited) { this.waited = waited; }

    public String getPriority() { return priority; }
    public void setPriority(String priority) { this.priority = priority; }

    public String getRoom() { return room; }
    public void setRoom(String room) { this.room = room; }

    public LocalDateTime getAppointmentTime() { return appointmentTime; }
    public void setAppointmentTime(LocalDateTime appointmentTime) { this.appointmentTime = appointmentTime; }
}
