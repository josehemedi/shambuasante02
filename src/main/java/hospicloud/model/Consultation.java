package hospicloud.model;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Consultation {

    private Integer idConsultation;

    @NotNull(message = "L'ID de l'hôpital est obligatoire")
    private Integer idHopital;

    @NotNull(message = "L'ID du patient est obligatoire")
    private Integer idPatient;

    @NotNull(message = "L'ID du médecin est obligatoire")
    private Integer idMedecin;

    private Integer idRdv; // Peut être null si consultation sans RDV

    // --- PHASE 1 : PRÉ-CONSULTATION (Signes vitaux) ---
    
    @DecimalMin(value = "0.5", message = "Le poids doit être supérieur à 0.5kg")
    @DecimalMax(value = "500.0", message = "Le poids semble incorrect")
    private BigDecimal poids;

    @DecimalMin(value = "30.0", message = "La température est trop basse")
    @DecimalMax(value = "45.0", message = "La température est trop élevée")
    private BigDecimal temperature;

    @Pattern(regexp = "^\\d{2,3}/\\d{2,3}$", message = "La tension doit être au format '12/8' ou '120/80'")
    private String tensionArterielle;

    @Min(value = 30, message = "Fréquence cardiaque trop basse")
    @Max(value = 250, message = "Fréquence cardiaque trop élevée")
    private Integer frequenceCardiaque;

    @DecimalMin(value = "0.1", message = "La glycémie doit être positive")
    private BigDecimal glycemie;

    // --- PHASE 2 : CONSULTATION MÉDICALE ---
    
    @Size(max = 1000, message = "Le motif est trop long")
    private String motifDetaille;

    @NotBlank(message = "Le diagnostic ne peut pas être vide lors de la clôture")
    private String diagnostic;

    private String observations;

    @PastOrPresent(message = "La date de consultation ne peut pas être dans le futur")
    private LocalDateTime dateConsultation;

    // --- GESTION DU FLUX ---
    
    @Pattern(regexp = "EN_ATTENTE|EN_COURS|TERMINE|ARCHIVE", 
             message = "Le statut de consultation est invalide")
    private String statutConsultation;
}