package hospicloud.servicesImpl;

import hospicloud.model.Societe;
import hospicloud.repositories.SocieteRepository;
import hospicloud.security.TenantContext;
import hospicloud.services.SocieteService;
import hospicloud.exceptions.ResourceNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class SocieteServiceImpl implements SocieteService {

    private static final Logger logger = LoggerFactory.getLogger(SocieteServiceImpl.class);

    private final SocieteRepository societeRepository;

    public SocieteServiceImpl(SocieteRepository societeRepository) {
        this.societeRepository = societeRepository;
    }

    // =========================
    // CREATE
    // =========================
    @Override
    @Transactional
    public void creerSociete(Societe societe) {

        if (societe == null || societe.getNomSociete() == null || societe.getNomSociete().isBlank()) {
            throw new IllegalArgumentException("Données société invalides");
        }

        societe.setNomSociete(normaliserNom(societe.getNomSociete()));

        if (societe.getTauxCouverture() == null) {
            societe.setTauxCouverture(0.0);
        }

        // 🔥 Tenant géré dans repository → pas besoin ici
        Optional<Societe> exist = societeRepository.trouverParNom(societe.getNomSociete());

        if (exist.isPresent()) {
            throw new IllegalStateException("Société déjà existante dans cet hôpital");
        }

        int result = societeRepository.enregistrerSociete(societe);

        if (result == 0) {
            throw new RuntimeException("Échec création société");
        }

        logger.info("Société créée: {}", societe.getNomSociete());
    }

    // =========================
    // UPDATE
    // =========================
    @Override
    @Transactional
    public void mettreAJourSociete(Societe societe) {

        if (societe == null || societe.getIdSociete() == null) {
            throw new IllegalArgumentException("Société invalide");
        }

        if (!societeRepository.existeParId(societe.getIdSociete())) {
            throw new ResourceNotFoundException("Société introuvable ou accès refusé");
        }

        int updated = societeRepository.modifierSociete(societe);

        if (updated == 0) {
            throw new RuntimeException("Erreur mise à jour société");
        }
    }

    // =========================
    // DELETE
    // =========================
    @Override
    @Transactional
    public void supprimerSociete(Long id) {

        if (id == null) {
            throw new IllegalArgumentException("ID invalide");
        }

        if (!societeRepository.existeParId(id)) {
            throw new ResourceNotFoundException("Suppression impossible : société introuvable");
        }

        societeRepository.supprimerSociete(id);

        logger.info("Société supprimée id={}", id);
    }

    // =========================
    // READ
    // =========================
    @Override
    @Transactional(readOnly = true)
    public List<Societe> listerParHopital() {
        return societeRepository.listerParHopital();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Societe> recupererParId(Long id) {
        return societeRepository.trouverParId(id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Societe> listerTout() {
        return societeRepository.ListerSocietes();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Societe> trouverParNom(String nomSociete) {

        if (nomSociete == null || nomSociete.isBlank()) {
            return Optional.empty();
        }

        return societeRepository.trouverParNom(normaliserNom(nomSociete));
    }

    // =========================
    // UTIL
    // =========================
    private String normaliserNom(String nom) {
        return nom.trim().toUpperCase();
    }

    @Override
    public boolean verifierAppartenance(Long id) {

        if (id == null) {
            return false;
        }

        Integer tenantId = TenantContext.getRequiredHopitalId();

        return societeRepository.trouverParId(id)
                .map(s -> s.getIdHopital() != null && s.getIdHopital().equals(tenantId))
                .orElse(false);
    }
}