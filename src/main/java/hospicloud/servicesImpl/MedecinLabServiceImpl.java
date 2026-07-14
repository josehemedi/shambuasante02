package hospicloud.servicesImpl;

import hospicloud.dtos.MedecinDemandeAnalyseRequestDTO;
import hospicloud.dtos.MedecinDemandeAnalyseResponseDTO;
import hospicloud.exceptions.ForbiddenException;
import hospicloud.model.AnalyseLaboratoire;
import hospicloud.model.Role;
import hospicloud.repositories.LaboratoryRepository;
import hospicloud.repositories.PatientRepository;
import hospicloud.security.CurrentUserService;
import hospicloud.security.TenantAuthorization;
import hospicloud.services.MedecinLabService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class MedecinLabServiceImpl implements MedecinLabService {

    private final LaboratoryRepository laboratoryRepository;
    private final PatientRepository patientRepository;
    private final CurrentUserService currentUserService;

    public MedecinLabServiceImpl(LaboratoryRepository laboratoryRepository,
                                 PatientRepository patientRepository,
                                 CurrentUserService currentUserService) {
        this.laboratoryRepository = laboratoryRepository;
        this.patientRepository = patientRepository;
        this.currentUserService = currentUserService;
    }

    @Override
    @Transactional
    public MedecinDemandeAnalyseResponseDTO creerDemande(MedecinDemandeAnalyseRequestDTO request) {
        if (currentUserService.getCurrentRole() != Role.MEDECIN) {
            throw new ForbiddenException("Seul un médecin peut envoyer une demande au laboratoire.");
        }
        if (request.getIdPatient() == null) {
            throw new IllegalArgumentException("Le patient est obligatoire.");
        }
        if (request.getTestCode() == null || request.getTestCode().isBlank()) {
            throw new IllegalArgumentException("Le type d'analyse est obligatoire.");
        }
        if (request.getSubmit() == null || !request.getSubmit()) {
            throw new IllegalArgumentException("Seules les demandes envoyées au laboratoire sont enregistrées.");
        }

        Integer hopitalId = currentUserService.getCurrentHopitalId();
        Integer utilisateurId = currentUserService.getCurrentUtilisateurId();
        if (utilisateurId == null) {
            throw new ForbiddenException("Session invalide.");
        }

        patientRepository.trouverPatientParId(request.getIdPatient().longValue())
                .orElseThrow(() -> new ForbiddenException("Patient introuvable pour votre établissement."));

        Integer idType = laboratoryRepository.resolveOrCreateTypeAnalyse(
                hopitalId, request.getTestCode(), request.getTestName());

        AnalyseLaboratoire analyse = new AnalyseLaboratoire();
        analyse.setIdPatient(request.getIdPatient());
        analyse.setIdMedecin(utilisateurId);
        analyse.setIdTypeAnalyse(idType);
        analyse.setIdConsultation(request.getIdConsultation());
        analyse.setDateDemande(LocalDateTime.now());
        analyse.setStatut("EN_ATTENTE");
        analyse.setUrgence(mapPriority(request.getPriority()));
        analyse.setObservationsMedecin(buildObservations(request));

        Integer idAnalyse = laboratoryRepository.insertAnalyse(analyse, hopitalId);

        return laboratoryRepository.listDemandesMedecin(hopitalId, utilisateurId).stream()
                .filter(d -> d.getIdAnalyse() != null && d.getIdAnalyse().equals(idAnalyse))
                .findFirst()
                .orElseGet(() -> {
                    MedecinDemandeAnalyseResponseDTO dto = new MedecinDemandeAnalyseResponseDTO();
                    dto.setIdAnalyse(idAnalyse);
                    dto.setId("LAB-" + String.format("%04d", idAnalyse));
                    dto.setStatus("Pending");
                    dto.setPriority(request.getPriority() != null ? request.getPriority() : "Routine");
                    dto.setDate(LocalDateTime.now());
                    dto.setTestName(request.getTestName());
                    dto.setNotes(analyse.getObservationsMedecin());
                    return dto;
                });
    }

    @Override
    public List<MedecinDemandeAnalyseResponseDTO> listerMesDemandes() {
        TenantAuthorization.assertStaffRole();
        if (currentUserService.getCurrentRole() != Role.MEDECIN) {
            throw new ForbiddenException("Accès réservé aux médecins.");
        }
        Integer hopitalId = currentUserService.getCurrentHopitalId();
        Integer utilisateurId = currentUserService.getCurrentUtilisateurId();
        if (utilisateurId == null) {
            throw new ForbiddenException("Session invalide.");
        }
        return laboratoryRepository.listDemandesMedecin(hopitalId, utilisateurId);
    }

    private String mapPriority(String priority) {
        if (priority == null) {
            return "NORMALE";
        }
        return switch (priority.trim().toUpperCase()) {
            case "URGENT" -> "HAUTE";
            case "STAT" -> "VITALE";
            default -> "NORMALE";
        };
    }

    private String buildObservations(MedecinDemandeAnalyseRequestDTO request) {
        StringBuilder sb = new StringBuilder();
        if (request.getNotes() != null && !request.getNotes().isBlank()) {
            sb.append(request.getNotes().trim());
        }
        if (Boolean.TRUE.equals(request.getFastingRequired())) {
            if (!sb.isEmpty()) {
                sb.append("\n");
            }
            sb.append("[À jeun requis]");
        }
        return sb.isEmpty() ? null : sb.toString();
    }
}
