package hospicloud.repositories;

import hospicloud.dtos.BillingDraftLineDTO;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface BillingCompositionRepository {
    boolean patientBelongsToHospital(Integer idPatient, Integer idHopital);

    Optional<Integer> findOpenFactureId(Integer idPatient, Integer idHopital);

    Optional<Integer> findFacturePatientId(Integer idFacture, Integer idHopital);

    BigDecimal findPatientInsuranceRate(Integer idPatient, Integer idHopital);

    BigDecimal findDefaultConsultationPrice(Integer idHopital);

    String findDefaultConsultationLabel(Integer idHopital);

    List<BillingDraftLineDTO> collectUnbilledConsultations(Integer idPatient, Integer idHopital);

    List<BillingDraftLineDTO> collectUnbilledAnalyses(Integer idPatient, Integer idHopital);

    List<BillingDraftLineDTO> collectUnbilledPharmacy(Integer idPatient, Integer idHopital);

    List<BillingDraftLineDTO> collectUnbilledHospitalization(Integer idPatient, Integer idHopital);

    List<BillingDraftLineDTO> collectUnbilledActes(Integer idPatient, Integer idHopital);

    BigDecimal sumUnappliedAdvances(Integer idPatient, Integer idHopital);

    BigDecimal sumAdvancesForFacture(Integer idFacture, Integer idHopital);

    Optional<BigDecimal> findFactureRemise(Integer idFacture, Integer idHopital);

    int insertFacture(Integer idPatient, Integer idHopital, String numero, BigDecimal ht, BigDecimal tva,
                      BigDecimal ttc, Integer idCaissier);

    void deleteAutoLines(Integer idFacture, Integer idHopital);

    void insertFactureItem(Integer idFacture, Integer idHopital, BillingDraftLineDTO line);

    BigDecimal sumFactureItems(Integer idFacture, Integer idHopital);

    void updateFactureComposition(
            Integer idFacture,
            Integer idHopital,
            BigDecimal sousTotalSoins,
            BigDecimal tauxAssurance,
            BigDecimal montantAssurance,
            BigDecimal montantRemise,
            BigDecimal montantAvances,
            BigDecimal montantHt,
            BigDecimal montantTtc,
            String statut,
            Integer idCaissier);

    void applyAdvancesToFacture(Integer idPatient, Integer idHopital, Integer idFacture);

    int insertAdvance(
            Integer idHopital,
            Integer idPatient,
            BigDecimal montant,
            String method,
            String reference,
            String notes);

    String nextFactureNumber(Integer idHopital);

    List<Integer> listPatientsWithOpenFactures(Integer idHopital);

    List<hospicloud.dtos.TarifHopitalDTO> listTarifs(Integer idHopital);

    int upsertTarif(Integer idHopital, hospicloud.dtos.TarifHopitalDTO tarif);

    long insertPharmacieDelivrance(
            Integer hopitalId,
            Integer idPatient,
            Long medicamentId,
            int quantite,
            BigDecimal prixUnitaire,
            String designation,
            Integer delivrePar);
}
