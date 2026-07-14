package hospicloud.services;

import hospicloud.dtos.BillingAdvanceRequestDTO;
import hospicloud.dtos.BillingComposeRequestDTO;

import java.util.Map;

public interface BillingCompositionService {
    Map<String, Object> composeInvoice(BillingComposeRequestDTO request);

    Map<String, Object> recordAdvance(BillingAdvanceRequestDTO request);

    /** Recompose toutes les factures ouvertes du tenant depuis les consommations. */
    Map<String, Object> refreshOpenInvoices();

    /**
     * Facturation automatique interne (appelée quand un soin est consommé).
     * Taxe aux tarifs admin + prix catalogue, puis recalcule le total patient.
     */
    Map<String, Object> chargePatientConsumptions(Integer hopitalId, Integer idPatient, Integer actorUserId);
}
