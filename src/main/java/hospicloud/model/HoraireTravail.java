package hospicloud.model;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

import java.time.LocalTime;

public class HoraireTravail {

    private Long id;

    @NotNull(message = "L'identifiant du médecin est obligatoire")
    private Integer medecinId;

    private Integer hopitalId;

    @NotBlank(message = "Le jour de la semaine est obligatoire")
    private String jourSemaine;

    @NotNull(message = "L'heure de début est obligatoire")
    private LocalTime heureDebut;

    @NotNull(message = "L'heure de fin est obligatoire")
    private LocalTime heureFin;

    @Min(value = 5, message = "Le pas de consultation doit être au moins de 5 minutes")
    @Max(value = 180, message = "Le pas de consultation ne peut pas dépasser 180 minutes")
    private int pasConsultation;
    @NotNull(message = "Le type Autorise est obligatoire")
    @Pattern(regexp = "PRESENTIEL|TELECONSULTATION|LES_DEUX", 
    message = "Le type doit être : PRESENTIEL, TELECONSULTATION ou LES_DEUX")
    private String typeAutorise = "Les Deux";

    // --- Constructeurs ---

    public HoraireTravail() {
    }

    public HoraireTravail(Long id,
                           Integer medecinId,
                           Integer hopitalId,
                           String jourSemaine,
                           LocalTime heureDebut,
                           LocalTime heureFin,
                           int pasConsultation,String typeAutorise ) {
        this.id = id;
        this.medecinId = medecinId;
        this.hopitalId = hopitalId;
        this.jourSemaine = jourSemaine;
        this.heureDebut = heureDebut;
        this.heureFin = heureFin;
        this.pasConsultation = pasConsultation;
        this.typeAutorise=typeAutorise;
    }

    // --- Getters et Setters ---

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Integer getMedecinId() {
        return medecinId;
    }

    public void setMedecinId(Integer medecinId) {
        this.medecinId = medecinId;
    }

    public Integer getHopitalId() {
        return hopitalId;
    }

    public void setHopitalId(Integer hopitalId) {
        this.hopitalId = hopitalId;
    }

    public String getJourSemaine() {
        return jourSemaine;
    }

    public void setJourSemaine(String jourSemaine) {
        this.jourSemaine = jourSemaine;
    }

    public LocalTime getHeureDebut() {
        return heureDebut;
    }

    public void setHeureDebut(LocalTime heureDebut) {
        this.heureDebut = heureDebut;
    }

    public LocalTime getHeureFin() {
        return heureFin;
    }

    public void setHeureFin(LocalTime heureFin) {
        this.heureFin = heureFin;
    }

    public int getPasConsultation() {
        return pasConsultation;
    }

    public void setPasConsultation(int pasConsultation) {
        this.pasConsultation = pasConsultation;
    }

	public String getTypeAutorise() {
		return typeAutorise;
	}

	public void setTypeAutorise(String typeAutorise) {
		this.typeAutorise = typeAutorise;
	}
    
    
}