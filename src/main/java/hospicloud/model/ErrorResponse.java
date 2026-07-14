package hospicloud.model;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ErrorResponse {
	private LocalDateTime horodatage;
    private int statut;
    private String erreur;
    private String message;
    private String chemin;

}
