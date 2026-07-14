package hospicloud.controlleurs;

import hospicloud.dtos.FactureRequestDto;
import hospicloud.dtos.FactureResponseDto;
import hospicloud.services.AsyncReportGateway;
import hospicloud.services.FactureService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/factures")
public class FactureController {

    private final FactureService factureService;
    private final AsyncReportGateway asyncReportGateway;

    @Autowired
    public FactureController(FactureService factureService, AsyncReportGateway asyncReportGateway) {
        this.factureService = factureService;
        this.asyncReportGateway = asyncReportGateway;
    }

    @PostMapping
    public ResponseEntity<FactureResponseDto> creer(@RequestBody FactureRequestDto requestDto) {
        FactureResponseDto created = factureService.creerFacture(requestDto);
        return new ResponseEntity<>(created, HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    public ResponseEntity<FactureResponseDto> obtenirParId(@PathVariable Integer id) {
        return ResponseEntity.ok(factureService.obtenirParId(id));
    }

    @GetMapping("/numero/{numero}")
    public ResponseEntity<FactureResponseDto> obtenirParNumero(@PathVariable String numero) {
        return ResponseEntity.ok(factureService.obtenirParNumero(numero));
    }

    @GetMapping("/patient/{idPatient}")
    public ResponseEntity<List<FactureResponseDto>> listerParPatient(@PathVariable Integer idPatient) {
        return ResponseEntity.ok(factureService.listerFacturesDuPatient(idPatient));
    }

    @GetMapping
    public ResponseEntity<List<FactureResponseDto>> listerToutes() {
        return ResponseEntity.ok(factureService.listerFacturesDeLHopital());
    }

    @GetMapping("/statut/{statut}")
    public ResponseEntity<List<FactureResponseDto>> listerParStatut(@PathVariable String statut) {
        return ResponseEntity.ok(factureService.listerParStatut(statut));
    }

    @PatchMapping("/{idFacture}/statut")
    public ResponseEntity<FactureResponseDto> mettreAJourStatut(
            @PathVariable Integer idFacture,
            @RequestParam String nouveauStatut) {
        return ResponseEntity.ok(factureService.mettreAJourStatut(idFacture, nouveauStatut));
    }

    @PostMapping("/{idFacture}/generer-pdf")
    public ResponseEntity<?> demanderGenerationPdf(@PathVariable Integer idFacture) {
        return asyncReportGateway.submit(
                hospicloud.async.AsyncJobType.REPORT_FACTURE,
                idFacture.longValue(),
                java.util.Map.of("idFacture", idFacture));
    }
}