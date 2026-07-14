package hospicloud.repositories;

import hospicloud.model.Antecedent;
import java.util.List;
import java.util.Optional;

public interface AntecedentRepository {

    void enregistrerAntecedent(Antecedent antecedent);

    void modifierAntecedent(Antecedent antecedent);

    void changerStatutAntecedent(int idAntecedent, String nouveauStatut);

    void supprimerAntecedent(int idAntecedent);

    List<Antecedent> listerParPatient(int idPatient);

    List<Antecedent> listerParPatient(int idPatient, int page, int size);

    List<Antecedent> preparerDonneesPourSynthese(
            int idPatient,
            List<String> priorityLibelles,
            int page,
            int size
    );

    Optional<Antecedent> trouverParId(int id);
}