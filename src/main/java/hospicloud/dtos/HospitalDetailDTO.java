package hospicloud.dtos;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class HospitalDetailDTO {
    private String id;
    private Integer idHopital;
    private String name;
    private String nomCommercial;
    private String sousDomaine;
    private String country;
    private String city;
    private String adresse;
    private String adresseComplete;
    private String type;
    private String plan;
    private Long users;
    private String status;
    private BigDecimal mrr;
    private String specialty;
    private String contact;
    private String email;
    private String phone;
    private String logoUrl;
    private boolean estActif;
    private LocalDateTime joined;
    private LocalDateTime lastActive;

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public Integer getIdHopital() { return idHopital; }
    public void setIdHopital(Integer idHopital) { this.idHopital = idHopital; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getNomCommercial() { return nomCommercial; }
    public void setNomCommercial(String nomCommercial) { this.nomCommercial = nomCommercial; }

    public String getSousDomaine() { return sousDomaine; }
    public void setSousDomaine(String sousDomaine) { this.sousDomaine = sousDomaine; }

    public String getCountry() { return country; }
    public void setCountry(String country) { this.country = country; }

    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }

    public String getAdresse() { return adresse; }
    public void setAdresse(String adresse) { this.adresse = adresse; }

    public String getAdresseComplete() { return adresseComplete; }
    public void setAdresseComplete(String adresseComplete) { this.adresseComplete = adresseComplete; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getPlan() { return plan; }
    public void setPlan(String plan) { this.plan = plan; }

    public Long getUsers() { return users; }
    public void setUsers(Long users) { this.users = users; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public BigDecimal getMrr() { return mrr; }
    public void setMrr(BigDecimal mrr) { this.mrr = mrr; }

    public String getSpecialty() { return specialty; }
    public void setSpecialty(String specialty) { this.specialty = specialty; }

    public String getContact() { return contact; }
    public void setContact(String contact) { this.contact = contact; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getLogoUrl() { return logoUrl; }
    public void setLogoUrl(String logoUrl) { this.logoUrl = logoUrl; }

    public boolean isEstActif() { return estActif; }
    public void setEstActif(boolean estActif) { this.estActif = estActif; }

    public LocalDateTime getJoined() { return joined; }
    public void setJoined(LocalDateTime joined) { this.joined = joined; }

    public LocalDateTime getLastActive() { return lastActive; }
    public void setLastActive(LocalDateTime lastActive) { this.lastActive = lastActive; }
}
