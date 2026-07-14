package hospicloud.repositories;

import hospicloud.model.Facture;

import java.util.List;
import java.util.Optional;

public interface FactureRepository {

    Facture save(Facture facture);

    Optional<Facture> findById(Integer id);

    Optional<Facture> findByNumeroFacture(String numeroFacture);

    List<Facture> findByIdHopital(Integer idHopital);

    List<Facture> findByIdPatient(Integer idPatient);

    List<Facture> findByStatutPaiement(String statutPaiement);

    boolean updateStatutPaiement(Integer factureId, String nouveauStatut);

    long getNextSequenceValue(String seqName);
}