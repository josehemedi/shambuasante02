package hospicloud.controlleurs;

import hospicloud.exceptions.ForbiddenException;
import hospicloud.model.RendezVous;
import hospicloud.services.RendezVousService;
import hospicloud.security.CurrentUserService;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/rendezvous")
public class RendezVousController {

    private final RendezVousService rendezVousService;
    private final CurrentUserService currentUserService;

    public RendezVousController(RendezVousService rendezVousService, CurrentUserService currentUserService) {
        this.rendezVousService = rendezVousService;
        this.currentUserService = currentUserService;
    }

    // =====================================================
    // CRÉATION RDV
    // =====================================================
    @PostMapping
    public ResponseEntity<RendezVous> creerRendezVous(
            @Valid @RequestBody RendezVous rendezVous) {

        if (rendezVous.getCreePar() == null) {
            Integer utilisateurId = currentUserService.getCurrentUtilisateurId();
            if (utilisateurId != null) {
                rendezVous.setCreePar(utilisateurId);
            }
        }

        RendezVous saved = rendezVousService.creerEtPublier(rendezVous);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(saved);
    }

    @GetMapping
    public ResponseEntity<List<RendezVous>> listerParHopital(
            @RequestParam(value = "mine", required = false) Boolean mine) {
        return ResponseEntity.ok(rendezVousService.listerParHopital(mine));
    }

    // =====================================================
    // CONSULTATIONS ET LECTURE
    // =====================================================
    @GetMapping("/{id}")
    public ResponseEntity<RendezVous> obtenirParId(@PathVariable Integer id) {
        RendezVous rdv = rendezVousService.obtenirParId(id);
        return ResponseEntity.ok(rdv);
    }

    @GetMapping("/jour")
    public ResponseEntity<List<RendezVous>> listerDuJour() {
        Integer idMedecin = currentUserService.getCurrentMedecinId();
        List<RendezVous> result = rendezVousService.listerRendezVousDuJourParMedecin(idMedecin);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/medecin")
    public ResponseEntity<List<RendezVous>> listerParMedecinEtDate(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        Integer idMedecin = currentUserService.getCurrentMedecinId();
        List<RendezVous> result = rendezVousService.listerParMedecinEtDate(idMedecin, date);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/medecin/historique")
    public ResponseEntity<List<RendezVous>> listerToutParMedecin() {
        Integer idMedecin = currentUserService.getCurrentMedecinId();
        if (idMedecin == null) {
            throw new ForbiddenException(
                    "Aucun profil médecin n'est associé à votre compte. Contactez l'administrateur.");
        }
        List<RendezVous> result = rendezVousService.listerParMedecin(idMedecin);
        return ResponseEntity.ok(result);
    }

    // =====================================================
    // VÉRIFIER DISPONIBILITÉ
    // =====================================================
    @GetMapping("/disponibilite")
    public ResponseEntity<Boolean> verifierCreneau(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime dateHeure) {
        Integer idMedecin = currentUserService.getCurrentMedecinId();
        boolean libre = rendezVousService.verifierCreneau(idMedecin, dateHeure);
        return ResponseEntity.ok(libre);
    }

    // =====================================================
    // MODIFICATION COMPLÈTE
    // =====================================================
    @PutMapping("/{id}")
    public ResponseEntity<Void> modifier(
            @PathVariable Integer id,
            @Valid @RequestBody RendezVous rendezVous) {

        rendezVous.setIdRdv(id);
        rendezVousService.modifierRendezVous(rendezVous);
        return ResponseEntity.noContent().build();
    }

    // =====================================================
    // ACTIONS SUR LE CYCLE DE VIE (PATCH)
    // =====================================================
    @PatchMapping("/{id}/reporter")
    public ResponseEntity<Void> reporter(
            @PathVariable Integer id,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime nouvelleDate) {
        
        rendezVousService.reporterRendezVous(id, nouvelleDate);
        return ResponseEntity.ok().build();
    }

    @PatchMapping("/{id}/confirmer")
    public ResponseEntity<Void> confirmer(@PathVariable Integer id) {
        rendezVousService.confirmerPresence(id);
        return ResponseEntity.ok().build();
    }

    @PatchMapping("/{id}/annuler")
    public ResponseEntity<Void> annuler(@PathVariable Integer id) {
        rendezVousService.annulerRendezVous(id);
        return ResponseEntity.ok().build();
    }

    @PatchMapping("/{id}/absent")
    public ResponseEntity<Void> absent(@PathVariable Integer id) {
        rendezVousService.marquerCommeAbsent(id);
        return ResponseEntity.ok().build();
    }

    @PatchMapping("/{id}/terminer")
    public ResponseEntity<Void> terminer(@PathVariable Integer id) {
        rendezVousService.marquerCommeTermine(id);
        return ResponseEntity.ok().build();
    }
}