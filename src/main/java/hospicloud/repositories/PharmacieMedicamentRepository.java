package hospicloud.repositories;

import hospicloud.dtos.CreatePharmacieMedicamentRequest;
import hospicloud.dtos.PharmacieMedicamentDTO;
import hospicloud.dtos.PharmacieStockAlertDTO;

import java.util.List;
import java.util.Optional;

public interface PharmacieMedicamentRepository {

    void ensureSchema();

    PharmacieMedicamentDTO create(Integer hopitalId, Integer creeParUtilisateurId, CreatePharmacieMedicamentRequest request);

    List<PharmacieMedicamentDTO> listByHopital(Integer hopitalId);

    Optional<PharmacieMedicamentDTO> findByIdAndHopital(Long id, Integer hopitalId);

    void decrementStock(Long id, Integer hopitalId, int quantity);

    void syncStatuts(Integer hopitalId);

    void processStockAlerts(Integer hopitalId);

    List<PharmacieStockAlertDTO> listActiveAlerts(Integer hopitalId);
}
