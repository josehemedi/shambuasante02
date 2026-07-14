package hospicloud.controlleurs;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import hospicloud.model.HoraireTravail;
import hospicloud.services.HoraireTravailService;
import hospicloud.security.CurrentUserService;

@RestController
@RequestMapping("/api/v1/horaires-travail")
public class HoraireTravailController {

	private final HoraireTravailService horaireService;
	private final CurrentUserService currentUserService;

	public HoraireTravailController(HoraireTravailService horaireService, CurrentUserService currentUserService) {
		this.horaireService = horaireService;
		this.currentUserService = currentUserService;
	}
	/**
	 * Créer un nouvel horaire de travail.
	 * POST http://localhost:8080/api/v1/horaires-travail
	 */
	@PostMapping
	public ResponseEntity<HoraireTravail> creerHoraire(@RequestBody HoraireTravail horaire) {
		HoraireTravail cree = horaireService.creerHoraire(horaire);
		return new ResponseEntity<>(cree, HttpStatus.CREATED);
	}
	/**
	 * Modifier un horaire de travail existant.
	 * PUT http://localhost:8080/api/v1/horaires-travail/{id}
	 */
	@PutMapping("/{id}")
	public ResponseEntity<HoraireTravail> modifierHoraire(
	        @PathVariable Long id,
	        @RequestBody HoraireTravail horaire) {

	    horaire.setId(id);

	    // 🔥 IGNORE CLIENT VALUE
	    horaire.setHopitalId(null);

	    HoraireTravail updated = horaireService.modifierHoraire(horaire);

	    return ResponseEntity.ok(updated);
	}
	/**
	 * Obtenir un horaire spécifique par son identifiant technique.
	 * GET http://localhost:8080/api/v1/horaires-travail/{id}
	 */
	@GetMapping("/{id}")
	public ResponseEntity<HoraireTravail> obtenirParId(@PathVariable Long id) {
		return horaireService.obtenirParId(id)
				.map(ResponseEntity::ok)
				.orElseGet(() -> ResponseEntity.notFound().build());
	}

	/**
	 * Récupérer la liste des horaires du médecin connecté.
	 * GET http://localhost:8080/api/v1/horaires-travail/medecin
	 */
	@GetMapping("/medecin")
	public ResponseEntity<List<HoraireTravail>> obtenirParMedecin() {
		Integer medecinId = currentUserService.getCurrentMedecinId();
		List<HoraireTravail> horaires = horaireService.obtenirParMedecin(medecinId);
		return ResponseEntity.ok(horaires);
	}

	/**
	 * Recherche croisée et optimisée (Multi-Tenant & Cache Redis).
	 * Le médecin et l'hôpital sont récupérés depuis le contexte utilisateur.
	 * GET http://localhost:8080/api/v1/horaires-travail/recherche?jourSemaine=Lundi
	 */
	@GetMapping("/recherche")
	public ResponseEntity<List<HoraireTravail>> rechercherHoraires(
			@RequestParam String jourSemaine) {
		
		Integer medecinId = currentUserService.getCurrentMedecinId();
		List<HoraireTravail> resultats = horaireService.obtenirParMedecinJourEtHopital(medecinId, jourSemaine);
		
		return ResponseEntity.ok(resultats);
	}

	/**
	 * Supprimer un horaire de travail.
	 * DELETE http://localhost:8080/api/v1/horaires-travail/{id}
	 */
	@DeleteMapping("/{id}")
	public ResponseEntity<String> supprimerHoraire(@PathVariable Long id) {
		boolean supprime = horaireService.supprimerHoraire(id);
		
		if (supprime) {
			return ResponseEntity.ok("L'horaire avec l'ID " + id + " a été supprimé avec succès.");
		} else {
			return ResponseEntity.status(HttpStatus.NOT_FOUND)
                                 .body("Échec de la suppression : L'horaire avec l'ID " + id + " est introuvable.");
		}
	}
}