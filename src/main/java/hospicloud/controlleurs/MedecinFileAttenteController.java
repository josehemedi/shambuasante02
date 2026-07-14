package hospicloud.controlleurs;

import hospicloud.dtos.CommencerConsultationResponseDTO;
import hospicloud.dtos.MedecinFileItemDTO;
import hospicloud.dtos.WaitingRoomCallEventDTO;
import hospicloud.services.MedecinFileAttenteService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/medecin/file-attente")
public class MedecinFileAttenteController {

    private final MedecinFileAttenteService fileAttenteService;

    public MedecinFileAttenteController(MedecinFileAttenteService fileAttenteService) {
        this.fileAttenteService = fileAttenteService;
    }

    @GetMapping
    public ResponseEntity<List<MedecinFileItemDTO>> lister() {
        return ResponseEntity.ok(fileAttenteService.listerMaFile());
    }

    @PostMapping("/{idAdmission}/appeler")
    public ResponseEntity<WaitingRoomCallEventDTO> appeler(@PathVariable Integer idAdmission) {
        return ResponseEntity.ok(fileAttenteService.appelerPatient(idAdmission));
    }

    @PostMapping("/rdv/{idRdv}/appeler")
    public ResponseEntity<WaitingRoomCallEventDTO> appelerDepuisRdv(@PathVariable Integer idRdv) {
        return ResponseEntity.ok(fileAttenteService.appelerDepuisRendezVous(idRdv));
    }

    @PostMapping("/{idAdmission}/commencer")
    public ResponseEntity<CommencerConsultationResponseDTO> commencer(@PathVariable Integer idAdmission) {
        return ResponseEntity.ok(fileAttenteService.commencerConsultation(idAdmission));
    }
}
