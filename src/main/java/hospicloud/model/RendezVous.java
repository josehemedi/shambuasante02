package hospicloud.model;

import jakarta.validation.constraints.*;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Modèle RendezVous sans Lombok
 */
public class RendezVous {

    @JsonProperty("idRdv")
    private Integer idRdv;

    private Integer idHopital;

    @NotNull(message = "Le patient est obligatoire")
    private Integer idPatient;

    @NotNull(message = "Le médecin est obligatoire")
    private Integer idMedecin;

    // =========================
    // 🧠 AJOUT POUR EMAIL
    // =========================

    private String emailMedecin;
    private String nomMedecin;
    private String nomPatient;

    // --- Détails temporels ---
    @NotNull(message = "La date et l'heure du rendez-vous sont obligatoires")
    @Future(message = "Le rendez-vous doit être une date future")
    private LocalDateTime dateHeureRdv;

    @Min(value = 5, message = "La durée minimale est de 5 minutes")
    @Max(value = 480, message = "La durée ne peut pas dépasser 8 heures")
    private Integer dureeEstimee;

    // --- Détails métier ---
    @NotBlank(message = "Le motif de la visite est requis")
    @Size(max = 255, message = "Le motif ne doit pas dépasser 255 caractères")
    private String motifVisite;

    @Pattern(regexp = "PHYSIQUE|TELECONSULTATION", message = "Le canal doit être PHYSIQUE ou TELECONSULTATION")
    private String canal;

    // --- Workflow ---
    @Pattern(regexp = "PROGRAMME|CONFIRME|ANNULE|VALIDE|ABSENT",
            message = "Le statut est invalide")
    private String statutRdv;

    private String urlVisio;

    @Size(max = 50, message = "Nom de salle invalide")
    private String sallePhysique;

    // --- Traçabilité ---
    @PastOrPresent(message = "La date de création ne peut pas être dans le futur")
    private LocalDateTime dateCreation;

    private Integer creePar;

    // Constructeurs
    public RendezVous() {
    }

    public RendezVous(Integer idRdv, Integer idHopital, Integer idPatient, Integer idMedecin,
                      LocalDateTime dateHeureRdv, Integer dureeEstimee, String motifVisite,
                      String canal, String statutRdv, String urlVisio, String sallePhysique,
                      LocalDateTime dateCreation, Integer creePar,
                      String emailMedecin, String nomMedecin, String nomPatient) {

        this.idRdv = idRdv;
        this.idHopital = idHopital;
        this.idPatient = idPatient;
        this.idMedecin = idMedecin;
        this.dateHeureRdv = dateHeureRdv;
        this.dureeEstimee = dureeEstimee;
        this.motifVisite = motifVisite;
        this.canal = canal;
        this.statutRdv = statutRdv;
        this.urlVisio = urlVisio;
        this.sallePhysique = sallePhysique;
        this.dateCreation = dateCreation;
        this.creePar = creePar;

        // 🧠 nouveaux champs email
        this.emailMedecin = emailMedecin;
        this.nomMedecin = nomMedecin;
        this.nomPatient = nomPatient;
    }

    // =========================
    // GETTERS & SETTERS
    // =========================

    public Integer getIdRdv() { return idRdv; }
    public void setIdRdv(Integer idRdv) { this.idRdv = idRdv; }

    public Integer getIdHopital() { return idHopital; }
    public void setIdHopital(Integer idHopital) { this.idHopital = idHopital; }

    public Integer getIdPatient() { return idPatient; }
    public void setIdPatient(Integer idPatient) { this.idPatient = idPatient; }

    public Integer getIdMedecin() { return idMedecin; }
    public void setIdMedecin(Integer idMedecin) { this.idMedecin = idMedecin; }

    public LocalDateTime getDateHeureRdv() { return dateHeureRdv; }
    public void setDateHeureRdv(LocalDateTime dateHeureRdv) { this.dateHeureRdv = dateHeureRdv; }

    public Integer getDureeEstimee() { return dureeEstimee; }
    public void setDureeEstimee(Integer dureeEstimee) { this.dureeEstimee = dureeEstimee; }

    public String getMotifVisite() { return motifVisite; }
    public void setMotifVisite(String motifVisite) { this.motifVisite = motifVisite; }

    public String getCanal() { return canal; }
    public void setCanal(String canal) { this.canal = canal; }

    public String getStatutRdv() { return statutRdv; }
    public void setStatutRdv(String statutRdv) { this.statutRdv = statutRdv; }

    public String getUrlVisio() { return urlVisio; }
    public void setUrlVisio(String urlVisio) { this.urlVisio = urlVisio; }

    public String getSallePhysique() { return sallePhysique; }
    public void setSallePhysique(String sallePhysique) { this.sallePhysique = sallePhysique; }

    public LocalDateTime getDateCreation() { return dateCreation; }
    public void setDateCreation(LocalDateTime dateCreation) { this.dateCreation = dateCreation; }

    public Integer getCreePar() { return creePar; }
    public void setCreePar(Integer creePar) { this.creePar = creePar; }

    // =========================
    // 🧠 NOUVEAUX GETTERS EMAIL
    // =========================

    public String getEmailMedecin() { return emailMedecin; }
    public void setEmailMedecin(String emailMedecin) { this.emailMedecin = emailMedecin; }

    public String getNomMedecin() { return nomMedecin; }
    public void setNomMedecin(String nomMedecin) { this.nomMedecin = nomMedecin; }

    public String getNomPatient() { return nomPatient; }
    public void setNomPatient(String nomPatient) { this.nomPatient = nomPatient; }
}