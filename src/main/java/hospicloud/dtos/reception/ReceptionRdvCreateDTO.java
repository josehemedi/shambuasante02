package hospicloud.dtos.reception;

import jakarta.validation.constraints.*;
import java.time.LocalDateTime;

/**
 * Création d'un rendez-vous réception — aligné sur la table rendez_vous01.
 */
public class ReceptionRdvCreateDTO {

    @NotNull(message = "Le patient est obligatoire")
    private Integer idPatient;

    @NotNull(message = "Le médecin est obligatoire")
    private Integer idMedecin;

    @NotNull(message = "La date et l'heure sont obligatoires")
    private LocalDateTime dateHeureRdv;

    @Min(5)
    @Max(480)
    private Integer dureeEstimee = 30;

    @NotBlank(message = "Le motif de visite est obligatoire")
    @Size(max = 255)
    private String motifVisite;

    @Pattern(regexp = "PHYSIQUE|TELECONSULTATION")
    private String canal = "PHYSIQUE";

    @Pattern(regexp = "PROGRAMME|CONFIRME|ANNULE|VALIDE|ABSENT")
    private String statutRdv = "PROGRAMME";

    private Integer creePar;

    /** Inscrire le patient en file d'attente (table admission) liée au RDV. */
    private boolean inscrireFileAttente = true;

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

    public Integer getCreePar() { return creePar; }
    public void setCreePar(Integer creePar) { this.creePar = creePar; }

    public boolean isInscrireFileAttente() { return inscrireFileAttente; }
    public void setInscrireFileAttente(boolean inscrireFileAttente) { this.inscrireFileAttente = inscrireFileAttente; }
}
