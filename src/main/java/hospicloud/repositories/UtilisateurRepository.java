package hospicloud.repositories;

import hospicloud.model.Role;
import hospicloud.model.Utilisateur;

import java.util.List;
import java.util.Optional;

public interface UtilisateurRepository {

    Optional<Utilisateur> findByEmail(String email);

    Optional<Utilisateur> findByEmailAnyStatus(String email);

    Optional<Utilisateur> findById(Integer id);

    Optional<Utilisateur> findByIdAndHopitalId(Integer id, Integer idHopital);

    List<Utilisateur> findAllByHopitalId(Integer idHopital);

    List<Utilisateur> findAllByHopitalIdIncludingInactive(Integer idHopital);

    boolean existsByEmail(String email);

    boolean existsByEmailExcludingId(String email, Integer excludeId);

    Utilisateur insert(Utilisateur utilisateur);

    void updateProfile(Utilisateur utilisateur);

    void updateMedecinLink(Integer idUtilisateur, Integer idHopital, Integer idMedecin);

    void updatePassword(Integer idUtilisateur, String encodedPassword);

    boolean setActive(Integer id, Integer idHopital, boolean active);

    /** Active / désactive un compte sans filtre hôpital (activation d'invitation). */
    boolean setActiveById(Integer id, boolean active);

    void ensureSchema();

    void seedIfEmpty();

    void syncDemoUsers();

    /** Agrégation plateforme Super Admin */
    Long countAllActive();

    Long countAllActiveExistingBefore(java.time.LocalDate date);

    Optional<Integer> findUtilisateurIdByPatient(Integer idPatient, Integer idHopital);

    Optional<String> findEmailByPatient(Integer idPatient, Integer idHopital);

    /**
     * Relie un compte PATIENT existant (même email / hôpital) à la fiche patient
     * si le compte n'est pas encore lié, ou déjà lié à cette même fiche.
     */
    boolean linkPatientAccountByEmail(Integer idPatient, Integer idHopital, String email);

    Optional<Integer> findUtilisateurIdByMedecin(Integer idMedecin, Integer idHopital);

    List<Integer> findActiveUtilisateurIdsByRole(Integer idHopital, Role role);
}
