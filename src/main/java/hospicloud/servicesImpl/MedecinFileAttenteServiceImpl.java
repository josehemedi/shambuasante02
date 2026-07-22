package hospicloud.servicesImpl;

import hospicloud.dtos.CommencerConsultationResponseDTO;
import hospicloud.dtos.ConsultationRequestDTO;
import hospicloud.dtos.ConsultationResponseDTO;
import hospicloud.dtos.MedecinFileItemDTO;
import hospicloud.dtos.WaitingRoomCallEventDTO;
import hospicloud.exceptions.ForbiddenException;
import hospicloud.exceptions.ResourceNotFoundException;
import hospicloud.model.RendezVous;
import hospicloud.model.reception.Admission;
import hospicloud.repositories.MedecinFileAttenteRepository;
import hospicloud.repositories.RendezVousRepository;
import hospicloud.security.CurrentUserService;
import hospicloud.security.MedecinQueueTopics;
import hospicloud.security.ReceptionLiveTopics;
import hospicloud.security.TenantContext;
import hospicloud.security.WaitingRoomTopics;
import hospicloud.services.ConsultationMedicaleService;
import hospicloud.services.MedecinFileAttenteService;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
public class MedecinFileAttenteServiceImpl implements MedecinFileAttenteService {

    private static final Set<String> STATUTS_APPEABLES = Set.of(
            "ATTENTE_TRIAGE", "EN_ATTENTE", "ORIENTE", "ENREGISTRE", "APPELE");

    private final MedecinFileAttenteRepository fileRepository;
    private final RendezVousRepository rendezVousRepository;
    private final ConsultationMedicaleService consultationMedicaleService;
    private final CurrentUserService currentUserService;
    private final SimpMessagingTemplate messagingTemplate;

    public MedecinFileAttenteServiceImpl(
            MedecinFileAttenteRepository fileRepository,
            RendezVousRepository rendezVousRepository,
            ConsultationMedicaleService consultationMedicaleService,
            CurrentUserService currentUserService,
            SimpMessagingTemplate messagingTemplate) {
        this.fileRepository = fileRepository;
        this.rendezVousRepository = rendezVousRepository;
        this.consultationMedicaleService = consultationMedicaleService;
        this.currentUserService = currentUserService;
        this.messagingTemplate = messagingTemplate;
    }

    @Override
    public List<MedecinFileItemDTO> listerMaFile() {
        Integer hopitalId = TenantContext.getRequiredHopitalId();
        Integer medecinId = requireMedecinId();
        return fileRepository.listerFileDuMedecin(medecinId, hopitalId);
    }

    @Override
    @Transactional
    public WaitingRoomCallEventDTO appelerPatient(Integer idAdmission) {
        Integer hopitalId = TenantContext.getRequiredHopitalId();
        Integer medecinId = requireMedecinId();

        Admission admission = fileRepository.trouverAdmission(idAdmission, hopitalId)
                .orElseThrow(() -> new ResourceNotFoundException("Patient introuvable dans votre file d'attente."));

        assertAdmissionPourMedecin(admission, medecinId);
        String statut = normalize(admission.getStatut());
        if (!STATUTS_APPEABLES.contains(statut)) {
            throw new IllegalStateException("Ce patient ne peut pas être appelé (statut: " + admission.getStatut() + ").");
        }

        boolean rappel = "APPELE".equals(statut);
        return finaliserAppel(admission, medecinId, hopitalId, rappel);
    }

    @Override
    @Transactional
    public WaitingRoomCallEventDTO appelerDepuisRendezVous(Integer idRdv) {
        Integer hopitalId = TenantContext.getRequiredHopitalId();
        Integer medecinId = requireMedecinId();

        RendezVous rdv = rendezVousRepository.trouverParId(idRdv);
        if (rdv == null || rdv.getIdHopital() == null || !rdv.getIdHopital().equals(hopitalId)) {
            throw new ResourceNotFoundException("Rendez-vous introuvable dans votre établissement.");
        }
        if (rdv.getIdMedecin() == null || !rdv.getIdMedecin().equals(medecinId)) {
            throw new ForbiddenException("Ce rendez-vous n'est pas assigné à votre compte médecin.");
        }

        Admission existing = fileRepository.trouverAdmissionOuverteParRdv(idRdv, hopitalId).orElse(null);
        if (existing != null) {
            boolean rappel = "APPELE".equalsIgnoreCase(normalize(existing.getStatut()));
            if (!STATUTS_APPEABLES.contains(normalize(existing.getStatut()))) {
                throw new IllegalStateException(
                        "Ce patient ne peut pas être appelé (statut: " + existing.getStatut() + ").");
            }
            return finaliserAppel(existing, medecinId, hopitalId, rappel);
        }

        Admission created = new Admission();
        created.setIdHopital(hopitalId);
        created.setIdPatient(rdv.getIdPatient());
        created.setIdMedecin(medecinId);
        created.setIdRendezVous(idRdv);
        created.setNiveauPriorite(3);
        created.setTempsArrivee(LocalDateTime.now());
        created.setStatut("EN_ATTENTE");
        created.setCreePar(currentUserService.getCurrentUtilisateurId());
        created.setSalle(fileRepository.trouverSalleRdv(idRdv, hopitalId));
        Integer idAdmission = fileRepository.creerAdmissionPourAppel(created);
        created.setIdAdmission(idAdmission);

        return finaliserAppel(created, medecinId, hopitalId, false);
    }

    @Override
    @Transactional
    public CommencerConsultationResponseDTO commencerConsultation(Integer idAdmission) {
        Integer hopitalId = TenantContext.getRequiredHopitalId();
        Integer medecinId = requireMedecinId();

        Admission admission = fileRepository.trouverAdmission(idAdmission, hopitalId)
                .orElseThrow(() -> new ResourceNotFoundException("Admission introuvable dans votre établissement."));

        assertAdmissionPourMedecin(admission, medecinId);
        if (!"APPELE".equalsIgnoreCase(normalize(admission.getStatut()))) {
            throw new IllegalStateException("Appelez d'abord le patient avant de commencer la consultation.");
        }

        fileRepository.mettreAJourStatut(idAdmission, hopitalId, "EN_CONSULTATION");

        ConsultationResponseDTO consultation;
        if (admission.getIdRendezVous() != null) {
            consultation = consultationMedicaleService.obtenirOuCreerParRdv(admission.getIdRendezVous());
        } else {
            ConsultationRequestDTO dto = new ConsultationRequestDTO();
            dto.setIdMedecin(medecinId);
            dto.setIdPatient(admission.getIdPatient());
            dto.setMotifVisite("Consultation — passage file d'attente");
            consultation = consultationMedicaleService.creerConsultation(dto);
        }

        messagingTemplate.convertAndSend(ReceptionLiveTopics.destination(hopitalId), "STATUS_UPDATED");

        CommencerConsultationResponseDTO response = new CommencerConsultationResponseDTO();
        response.setIdAdmission(idAdmission);
        response.setStatutAdmission("EN_CONSULTATION");
        response.setConsultation(consultation);
        return response;
    }

    private WaitingRoomCallEventDTO finaliserAppel(
            Admission admission, Integer medecinId, Integer hopitalId, boolean rappel) {
        Integer numero = admission.getNumeroPassage();
        if (numero == null || numero <= 0) {
            numero = fileRepository.prochainNumeroPassage(hopitalId);
        }
        String salle = admission.getSalle();
        if (salle == null || salle.isBlank()) {
            salle = fileRepository.trouverSalleRdv(admission.getIdRendezVous(), hopitalId);
        }
        if (salle == null || salle.isBlank()) {
            salle = "Consultation";
        }

        fileRepository.marquerAppele(admission.getIdAdmission(), hopitalId, numero, salle);
        admission.setStatut("APPELE");
        admission.setNumeroPassage(numero);
        admission.setSalle(salle);
        admission.setAppeleAt(LocalDateTime.now());

        WaitingRoomCallEventDTO event = buildEvent(admission, medecinId, hopitalId, rappel);
        if (event.getNumeroPassage() == null) {
            event.setNumeroPassage(numero);
        }
        if (event.getSalle() == null || event.getSalle().isBlank()) {
            event.setSalle(salle);
        }
        messagingTemplate.convertAndSend(WaitingRoomTopics.destination(hopitalId), event);
        messagingTemplate.convertAndSend(ReceptionLiveTopics.destination(hopitalId), "PATIENT_CALLED");
        messagingTemplate.convertAndSend(
                MedecinQueueTopics.destination(hopitalId, medecinId),
                Map.of(
                        "type", rappel ? "PATIENT_RECALLED" : "PATIENT_CALLED",
                        "idAdmission", admission.getIdAdmission(),
                        "numeroPassage", numero,
                        "salle", salle,
                        "rappel", rappel
                )
        );
        return event;
    }

    private WaitingRoomCallEventDTO buildEvent(
            Admission admission, Integer medecinId, Integer hopitalId, boolean rappel) {
        WaitingRoomCallEventDTO event = new WaitingRoomCallEventDTO();
        event.setType(rappel ? "PATIENT_RECALLED" : "PATIENT_CALLED");
        event.setRappel(rappel);
        event.setIdHopital(hopitalId);
        event.setIdAdmission(admission.getIdAdmission());
        event.setIdPatient(admission.getIdPatient());
        event.setIdMedecin(medecinId);
        event.setPatientNom(fileRepository.trouverNomPatient(admission.getIdPatient(), hopitalId));
        event.setMedecinNom(fileRepository.trouverNomMedecin(medecinId, hopitalId));
        event.setSalle(admission.getSalle() != null ? admission.getSalle() : "Consultation");
        event.setNumeroPassage(admission.getNumeroPassage());
        event.setAppeleAt(admission.getAppeleAt() != null ? admission.getAppeleAt() : LocalDateTime.now());
        return event;
    }

    private void assertAdmissionPourMedecin(Admission admission, Integer medecinId) {
        if (admission.getIdMedecin() == null) {
            throw new ForbiddenException("Cette admission n'est pas encore affectée à un médecin.");
        }
        if (!admission.getIdMedecin().equals(medecinId)) {
            throw new ForbiddenException("Ce patient n'est pas dans votre file d'attente.");
        }
    }

    private Integer requireMedecinId() {
        Integer medecinId = currentUserService.getCurrentMedecinId();
        if (medecinId == null) {
            throw new ForbiddenException("Aucun profil médecin associé à ce compte.");
        }
        return medecinId;
    }

    private String normalize(String statut) {
        return statut == null ? "" : statut.trim().toUpperCase();
    }
}
