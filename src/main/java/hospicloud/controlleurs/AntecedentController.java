package hospicloud.controlleurs;

import hospicloud.model.Antecedent;
import hospicloud.services.AntecedentService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

@RestController
@RequestMapping(path = "/api/antecedents", produces = MediaType.APPLICATION_JSON_VALUE)
public class AntecedentController {

    private static final Logger logger = LoggerFactory.getLogger(AntecedentController.class);
    private final AntecedentService antecedentService;

    @Autowired
    public AntecedentController(AntecedentService antecedentService) {
        this.antecedentService = antecedentService;
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> ajouter(@Valid @RequestBody Antecedent antecedent) {
        try {
            antecedentService.ajouterAntecedent(antecedent);
            return ResponseEntity.status(HttpStatus.CREATED).body(antecedent);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            logger.error("Erreur lors de l'ajout", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> modifier(@PathVariable int id, @RequestBody Antecedent antecedent) {
        antecedent.setIdAntecendent(id);
        antecedentService.mettreAJourAntecedent(antecedent);
        return ResponseEntity.ok(antecedent);
    }

    @GetMapping("/patient/{idPatient}")
    public ResponseEntity<List<Antecedent>> listerParPatient(
            @PathVariable int idPatient,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        // Plus besoin de passer idHopital, le service le récupère du contexte
        return ResponseEntity.ok(antecedentService.recupererDossierPatient(idPatient, page, size));
    }

    @GetMapping("/patient/{idPatient}/synthese")
    public ResponseEntity<List<Antecedent>> obtenirSynthese(@PathVariable int idPatient) {
        return ResponseEntity.ok(antecedentService.genererSyntheseMedicale(idPatient));
    }

    @PatchMapping("/{id}/basculer-statut")
    public ResponseEntity<?> basculerStatut(@PathVariable int id) {
        antecedentService.basculerStatut(id);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> supprimer(@PathVariable int id) {
        antecedentService.retirerAntecedent(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Antecedent> trouverParId(@PathVariable int id) {
        return antecedentService.trouverParId(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}