package hospicloud.services;

import hospicloud.dtos.CreatePharmacieMedicamentRequest;
import hospicloud.dtos.PharmacieMedicamentDTO;
import hospicloud.dtos.PharmacieStockAlertDTO;
import hospicloud.dtos.PharmacyDispenseRequestDTO;

import java.util.List;
import java.util.Map;

public interface TenantPharmacyService {

    List<PharmacieMedicamentDTO> listMedicaments();

    PharmacieMedicamentDTO createMedicament(CreatePharmacieMedicamentRequest request);

    List<PharmacieStockAlertDTO> getStockAlerts();

    Map<String, Object> dispenseToPatient(PharmacyDispenseRequestDTO request);
}
