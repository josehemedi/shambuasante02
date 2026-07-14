package hospicloud.controlleurs;

import hospicloud.dtos.*;
import hospicloud.services.ConsultationMedicaleService;
import hospicloud.services.SaasPlanService;
import hospicloud.security.CurrentUserService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/consultations")
@CrossOrigin(origins = "*")
public class ConsultationMedicaleController {

    private final ConsultationMedicaleService service;
    private final CurrentUserService currentUserService;
    private final SaasPlanService saasPlanService;

    public ConsultationMedicaleController(ConsultationMedicaleService service,
                                          CurrentUserService currentUserService,
                                          SaasPlanService saasPlanService) {
        this.service = service;
        this.currentUserService = currentUserService;
        this.saasPlanService = saasPlanService;
    }

    @PostMapping
    public ResponseEntity<ConsultationResponseDTO> creer(@RequestBody ConsultationRequestDTO requestDTO) {
        return new ResponseEntity<>(service.creerConsultation(requestDTO), HttpStatus.CREATED);
    }

    @GetMapping("/patient/{idPatient}")
    public ResponseEntity<List<ConsultationResponseDTO>> obtenirHistorique(@PathVariable Integer idPatient) {
        return ResponseEntity.ok(service.obtenirHistoriquePatient(idPatient));
    }

    @GetMapping("/medecin/historique")
    public ResponseEntity<List<ConsultationResponseDTO>> obtenirHistoriqueMedecin() {
        Integer idMedecin = currentUserService.getCurrentMedecinId();
        return ResponseEntity.ok(service.obtenirHistoriqueMedecin(idMedecin));
    }

    @PutMapping("/{idConsultation}/clinique")
    public ResponseEntity<ConsultationResponseDTO> completer(
            @PathVariable Long idConsultation,
            @RequestBody Map<String, String> updates) {
        
        String observations = updates.get("observations");
        String diagnostic = updates.get("diagnostic");
        return ResponseEntity.ok(service.completerConsultation(idConsultation, observations, diagnostic));
    }

    @PutMapping("/{idConsultation}/constantes")
    public ResponseEntity<ConsultationResponseDTO> mettreAJourConstantes(
            @PathVariable Long idConsultation,
            @RequestBody ConsultationRequestDTO constantesDTO) {
        
        return ResponseEntity.ok(service.mettreAJourConstantes(idConsultation, constantesDTO));
    }

    @PostMapping("/teleconsultation/token")
    public ResponseEntity<LiveKitTokenResponse> genererTokenTeleconsultation(
            @RequestBody LiveKitTokenRequest request) {

        if (request.getIdRendezVous() == null) {
            throw new IllegalArgumentException("idRendezVous est requis");
        }

        Integer hopitalId = currentUserService.getCurrentHopitalId();
        if (hopitalId != null) {
            saasPlanService.assertTeleconsultationQuota(hopitalId);
        }

        return ResponseEntity.ok(service.genererTokenTeleconsultation(request.getIdRendezVous().longValue()));
    }

    @GetMapping("/rendezvous/{idRdv}")
    public ResponseEntity<ConsultationResponseDTO> obtenirParRdv(@PathVariable Integer idRdv) {
        ConsultationResponseDTO dto = service.obtenirParRdv(idRdv);
        if (dto == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(dto);
    }

    @PostMapping("/rendezvous/{idRdv}/fiche")
    public ResponseEntity<ConsultationResponseDTO> ouvrirFicheTeleconsultation(@PathVariable Integer idRdv) {
        return ResponseEntity.ok(service.obtenirOuCreerParRdv(idRdv));
    }

    @GetMapping("/{idConsultation}")
    public ResponseEntity<ConsultationResponseDTO> obtenirParId(@PathVariable Long idConsultation) {
        return ResponseEntity.ok(service.obtenirParId(idConsultation));
    }

    @PutMapping("/{idConsultation}/fiche")
    public ResponseEntity<ConsultationResponseDTO> enregistrerFiche(
            @PathVariable Long idConsultation,
            @RequestBody ConsultationFicheDTO fiche) {
        return ResponseEntity.ok(service.enregistrerFiche(idConsultation, fiche));
    }

    @GetMapping(value = "/{idConsultation}/pdf", produces = MediaType.APPLICATION_PDF_VALUE)
    public ResponseEntity<byte[]> telechargerFichePdf(@PathVariable Long idConsultation) {
        byte[] pdf = service.genererPdfFicheConsultation(idConsultation);
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "inline; filename=fiche_consultation_" + idConsultation + ".pdf")
                .body(pdf);
    }
}