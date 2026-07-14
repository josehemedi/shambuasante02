package hospicloud.servicesImpl;

import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Pattern;

import hospicloud.model.Hopital;
import hospicloud.repositories.HopitalRepository;
import hospicloud.services.HospitalService;
import hospicloud.exceptions.ResourceNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service professionnel pour la gestion des hôpitaux.
 * Valide les données, applique la logique métier et délègue
 * la persistance au repository.
 */
@Service
public class HopitalServiceImpl implements HospitalService {

    private static final Logger logger = LoggerFactory.getLogger(HopitalServiceImpl.class);

    // Simple email pattern (pragmatique)
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$");
    // Phone: accept digits, spaces, dashes and optional leading +, length between 7 and 15 digits
    private static final Pattern PHONE_PATTERN = Pattern.compile("^\\+?[0-9\\s-]{7,20}$");
    // Types alignés sur l'enum SQL hopitaux.type
    private static final Set<String> ALLOWED_TYPES = Set.of(
            "CLINIQUE", "HOPITAL_GENERAL", "CENTRE_MEDICAL", "MATERNITE", "LABORATOIRE");

    private final HopitalRepository hopitalRepository;

    @Autowired
    public HopitalServiceImpl(HopitalRepository hopitalRepository) {
        this.hopitalRepository = hopitalRepository;
    }

    @Override
    @Transactional
    public void enresgitrerHopital(Hopital hopital) {
        Objects.requireNonNull(hopital, "hopital ne peut pas être null");

        if (hopital.getNom() == null || hopital.getNom().trim().isEmpty()) {
            logger.warn("Tentative d'enregistrement d'un hôpital sans nom");
            throw new IllegalArgumentException("Le nom de l'hôpital est requis.");
        }

        // Validate other business rules
        validateHopitalForCreateOrUpdate(hopital, true);

        String nomNettoye = hopital.getNom().trim();

        // Vérification de doublon au niveau service (complément du repository)
        Hopital exist = hopitalRepository.rechercherParNom(nomNettoye);
        if (exist != null) {
            logger.info("Refus d'enregistrer l'hôpital : nom déjà utilisé -> {}", nomNettoye);
            throw new IllegalStateException("Un hôpital avec ce nom existe déjà.");
        }

        // Valeurs par défaut possibles
        if (hopital.getDateCreation() == null) {
            hopital.setDateCreation(java.time.LocalDateTime.now());
        }

        hopitalRepository.enresgitrerHopital(hopital);
        logger.info("Hôpital créé : {} (id={})", hopital.getNom(), hopital.getIdHopital());
    }

    @Override
    @Transactional(readOnly = true)
    public Hopital rechercherhopitalParId(Long idHopital) {
        if (idHopital == null) return null;
        Hopital h = hopitalRepository.rechercherhopitalParId(idHopital);
        if (h == null) {
            logger.debug("Hôpital introuvable id={}", idHopital);
        }
        return h;
    }

    @Override
    @Transactional(readOnly = true)
    public Hopital rechercherParNom(String nom) {
        if (nom == null || nom.trim().isEmpty()) return null;
        return hopitalRepository.rechercherParNom(nom.trim());
    }

    @Override
    @Transactional(readOnly = true)
    public List<Hopital> listerTous() {
        return hopitalRepository.listerTous();
    }

    @Override
    @Transactional
    public void modifier(Hopital hopital) {
        Objects.requireNonNull(hopital, "hopital ne peut pas être null");
        if (hopital.getIdHopital() == null) {
            logger.warn("Tentative de modification sans identifiant d'hôpital");
            throw new IllegalArgumentException("Identifiant de l'hôpital requis pour la modification.");
        }

        Hopital existing = hopitalRepository.rechercherhopitalParId(hopital.getIdHopital().longValue());
        if (existing == null) {
            logger.info("Modification impossible : hôpital introuvable id={}", hopital.getIdHopital());
            throw new ResourceNotFoundException("Hôpital introuvable pour id=" + hopital.getIdHopital());
        }

        // Validate fields for update (email/phone/type/city/country)
        validateHopitalForCreateOrUpdate(hopital, false);

        // Si le nom change, vérifier l'unicité
        if (hopital.getNom() != null && !hopital.getNom().trim().isEmpty()) {
            String nomNettoye = hopital.getNom().trim();
            Hopital byName = hopitalRepository.rechercherParNom(nomNettoye);
            if (byName != null && !byName.getIdHopital().equals(hopital.getIdHopital())) {
                logger.info("Impossible de renommer l'hôpital, nom déjà utilisé : {}", nomNettoye);
                throw new IllegalStateException("Le nom de l'hôpital est déjà utilisé par un autre établissement.");
            }
        }

        // Mettre à jour la date de modification
        hopital.setDateModification(java.time.LocalDateTime.now());

        hopitalRepository.modifier(hopital);
        logger.info("Hôpital mis à jour : id={}", hopital.getIdHopital());
    }

    @Override
    @Transactional
    public void supprimer(Integer id) {
        if (id == null) return;

        // Vérifier l'existence
        Hopital existing = hopitalRepository.rechercherhopitalParId(id.longValue());
        if (existing == null) {
            logger.info("Suppression ignorée : hôpital introuvable id={}", id);
            throw new ResourceNotFoundException("Hôpital introuvable pour id=" + id);
        }

        hopitalRepository.supprimer(id);
        logger.info("Hôpital supprimé physiquement id={}", id);
    }

    // ----- Validation métier -----
    private void validateHopitalForCreateOrUpdate(Hopital hopital, boolean isCreate) {
        // email validation
        if (hopital.getEmail() != null && !hopital.getEmail().trim().isEmpty()) {
            if (!EMAIL_PATTERN.matcher(hopital.getEmail().trim()).matches()) {
                logger.warn("Email invalide pour l'hôpital {} : {}", hopital.getNom(), hopital.getEmail());
                throw new IllegalArgumentException("Format d'email invalide.");
            }
        }

        // telephone validation
        if (hopital.getTelephone() != null && !hopital.getTelephone().trim().isEmpty()) {
            if (!PHONE_PATTERN.matcher(hopital.getTelephone().trim()).matches()) {
                logger.warn("Téléphone invalide pour l'hôpital {} : {}", hopital.getNom(), hopital.getTelephone());
                throw new IllegalArgumentException("Format de téléphone invalide. Attendu chiffres et optionnellement '+' et espaces.");
            }
        }

        // type validation
        if (hopital.getType() != null && !hopital.getType().trim().isEmpty()) {
            String t = hopital.getType().trim().toUpperCase();
            if (!ALLOWED_TYPES.contains(t)) {
                logger.warn("Type d'hôpital non autorisé pour {} : {}", hopital.getNom(), hopital.getType());
                throw new IllegalArgumentException("Type d'hôpital invalide. Valeurs attendues: " + ALLOWED_TYPES);
            }
        }

        // ville/pays validation : si fournis, doivent être raisonnables
        if (hopital.getVille() != null && hopital.getVille().trim().length() > 100) {
            throw new IllegalArgumentException("Le nom de la ville est trop long.");
        }
        if (hopital.getPays() != null && hopital.getPays().trim().length() > 100) {
            throw new IllegalArgumentException("Le nom du pays est trop long.");
        }

        // On peut ajouter d'autres règles métier ici (par ex. vérification d'existence pays via liste blanche)
    }

}