package hospicloud.dtos;

public class AuthUserDTO {

    private Integer idUtilisateur;
    private String email;
    private String nom;
    private String prenom;
    private String role;
    private String frontendRole;
    private Integer idHopital;
    private Integer idMedecin;
    private Long idPatient;
    private String tenantLabel;
    private boolean tenantAccessRestricted;

    public Integer getIdUtilisateur() {
        return idUtilisateur;
    }

    public void setIdUtilisateur(Integer idUtilisateur) {
        this.idUtilisateur = idUtilisateur;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public String getPrenom() {
        return prenom;
    }

    public void setPrenom(String prenom) {
        this.prenom = prenom;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getFrontendRole() {
        return frontendRole;
    }

    public void setFrontendRole(String frontendRole) {
        this.frontendRole = frontendRole;
    }

    public Integer getIdHopital() {
        return idHopital;
    }

    public void setIdHopital(Integer idHopital) {
        this.idHopital = idHopital;
    }

    public Integer getIdMedecin() {
        return idMedecin;
    }

    public void setIdMedecin(Integer idMedecin) {
        this.idMedecin = idMedecin;
    }

    public Long getIdPatient() {
        return idPatient;
    }

    public void setIdPatient(Long idPatient) {
        this.idPatient = idPatient;
    }

    public String getTenantLabel() {
        return tenantLabel;
    }

    public void setTenantLabel(String tenantLabel) {
        this.tenantLabel = tenantLabel;
    }

    public boolean isTenantAccessRestricted() {
        return tenantAccessRestricted;
    }

    public void setTenantAccessRestricted(boolean tenantAccessRestricted) {
        this.tenantAccessRestricted = tenantAccessRestricted;
    }
}
