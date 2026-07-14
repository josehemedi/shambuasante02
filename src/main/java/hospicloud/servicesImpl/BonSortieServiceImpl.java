package hospicloud.servicesImpl;

import hospicloud.security.TenantContext;
import hospicloud.dtos.BonSortieRequestDto;
import hospicloud.dtos.BonSortieResponseDto;
import hospicloud.model.BonSortie;
import hospicloud.model.Hopital;
import hospicloud.model.Patient;
import hospicloud.repositories.BonSortieRepository;
import hospicloud.repositories.HopitalRepository;
import hospicloud.repositories.PatientRepository;
import hospicloud.services.BonSortieService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Transactional
public class BonSortieServiceImpl implements BonSortieService {

    private final BonSortieRepository repository;
    private final PatientRepository patientRepository;
    private final HopitalRepository hopitalRepository;

    @Autowired
    public BonSortieServiceImpl(BonSortieRepository repository,
            PatientRepository patientRepository,
            HopitalRepository hopitalRepository) {
        this.repository = repository;
        this.patientRepository = patientRepository;
        this.hopitalRepository = hopitalRepository;
    }

    @Override
    public BonSortieResponseDto createDischargeNote(BonSortieRequestDto dto) {
        BonSortie entity = new BonSortie();

        Integer hopitalId = TenantContext.getRequiredHopitalId();
        entity.setIdHopital(hopitalId);

        if (dto.getIdPatient() == null) {
            throw new IllegalArgumentException("Patient obligatoire pour le bon de sortie");
        }
        patientRepository.trouverPatientParId(dto.getIdPatient().longValue())
                .orElseThrow(() -> new IllegalArgumentException("Patient introuvable pour cet établissement"));

        entity.setIdPatient(dto.getIdPatient());
        entity.setIdConsultation(dto.getIdConsultation()); // Assuré selon votre structure DB
        entity.setNumeroBon(dto.getNumeroBon());
        entity.setDiagnosticFinal(dto.getDiagnosticFinal());
        entity.setEtatSortie(dto.getEtatSortie());
        entity.setRecommandationsPostHospitalisation(dto.getRecommandationsPostHospitalisation());
        entity.setStatutPaiementFinal(dto.getStatutPaiementFinal());
        entity.setAutorisePar(dto.getAutorisePar());
        entity.setDateSortie(LocalDateTime.now());

        BonSortie savedEntity = repository.save(entity);
        return mapToResponseDto(savedEntity);
    }

    @Override
    @Transactional(readOnly = true)
    public BonSortieResponseDto getDischargeNoteById(Integer id) {
        return repository.findById(id)
                .map(this::mapToResponseDto)
                .orElseThrow(() -> new RuntimeException("Discharge note not found with id: " + id));
    }

    @Override
    @Transactional(readOnly = true)
    public BonSortieResponseDto getDischargeNoteByReference(String referenceNumber) {
        return repository.findByReferenceNumber(referenceNumber)
                .map(this::mapToResponseDto)
                .orElseThrow(() -> new RuntimeException("Discharge note not found with reference: " + referenceNumber));
    }

    @Override
    @Transactional(readOnly = true)
    public List<BonSortieResponseDto> getDischargeNotesByPatient(Integer patientId) {
        return repository.findByPatientId(patientId).stream()
                .map(this::mapToResponseDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<BonSortieResponseDto> getDischargeNotesByPeriod(LocalDateTime start, LocalDateTime end) {
        return repository.findByDischargeDateBetween(start, end).stream()
                .map(this::mapToResponseDto)
                .collect(Collectors.toList());
    }

    @Override
    public BonSortieResponseDto updateDischargeNote(Integer id, BonSortieRequestDto requestDto) {
        BonSortie existingEntity = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Cannot update: Discharge note not found with id: " + id));

        existingEntity.setDiagnosticFinal(requestDto.getDiagnosticFinal());
        existingEntity.setEtatSortie(requestDto.getEtatSortie());
        existingEntity.setRecommandationsPostHospitalisation(requestDto.getRecommandationsPostHospitalisation());
        existingEntity.setStatutPaiementFinal(requestDto.getStatutPaiementFinal());
        existingEntity.setAutorisePar(requestDto.getAutorisePar());

        repository.update(existingEntity);
        return mapToResponseDto(existingEntity);
    }

    @Override
    public void deleteDischargeNote(Integer id) {
        if (!repository.deleteById(id)) {
            throw new RuntimeException("Deletion aborted: Discharge note not found with id: " + id);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isPaymentSettled(Integer id) {
        repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Discharge note not found with ID: " + id));
        return repository.isPaymentSettled(id);
    }

    @Override
    @Transactional(readOnly = true)
    public Map<String, Object> getBulletinSortieParams(Integer idBonSortie) {
        BonSortie bonSortie = repository.findById(idBonSortie)
                .orElseThrow(() -> new RuntimeException("Discharge note not found with ID: " + idBonSortie));

        if (!Boolean.TRUE.equals(bonSortie.getStatutPaiementFinal())) {
            throw new RuntimeException(
                    "Le patient n'a pas finalisé ses frais administratifs. Le bulletin de sortie ne peut être généré.");
        }

        Patient patient = patientRepository.trouverPatientParId(bonSortie.getIdPatient().longValue())
                .orElseThrow(() -> new RuntimeException("Patient not found with ID: " + bonSortie.getIdPatient()));

        Hopital hopital = hopitalRepository.rechercherhopitalParId(bonSortie.getIdHopital().longValue());

        Map<String, Object> params = new HashMap<>();
        params.put("NOM_HOPITAL", hopital != null ? hopital.getNom() : "Hospicloud");
        params.put("nomPatient", patient.getPrenom() + " " + patient.getNom());
        params.put("dateSortie", java.sql.Timestamp.valueOf(bonSortie.getDateSortie()));

        return params;
    }

    // --- Méthode de mapping manuel robuste ---
    private BonSortieResponseDto mapToResponseDto(BonSortie entity) {
        BonSortieResponseDto dto = new BonSortieResponseDto();
        dto.setIdBonSortie(entity.getIdBonSortie());
        dto.setNumeroBon(entity.getNumeroBon());
        dto.setDateSortie(entity.getDateSortie());
        dto.setDiagnosticFinal(entity.getDiagnosticFinal());
        dto.setEtatSortie(entity.getEtatSortie());
        dto.setRecommandations(entity.getRecommandationsPostHospitalisation());
        dto.setStatutPaiementFinal(entity.getStatutPaiementFinal());
        dto.setAutorisePar(entity.getAutorisePar());

        if (entity.getIdPatient() != null) {
            try {
                patientRepository.trouverPatientParId(entity.getIdPatient().longValue())
                        .ifPresent(patient -> dto.setNomPatient(patient.getPrenom() + " " + patient.getNom()));
            } catch (Exception ignored) {
            }
        }

        return dto;
    }
}