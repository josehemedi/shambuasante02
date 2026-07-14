package hospicloud.controlleurs;

import hospicloud.dtos.MedecinDemandeAnalyseRequestDTO;
import hospicloud.dtos.MedecinDemandeAnalyseResponseDTO;
import hospicloud.services.MedecinLabService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/medecin/laboratoire/demandes")
public class MedecinLabController {

    private final MedecinLabService medecinLabService;

    public MedecinLabController(MedecinLabService medecinLabService) {
        this.medecinLabService = medecinLabService;
    }

    @GetMapping
    public ResponseEntity<List<MedecinDemandeAnalyseResponseDTO>> lister() {
        return ResponseEntity.ok(medecinLabService.listerMesDemandes());
    }

    @PostMapping
    public ResponseEntity<MedecinDemandeAnalyseResponseDTO> creer(
            @RequestBody MedecinDemandeAnalyseRequestDTO request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(medecinLabService.creerDemande(request));
    }
}
