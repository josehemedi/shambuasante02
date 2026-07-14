package hospicloud.controlleurs;

import hospicloud.dtos.LabResultatSubmitDTO;
import hospicloud.dtos.MedecinDemandeAnalyseResponseDTO;
import hospicloud.services.LaborantinLabService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/lab/analyses")
public class LaborantinLabController {

    private final LaborantinLabService laborantinLabService;

    public LaborantinLabController(LaborantinLabService laborantinLabService) {
        this.laborantinLabService = laborantinLabService;
    }

    @GetMapping
    public ResponseEntity<List<MedecinDemandeAnalyseResponseDTO>> lister() {
        return ResponseEntity.ok(laborantinLabService.listerFileHopital());
    }

    @PutMapping("/{idAnalyse}/resultat")
    public ResponseEntity<MedecinDemandeAnalyseResponseDTO> soumettre(
            @PathVariable Integer idAnalyse,
            @RequestBody LabResultatSubmitDTO request) {
        return ResponseEntity.ok(laborantinLabService.soumettreResultat(idAnalyse, request));
    }
}
