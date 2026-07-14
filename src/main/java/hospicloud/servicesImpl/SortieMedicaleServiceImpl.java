package hospicloud.servicesImpl;

import hospicloud.dtos.BonSortieResponseDto;
import hospicloud.dtos.OrdonnanceRequest;
import hospicloud.dtos.sortie.AutoriserSortieRequestDTO;
import hospicloud.dtos.sortie.AutoriserSortieResponseDTO;
import hospicloud.dtos.sortie.ContexteSortieDTO;
import hospicloud.dtos.sortie.PretSortieDTO;
import hospicloud.exceptions.BadRequestException;
import hospicloud.exceptions.ForbiddenException;
import hospicloud.exceptions.ResourceNotFoundException;
import hospicloud.exceptions.patient.PatientNotFoundException;
import hospicloud.model.BonSortie;
import hospicloud.model.ConsultationMedicale;
import hospicloud.model.Medecin;
import hospicloud.model.Patient;
import hospicloud.model.Role;
import hospicloud.model.reception.Admission;
import hospicloud.model.archive.TypeEpisode;
import hospicloud.repositories.BonSortieRepository;
import hospicloud.repositories.ConsultationMedicaleRepository;
import hospicloud.repositories.MedecinRepository;
import hospicloud.repositories.PatientRepository;
import hospicloud.repositories.ReceptionDashboardRepository;
import hospicloud.security.CurrentUserContext;
import hospicloud.security.CurrentUserService;
import hospicloud.security.TenantAuthorization;
import hospicloud.security.TenantContext;
import hospicloud.services.OrdonnanceService;
import hospicloud.services.SortieMedicaleService;
import hospicloud.services.archive.ArchiveWorkflowService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.time.Year;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
@Transactional
public class SortieMedicaleServiceImpl implements SortieMedicaleService {

    private static final Set<String> ETATS_SORTIE_VALIDES = Set.of(
            "GUERI", "AMELIORE", "STATIONNAIRE", "DECES", "TRANSFERE");
    private static final Set<String> STATUTS_DEJA_SORTI = Set.of("SORTIE_AUTORISEE", "SORTI");

    private final PatientRepository patientRepository;
    private final MedecinRepository medecinRepository;
    private final ConsultationMedicaleRepository consultationRepository;
    private final ReceptionDashboardRepository admissionRepository;
    private final BonSortieRepository bonSortieRepository;
    private final OrdonnanceService ordonnanceService;
    private final CurrentUserService currentUserService;
    private final ArchiveWorkflowService archiveWorkflowService;

    public SortieMedicaleServiceImpl(PatientRepository patientRepository,
                                     MedecinRepository medecinRepository,
                                     ConsultationMedicaleRepository consultationRepository,
                                     ReceptionDashboardRepository admissionRepository,
                                     BonSortieRepository bonSortieRepository,
                                     OrdonnanceService ordonnanceService,
                                     CurrentUserService currentUserService,
                                     ArchiveWorkflowService archiveWorkflowService) {
        this.patientRepository = patientRepository;
        this.medecinRepository = medecinRepository;
        this.consultationRepository = consultationRepository;
        this.admissionRepository = admissionRepository;
        this.bonSortieRepository = bonSortieRepository;
        this.ordonnanceService = ordonnanceService;
        this.currentUserService = currentUserService;
        this.archiveWorkflowService = archiveWorkflowService;
    }

    @Override
    @Transactional(readOnly = true)
    public ContexteSortieDTO getContexteSortie(Integer idPatient) {
        assertMedecinRole();
        Integer idMedecin = requireMedecinId();
        Integer hopitalId = TenantContext.getRequiredHopitalId();

        Patient patient = patientRepository.trouverPatientParId(idPatient.longValue())
                .orElseThrow(() -> new PatientNotFoundException(idPatient));

        ContexteSortieDTO ctx = new ContexteSortieDTO();
        ctx.setIdPatient(idPatient);
        ctx.setNomPatient(formatNom(patient));
        ctx.setStatutClinique(resolveStatutClinique(patient));

        if (STATUTS_DEJA_SORTI.contains(ctx.getStatutClinique())
                || bonSortieRepository.existsAutorisationEnCours(idPatient)) {
            ctx.setPeutAutoriser(false);
            ctx.setMessage("Le patient a déjà une sortie autorisée ou est déjà sorti.");
            return ctx;
        }

        ConsultationMedicale consultation = resolveConsultation(idPatient, idMedecin, hopitalId, null);
        Admission admission = admissionRepository.trouverAdmissionActiveParPatient(idPatient, hopitalId);

        if (consultation == null && admission == null) {
            ctx.setPeutAutoriser(false);
            ctx.setMessage("Aucune consultation ou hospitalisation active pour ce patient.");
            return ctx;
        }

        if (!medecinAutorisePourPatient(idMedecin, idPatient, consultation, admission)) {
            ctx.setPeutAutoriser(false);
            ctx.setMessage("Vous n'êtes pas autorisé à traiter ce patient.");
            return ctx;
        }

        if (admission != null && "HOSPITALISE".equalsIgnoreCase(admission.getStatut())) {
            ctx.setTypePriseEnCharge("HOSPITALISATION");
            ctx.setIdAdmissionActive(admission.getIdAdmission());
            ctx.setMotifPriseEnCharge("Hospitalisation en cours");
            ctx.setDatePriseEnCharge(admission.getTempsArrivee());
        } else if (consultation != null) {
            ctx.setTypePriseEnCharge("CONSULTATION");
            ctx.setIdConsultationActive(consultation.getIdConsultation());
            ctx.setMotifPriseEnCharge(consultation.getMotifVisite());
            ctx.setDatePriseEnCharge(consultation.getDateConsultation());
            if (admission != null) {
                ctx.setIdAdmissionActive(admission.getIdAdmission());
            }
        } else if (admission != null) {
            ctx.setTypePriseEnCharge("ADMISSION");
            ctx.setIdAdmissionActive(admission.getIdAdmission());
            ctx.setMotifPriseEnCharge("Admission — " + admission.getStatut());
            ctx.setDatePriseEnCharge(admission.getTempsArrivee());
        }

        ctx.setPeutAutoriser(true);
        ctx.setMessage("Le patient peut recevoir une autorisation de sortie médicale.");
        return ctx;
    }

    @Override
    public AutoriserSortieResponseDTO autoriserSortieMedicale(AutoriserSortieRequestDTO request) {
        assertMedecinRole();
        Integer idMedecin = requireMedecinId();
        Integer hopitalId = TenantContext.getRequiredHopitalId();
        Integer idPatient = request.getIdPatient();

        if (!ETATS_SORTIE_VALIDES.contains(request.getEtatSortie().toUpperCase())) {
            throw new BadRequestException("État de sortie invalide.");
        }

        Patient patient = patientRepository.trouverPatientParId(idPatient.longValue())
                .orElseThrow(() -> new PatientNotFoundException(idPatient));

        String statutClinique = resolveStatutClinique(patient);
        if (STATUTS_DEJA_SORTI.contains(statutClinique)
                || bonSortieRepository.existsAutorisationEnCours(idPatient)) {
            throw new BadRequestException("Le patient est déjà sorti ou une autorisation est en cours.");
        }

        ConsultationMedicale consultation = resolveConsultation(
                idPatient, idMedecin, hopitalId, request.getIdConsultation());
        Admission admission = request.getIdAdmission() != null
                ? admissionRepository.trouverAdmissionParId(request.getIdAdmission(), hopitalId)
                : admissionRepository.trouverAdmissionActiveParPatient(idPatient, hopitalId);

        if (consultation == null && admission == null) {
            throw new BadRequestException("Aucune consultation ou hospitalisation active.");
        }

        if (!medecinAutorisePourPatient(idMedecin, idPatient, consultation, admission)) {
            throw new ForbiddenException("Vous n'êtes pas autorisé à autoriser la sortie de ce patient.");
        }

        Medecin medecin = medecinRepository.trouverParId(idMedecin)
                .orElseThrow(() -> new ForbiddenException("Médecin introuvable pour cet établissement."));

        if (consultation != null) {
            String obs = StringUtils.hasText(request.getObservationsOrdonnance())
                    ? request.getObservationsOrdonnance()
                    : request.getRecommandationsPostHospitalisation();
            consultationRepository.updateObservationsEtDiagnostic(
                    consultation.getIdConsultation(), obs, request.getDiagnosticFinal());
        }

        Long idOrdonnance = null;
        if (StringUtils.hasText(request.getContenuOrdonnance())) {
            OrdonnanceRequest ord = new OrdonnanceRequest();
            ord.setIdPatient(idPatient);
            ord.setIdMedecin(idMedecin);
            ord.setDiagnostic(request.getDiagnosticFinal());
            ord.setContenuOrdonnance(request.getContenuOrdonnance());
            ord.setObservations(request.getObservationsOrdonnance());
            ord.setDateExpiration(LocalDate.now().plusMonths(1));
            ordonnanceService.creerOrdonnance(ord);
        }

        BonSortie bon = new BonSortie();
        bon.setIdHopital(hopitalId);
        bon.setIdPatient(idPatient);
        bon.setIdConsultation(consultation != null ? consultation.getIdConsultation().intValue() : null);
        bon.setIdAdmission(admission != null ? admission.getIdAdmission() : null);
        bon.setIdOrdonnance(idOrdonnance);
        bon.setNumeroBon(genererNumeroBon());
        bon.setDiagnosticFinal(request.getDiagnosticFinal());
        bon.setEtatSortie(request.getEtatSortie().toUpperCase());
        bon.setRecommandationsPostHospitalisation(request.getRecommandationsPostHospitalisation());
        bon.setStatutPaiementFinal(false);
        bon.setStatutWorkflow("AUTORISE_MEDICALEMENT");
        bon.setAutorisePar(formatNomMedecin(medecin));

        BonSortie saved = bonSortieRepository.save(bon);

        patientRepository.mettreAJourStatutClinique(idPatient.longValue(), "SORTIE_AUTORISEE");

        String statutAdmission = null;
        if (admission != null) {
            statutAdmission = "SORTIE_AUTORISEE";
            admissionRepository.mettreAJourStatutAdmission(
                    admission.getIdAdmission(), hopitalId, statutAdmission);
        }

        AutoriserSortieResponseDTO response = new AutoriserSortieResponseDTO();
        response.setBonSortie(toResponseDto(saved, patient));
        response.setIdOrdonnance(idOrdonnance);
        response.setStatutPatient("SORTIE_AUTORISEE");
        response.setStatutAdmission(statutAdmission);

        if (admission != null) {
            Optional<Long> archiveId = archiveWorkflowService.soumettreApresAutorisationSortie(
                    hopitalId, admission, idPatient, idMedecin, saved.getIdBonSortie());
            archiveId.ifPresent(id -> {
                response.setIdArchiveDossier(id);
                response.setDossierEnvoyeArchiviste(true);
            });
            response.setMessage(archiveId.isPresent()
                    ? "Sortie médicale autorisée. L'archiviste a été notifié : le dossier est prêt à être archivé."
                    : "Sortie médicale autorisée. La réception peut délivrer le bon de sortie final.");
        } else {
            Long episodeId = consultation != null && consultation.getIdConsultation() != null
                    ? consultation.getIdConsultation()
                    : saved.getIdBonSortie().longValue();
            Optional<Long> archiveId = archiveWorkflowService.soumettreApresSortieOfficielle(
                    hopitalId,
                    TypeEpisode.CONSULTATION,
                    episodeId,
                    idPatient,
                    idMedecin,
                    saved.getIdBonSortie(),
                    "Sortie officielle de consultation — dossier prêt à être archivé");
            archiveId.ifPresent(id -> {
                response.setIdArchiveDossier(id);
                response.setDossierEnvoyeArchiviste(true);
            });
            response.setMessage(archiveId.isPresent()
                    ? "Sortie médicale autorisée. L'archiviste a été notifié : le dossier est prêt à être archivé."
                    : "Sortie médicale autorisée. La réception peut délivrer le bon de sortie final.");
        }
        return response;
    }

    @Override
    @Transactional(readOnly = true)
    public List<PretSortieDTO> listerPretesPourDelivrance() {
        TenantAuthorization.assertStaffRole();
        return bonSortieRepository.listPretesPourDelivrance(TenantContext.getRequiredHopitalId());
    }

    @Override
    public PretSortieDTO delivrerBonSortie(Integer idBonSortie, boolean paiementConfirme) {
        Integer hopitalId = TenantContext.getRequiredHopitalId();
        BonSortie bon = bonSortieRepository.findById(idBonSortie)
                .orElseThrow(() -> new ResourceNotFoundException("Bon de sortie introuvable."));

        if (!"AUTORISE_MEDICALEMENT".equals(bon.getStatutWorkflow())) {
            throw new BadRequestException("Ce bon de sortie n'est pas en attente de délivrance.");
        }

        boolean updated = bonSortieRepository.finaliserDelivrance(
                idBonSortie, hopitalId, paiementConfirme, currentUserService.getCurrentUtilisateurId());
        if (!updated) {
            throw new BadRequestException("Impossible de finaliser la délivrance du bon de sortie.");
        }

        patientRepository.mettreAJourStatutClinique(bon.getIdPatient().longValue(), "SORTI");

        if (bon.getIdAdmission() != null) {
            admissionRepository.mettreAJourStatutAdmission(bon.getIdAdmission(), hopitalId, "SORTI");
            archiveWorkflowService.soumettreApresDelivranceSiAbsent(
                    hopitalId, bon.getIdAdmission(), bon.getIdPatient(), idBonSortie);
        } else {
            Long episodeId = bon.getIdConsultation() != null
                    ? bon.getIdConsultation().longValue()
                    : idBonSortie.longValue();
            archiveWorkflowService.soumettreApresSortieOfficielle(
                    hopitalId,
                    TypeEpisode.CONSULTATION,
                    episodeId,
                    bon.getIdPatient(),
                    null,
                    idBonSortie,
                    "Bon de sortie délivré — dossier prêt à être archivé");
        }

        Patient patient = patientRepository.trouverPatientParId(bon.getIdPatient().longValue()).orElse(null);

        PretSortieDTO dto = new PretSortieDTO();
        dto.setIdBonSortie(idBonSortie);
        dto.setNumeroBon(bon.getNumeroBon());
        dto.setIdPatient(bon.getIdPatient());
        dto.setNomPatient(patient != null ? formatNom(patient) : null);
        dto.setDiagnosticFinal(bon.getDiagnosticFinal());
        dto.setEtatSortie(bon.getEtatSortie());
        dto.setAutorisePar(bon.getAutorisePar());
        dto.setStatutPaiementFinal(paiementConfirme);
        dto.setStatutWorkflow("DELIVRE");
        dto.setDateSortie(bon.getDateSortie());
        dto.setRecommandations(bon.getRecommandationsPostHospitalisation());
        return dto;
    }

    private void assertMedecinRole() {
        Role role = CurrentUserContext.getRole();
        if (role != Role.MEDECIN && role != Role.TENANT_ADMIN) {
            throw new ForbiddenException("Seul un médecin peut autoriser une sortie médicale.");
        }
    }

    private Integer requireMedecinId() {
        Integer idMedecin = CurrentUserContext.getMedecinId();
        if (idMedecin == null) {
            throw new ForbiddenException("Profil médecin requis pour cette action.");
        }
        return idMedecin;
    }

    private ConsultationMedicale resolveConsultation(Integer idPatient,
                                                     Integer idMedecin,
                                                     Integer hopitalId,
                                                     Long idConsultationForcee) {
        if (idConsultationForcee != null) {
            return consultationRepository.findById(idConsultationForcee)
                    .filter(c -> c.getIdPatient().equals(idPatient)
                            && c.getIdMedecin().equals(idMedecin)
                            && c.getIdHopital().equals(hopitalId))
                    .orElse(null);
        }
        return consultationRepository.findActiveForPatientAndMedecin(idPatient, idMedecin).orElse(null);
    }

    private boolean medecinAutorisePourPatient(Integer idMedecin,
                                               Integer idPatient,
                                               ConsultationMedicale consultation,
                                               Admission admission) {
        if (consultation != null && idMedecin.equals(consultation.getIdMedecin())) {
            return true;
        }
        if (admission != null && admission.getIdMedecin() != null
                && idMedecin.equals(admission.getIdMedecin())) {
            return true;
        }
        return patientRepository.trouverPatientParId(idPatient.longValue()).isPresent();
    }

    private String resolveStatutClinique(Patient patient) {
        if (patient.getStatutClinique() != null && !patient.getStatutClinique().isBlank()) {
            return patient.getStatutClinique();
        }
        return "AMBULATOIRE";
    }

    private String genererNumeroBon() {
        int year = Year.now().getValue();
        int count = bonSortieRepository.countDischargeNotesByYear(year) + 1;
        return String.format("BS-%d-%04d", year, count);
    }

    private String formatNom(Patient patient) {
        return ((patient.getPrenom() != null ? patient.getPrenom() : "") + " "
                + (patient.getNom() != null ? patient.getNom() : "")).trim();
    }

    private String formatNomMedecin(Medecin medecin) {
        String nom = formatNomPatientStyle(medecin.getPrenom(), medecin.getNom());
        return nom.isBlank() ? "Dr. Médecin" : "Dr " + nom;
    }

    private String formatNomPatientStyle(String prenom, String nom) {
        return ((prenom != null ? prenom : "") + " " + (nom != null ? nom : "")).trim();
    }

    private BonSortieResponseDto toResponseDto(BonSortie entity, Patient patient) {
        BonSortieResponseDto dto = new BonSortieResponseDto();
        dto.setIdBonSortie(entity.getIdBonSortie());
        dto.setNumeroBon(entity.getNumeroBon());
        dto.setDateSortie(entity.getDateSortie());
        dto.setNomPatient(formatNom(patient));
        dto.setDiagnosticFinal(entity.getDiagnosticFinal());
        dto.setEtatSortie(entity.getEtatSortie());
        dto.setRecommandations(entity.getRecommandationsPostHospitalisation());
        dto.setStatutPaiementFinal(entity.getStatutPaiementFinal());
        dto.setAutorisePar(entity.getAutorisePar());
        dto.setStatutWorkflow(entity.getStatutWorkflow());
        dto.setIdPatient(entity.getIdPatient());
        return dto;
    }
}
