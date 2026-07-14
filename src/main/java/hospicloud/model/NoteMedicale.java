package hospicloud.model;

/**
 * Entité gérant les notes et observations privées du médecin.
 * Permet de stocker des réflexions cliniques non destinées au dossier public.
 * * Développé par Siku Hemedi Jose - Projet Hospicloud.
 */
public class NoteMedicale {

    private Integer idNote;
    private Integer idConsultation; // Lien direct avec l'examen médical
    private String contenuNote;
    private boolean estPrivee;      // Flag de visibilité restreinte au praticien

    // Constructeur par défaut
    public NoteMedicale() {
    }

    // Constructeur complet
    public NoteMedicale(Integer idNote, Integer idConsultation, String contenuNote, boolean estPrivee) {
        this.idNote = idNote;
        this.idConsultation = idConsultation;
        this.contenuNote = contenuNote;
        this.estPrivee = estPrivee;
    }

    // Getters & Setters
    public Integer getIdNote() {
        return idNote;
    }

    public void setIdNote(Integer idNote) {
        this.idNote = idNote;
    }

    public Integer getIdConsultation() {
        return idConsultation;
    }

    public void setIdConsultation(Integer idConsultation) {
        this.idConsultation = idConsultation;
    }

    public String getContenuNote() {
        return contenuNote;
    }

    public void setContenuNote(String contenuNote) {
        this.contenuNote = contenuNote;
    }

    public boolean isEstPrivee() {
        return estPrivee;
    }

    public void setEstPrivee(boolean estPrivee) {
        this.estPrivee = estPrivee;
    }

    @Override
    public String toString() {
        return "NoteMedicale{" +
                "id=" + idNote +
                ", consultationId=" + idConsultation +
                ", estPrivee=" + estPrivee +
                '}';
    }
}