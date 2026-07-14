package hospicloud.controlleurs;

import hospicloud.dtos.MedecinRequest;
import hospicloud.dtos.MedecinResponse;
import hospicloud.dtos.StatistiqueMedecinDTO;
import hospicloud.security.CurrentUserService;
import hospicloud.services.MedecinService;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/medecins")
public class MedecinController {

    private final MedecinService medecinService;
    private final CurrentUserService currentUserService;

    public MedecinController(MedecinService medecinService,CurrentUserService currentUserService) {
        this.medecinService = medecinService;
        this.currentUserService=currentUserService;
        
    }

    // =========================
    // CREATE
    // =========================
    @PostMapping
    public ResponseEntity<String> creer(@Valid @RequestBody MedecinRequest request) {

        medecinService.creer(request);
        return ResponseEntity.status(201).body("Médecin créé avec succès.");
    }

    // =========================
    // GET BY ID
    // =========================
    @GetMapping("/{id}")
    public ResponseEntity<MedecinResponse> trouverParId(@PathVariable Integer id) {

        return medecinService.trouverParId(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // =========================
    // LIST BY HOPITAL (TENANT AUTO)
    // =========================
    @GetMapping
    public ResponseEntity<List<MedecinResponse>> listerParHopital() {

        return ResponseEntity.ok(medecinService.listerParHopital());
    }

    // =========================
    // UPDATE
    // =========================
    @PutMapping("/{id}")
    public ResponseEntity<MedecinResponse> mettreAJour(
            @PathVariable Integer id,
            @Valid @RequestBody MedecinRequest request) {

        return ResponseEntity.ok(
                medecinService.mettreAJour(id, request)
        );
    }

    // =========================
    // CHANGE DISPONIBILITE
    // =========================
    @PatchMapping("/{id}/disponibilite")
    public ResponseEntity<String> changerDisponibilite(
            @PathVariable Integer id,
            @RequestParam Boolean status) {

        medecinService.changerDisponibilite(id, status);
        return ResponseEntity.ok("Disponibilité mise à jour avec succès.");
    }
    @GetMapping("/{id}/profil")
    public ResponseEntity<MedecinResponse> profilMedecin(@PathVariable Integer id) {

        return medecinService.trouverParId(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    
// =====================================================
    // ENDPOINTS STATISTIQUES (Nouveauté)
    // =====================================================
    @GetMapping("/dashboard/stats")
    public ResponseEntity<StatistiqueMedecinDTO> getStats() {
        Integer idMedecin = currentUserService.getCurrentMedecinId();
        StatistiqueMedecinDTO stats = medecinService.getDashboardStats(idMedecin);
        return ResponseEntity.ok(stats);
    }

    @GetMapping("/dashboard/patients")
    public ResponseEntity<Long> getNombrePatients() {
        Integer idMedecin = currentUserService.getCurrentMedecinId();
        Integer idHopital = currentUserService.getCurrentHopitalId();
        return ResponseEntity.ok(medecinService.getNombrePatients(idMedecin, idHopital));
    }

    @GetMapping("/dashboard/consultations")
    public ResponseEntity<Long> getConsultationsAujourdhui() {
        Integer idMedecin = currentUserService.getCurrentMedecinId();
        Integer idHopital = currentUserService.getCurrentHopitalId();
        return ResponseEntity.ok(medecinService.getConsultationsAujourdhui(idMedecin, idHopital));
    }

    @GetMapping("/dashboard/rendezvous")
    public ResponseEntity<Long> getRendezVousAujourdhui() {
        Integer idMedecin = currentUserService.getCurrentMedecinId();
        Integer idHopital = currentUserService.getCurrentHopitalId();
        return ResponseEntity.ok(medecinService.getRendezVousAujourdhui(idMedecin, idHopital));
    }

    @GetMapping("/dashboard/hospitalisations")
    public ResponseEntity<Long> getHospitalisationsEncours() {
        Integer idMedecin = currentUserService.getCurrentMedecinId();
        Integer idHopital = currentUserService.getCurrentHopitalId();
        return ResponseEntity.ok(medecinService.getHospitalisationsEncours(idMedecin, idHopital));
    }

    @GetMapping("/dashboard/examens")
    public ResponseEntity<Long> getExamensEnAttente() {
        Integer idMedecin = currentUserService.getCurrentMedecinId();
        Integer idHopital = currentUserService.getCurrentHopitalId();
        return ResponseEntity.ok(medecinService.getExamensEnAttente(idMedecin, idHopital));
    }

    @GetMapping("/dashboard/notifications")
    public ResponseEntity<Long> getNotificationsNonLues() {
        Integer idMedecin = currentUserService.getCurrentMedecinId();
        Integer idHopital = currentUserService.getCurrentHopitalId();
        return ResponseEntity.ok(medecinService.getNotificationsNonLues(idMedecin, idHopital));
    }
}