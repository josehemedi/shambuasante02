package hospicloud.dtos;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalTime;

/**
 * DTO professionnel pour la création ou modification d'un horaire.
 * Hérite de BaseDto et maintient l'immuabilité pour la sécurité des données.
 */
public final class HoraireTravailRequestDTO extends BaseDto {

    @NotNull(message = "L'identifiant du médecin est obligatoire.")
    private final Integer medecinId;
    
    @NotBlank(message = "Le jour de la semaine est obligatoire.")
    @Size(min = 4, max = 10, message = "Le jour de la semaine doit faire entre 4 et 10 caractères.")
    private final String jourSemaine;
    
    @NotNull(message = "L'heure de début est obligatoire.")
    private final LocalTime heureDebut;
    
    @NotNull(message = "L'heure de fin est obligatoire.")
    private final LocalTime heureFin;
    
    @NotNull(message = "Le pas de consultation est obligatoire.")
    @Min(value = 5, message = "Le pas de consultation doit être d'au moins 5 minutes.")
    private final Integer pasConsultation;

    /**
     * Constructeur complet avec validation de la cohérence des heures.
     */
    public HoraireTravailRequestDTO(Integer medecinId, String jourSemaine, LocalTime heureDebut, LocalTime heureFin, Integer pasConsultation) {
        // Validation métier à l'instanciation
        if (heureDebut != null && heureFin != null && !heureFin.isAfter(heureDebut)) {
            throw new IllegalArgumentException("L'heure de fin doit être strictement après l'heure de début.");
        }
        
        this.medecinId = medecinId;
        this.jourSemaine = jourSemaine;
        this.heureDebut = heureDebut;
        this.heureFin = heureFin;
        this.pasConsultation = pasConsultation;
    }

    // --- Getters uniquement (Pas de setters pour garantir l'immuabilité comme un Record) ---

    public Integer getMedecinId() {
        return medecinId;
    }

    public String getJourSemaine() {
        return jourSemaine;
    }

    public LocalTime getHeureDebut() {
        return heureDebut;
    }

    public LocalTime getHeureFin() {
        return heureFin;
    }

    public Integer getPasConsultation() {
        return pasConsultation;
    }
}