package hospicloud.dtos.patient;

public class PatientRegistrationResponseDTO {
    private Integer idPatient;
    private String codePatient;
    private Integer idHopital;
    private String nomHopital;
    private String email;
    private String message;

    public Integer getIdPatient() { return idPatient; }
    public void setIdPatient(Integer idPatient) { this.idPatient = idPatient; }
    public String getCodePatient() { return codePatient; }
    public void setCodePatient(String codePatient) { this.codePatient = codePatient; }
    public Integer getIdHopital() { return idHopital; }
    public void setIdHopital(Integer idHopital) { this.idHopital = idHopital; }
    public String getNomHopital() { return nomHopital; }
    public void setNomHopital(String nomHopital) { this.nomHopital = nomHopital; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
}
