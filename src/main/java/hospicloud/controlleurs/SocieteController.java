package hospicloud.controlleurs;

import hospicloud.dtos.SocieteDTO;
import hospicloud.model.Societe;
import hospicloud.services.SocieteService;
import hospicloud.exceptions.ResourceNotFoundException;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping(path = "/api/societes", produces = MediaType.APPLICATION_JSON_VALUE)
@Validated
public class SocieteController {

    private static final Logger logger = LoggerFactory.getLogger(SocieteController.class);

    private final SocieteService societeService;

    @Autowired
    public SocieteController(SocieteService societeService) {
        this.societeService = societeService;
    }

    // =========================
    // MAPPERS
    // =========================

    private Societe toEntity(SocieteDTO dto) {
        if (dto == null) return null;

        Societe s = new Societe();
        s.setIdSociete(dto.getIdSociete());
        s.setNomSociete(dto.getNomSociete());
        s.setAdresseFacturation(dto.getAdresseFacturation());
        s.setTelephoneContact(dto.getTelephoneContact());
        s.setEmailContact(dto.getEmailContact());
        s.setTauxCouverture(dto.getTauxCouverture());
        return s;
    }

    private SocieteDTO toDTO(Societe s) {
        if (s == null) return null;

        return SocieteDTO.builder()
                .idSociete(s.getIdSociete())
                .nomSociete(s.getNomSociete())
                .adresseFacturation(s.getAdresseFacturation())
                .telephoneContact(s.getTelephoneContact())
                .emailContact(s.getEmailContact())
                .tauxCouverture(s.getTauxCouverture())
                .idHopital(s.getIdHopital())     
                .nomHopital(s.getNomHopital())
                .build();
    }

    // =========================
    // CREATE
    // =========================
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<SocieteDTO> creerSociete(@Valid @RequestBody SocieteDTO dto) {

        Societe entity = toEntity(dto);
        societeService.creerSociete(entity);

        logger.info("Société créée: {}", entity.getNomSociete());

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(toDTO(entity));
    }

    // =========================
    // UPDATE
    // =========================
    @PutMapping("/{id}")
    public ResponseEntity<SocieteDTO> modifierSociete(
            @PathVariable Long id,
            @Valid @RequestBody SocieteDTO dto) {

        Societe entity = toEntity(dto);
        entity.setIdSociete(id);

        societeService.mettreAJourSociete(entity);

        return ResponseEntity.ok(toDTO(entity));
    }

    // =========================
    // DELETE
    // =========================
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> supprimerSociete(@PathVariable Long id) {

        societeService.supprimerSociete(id);

        return ResponseEntity.noContent().build();
    }

    // =========================
    // GET BY ID
    // =========================
    @GetMapping("/{id}")
    public ResponseEntity<SocieteDTO> trouverParId(@PathVariable Long id) {

        return societeService.recupererParId(id)
                .map(s -> ResponseEntity.ok(toDTO(s)))
                .orElse(ResponseEntity.notFound().build());
    }

    // =========================
    // LIST (TENANT)
    // =========================
    @GetMapping
    public ResponseEntity<List<SocieteDTO>> listerParHopital() {

        List<SocieteDTO> result = societeService.listerParHopital()
                .stream()
                .map(this::toDTO)
                .toList();

        return ResponseEntity.ok(result);
    }

    // =========================
    // GLOBAL (ADMIN)
    // =========================
    @GetMapping("/admin/all")
    public ResponseEntity<List<SocieteDTO>> listerTout() {

        List<SocieteDTO> result = societeService.listerTout()
                .stream()
                .map(this::toDTO)
                .toList();

        return ResponseEntity.ok(result);
    }

    // =========================
    // SEARCH (TENANT SAFE)
    // =========================
    @GetMapping("/search")
    public ResponseEntity<?> rechercher(@RequestParam String nom) {

        return societeService.trouverParNom(nom)
                .map(s -> ResponseEntity.ok(toDTO(s)))
                .orElse(ResponseEntity.notFound().build());
    }
}   
