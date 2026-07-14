package hospicloud.dtos;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ConsultationDto extends BaseDto {

    private Integer idConsultation;
    
    @NotNull(message = "L'ID de l'hôpital est obligatoire")
    private Integer idHopital; // Ajouté pour l'isolation SaaS

    private Integer idRdv;

    @NotNull(message = "Le patient est obligatoire")
    private Integer idPatient;

    @NotNull(message = "Le médecin est obligatoire")
    private Integer idMedecin;

    @PastOrPresent(message = "La date ne peut pas être dans le futur")
    private LocalDateTime dateConsultation;

    // --- Signes vitaux ---
    @DecimalMin(value = "0.5")
    private BigDecimal poids;

    @DecimalMin(value = "30.0")
    private BigDecimal temperature;

    @Pattern(regexp = "^\\d{2,3}/\\d{2,3}$", message = "Format tension invalide (ex: 12/8)")
    private String tensionArterielle;

    private Integer frequenceCardiaque;
    
    private BigDecimal glycemie; // Ajouté pour correspondre au Model

    // --- Contenu médical ---
    private String motifDetaille; // Harmonisé avec le Model
    
    @NotBlank(message = "Le diagnostic est requis")
    private String diagnostic;    // Harmonisé avec le Model
    
    private String observations;  // Harmonisé avec le Model (remplace observationGenerale)

    private String statutConsultation;
}