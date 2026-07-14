package hospicloud.controlleurs;

import hospicloud.dtos.HopitalDto;
import hospicloud.dtos.HospitalActivityDTO;
import hospicloud.dtos.HospitalCreateDTO;
import hospicloud.dtos.HospitalDetailDTO;
import hospicloud.dtos.HospitalOverviewDTO;
import hospicloud.dtos.HospitalPlanCatalogDTO;
import hospicloud.dtos.HospitalPlatformStatsDTO;
import hospicloud.dtos.HospitalStatusUpdateDTO;
import hospicloud.dtos.HospitalUpdateDTO;
import hospicloud.dtos.mappers.HopitalMapper;
import hospicloud.model.Hopital;
import hospicloud.services.HospitalService;
import hospicloud.services.HopitalPlatformService;
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
import java.util.stream.Collectors;

@RestController
@RequestMapping(path = "/api/hopitaux", produces = MediaType.APPLICATION_JSON_VALUE)
@Validated
public class HopitalController {

    private static final Logger logger = LoggerFactory.getLogger(HopitalController.class);

    private final HospitalService hospitalService;
    private final HopitalMapper hopitalMapper;
    private final HopitalPlatformService hopitalPlatformService;

    @Autowired
    public HopitalController(HospitalService hospitalService,
                             HopitalMapper hopitalMapper,
                             HopitalPlatformService hopitalPlatformService) {
        this.hospitalService = hospitalService;
        this.hopitalMapper = hopitalMapper;
        this.hopitalPlatformService = hopitalPlatformService;
    }

    @PostMapping(path = "/register", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> registerHospital(@Valid @RequestBody HospitalCreateDTO dto) {
        try {
            HospitalOverviewDTO created = hopitalPlatformService.createHospital(dto);
            URI location = URI.create("/api/hopitaux/" + created.getIdHopital());
            return ResponseEntity.created(location).body(created);
        } catch (IllegalArgumentException | IllegalStateException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            logger.error("Erreur inscription hôpital", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Erreur lors de l'inscription de l'hôpital.");
        }
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> creerHopital(@Valid @RequestBody HopitalDto dto) {
        try {
            Hopital ent = hopitalMapper.toEntity(dto);
            hospitalService.enresgitrerHopital(ent);
            HopitalDto out = hopitalMapper.toDto(ent);
            URI location = URI.create("/api/hopitaux/" + out.getIdHopital());
            return ResponseEntity.created(location).body(out);
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            logger.error("Erreur création hôpital", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Erreur lors de la création de l'hôpital.");
        }
    }

    @PutMapping(path = "/{id}", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> modifierHopital(@PathVariable("id") Integer id, @RequestBody HopitalDto dto) {
        try {
            Hopital ent = hopitalMapper.toEntity(dto);
            ent.setIdHopital(id);
            hospitalService.modifier(ent);
            return ResponseEntity.ok(hopitalMapper.toDto(ent));
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
        } catch (Exception e) {
            logger.error("Erreur modification hôpital {}", id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @DeleteMapping(path = "/{id}")
    public ResponseEntity<?> supprimerHopital(@PathVariable("id") Integer id) {
        try {
            hospitalService.supprimer(id);
            return ResponseEntity.noContent().build();
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            logger.error("Erreur suppression hôpital {}", id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping
    public ResponseEntity<List<HopitalDto>> listerTous() {
        List<Hopital> list = hospitalService.listerTous();
        List<HopitalDto> dtos = list.stream().map(hopitalMapper::toDto).collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    @GetMapping(path = "/stats")
    public ResponseEntity<HospitalPlatformStatsDTO> getPlatformStats() {
        return ResponseEntity.ok(hopitalPlatformService.getPlatformStats());
    }

    @GetMapping(path = "/overview")
    public ResponseEntity<List<HospitalOverviewDTO>> listOverview() {
        return ResponseEntity.ok(hopitalPlatformService.listOverview());
    }

    @GetMapping(path = "/overview/{id}")
    public ResponseEntity<?> getOverviewById(@PathVariable("id") Integer id) {
        try {
            return ResponseEntity.ok(hopitalPlatformService.getHospitalDetail(id));
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }

    @PutMapping(path = "/{id}/platform", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> updateHospitalPlatform(@PathVariable("id") Integer id,
                                                    @Valid @RequestBody HospitalUpdateDTO dto) {
        try {
            HospitalDetailDTO updated = hopitalPlatformService.updateHospital(id, dto);
            return ResponseEntity.ok(updated);
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (IllegalArgumentException | IllegalStateException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            logger.error("Erreur mise à jour hôpital {}", id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Erreur lors de la mise à jour de l'hôpital.");
        }
    }

    @PatchMapping(path = "/{id}/status", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> updateHospitalStatus(@PathVariable("id") Integer id,
                                                  @Valid @RequestBody HospitalStatusUpdateDTO dto) {
        try {
            if (dto.getActive() == null) {
                return ResponseEntity.badRequest().body("Le champ active est requis.");
            }
            HospitalDetailDTO updated = hopitalPlatformService.setHospitalStatus(id, dto.getActive());
            return ResponseEntity.ok(updated);
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (IllegalArgumentException | IllegalStateException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            logger.error("Erreur changement statut hôpital {}", id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Erreur lors du changement de statut de l'hôpital.");
        }
    }

    @GetMapping(path = "/activity")
    public ResponseEntity<List<HospitalActivityDTO>> listActivity(
            @RequestParam(value = "limit", defaultValue = "20") int limit) {
        return ResponseEntity.ok(hopitalPlatformService.listRecentActivity(limit));
    }

    @GetMapping(path = "/plans")
    public ResponseEntity<List<HospitalPlanCatalogDTO>> listPlans() {
        return ResponseEntity.ok(hopitalPlatformService.listPlansCatalog());
    }

    @GetMapping(path = "/{id}")
    public ResponseEntity<?> trouverParId(@PathVariable("id") Integer id) {
        Hopital h = hospitalService.rechercherhopitalParId(id != null ? id.longValue() : null);
        if (h == null) return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        return ResponseEntity.ok(hopitalMapper.toDto(h));
    }

    @GetMapping(path = "/chercher")
    public ResponseEntity<?> rechercherParNom(@RequestParam(value = "nom", required = true) String nom) {
        if (nom == null || nom.trim().isEmpty()) return ResponseEntity.badRequest().body("Le paramètre 'nom' est requis.");
        Hopital h = hospitalService.rechercherParNom(nom.trim());
        if (h == null) return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Hôpital introuvable.");
        return ResponseEntity.ok(hopitalMapper.toDto(h));
    }

}