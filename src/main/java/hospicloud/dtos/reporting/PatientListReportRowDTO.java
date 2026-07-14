package hospicloud.dtos.reporting;

public class PatientListReportRowDTO {

    private String numero;
    private String codePatient;
    private String nomComplet;
    private String sexe;
    private String dateNaissance;
    private String age;
    private String telephone;
    private String email;
    private String groupeSanguin;
    private String estActif;
    private String statutClinique;
    private String dateEnregistrement;
    private String profession;
    private String numeroMatricule;

    public PatientListReportRowDTO() {
    }

    public PatientListReportRowDTO(
            String numero,
            String codePatient,
            String nomComplet,
            String sexe,
            String dateNaissance,
            String age,
            String telephone,
            String email,
            String groupeSanguin,
            String estActif,
            String statutClinique,
            String dateEnregistrement,
            String profession,
            String numeroMatricule) {
        this.numero = numero;
        this.codePatient = codePatient;
        this.nomComplet = nomComplet;
        this.sexe = sexe;
        this.dateNaissance = dateNaissance;
        this.age = age;
        this.telephone = telephone;
        this.email = email;
        this.groupeSanguin = groupeSanguin;
        this.estActif = estActif;
        this.statutClinique = statutClinique;
        this.dateEnregistrement = dateEnregistrement;
        this.profession = profession;
        this.numeroMatricule = numeroMatricule;
    }

    public String getNumero() {
        return numero;
    }

    public void setNumero(String numero) {
        this.numero = numero;
    }

    public String getCodePatient() {
        return codePatient;
    }

    public void setCodePatient(String codePatient) {
        this.codePatient = codePatient;
    }

    public String getNomComplet() {
        return nomComplet;
    }

    public void setNomComplet(String nomComplet) {
        this.nomComplet = nomComplet;
    }

    public String getSexe() {
        return sexe;
    }

    public void setSexe(String sexe) {
        this.sexe = sexe;
    }

    public String getDateNaissance() {
        return dateNaissance;
    }

    public void setDateNaissance(String dateNaissance) {
        this.dateNaissance = dateNaissance;
    }

    public String getAge() {
        return age;
    }

    public void setAge(String age) {
        this.age = age;
    }

    public String getTelephone() {
        return telephone;
    }

    public void setTelephone(String telephone) {
        this.telephone = telephone;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getGroupeSanguin() {
        return groupeSanguin;
    }

    public void setGroupeSanguin(String groupeSanguin) {
        this.groupeSanguin = groupeSanguin;
    }

    public String getEstActif() {
        return estActif;
    }

    public void setEstActif(String estActif) {
        this.estActif = estActif;
    }

    public String getStatutClinique() {
        return statutClinique;
    }

    public void setStatutClinique(String statutClinique) {
        this.statutClinique = statutClinique;
    }

    public String getDateEnregistrement() {
        return dateEnregistrement;
    }

    public void setDateEnregistrement(String dateEnregistrement) {
        this.dateEnregistrement = dateEnregistrement;
    }

    public String getProfession() {
        return profession;
    }

    public void setProfession(String profession) {
        this.profession = profession;
    }

    public String getNumeroMatricule() {
        return numeroMatricule;
    }

    public void setNumeroMatricule(String numeroMatricule) {
        this.numeroMatricule = numeroMatricule;
    }
}
