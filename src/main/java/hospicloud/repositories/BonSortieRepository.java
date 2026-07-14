package hospicloud.repositories;

import hospicloud.model.BonSortie;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository interface for BonSortie management.
 * Follows standard naming conventions for Spring-based architectures.
 */
public interface BonSortieRepository {

    // --- CRUD Operations ---
    
    /**
     * Persists a new discharge note.
     */
    BonSortie save(BonSortie dischargeNote);
    
    /**
     * Updates an existing discharge note.
     */
    boolean update(BonSortie dischargeNote);
    
    /**
     * Deletes a record by its unique identifier.
     */
    boolean deleteById(Integer id);

    // --- Basic Read Operations ---

    Optional<BonSortie> findById(Integer id);

    Optional<BonSortie> findByReferenceNumber(String referenceNumber);

    // --- Business Logic Queries ---
    
    /**
     * Retrieves all discharge notes associated with a specific patient.
     */
    List<BonSortie> findByPatientId(Integer patientId);
    
    /**
     * Retrieves discharge notes authorized by a specific physician.
     */
    List<BonSortie> findByAuthorizedBy(String physicianName);
    
    /**
     * Retrieves discharge notes within a specific date range.
     */
    List<BonSortie> findByDischargeDateBetween(LocalDateTime startDate, LocalDateTime endDate);
    
    /**
     * Retrieves discharge notes based on the discharge status (e.g., RECOVERED, DECEASED).
     */
    List<BonSortie> findByDischargeStatus(String status);

    // --- Utility Methods ---
    
    /**
     * Counts the number of discharge notes created in a specific year.
     * Useful for reference number generation (e.g., BS-2026-0001).
     */
    int countDischargeNotesByYear(int year);
    
    /**
     * Checks if the final payment has been settled for a discharge note.
     */
    boolean isPaymentSettled(Integer dischargeNoteId);

    boolean existsAutorisationEnCours(Integer patientId);

    List<hospicloud.dtos.sortie.PretSortieDTO> listPretesPourDelivrance(Integer hopitalId);

    boolean finaliserDelivrance(Integer idBonSortie, Integer hopitalId, boolean paiementConfirme, Integer delivrePar);
}