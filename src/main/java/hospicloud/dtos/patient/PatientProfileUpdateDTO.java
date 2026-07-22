package hospicloud.dtos.patient;

import jakarta.validation.constraints.NotBlank;

public class PatientProfileUpdateDTO {
    private String telephone;
    private String adresse;
    private String contactUrgence;
    private String profession;
    private String email;

    public String getTelephone() { return telephone; }
    public void setTelephone(String telephone) { this.telephone = telephone; }
    public String getAdresse() { return adresse; }
    public void setAdresse(String adresse) { this.adresse = adresse; }
    public String getContactUrgence() { return contactUrgence; }
    public void setContactUrgence(String contactUrgence) { this.contactUrgence = contactUrgence; }
    public String getProfession() { return profession; }
    public void setProfession(String profession) { this.profession = profession; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
}
