package hospicloud.controlleurs;

import hospicloud.dtos.DocumentEnvoiResponse;
import hospicloud.services.MedecinPatientShareService;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping(path = "/api/medecin/partages", produces = MediaType.APPLICATION_JSON_VALUE)
public class MedecinPatientShareController {

    private final MedecinPatientShareService shareService;

    public MedecinPatientShareController(MedecinPatientShareService shareService) {
        this.shareService = shareService;
    }

    /** Envoie un résultat de laboratoire au patient concerné. */
    @PostMapping("/lab/{idAnalyse}/envoyer")
    public ResponseEntity<DocumentEnvoiResponse> envoyerLabo(@PathVariable Integer idAnalyse) {
        return ResponseEntity.ok(shareService.envoyerResultatLabo(idAnalyse));
    }

    /** Envoie la fiche PDF d'une consultation au patient. */
    @PostMapping("/consultations/{idConsultation}/envoyer")
    public ResponseEntity<DocumentEnvoiResponse> envoyerConsultation(@PathVariable Long idConsultation) {
        return ResponseEntity.ok(shareService.envoyerFicheConsultation(idConsultation));
    }

    /** Upload + partage d'un document médical avec le patient. */
    @PostMapping(path = "/documents", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<DocumentEnvoiResponse> envoyerDocument(
            @RequestParam("idPatient") Integer idPatient,
            @RequestParam(value = "typeDocument", required = false) String typeDocument,
            @RequestParam(value = "titre", required = false) String titre,
            @RequestPart("fichier") MultipartFile fichier) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(shareService.envoyerDocumentFichier(idPatient, typeDocument, titre, fichier));
    }
}
