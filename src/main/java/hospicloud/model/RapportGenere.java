package hospicloud.model;

import java.time.LocalDateTime;

/**
 * Entité traçant l'historique des documents PDF générés par le système.
 * Assure la traçabilité des impressions et permet la ré-impression rapide.
 * * Développé par Siku Hemedi Jose - Projet Hospicloud.
 */
public class RapportGenere {

    private Integer idRapport;
    private Integer idHopital;
    private Integer idUtilisateur;  // L'auteur de la génération
    private Integer idTemplate;     // Le modèle utilisé (ex: Ordonnance V1)
    private String nomFichierPdf;   // ex: Facture_FAC-2026-0001.pdf
    private String urlTelechargement; // Chemin vers le stockage (ex: S3 ou local)
    private LocalDateTime dateGeneration;

    // Constructeur par défaut
    public RapportGenere() {
    }

    // Constructeur complet
    public RapportGenere(Integer idRapport, Integer idHopital, Integer idUtilisateur, 
                         Integer idTemplate, String nomFichierPdf, 
                         String urlTelechargement, LocalDateTime dateGeneration) {
        this.idRapport = idRapport;
        this.idHopital = idHopital;
        this.idUtilisateur = idUtilisateur;
        this.idTemplate = idTemplate;
        this.nomFichierPdf = nomFichierPdf;
        this.urlTelechargement = urlTelechargement;
        this.dateGeneration = dateGeneration;
    }

    // Getters & Setters
    public Integer getIdRapport() { return idRapport; }
    public void setIdRapport(Integer idRapport) { this.idRapport = idRapport; }

    public Integer getIdHopital() { return idHopital; }
    public void setIdHopital(Integer idHopital) { this.idHopital = idHopital; }

    public Integer getIdUtilisateur() { return idUtilisateur; }
    public void setIdUtilisateur(Integer idUtilisateur) { this.idUtilisateur = idUtilisateur; }

    public Integer getIdTemplate() { return idTemplate; }
    public void setIdTemplate(Integer idTemplate) { this.idTemplate = idTemplate; }

    public String getNomFichierPdf() { return nomFichierPdf; }
    public void setNomFichierPdf(String nomFichierPdf) { this.nomFichierPdf = nomFichierPdf; }

    public String getUrlTelechargement() { return urlTelechargement; }
    public void setUrlTelechargement(String urlTelechargement) { this.urlTelechargement = urlTelechargement; }

    public LocalDateTime getDateGeneration() { return dateGeneration; }
    public void setDateGeneration(LocalDateTime dateGeneration) { this.dateGeneration = dateGeneration; }

    @Override
    public String toString() {
        return "RapportGenere{" +
                "id=" + idRapport +
                ", fichier='" + nomFichierPdf + '\'' +
                ", date=" + dateGeneration +
                '}';
    }
}