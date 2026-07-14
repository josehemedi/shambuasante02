package hospicloud.servicesImpl;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import hospicloud.model.HoraireTravail;
import hospicloud.repositories.HoraireTravailRepository;
import hospicloud.security.TenantContext;
import hospicloud.services.HoraireTravailService;

@Service
public class HoraireTravailServiceImpl implements HoraireTravailService {

    private final HoraireTravailRepository horaireRepository;

    public HoraireTravailServiceImpl(HoraireTravailRepository horaireRepository) {
        this.horaireRepository = horaireRepository;
    }

    // ================= CREATE =================
    @Override
    @Transactional
    public HoraireTravail creerHoraire(HoraireTravail horaire) {

        if (horaire == null)
            throw new IllegalArgumentException("Horaire obligatoire");

        if (horaire.getMedecinId() == null)
            throw new IllegalArgumentException("Médecin obligatoire");

        if (horaire.getJourSemaine() == null || horaire.getJourSemaine().isBlank())
            throw new IllegalArgumentException("Jour obligatoire");

        if (horaire.getHeureDebut() == null || horaire.getHeureFin() == null)
            throw new IllegalArgumentException("Heures obligatoires");

        if (horaire.getHeureDebut().isAfter(horaire.getHeureFin()))
            throw new IllegalArgumentException("Heure début > fin");

        // 🔥 FORCE SAAS TENANT
        horaire.setHopitalId(TenantContext.getRequiredHopitalId());

        return horaireRepository.enregistrer(horaire);
    }

    // ================= UPDATE =================
    @Override
    @Transactional
    public HoraireTravail modifierHoraire(HoraireTravail horaire) {

        if (horaire == null || horaire.getId() == null)
            throw new IllegalArgumentException("ID requis");

        Integer hopitalId = TenantContext.getRequiredHopitalId();

        HoraireTravail existant = horaireRepository.trouverParId(horaire.getId())
                .orElseThrow(() -> new IllegalArgumentException("Introuvable"));

        // 🔐 SaaS CHECK
        if (!existant.getHopitalId().equals(hopitalId)) {
            throw new SecurityException("Accès interdit (SaaS violation)");
        }

        // 🔥 FORCE TENANT
        horaire.setHopitalId(hopitalId);

        int rows = horaireRepository.modifier(horaire);

        if (rows == 0)
            throw new RuntimeException("Update failed");

        return horaire;
    }

    // ================= DELETE =================
    @Override
    @Transactional
    public boolean supprimerHoraire(Long id) {
        return id != null && horaireRepository.supprimerParId(id) > 0;
    }

    // ================= GET BY ID (FIX IMPORTANT) =================
    @Override
    public Optional<HoraireTravail> obtenirParId(Long id) {

        if (id == null) return Optional.empty();

        // 🔥 DEBUG IMPORTANT (TEMPORAIRE)
        Integer hopitalId = TenantContext.getRequiredHopitalId();
        System.out.println("GET ID=" + id + " HOPITAL=" + hopitalId);

        return horaireRepository.trouverParId(id);
    }

    // ================= LIST BY MED =================
    @Override
    public List<HoraireTravail> obtenirParMedecin(Integer medecinId) {
        return medecinId == null
                ? List.of()
                : horaireRepository.trouverParMedecinId(medecinId);
    }

    // ================= SEARCH =================
    @Override
    public List<HoraireTravail> obtenirParMedecinJourEtHopital(
            Integer medecinId,
            String jourSemaine
    ) {
        if (medecinId == null || jourSemaine == null || jourSemaine.isBlank())
            return List.of();

        return horaireRepository.trouverParMedecinIdEtJour(medecinId, jourSemaine);
    }

    @Override
    public List<HoraireTravail> obtenirParMedecinEtJour(Integer medecinId, String jourSemaine) {
        return obtenirParMedecinJourEtHopital(medecinId, jourSemaine);
    }
}