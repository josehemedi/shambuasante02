package hospicloud.model;

import java.time.LocalDateTime;

/**
 * Entité gérant les pièces jointes et documents numérisés du dossier patient.
 * Permet de centraliser les radios, analyses et documents externes.
 * * Développé par Siku Hemedi Jose - Projet Hospicloud.
 */
public class PatientDocument {

    private Integer idDocument;
    private Integer idPatient; // Clé étrangère vers la table patients
    private String nomFichier;
    private String typeDocument; // Ex: Radio, Analyse PDF, Scanner
    private String urlFichier;   // Chemin d'accès au fichier stocké
    private LocalDateTime dateUpload;

    // Constructeur par défaut
    public PatientDocument() {
    }

    // Constructeur complet
    public PatientDocument(Integer idDocument, Integer idPatient, String nomFichier, 
                          String typeDocument, String urlFichier, LocalDateTime dateUpload) {
        this.idDocument = idDocument;
        this.idPatient = idPatient;
        this.nomFichier = nomFichier;
        this.typeDocument = typeDocument;
        this.urlFichier = urlFichier;
        this.dateUpload = dateUpload;
    }

    // Getters & Setters
    public Integer getIdDocument() {
        return idDocument;
    }

    public void setIdDocument(Integer idDocument) {
        this.idDocument = idDocument;
    }

    public Integer getIdPatient() {
        return idPatient;
    }

    public void setIdPatient(Integer idPatient) {
        this.idPatient = idPatient;
    }

    public String getNomFichier() {
        return nomFichier;
    }

    public void setNomFichier(String nomFichier) {
        this.nomFichier = nomFichier;
    }

    public String getTypeDocument() {
        return typeDocument;
    }

    public void setTypeDocument(String typeDocument) {
        this.typeDocument = typeDocument;
    }

    public String getUrlFichier() {
        return urlFichier;
    }

    public void setUrlFichier(String urlFichier) {
        this.urlFichier = urlFichier;
    }

    public LocalDateTime getDateUpload() {
        return dateUpload;
    }

    public void setDateUpload(LocalDateTime dateUpload) {
        this.dateUpload = dateUpload;
    }

    @Override
    public String toString() {
        return "PatientDocument{" +
                "id=" + idDocument +
                ", nom='" + nomFichier + '\'' +
                ", type='" + typeDocument + '\'' +
                '}';
    }
}