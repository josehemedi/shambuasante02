package hospicloud.model;

/**
 * Entité représentant les personnes à contacter en cas d'urgence pour un patient.
 * Permet de lier un ou plusieurs proches à un dossier médical.
 * * Développé par Siku Hemedi Jose - Projet Hospicloud.
 */
public class PatientContact {

    private Integer idContact;
    private Integer idPatient; // Clé étrangère vers la table patients
    private String nomContact;
    private String relation;   // Ex: Époux, Père, Mère, Ami
    private String telephoneContact;

    // Constructeur par défaut
    public PatientContact() {
    }

    // Constructeur complet
    public PatientContact(Integer idContact, Integer idPatient, String nomContact, 
                          String relation, String telephoneContact) {
        this.idContact = idContact;
        this.idPatient = idPatient;
        this.nomContact = nomContact;
        this.relation = relation;
        this.telephoneContact = telephoneContact;
    }

    // Getters & Setters
    public Integer getIdContact() {
        return idContact;
    }

    public void setIdContact(Integer idContact) {
        this.idContact = idContact;
    }

    public Integer getIdPatient() {
        return idPatient;
    }

    public void setIdPatient(Integer idPatient) {
        this.idPatient = idPatient;
    }

    public String getNomContact() {
        return nomContact;
    }

    public void setNomContact(String nomContact) {
        this.nomContact = nomContact;
    }

    public String getRelation() {
        return relation;
    }

    public void setRelation(String relation) {
        this.relation = relation;
    }

    public String getTelephoneContact() {
        return telephoneContact;
    }

    public void setTelephoneContact(String telephoneContact) {
        this.telephoneContact = telephoneContact;
    }

    @Override
    public String toString() {
        return "PatientContact{" +
                "id=" + idContact +
                ", patientId=" + idPatient +
                ", nom='" + nomContact + '\'' +
                ", relation='" + relation + '\'' +
                '}';
    }
}