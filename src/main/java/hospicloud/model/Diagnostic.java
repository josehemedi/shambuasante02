package hospicloud.model;

/**
 * Entité représentant la conclusion médicale issue d'une consultation.
 * Supporte le codage international des maladies (ICD-10 / CIM-10).
 * * Développé par Siku Hemedi Jose - Projet Hospicloud.
 */
public class Diagnostic {

    private Integer idDiagnostic;
    private Integer idConsultation;
    private String codeIcd10; // Code international (ex: B50.9 pour Paludisme)
    private String descriptionDiagnostic;
    private boolean estFinal; // Différencie un diagnostic provisoire d'un diagnostic confirmé

    // Constructeur par défaut
    public Diagnostic() {
    }

    // Constructeur complet
    public Diagnostic(Integer idDiagnostic, Integer idConsultation, String codeIcd10, 
                      String descriptionDiagnostic, boolean estFinal) {
        this.idDiagnostic = idDiagnostic;
        this.idConsultation = idConsultation;
        this.codeIcd10 = codeIcd10;
        this.descriptionDiagnostic = descriptionDiagnostic;
        this.estFinal = estFinal;
    }

    // Getters & Setters
    public Integer getIdDiagnostic() {
        return idDiagnostic;
    }

    public void setIdDiagnostic(Integer idDiagnostic) {
        this.idDiagnostic = idDiagnostic;
    }

    public Integer getIdConsultation() {
        return idConsultation;
    }

    public void setIdConsultation(Integer idConsultation) {
        this.idConsultation = idConsultation;
    }

    public String getCodeIcd10() {
        return codeIcd10;
    }

    public void setCodeIcd10(String codeIcd10) {
        this.codeIcd10 = codeIcd10;
    }

    public String getDescriptionDiagnostic() {
        return descriptionDiagnostic;
    }

    public void setDescriptionDiagnostic(String descriptionDiagnostic) {
        this.descriptionDiagnostic = descriptionDiagnostic;
    }

    public boolean isEstFinal() {
        return estFinal;
    }

    public void setEstFinal(boolean estFinal) {
        this.estFinal = estFinal;
    }

    @Override
    public String toString() {
        return "Diagnostic{" +
                "id=" + idDiagnostic +
                ", code='" + codeIcd10 + '\'' +
                ", final=" + estFinal +
                '}';
    }
}