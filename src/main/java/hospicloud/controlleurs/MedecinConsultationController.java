package hospicloud.controlleurs;

import hospicloud.dtos.SignatureConsultationResponseDTO;
import hospicloud.dtos.SignerConsultationRequestDTO;
import hospicloud.services.ConsultationSignatureService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/medecin/consultations")
@CrossOrigin(origins = "*")
public class MedecinConsultationController {

    private final ConsultationSignatureService consultationSignatureService;

    public MedecinConsultationController(ConsultationSignatureService consultationSignatureService) {
        this.consultationSignatureService = consultationSignatureService;
    }

    @PostMapping("/{consultationId}/signer")
    public ResponseEntity<SignatureConsultationResponseDTO> signerConsultation(
            @PathVariable Long consultationId,
            @Valid @RequestBody SignerConsultationRequestDTO request,
            HttpServletRequest httpRequest) {
        return ResponseEntity.ok(
                consultationSignatureService.signerConsultation(consultationId, request, httpRequest));
    }
}
