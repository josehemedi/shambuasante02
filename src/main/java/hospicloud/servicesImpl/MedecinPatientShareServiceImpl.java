package hospicloud.servicesImpl;

import hospicloud.dtos.DocumentEnvoiResponse;
import hospicloud.dtos.MedecinDemandeAnalyseResponseDTO;
import hospicloud.exceptions.BadRequestException;
import hospicloud.exceptions.ForbiddenException;
import hospicloud.exceptions.ResourceNotFoundException;
import hospicloud.model.Hopital;
import hospicloud.model.Medecin;
import hospicloud.model.Patient;
import hospicloud.repositories.HopitalRepository;
import hospicloud.repositories.LaboratoryRepository;
import hospicloud.repositories.MedecinRepository;
import hospicloud.repositories.PatientRepository;
import hospicloud.repositories.UtilisateurRepository;
import hospicloud.security.CurrentUserService;
import hospicloud.security.TenantContext;
import hospicloud.services.ConsultationMedicaleService;
import hospicloud.services.MedecinPatientShareService;
import hospicloud.services.RealtimeNotificationService;
import hospicloud.utils.NotificationService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.sql.Timestamp;
import java.sql.Types;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;

@Service
public class MedecinPatientShareServiceImpl implements MedecinPatientShareService {

    private final LaboratoryRepository laboratoryRepository;
    private final PatientRepository patientRepository;
    private final MedecinRepository medecinRepository;
    private final HopitalRepository hopitalRepository;
    private final UtilisateurRepository utilisateurRepository;
    private final ConsultationMedicaleService consultationMedicaleService;
    private final LaboratoireReportService laboratoireReportService;
    private final NotificationService notificationService;
    private final RealtimeNotificationService realtimeNotificationService;
    private final CurrentUserService currentUserService;
    private final JdbcTemplate jdbcTemplate;

    @Value("${hospicloud.documents.upload-dir:uploads/patient-documents}")
    private String uploadDir;

    public MedecinPatientShareServiceImpl(
            LaboratoryRepository laboratoryRepository,
            PatientRepository patientRepository,
            MedecinRepository medecinRepository,
            HopitalRepository hopitalRepository,
            UtilisateurRepository utilisateurRepository,
            ConsultationMedicaleService consultationMedicaleService,
            LaboratoireReportService laboratoireReportService,
            NotificationService notificationService,
            RealtimeNotificationService realtimeNotificationService,
            CurrentUserService currentUserService,
            JdbcTemplate jdbcTemplate) {
        this.laboratoryRepository = laboratoryRepository;
        this.patientRepository = patientRepository;
        this.medecinRepository = medecinRepository;
        this.hopitalRepository = hopitalRepository;
        this.utilisateurRepository = utilisateurRepository;
        this.consultationMedicaleService = consultationMedicaleService;
        this.laboratoireReportService = laboratoireReportService;
        this.notificationService = notificationService;
        this.realtimeNotificationService = realtimeNotificationService;
        this.currentUserService = currentUserService;
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    @Transactional
    public DocumentEnvoiResponse envoyerResultatLabo(Integer idAnalyse) {
        Integer hopitalId = TenantContext.getRequiredHopitalId();
        requireMedecinId();
        MedecinDemandeAnalyseResponseDTO analyse = laboratoryRepository.trouverDemande(idAnalyse, hopitalId);
        if (analyse == null) {
            throw new ResourceNotFoundException("Analyse introuvable.");
        }
        String statut = analyse.getStatus() != null ? analyse.getStatus().toUpperCase(Locale.ROOT) : "";
        boolean ready = statut.contains("COMPLETE") || statut.contains("TERMINE") || statut.contains("VALID")
                || StringUtils.hasText(analyse.getResultatTexte());
        if (!ready) {
            throw new BadRequestException("Le résultat n'est pas encore disponible pour transmission.");
        }
        if (analyse.getIdPatient() == null) {
            throw new BadRequestException("Patient manquant sur cette analyse.");
        }

        Patient patient = patientRepository.trouverPatientParId(analyse.getIdPatient().longValue())
                .orElseThrow(() -> new ResourceNotFoundException("Patient introuvable."));

        byte[] pdf = laboratoireReportService.genererPdf(idAnalyse);
        String titre = "Résultat labo — " + (StringUtils.hasText(analyse.getTestName())
                ? analyse.getTestName() : "Analyse #" + idAnalyse);
        String resume = buildLabResume(analyse);
        String fileName = "resultat_labo_" + idAnalyse + ".pdf";
        String storedPath = storeBytes(hopitalId, analyse.getIdPatient(), fileName, pdf);

        Integer idDoc = insertSharedDocument(
                hopitalId,
                analyse.getIdPatient(),
                titre,
                "LABO",
                storedPath,
                resume,
                "LAB_RESULT",
                idAnalyse.longValue());

        return finalizeShare(patient, hopitalId, idDoc, "LABO", titre, resume, fileName, pdf);
    }

    @Override
    @Transactional
    public DocumentEnvoiResponse envoyerFicheConsultation(Long idConsultation) {
        Integer hopitalId = TenantContext.getRequiredHopitalId();
        requireMedecinId();
        byte[] pdf = consultationMedicaleService.genererPdfFicheConsultation(idConsultation);
        if (pdf == null || pdf.length == 0) {
            throw new BadRequestException("Impossible de générer le PDF de consultation.");
        }

        // Ownership / patient id via consultation service PDF already checks access
        Integer idPatient = resolveConsultationPatientId(idConsultation, hopitalId);
        Patient patient = patientRepository.trouverPatientParId(idPatient.longValue())
                .orElseThrow(() -> new ResourceNotFoundException("Patient introuvable."));

        String titre = "Fiche de consultation #" + idConsultation;
        String storedPath = storeBytes(hopitalId, idPatient, "consultation_" + idConsultation + ".pdf", pdf);
        Integer idDoc = insertSharedDocument(
                hopitalId, idPatient, titre, "CONSULTATION", storedPath,
                "Fiche de consultation transmise par votre médecin.",
                "CONSULTATION", idConsultation);

        return finalizeShare(patient, hopitalId, idDoc, "CONSULTATION", titre,
                "Votre médecin vous a transmis la fiche de consultation.",
                "fiche_consultation_" + idConsultation + ".pdf", pdf);
    }

    @Override
    @Transactional
    public DocumentEnvoiResponse envoyerDocumentFichier(
            Integer idPatient,
            String typeDocument,
            String titre,
            MultipartFile fichier) {
        Integer hopitalId = TenantContext.getRequiredHopitalId();
        requireMedecinId();
        if (idPatient == null) {
            throw new BadRequestException("Patient obligatoire.");
        }
        if (fichier == null || fichier.isEmpty()) {
            throw new BadRequestException("Fichier obligatoire.");
        }
        Patient patient = patientRepository.trouverPatientParId(idPatient.longValue())
                .orElseThrow(() -> new ResourceNotFoundException("Patient introuvable."));

        String safeTitre = StringUtils.hasText(titre) ? titre.trim()
                : (fichier.getOriginalFilename() != null ? fichier.getOriginalFilename() : "Document médical");
        String type = StringUtils.hasText(typeDocument) ? typeDocument.trim().toUpperCase(Locale.ROOT) : "DOCUMENT";

        try {
            byte[] bytes = fichier.getBytes();
            String stored = storeBytes(hopitalId, idPatient, safeFileName(fichier.getOriginalFilename()), bytes);
            Integer idDoc = insertSharedDocument(
                    hopitalId, idPatient, safeTitre, type, stored,
                    "Document médical partagé par votre médecin.",
                    "DOCUMENT", null);
            String contentType = fichier.getContentType() != null ? fichier.getContentType() : "application/octet-stream";
            return finalizeShare(patient, hopitalId, idDoc, type, safeTitre,
                    "Votre médecin vous a transmis un document médical.",
                    safeFileName(fichier.getOriginalFilename()), bytes, contentType);
        } catch (IOException e) {
            throw new IllegalStateException("Échec de lecture du fichier uploadé.", e);
        }
    }

    private DocumentEnvoiResponse finalizeShare(
            Patient patient,
            Integer hopitalId,
            Integer idDoc,
            String type,
            String titre,
            String resumeHtml,
            String attachmentName,
            byte[] attachmentBytes) {
        return finalizeShare(patient, hopitalId, idDoc, type, titre, resumeHtml,
                attachmentName, attachmentBytes, "application/pdf");
    }

    private DocumentEnvoiResponse finalizeShare(
            Patient patient,
            Integer hopitalId,
            Integer idDoc,
            String type,
            String titre,
            String resumeHtml,
            String attachmentName,
            byte[] attachmentBytes,
            String contentType) {

        String email = resolvePatientEmail(patient, hopitalId);
        String nomPatient = ((patient.getPrenom() != null ? patient.getPrenom() : "") + " "
                + (patient.getNom() != null ? patient.getNom() : "")).trim();
        String nomMedecin = resolveMedecinNom();
        Hopital hopital = hopitalRepository.rechercherhopitalParId(hopitalId.longValue());
        String nomHopital = hopital != null && StringUtils.hasText(hopital.getNom())
                ? hopital.getNom() : "Shambua Santé";

        if (StringUtils.hasText(email)) {
            try {
                notificationService.notifierDocumentCliniquePatient(
                        email, nomPatient, nomMedecin, nomHopital, type, titre, resumeHtml,
                        attachmentName, attachmentBytes, contentType);
            } catch (RuntimeException e) {
                throw new IllegalStateException(
                        "Document enregistré pour le portail, mais l'e-mail a échoué : " + e.getMessage(), e);
            }
        }

        try {
            realtimeNotificationService.notifyDocumentCliniqueEnvoyeAuPatient(
                    hopitalId, patient.getIdPatient().intValue(), type, titre, nomMedecin, idDoc);
        } catch (Exception ignored) {
        }

        DocumentEnvoiResponse response = new DocumentEnvoiResponse();
        response.setIdDocument(idDoc);
        response.setIdPatient(patient.getIdPatient().intValue());
        response.setNomPatient(nomPatient);
        response.setTypeDocument(type);
        response.setTitre(titre);
        response.setEmailMasque(maskEmail(email));
        response.setEnvoyeLe(LocalDateTime.now());
        response.setMessage(StringUtils.hasText(email)
                ? "Document transmis au patient (portail + e-mail)."
                : "Document disponible sur le portail patient (aucun e-mail sur la fiche).");
        return response;
    }

    private Integer insertSharedDocument(
            Integer hopitalId,
            Integer idPatient,
            String nomFichier,
            String typeDocument,
            String urlFichier,
            String resume,
            String referenceType,
            Long referenceId) {
        Integer userId = currentUserService.getCurrentUtilisateurId();
        LocalDateTime now = LocalDateTime.now();
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(
                    """
                    INSERT INTO patients_documents
                    (id_hopital, id_patient, nom_fichier, type_document, url_fichier, contenu_resume,
                     partage_patient, envoye_par, reference_type, reference_id, date_upload, date_envoi)
                    VALUES (?, ?, ?, ?, ?, ?, 1, ?, ?, ?, ?, ?)
                    """,
                    Statement.RETURN_GENERATED_KEYS);
            ps.setInt(1, hopitalId);
            ps.setInt(2, idPatient);
            ps.setString(3, nomFichier);
            ps.setString(4, typeDocument);
            if (urlFichier != null) ps.setString(5, urlFichier); else ps.setNull(5, Types.VARCHAR);
            ps.setString(6, resume);
            if (userId != null) ps.setInt(7, userId); else ps.setNull(7, Types.INTEGER);
            ps.setString(8, referenceType);
            if (referenceId != null) ps.setLong(9, referenceId); else ps.setNull(9, Types.BIGINT);
            ps.setTimestamp(10, Timestamp.valueOf(now));
            ps.setTimestamp(11, Timestamp.valueOf(now));
            return ps;
        }, keyHolder);
        Number key = keyHolder.getKey();
        return key != null ? key.intValue() : null;
    }

    private Integer resolveConsultationPatientId(Long idConsultation, Integer hopitalId) {
        try {
            Integer id = jdbcTemplate.queryForObject(
                    """
                    SELECT id_patient FROM consultations_medicales
                    WHERE id_consultation = ? AND id_hopital = ?
                    """,
                    Integer.class, idConsultation, hopitalId);
            if (id == null) {
                throw new ResourceNotFoundException("Consultation introuvable.");
            }
            return id;
        } catch (ResourceNotFoundException e) {
            throw e;
        } catch (Exception e) {
            throw new ResourceNotFoundException("Consultation introuvable.");
        }
    }

    private String storeBytes(Integer hopitalId, Integer idPatient, String fileName, byte[] bytes) {
        try {
            Path dir = Paths.get(uploadDir, String.valueOf(hopitalId), String.valueOf(idPatient));
            Files.createDirectories(dir);
            String unique = UUID.randomUUID().toString().substring(0, 8) + "_" + fileName;
            Path target = dir.resolve(unique);
            Files.write(target, bytes);
            return target.toString().replace('\\', '/');
        } catch (IOException e) {
            throw new IllegalStateException("Impossible d'enregistrer le fichier sur le serveur.", e);
        }
    }

    private String buildLabResume(MedecinDemandeAnalyseResponseDTO a) {
        StringBuilder sb = new StringBuilder();
        sb.append("Examen : ").append(StringUtils.hasText(a.getTestName()) ? a.getTestName() : "—").append("\n");
        if (StringUtils.hasText(a.getResultatTexte())) {
            sb.append("Résultat : ").append(a.getResultatTexte()).append("\n");
        }
        if (StringUtils.hasText(a.getInterpretation())) {
            sb.append("Interprétation : ").append(a.getInterpretation()).append("\n");
        }
        if (StringUtils.hasText(a.getValeursReference())) {
            sb.append("Références : ").append(a.getValeursReference()).append("\n");
        }
        if (a.getDate() != null) {
            sb.append("Date : ").append(a.getDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")));
        }
        return sb.toString().trim();
    }

    private String resolvePatientEmail(Patient patient, Integer hopitalId) {
        if (StringUtils.hasText(patient.getEmail())) {
            return patient.getEmail().trim();
        }
        return utilisateurRepository.findEmailByPatient(patient.getIdPatient().intValue(), hopitalId)
                .filter(StringUtils::hasText)
                .orElse(null);
    }

    private String resolveMedecinNom() {
        Integer idMedecin = currentUserService.getCurrentMedecinId();
        if (idMedecin == null) return "Votre médecin";
        Optional<Medecin> m = medecinRepository.trouverParId(idMedecin);
        if (m.isEmpty()) return "Votre médecin";
        Medecin med = m.get();
        return ("Dr " + (med.getPrenom() != null ? med.getPrenom() + " " : "")
                + (med.getNom() != null ? med.getNom() : "")).trim();
    }

    private Integer requireMedecinId() {
        Integer id = currentUserService.getCurrentMedecinId();
        if (id == null) {
            throw new ForbiddenException("Profil médecin requis pour partager des documents.");
        }
        return id;
    }

    private static String safeFileName(String original) {
        if (!StringUtils.hasText(original)) return "document.pdf";
        return original.replaceAll("[^A-Za-z0-9._-]", "_");
    }

    private static String maskEmail(String email) {
        if (email == null || !email.contains("@")) return "—";
        String[] parts = email.split("@", 2);
        String local = parts[0];
        String domain = parts[1];
        if (local.length() <= 2) return local.charAt(0) + "*@" + domain;
        return local.charAt(0) + "***" + local.charAt(local.length() - 1) + "@" + domain;
    }
}
