package hospicloud.model;

/**
 * Entité représentant les symptômes rapportés par le patient lors d'une consultation.
 * Permet de documenter précisément l'anamnèse clinique.
 * * Développé par Siku Hemedi Jose - Projet Hospicloud.
 */
public class Symptome {

    private Integer idSymptome;
    private Integer idConsultation; // Clé étrangère vers la table consultations
    private String descriptionSymptome;
    private String dureeSymptome;   // ex: "Depuis 3 jours", "Chronique"

    // Constructeur par défaut
    public Symptome() {
    }

    // Constructeur complet
    public Symptome(Integer idSymptome, Integer idConsultation, String descriptionSymptome, String dureeSymptome) {
        this.idSymptome = idSymptome;
        this.idConsultation = idConsultation;
        this.descriptionSymptome = descriptionSymptome;
        this.dureeSymptome = dureeSymptome;
    }

    // Getters & Setters
    public Integer getIdSymptome() {
        return idSymptome;
    }

    public void setIdSymptome(Integer idSymptome) {
        this.idSymptome = idSymptome;
    }

    public Integer getIdConsultation() {
        return idConsultation;
    }

    public void setIdConsultation(Integer idConsultation) {
        this.idConsultation = idConsultation;
    }

    public String getDescriptionSymptome() {
        return descriptionSymptome;
    }

    public void setDescriptionSymptome(String descriptionSymptome) {
        this.descriptionSymptome = descriptionSymptome;
    }

    public String getDureeSymptome() {
        return dureeSymptome;
    }

    public void setDureeSymptome(String dureeSymptome) {
        this.dureeSymptome = dureeSymptome;
    }

    @Override
    public String toString() {
        return "Symptome{" +
                "id=" + idSymptome +
                ", consultationId=" + idConsultation +
                ", description='" + descriptionSymptome + '\'' +
                ", duree='" + dureeSymptome + '\'' +
                '}';
    }
}