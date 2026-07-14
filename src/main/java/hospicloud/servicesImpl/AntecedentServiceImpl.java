package hospicloud.servicesImpl;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import hospicloud.enumeration.StatutAntecedent;
import hospicloud.exceptions.ResourceNotFoundException;
import hospicloud.model.Antecedent;
import hospicloud.repositories.AntecedentRepository;
import hospicloud.security.TenantContext;
import hospicloud.services.AntecedentService;

@Service
public class AntecedentServiceImpl implements AntecedentService {

    private final AntecedentRepository antecedentRepository;

    @Autowired
    public AntecedentServiceImpl(AntecedentRepository antecedentRepository) {
        this.antecedentRepository = antecedentRepository;
    }

    @Override
    @Transactional
    public void ajouterAntecedent(Antecedent antecedent) {

        if (antecedent == null) {
            throw new IllegalArgumentException("Données antécédent manquantes.");
        }

        Integer idPatient = antecedent.getIdPatient();
        if (idPatient == null || idPatient <= 0) {
            throw new IllegalArgumentException("L'identifiant du patient est requis.");
        }

        Integer tenantId = TenantContext.getRequiredHopitalId();
        antecedent.setIdHopiatl(tenantId);

        if (antecedent.getDateEnregistrement() == null) {
            antecedent.setDateEnregistrement(LocalDate.now());
        }

        if (antecedent.getStatut() == null) {
            antecedent.setStatut(StatutAntecedent.ACTIF);
        }
        antecedentRepository.enregistrerAntecedent(antecedent);
    }

    @Override
    @Transactional
    public void mettreAJourAntecedent(Antecedent antecedent) {

        if (antecedent == null || antecedent.getIdAntecendent() == null || antecedent.getIdAntecendent() <= 0) {
            throw new IllegalArgumentException("Données incomplètes pour la mise à jour.");
        }

        Integer tenantId = TenantContext.getRequiredHopitalId();
        antecedent.setIdHopiatl(tenantId);

        antecedentRepository.modifierAntecedent(antecedent);
    }

    @Override
    @Transactional
    public void retirerAntecedent(int id) {
        if (id <= 0) {
            throw new ResourceNotFoundException("Identifiant d'antécédent invalide.");
        }
        antecedentRepository.supprimerAntecedent(id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Antecedent> recupererDossierPatient(int idPatient, int page, int size) {

        if (idPatient <= 0) return List.of();

        int p = Math.max(0, page);
        int s = (size <= 0) ? 20 : size;

        return antecedentRepository.listerParPatient(idPatient, p, s);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Antecedent> genererSyntheseMedicale(int idPatient) {

        if (idPatient <= 0) return List.of();

        List<String> priorities = Arrays.asList(
                "ALLERGIE", "CHRONIQUE", "DIABETE", "HYPERTENSION"
        );

        return antecedentRepository.preparerDonneesPourSynthese(idPatient, priorities, 0, 500);
    }

    @Override
    @Transactional
    public void basculerStatut(int id) {

        if (id <= 0) return;

        Optional<Antecedent> opt = antecedentRepository.trouverParId(id);

        if (opt.isPresent()) {

            Antecedent a = opt.get();

            StatutAntecedent actuel = a.getStatut();

            StatutAntecedent nouveauStatut =
                    (actuel == StatutAntecedent.ACTIF)
                            ? StatutAntecedent.GUERI
                            : StatutAntecedent.ACTIF;

            antecedentRepository.changerStatutAntecedent(
                    id,
                    nouveauStatut.name()
            );

        } else {
            throw new ResourceNotFoundException(
                    "Impossible de trouver l'antécédent " + id + " pour cet hôpital."
            );
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Antecedent> trouverParId(int id) {

        if (id <= 0) return Optional.empty();

        return antecedentRepository.trouverParId(id);
    }
}