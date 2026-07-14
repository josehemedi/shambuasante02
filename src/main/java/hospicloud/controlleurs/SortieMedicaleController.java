package hospicloud.controlleurs;

import hospicloud.dtos.sortie.AutoriserSortieRequestDTO;
import hospicloud.dtos.sortie.AutoriserSortieResponseDTO;
import hospicloud.dtos.sortie.ContexteSortieDTO;
import hospicloud.dtos.sortie.PretSortieDTO;
import hospicloud.services.SortieMedicaleService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/sortie")
public class SortieMedicaleController {

    private final SortieMedicaleService sortieMedicaleService;

    public SortieMedicaleController(SortieMedicaleService sortieMedicaleService) {
        this.sortieMedicaleService = sortieMedicaleService;
    }

    @GetMapping("/patient/{idPatient}/contexte")
    public ResponseEntity<ContexteSortieDTO> getContexte(@PathVariable Integer idPatient) {
        return ResponseEntity.ok(sortieMedicaleService.getContexteSortie(idPatient));
    }

    @PostMapping("/autoriser")
    public ResponseEntity<AutoriserSortieResponseDTO> autoriser(
            @Valid @RequestBody AutoriserSortieRequestDTO request) {
        AutoriserSortieResponseDTO response = sortieMedicaleService.autoriserSortieMedicale(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/pretes")
    public ResponseEntity<List<PretSortieDTO>> listerPretes() {
        return ResponseEntity.ok(sortieMedicaleService.listerPretesPourDelivrance());
    }

    @PostMapping("/{idBonSortie}/delivrer")
    public ResponseEntity<PretSortieDTO> delivrer(
            @PathVariable Integer idBonSortie,
            @RequestBody(required = false) Map<String, Boolean> body) {
        boolean paiementConfirme = body != null && Boolean.TRUE.equals(body.get("paiementConfirme"));
        return ResponseEntity.ok(sortieMedicaleService.delivrerBonSortie(idBonSortie, paiementConfirme));
    }
}
