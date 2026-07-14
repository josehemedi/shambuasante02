package hospicloud.controlleurs;

import hospicloud.dtos.reception.AdmissionDTO;
import hospicloud.dtos.reception.MedecinDisponibleDTO;
import hospicloud.dtos.reception.ReceptionDashboardStatsDTO;
import hospicloud.dtos.reception.ReceptionRegistrationPointDTO;
import hospicloud.dtos.reception.ReceptionRdvCreateDTO;
import hospicloud.dtos.reception.WalkInRegistrationRequestDTO;
import hospicloud.dtos.reception.WalkInRegistrationResponseDTO;
import hospicloud.model.RendezVous;
import hospicloud.model.reception.Admission;
import hospicloud.services.ReceptionDashboardService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/reception/dashboard")
public class ReceptionDashboardController {

    private final ReceptionDashboardService receptionService;

    public ReceptionDashboardController(ReceptionDashboardService receptionService) {
        this.receptionService = receptionService;
    }

    @GetMapping("/stats")
    public ResponseEntity<ReceptionDashboardStatsDTO> getStats() {
        return ResponseEntity.ok(receptionService.getDashboardStats());
    }

    @GetMapping("/file-attente")
    public ResponseEntity<List<AdmissionDTO>> getFileAttente() {
        return ResponseEntity.ok(receptionService.getFileAttente());
    }

    @GetMapping("/inscriptions-jour")
    public ResponseEntity<List<ReceptionRegistrationPointDTO>> getInscriptionsParHeure() {
        return ResponseEntity.ok(receptionService.getInscriptionsParHeure());
    }

    @GetMapping("/rendezvous")
    public ResponseEntity<List<RendezVous>> listerRendezVousDuJour() {
        return ResponseEntity.ok(receptionService.listerRendezVousDuJour());
    }

    @GetMapping("/medecins-disponibles")
    public ResponseEntity<List<MedecinDisponibleDTO>> listerMedecinsDisponibles(
            @RequestParam(required = false) String specialite,
            @RequestParam(required = false) String service,
            @RequestParam(defaultValue = "false") boolean uniquementEnHoraire) {
        String filtre = specialite != null && !specialite.isBlank() ? specialite : service;
        return ResponseEntity.ok(receptionService.listerMedecinsDisponibles(filtre, uniquementEnHoraire));
    }

    @GetMapping("/specialites")
    public ResponseEntity<List<String>> listerSpecialites() {
        return ResponseEntity.ok(receptionService.listerSpecialites());
    }

    @PostMapping("/arrivees")
    public ResponseEntity<WalkInRegistrationResponseDTO> enregistrerArrivee(
            @Valid @RequestBody WalkInRegistrationRequestDTO request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(receptionService.enregistrerArrivee(request));
    }

    @PostMapping("/rendezvous")
    public ResponseEntity<RendezVous> creerRendezVous(@Valid @RequestBody ReceptionRdvCreateDTO dto) {
        RendezVous created = receptionService.creerRendezVous(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PostMapping("/admissions/{idAdmission}/statut")
    public ResponseEntity<Void> updateStatut(
            @PathVariable Integer idAdmission,
            @RequestParam String nouveauStatut) {
        receptionService.changerStatutAdmission(idAdmission, nouveauStatut);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/admissions")
    public ResponseEntity<Void> inscrirePatient(
            @RequestBody Admission admission,
            @RequestParam(defaultValue = "false") boolean strictMode) {
        receptionService.inscrirePatientFileAttente(admission, strictMode);
        return ResponseEntity.ok().build();
    }
}
