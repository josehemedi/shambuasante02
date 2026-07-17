package hospicloud.servicesImpl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import hospicloud.dtos.*;
import hospicloud.exceptions.ForbiddenException;
import hospicloud.exceptions.ResourceNotFoundException;
import hospicloud.exceptions.patient.PatientNotFoundException;
import hospicloud.model.*;
import hospicloud.repositories.*;
import hospicloud.security.CurrentUserContext;
import hospicloud.security.TenantAuthorization;
import hospicloud.security.TenantContext;
import hospicloud.services.*;
import hospicloud.services.reporting.ReportGenerator;
import hospicloud.utils.DocumentHashUtil;
import hospicloud.utils.TenantReportParamsHelper;
import hospicloud.dtos.events.TechnicalLogEvent;
import hospicloud.exceptions.ConsultationBusinessException;
import hospicloud.model.enums.ConsultationStatut;
import hospicloud.model.enums.TypeDocument;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.awt.image.BufferedImage;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class ConsultationMedicaleServiceImpl implements ConsultationMedicaleService {

    private final ConsultationMedicaleRepository repository;
    private final MedecinRepository medecinRepository;
    private final HopitalRepository hopitalRepository;
    private final PatientRepository patientRepository;
    private final RendezVousRepository rendezVousRepository;
    private final LiveKitService liveKitService;
    private final ReportGenerator reportGenerator;
    private final ObjectMapper objectMapper;
    private final SignatureDocumentRepository signatureDocumentRepository;
    private final TechnicalLogService technicalLogService;
    private final BillingCompositionService billingCompositionService;
    private final boolean strictTimeWindow;
    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    public ConsultationMedicaleServiceImpl(ConsultationMedicaleRepository repository,
                                            MedecinRepository medecinRepository,
                                            HopitalRepository hopitalRepository,
                                            PatientRepository patientRepository,
                                            RendezVousRepository rendezVousRepository,
                                            LiveKitService liveKitService,
                                            ReportGenerator reportGenerator,
                                            ObjectMapper objectMapper,
                                            SignatureDocumentRepository signatureDocumentRepository,
                                            TechnicalLogService technicalLogService,
                                            BillingCompositionService billingCompositionService,
                                            @Value("${app.teleconsultation.strict-time-window:false}") boolean strictTimeWindow) {
        this.repository = repository;
        this.medecinRepository = medecinRepository;
        this.hopitalRepository = hopitalRepository;
        this.patientRepository = patientRepository;
        this.rendezVousRepository = rendezVousRepository;
        this.liveKitService = liveKitService;
        this.reportGenerator = reportGenerator;
        this.objectMapper = objectMapper;
        this.signatureDocumentRepository = signatureDocumentRepository;
        this.technicalLogService = technicalLogService;
        this.billingCompositionService = billingCompositionService;
        this.strictTimeWindow = strictTimeWindow;
    }

    @Override
    public ConsultationResponseDTO creerConsultation(ConsultationRequestDTO dto) {
        TenantAuthorization.assertStaffRole();
        Integer hopitalId = TenantContext.getRequiredHopitalId();

        if (dto.getIdPatient() == null) {
            throw new IllegalArgumentException("Le patient est obligatoire.");
        }
        if (dto.getIdMedecin() == null) {
            throw new IllegalArgumentException("Le médecin est obligatoire.");
        }

        // id_hopital du client ignoré — toujours issu du JWT / TenantContext
        dto.setIdHopital(null);

        patientRepository.trouverPatientParId(dto.getIdPatient().longValue())
                .orElseThrow(() -> new PatientNotFoundException(dto.getIdPatient()));

        medecinRepository.trouverParId(dto.getIdMedecin())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Médecin introuvable dans votre établissement."));

        TenantAuthorization.assertMedecinScope(dto.getIdMedecin());

        if (dto.getIdRdv() != null) {
            RendezVous rdv = requireRendezVousTenant(dto.getIdRdv());
            assertRdvCoherentAvecConsultation(rdv, dto);
        }

        ConsultationMedicale saved = repository.save(toEntity(dto));
        saved.setIdHopital(hopitalId);

        // Taxe automatiquement le tarif consultation fixé par l'admin hôpital
        Integer actorId = null;
        try {
            var auth = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
            if (auth != null && auth.getPrincipal() instanceof hospicloud.security.UtilisateurPrincipal up) {
                actorId = up.getIdUtilisateur();
            }
        } catch (Exception ignored) {
        }
        billingCompositionService.chargePatientConsumptions(hopitalId, dto.getIdPatient(), actorId);

        return toResponseDTO(saved);
    }

    @Override
    public List<ConsultationResponseDTO> obtenirHistoriquePatient(Integer idPatient) {
        Role role = CurrentUserContext.getRole();
        if (role == Role.PATIENT) {
            TenantAuthorization.assertPatientOwns(idPatient);
        } else {
            TenantAuthorization.assertStaffRole();
        }
        Patient patient = patientRepository.trouverPatientParId(idPatient.longValue())
                .orElseThrow(() -> new PatientNotFoundException(idPatient));
        TenantAuthorization.assertSameTenant(patient.getIdHopital());
        return repository.findByPatient(idPatient).stream()
                .map(this::toResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<ConsultationResponseDTO> obtenirHistoriqueMedecin(Integer idMedecin) {
        TenantAuthorization.assertStaffRole();
        TenantAuthorization.assertMedecinScope(idMedecin);
        if (idMedecin == null) {
            return List.of();
        }
        // Uniquement patients attribués au médecin et encore en gérance clinique
        return repository.findEnGeranceParMedecin(idMedecin).stream()
                .map(this::toResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    public ConsultationResponseDTO completerConsultation(Long idConsultation, String observations, String diagnostic) {
        TenantAuthorization.assertStaffRole();
        ConsultationMedicale consultation = requireConsultationTenant(idConsultation);
        assertConsultationModifiable(consultation);
        TenantAuthorization.assertMedecinScope(consultation.getIdMedecin());
        
        repository.updateObservationsEtDiagnostic(idConsultation, observations, diagnostic);
        consultation.setObservations(observations);
        consultation.setDiagnostic(diagnostic);
        
        return toResponseDTO(consultation);
    }

    @Override
    public ConsultationResponseDTO mettreAJourConstantes(Long idConsultation, ConsultationRequestDTO dto) {
        TenantAuthorization.assertStaffRole();
        ConsultationMedicale c = requireConsultationTenant(idConsultation);
        assertConsultationModifiable(c);
        TenantAuthorization.assertMedecinScope(c.getIdMedecin());

        c.setPoids(dto.getPoids());
        c.setTaille(dto.getTaille());
        c.setTensionArterielle(dto.getTensionArterielle());
        c.setTemperature(dto.getTemperature());
        c.setFrequenceCardiaque(dto.getFrequenceCardiaque());
        repository.updateFiche(c);
        
        return toResponseDTO(c);
    }

    @Override
    public ConsultationResponseDTO obtenirParRdv(Integer idRdv) {
        TenantAuthorization.assertStaffRole();
        requireRendezVousTenant(idRdv);
        return repository.findByRdv(idRdv)
                .map(this::toResponseDTO)
                .orElse(null);
    }

    @Override
    public ConsultationResponseDTO obtenirOuCreerParRdv(Integer idRdv) {
        TenantAuthorization.assertStaffRole();
        RendezVous rdv = requireRendezVousTenant(idRdv);
        TenantAuthorization.assertMedecinScope(rdv.getIdMedecin());

        Optional<ConsultationMedicale> existing = repository.findByRdv(idRdv);
        if (existing.isPresent()) {
            ConsultationMedicale consultation = existing.get();
            TenantAuthorization.assertSameTenant(consultation.getIdHopital());
            return toResponseDTO(consultation);
        }

        Integer currentMedecinId = CurrentUserContext.getMedecinId();
        ConsultationRequestDTO dto = new ConsultationRequestDTO();
        dto.setIdMedecin(currentMedecinId != null ? currentMedecinId : rdv.getIdMedecin());
        dto.setIdPatient(rdv.getIdPatient());
        dto.setIdRdv(idRdv);
        dto.setMotifVisite(rdv.getMotifVisite());
        return creerConsultation(dto);
    }

    @Override
    public ConsultationResponseDTO enregistrerFiche(Long idConsultation, ConsultationFicheDTO fiche) {
        TenantAuthorization.assertStaffRole();
        ConsultationMedicale consultation = requireConsultationTenant(idConsultation);
        assertConsultationModifiable(consultation);
        TenantAuthorization.assertMedecinScope(consultation.getIdMedecin());

        consultation.setPoids(fiche.getPoids());
        consultation.setTaille(fiche.getTaille());
        consultation.setTensionArterielle(fiche.getTensionArterielle());
        consultation.setTemperature(fiche.getTemperature());
        consultation.setFrequenceCardiaque(fiche.getFrequenceCardiaque());
        consultation.setObservations(fiche.getObservations());
        consultation.setDiagnostic(fiche.getDiagnostic());
        consultation.setAnalysesPrescrites(serializeAnalyses(fiche.getAnalyses()));
        if (Boolean.TRUE.equals(fiche.getFinaliser())) {
            consultation.setFicheFinalisee(true);
        }

        repository.updateFiche(consultation);
        return toResponseDTO(consultation);
    }

    @Override
    public ConsultationResponseDTO obtenirParId(Long idConsultation) {
        TenantAuthorization.assertStaffRole();
        ConsultationMedicale consultation = requireConsultationTenant(idConsultation);
        TenantAuthorization.assertMedecinScope(consultation.getIdMedecin());
        return toResponseDTO(consultation);
    }

    private ConsultationMedicale requireConsultationTenant(Long idConsultation) {
        ConsultationMedicale consultation = repository.findById(idConsultation)
                .orElseThrow(() -> new ResourceNotFoundException("Consultation introuvable dans votre établissement."));
        TenantAuthorization.assertSameTenant(consultation.getIdHopital());
        return consultation;
    }

    private RendezVous requireRendezVousTenant(Integer idRdv) {
        RendezVous rdv = rendezVousRepository.trouverParId(idRdv);
        if (rdv == null) {
            throw new ResourceNotFoundException("Rendez-vous introuvable dans votre établissement.");
        }
        TenantAuthorization.assertSameTenant(rdv.getIdHopital());
        return rdv;
    }

    private void assertRdvCoherentAvecConsultation(RendezVous rdv, ConsultationRequestDTO dto) {
        if (dto.getIdPatient() != null && rdv.getIdPatient() != null
                && !dto.getIdPatient().equals(rdv.getIdPatient())) {
            throw new ForbiddenException("Le patient ne correspond pas au rendez-vous de votre établissement.");
        }
        if (dto.getIdMedecin() != null && rdv.getIdMedecin() != null
                && !dto.getIdMedecin().equals(rdv.getIdMedecin())) {
            throw new ForbiddenException("Le médecin ne correspond pas au rendez-vous de votre établissement.");
        }
    }

    private String serializeAnalyses(List<AnalyseConsultationDTO> analyses) {
        if (analyses == null || analyses.isEmpty()) {
            return null;
        }
        try {
            return objectMapper.writeValueAsString(analyses);
        } catch (Exception e) {
            throw new IllegalArgumentException("Format d'analyses invalide.");
        }
    }

    private List<AnalyseConsultationDTO> deserializeAnalyses(String json) {
        if (json == null || json.isBlank()) {
            return List.of();
        }
        try {
            return objectMapper.readValue(json, new TypeReference<List<AnalyseConsultationDTO>>() {});
        } catch (Exception e) {
            return List.of();
        }
    }

    private ConsultationMedicale toEntity(ConsultationRequestDTO dto) {
        ConsultationMedicale c = new ConsultationMedicale();
        c.setIdMedecin(dto.getIdMedecin());
        c.setIdPatient(dto.getIdPatient());
        c.setIdRdv(dto.getIdRdv());
        c.setMotifVisite(dto.getMotifVisite());
        c.setPoids(dto.getPoids());
        c.setTaille(dto.getTaille());
        c.setTensionArterielle(dto.getTensionArterielle());
        c.setTemperature(dto.getTemperature());
        c.setFrequenceCardiaque(dto.getFrequenceCardiaque());
        c.setObservations(dto.getObservations());
        c.setDiagnostic(dto.getDiagnostic());
        return c;
    }

    private ConsultationResponseDTO toResponseDTO(ConsultationMedicale c) {
        ConsultationResponseDTO dto = new ConsultationResponseDTO();
        dto.setIdConsultation(c.getIdConsultation());
        dto.setIdHopital(c.getIdHopital());
        dto.setIdMedecin(c.getIdMedecin());
        dto.setIdPatient(c.getIdPatient());
        dto.setIdRdv(c.getIdRdv());
        if (c.getDateConsultation() != null) {
            dto.setDateConsultation(c.getDateConsultation().format(dateFormatter));
        }
        dto.setMotifVisite(c.getMotifVisite());
        dto.setPoids(c.getPoids());
        dto.setTaille(c.getTaille());
        dto.setTensionArterielle(c.getTensionArterielle());
        dto.setTemperature(c.getTemperature());
        dto.setFrequenceCardiaque(c.getFrequenceCardiaque());
        dto.setObservations(c.getObservations());
        dto.setDiagnostic(c.getDiagnostic());
        dto.setAnalyses(deserializeAnalyses(c.getAnalysesPrescrites()));

        if (c.getNomHopital() != null && !c.getNomHopital().isBlank()) {
            dto.setNomHopital(c.getNomHopital());
        } else if (c.getIdHopital() != null) {
            Hopital hopital = hopitalRepository.rechercherhopitalParId(c.getIdHopital().longValue());
            if (hopital != null) {
                dto.setNomHopital(hopital.getNom());
            }
        }
        if (c.getNomMedecin() != null && !c.getNomMedecin().isBlank()) {
            dto.setNomMedecin(c.getNomMedecin());
        } else if (c.getIdMedecin() != null) {
            medecinRepository.trouverParId(c.getIdMedecin()).ifPresent(m -> {
                String nom = Stream.of(m.getPrenom(), m.getNom())
                        .filter(Objects::nonNull)
                        .collect(Collectors.joining(" "))
                        .trim();
                dto.setNomMedecin(nom.isBlank() ? null : nom);
            });
        }
        if (c.getNomPatient() != null && !c.getNomPatient().isBlank()) {
            dto.setNomPatient(c.getNomPatient());
        } else if (c.getIdPatient() != null) {
            patientRepository.trouverPatientParId(c.getIdPatient().longValue()).ifPresent(p -> {
                String nom = Stream.of(p.getPrenom(), p.getNom())
                        .filter(Objects::nonNull)
                        .collect(Collectors.joining(" "))
                        .trim();
                dto.setNomPatient(nom.isBlank() ? null : nom);
            });
        }

        ConsultationStatut statut = c.getStatut() != null ? c.getStatut() : ConsultationStatut.BROUILLON;
        dto.setStatut(statut.name());
        if (c.getDateSignature() != null) {
            dto.setDateSignature(c.getDateSignature().format(
                    java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")));
        }
        if (c.getIdMedecin() != null) {
            medecinRepository.trouverParId(c.getIdMedecin()).ifPresent(m -> {
                if (m.getNumeroOrdre() != null && !m.getNumeroOrdre().isBlank()) {
                    dto.setNumeroOrdreMedecin(m.getNumeroOrdre());
                }
            });
        }
        if (c.getIdConsultation() != null && c.getIdHopital() != null && statut == ConsultationStatut.SIGNEE) {
            signatureDocumentRepository.findActiveByDocument(
                    TypeDocument.CONSULTATION, c.getIdConsultation(), c.getIdHopital())
                    .ifPresent(sig -> {
                        dto.setReferenceSignature(sig.getReferenceSignature());
                        dto.setHashAbrege(DocumentHashUtil.abbreviateHash(sig.getHashDocument()));
                        if (dto.getDateSignature() == null && sig.getDateSignature() != null) {
                            dto.setDateSignature(sig.getDateSignature().format(
                                    java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")));
                        }
                    });
        }
        return dto;
    }

    private void assertConsultationModifiable(ConsultationMedicale consultation) {
        ConsultationStatut statut = consultation.getStatut() != null
                ? consultation.getStatut()
                : ConsultationStatut.BROUILLON;
        if (statut == ConsultationStatut.SIGNEE) {
            TechnicalLogEvent event = new TechnicalLogEvent();
            event.setHopitalId(consultation.getIdHopital());
            event.setModule("consultations");
            event.setAction("TENTATIVE_MODIFICATION_DOCUMENT_SIGNE");
            event.setEndpoint("/api/consultations/" + consultation.getIdConsultation());
            event.setHttpMethod("PUT");
            event.setStatus("REFUSE");
            event.setMessage("typeDocument=CONSULTATION;documentId=" + consultation.getIdConsultation());
            technicalLogService.record(event);
            throw new ConsultationBusinessException(
                    "CONSULTATION_DEJA_SIGNEE",
                    "Cette consultation est signée et ne peut plus être modifiée directement.",
                    HttpStatus.CONFLICT);
        }
    }
    
    private java.io.InputStream loadLogoInputStream(hospicloud.model.Hopital hopital) {
        if (hopital == null || hopital.getLogoUrl() == null || hopital.getLogoUrl().trim().isEmpty()) {
            return null;
        }
        
        String logoPath = hopital.getLogoUrl();
        try {
            java.io.InputStream is = getClass().getClassLoader().getResourceAsStream(logoPath);
            if (is != null) {
                return is;
            }
            
            java.io.File logoFile = new java.io.File(logoPath);
            if (logoFile.exists() && logoFile.isFile()) {
                return new java.io.FileInputStream(logoFile);
            }
            
            java.io.File resourcesFile = new java.io.File("src/main/resources/" + logoPath);
            if (resourcesFile.exists() && resourcesFile.isFile()) {
                return new java.io.FileInputStream(resourcesFile);
            }
            
            System.out.println("Logo non trouvé: " + logoPath);
            return null;
            
        } catch (java.io.FileNotFoundException e) {
            System.out.println("Logo non trouvé: " + logoPath + " - " + e.getMessage());
            return null;
        } catch (Exception e) {
            System.out.println("Erreur chargement logo: " + e.getMessage());
            return null;
        }
    }

    @Override
    public Map<String, Object> getOrdonnanceParams(Long idConsultation) {
        ConsultationMedicale consultation = requireConsultationForReport(idConsultation);
        return buildConsultationReportParams(consultation);
    }

    @Override
    public byte[] genererPdfFicheConsultation(Long idConsultation) {
        ConsultationMedicale consultation = requireConsultationForReport(idConsultation);
        Map<String, Object> params = buildConsultationReportParams(consultation);
        try {
            return reportGenerator.generate("Fiche_Consultation.jasper", params, null);
        } catch (Exception e) {
            throw new IllegalStateException("Impossible de générer la fiche de consultation.", e);
        }
    }

    private ConsultationMedicale requireConsultationForReport(Long idConsultation) {
        ConsultationMedicale consultation = requireConsultationTenant(idConsultation);
        Role role = CurrentUserContext.getRole();
        if (role == Role.PATIENT) {
            TenantAuthorization.assertPatientOwns(consultation.getIdPatient());
        } else {
            TenantAuthorization.assertStaffRole();
            if (role == Role.MEDECIN) {
                TenantAuthorization.assertMedecinScope(consultation.getIdMedecin());
            }
        }
        return consultation;
    }

    private Map<String, Object> buildConsultationReportParams(ConsultationMedicale consultation) {
        Patient patient = patientRepository.trouverPatientParId(consultation.getIdPatient().longValue())
                .orElseThrow(() -> new PatientNotFoundException(consultation.getIdPatient()));

        Hopital hopital = consultation.getIdHopital() != null
                ? TenantReportParamsHelper.resolveActiveHopital(hopitalRepository, consultation.getIdHopital())
                : null;

        String nomPatient = Stream.of(patient.getPrenom(), patient.getNom())
                .filter(Objects::nonNull)
                .collect(Collectors.joining(" "))
                .trim();
        if (nomPatient.isBlank()) {
            nomPatient = "Patient";
        }

        String nomMedecin = formatNomMedecin(consultation);

        Map<String, Object> params = new HashMap<>();
        if (hopital != null) {
            TenantReportParamsHelper.applyTenantBranding(params, hopital, consultation.getIdHopital());
        } else {
            params.put("NOM_HOPITAL", consultation.getNomHopital() != null ? consultation.getNomHopital() : "Shambua Santé");
            params.put("ID_TENANT", "—");
            params.put("SOUS_DOMAINE", "—");
            params.put("INFOS_ETABLISSEMENT", "—");
        }
        params.put("NOM_PATIENT", nomPatient);
        params.put("AGE_PATIENT", formatAgePatient(patient.getDateNaissance()));
        params.put("NOM_MEDECIN", nomMedecin);
        params.put("REF_CONSULTATION", "CONS-" + consultation.getIdConsultation());
        params.put("ID_RDV", consultation.getIdRdv() != null ? "#" + consultation.getIdRdv() : "—");
        params.put("MOTIF_VISITE", nullToDash(consultation.getMotifVisite()));
        params.put("POIDS", consultation.getPoids() != null ? consultation.getPoids() + " kg" : "—");
        params.put("TAILLE", consultation.getTaille() != null ? consultation.getTaille() + " cm" : "—");
        params.put("TENSION_ARTERIELLE", nullToDash(consultation.getTensionArterielle()));
        params.put("TEMPERATURE", consultation.getTemperature() != null ? consultation.getTemperature() + " °C" : "—");
        params.put("FREQUENCE_CARDIAQUE",
                consultation.getFrequenceCardiaque() != null ? consultation.getFrequenceCardiaque() + " bpm" : "—");
        params.put("OBSERVATIONS", nullToDash(consultation.getObservations()));
        params.put("DIAGNOSTIC", nullToDash(consultation.getDiagnostic()));
        params.put("ANALYSES_TEXTE", formatAnalysesText(consultation));
        params.put("LOGO_HOPITAL", loadLogoInputStream(hopital));

        if (consultation.getDateConsultation() != null) {
            params.put("DATE_CONSULTATION", java.sql.Timestamp.valueOf(consultation.getDateConsultation()));
        } else {
            params.put("DATE_CONSULTATION", new java.sql.Timestamp(System.currentTimeMillis()));
        }

        try {
            BufferedImage qrImage = generateQRCodeImage("CONS-" + consultation.getIdConsultation());
            params.put("QR_CODE_IMAGE", qrImage);
        } catch (Exception e) {
            params.put("QR_CODE_IMAGE", null);
        }

        params.put("DOCUMENT_SIGNE", Boolean.FALSE);
        params.put("DATE_SIGNATURE_TEXTE", "—");
        params.put("REFERENCE_SIGNATURE", "—");
        params.put("NUMERO_ORDRE_MEDECIN", "—");
        params.put("HASH_ABREGE", "—");

        if (consultation.getIdHopital() != null && consultation.getStatut() == ConsultationStatut.SIGNEE) {
            signatureDocumentRepository.findActiveByDocument(
                    TypeDocument.CONSULTATION,
                    consultation.getIdConsultation(),
                    consultation.getIdHopital()).ifPresent(sig -> {
                params.put("DOCUMENT_SIGNE", Boolean.TRUE);
                if (sig.getDateSignature() != null) {
                    params.put("DATE_SIGNATURE_TEXTE", sig.getDateSignature().format(
                            java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")));
                }
                params.put("REFERENCE_SIGNATURE", nullToDash(sig.getReferenceSignature()));
                params.put("HASH_ABREGE", DocumentHashUtil.abbreviateHash(sig.getHashDocument()));
            });
            medecinRepository.trouverParId(consultation.getIdMedecin()).ifPresent(m -> {
                if (m.getNumeroOrdre() != null && !m.getNumeroOrdre().isBlank()) {
                    params.put("NUMERO_ORDRE_MEDECIN", m.getNumeroOrdre());
                }
            });
        }

        return params;
    }

    private String formatNomMedecin(ConsultationMedicale consultation) {
        if (consultation.getNomMedecin() != null && !consultation.getNomMedecin().isBlank()) {
            return "Dr " + consultation.getNomMedecin().trim();
        }
        if (consultation.getIdMedecin() == null) {
            return "Dr. Médecin";
        }
        return medecinRepository.trouverParId(consultation.getIdMedecin())
                .map(m -> {
                    String prenom = m.getPrenom() != null ? m.getPrenom().trim() : "";
                    String nom = m.getNom() != null ? m.getNom().trim() : "";
                    if (!prenom.isEmpty() && !nom.isEmpty()) {
                        return "Dr " + prenom + " " + nom;
                    }
                    if (!nom.isEmpty()) {
                        return "Dr " + nom;
                    }
                    if (m.getSpecialite() != null && !m.getSpecialite().isEmpty()) {
                        return "Dr " + m.getSpecialite();
                    }
                    return "Dr. Médecin";
                })
                .orElse("Dr. Médecin");
    }

    private String formatAgePatient(LocalDate dateNaissance) {
        if (dateNaissance == null) {
            return "—";
        }
        int years = Period.between(dateNaissance, LocalDate.now()).getYears();
        return years + " ans";
    }

    private String formatAnalysesText(ConsultationMedicale consultation) {
        List<AnalyseConsultationDTO> analyses = deserializeAnalyses(consultation.getAnalysesPrescrites());
        if (analyses.isEmpty()) {
            return "—";
        }
        StringBuilder sb = new StringBuilder();
        int index = 1;
        for (AnalyseConsultationDTO analyse : analyses) {
            sb.append(index++).append(". ").append(nullToDash(analyse.getTypeAnalyse()));
            if (analyse.getResultat() != null && !analyse.getResultat().isBlank()) {
                sb.append(" — Résultat : ").append(analyse.getResultat().trim());
            }
            if (analyse.getNotes() != null && !analyse.getNotes().isBlank()) {
                sb.append(" (").append(analyse.getNotes().trim()).append(")");
            }
            sb.append("\n");
        }
        return sb.toString().trim();
    }

    private String nullToDash(String value) {
        return value == null || value.isBlank() ? "—" : value.trim();
    }

    private BufferedImage generateQRCodeImage(String text) throws Exception {
        com.google.zxing.qrcode.QRCodeWriter qrCodeWriter = new com.google.zxing.qrcode.QRCodeWriter();
        com.google.zxing.common.BitMatrix bitMatrix = qrCodeWriter.encode(
                text, com.google.zxing.BarcodeFormat.QR_CODE, 200, 200);
        return com.google.zxing.client.j2se.MatrixToImageWriter.toBufferedImage(bitMatrix);
    }

    @Override
    public LiveKitTokenResponse genererTokenTeleconsultation(Long idRdv) {
        RendezVous rdv = requireRendezVousTenant(idRdv.intValue());

        if (!"TELECONSULTATION".equalsIgnoreCase(rdv.getCanal())) {
            throw new ForbiddenException("Ce rendez-vous n'est pas une téléconsultation.");
        }

        String statut = rdv.getStatutRdv() != null ? rdv.getStatutRdv().toUpperCase() : "";
        if ("ANNULE".equals(statut) || "ABSENT".equals(statut)) {
            throw new ForbiddenException("Ce rendez-vous n'est plus actif.");
        }

        if (strictTimeWindow) {
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime start = rdv.getDateHeureRdv().minusMinutes(15);
            int duration = rdv.getDureeEstimee() != null ? rdv.getDureeEstimee() : 30;
            LocalDateTime end = rdv.getDateHeureRdv().plusMinutes(duration + 30L);
            if (now.isBefore(start) || now.isAfter(end)) {
                throw new ForbiddenException("La téléconsultation n'est pas disponible pour le moment.");
            }
        }

        Integer medecinId = CurrentUserContext.getMedecinId();
        Integer patientId = CurrentUserContext.getPatientId();
        boolean isAssignedMedecin = medecinId != null && rdv.getIdMedecin() != null
                && medecinId.equals(rdv.getIdMedecin());
        boolean isAssignedPatient = patientId != null && rdv.getIdPatient() != null
                && patientId.equals(rdv.getIdPatient());

        if (!isAssignedMedecin && !isAssignedPatient) {
            throw new ForbiddenException("Seuls le médecin et le patient de ce rendez-vous peuvent rejoindre la visio.");
        }

        String participantIdentity;
        String displayName;
        if (isAssignedMedecin) {
            participantIdentity = "medecin-" + medecinId;
            displayName = rdv.getNomMedecin() != null ? rdv.getNomMedecin() : "Médecin";
        } else {
            participantIdentity = "patient-" + patientId;
            displayName = rdv.getNomPatient() != null ? rdv.getNomPatient() : "Patient";
        }

        String roomName = liveKitService.generateRoomName(rdv.getIdHopital(), idRdv.intValue());
        String token = liveKitService.generateToken(roomName, participantIdentity, displayName);

        return new LiveKitTokenResponse(
                token,
                roomName,
                participantIdentity,
                liveKitService.getLiveKitUrl(),
                displayName);
    }
}