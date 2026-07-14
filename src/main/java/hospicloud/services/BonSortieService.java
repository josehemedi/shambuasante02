package hospicloud.services;

import hospicloud.dtos.BonSortieRequestDto;
import hospicloud.dtos.BonSortieResponseDto;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Service interface for handling business logic regarding Discharge Notes.
 * Acts as a bridge between the Controller and the Repository.
 */
public interface BonSortieService {

    /**
     * Creates and persists a new discharge note.
     * @param requestDto The data from the front-end.
     * @return The created discharge note as a response DTO.
     */
    BonSortieResponseDto createDischargeNote(BonSortieRequestDto requestDto);

    /**
     * Retrieves a specific discharge note by its ID.
     */
    BonSortieResponseDto getDischargeNoteById(Integer id);

    /**
     * Retrieves a discharge note by its reference number.
     */
    BonSortieResponseDto getDischargeNoteByReference(String referenceNumber);

    /**
     * Retrieves all discharge notes for a specific patient.
     */
    List<BonSortieResponseDto> getDischargeNotesByPatient(Integer patientId);

    /**
     * Retrieves discharge notes within a specific date range (for reporting).
     */
    List<BonSortieResponseDto> getDischargeNotesByPeriod(LocalDateTime start, LocalDateTime end);

    /**
     * Updates an existing discharge note (e.g., changing status or payment info).
     */
    BonSortieResponseDto updateDischargeNote(Integer id, BonSortieRequestDto requestDto);

    /**
     * Removes a discharge note.
     */
    void deleteDischargeNote(Integer id);

    /**
     * Checks if the financial settlement is complete.
     */
    boolean isPaymentSettled(Integer id);

    /**
     * Prepares report parameters for Bulletin de Sortie.
     * Verifies payment is finalized before allowing report generation.
     * @param idBonSortie ID of the discharge note
     * @return Map containing report parameters
     * @throws RuntimeException if payment is not finalized
     */
    Map<String, Object> getBulletinSortieParams(Integer idBonSortie);
}