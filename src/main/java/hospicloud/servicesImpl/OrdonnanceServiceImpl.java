package hospicloud.servicesImpl;

import hospicloud.dtos.OrdonnanceRequest;
import hospicloud.dtos.OrdonnanceEnvoiResponse;
import hospicloud.exceptions.BadRequestException;
import hospicloud.exceptions.ForbiddenException;
import hospicloud.model.Hopital;
import hospicloud.model.Ordonnance;
import hospicloud.model.Patient;
import hospicloud.repositories.HopitalRepository;
import hospicloud.repositories.MedecinRepository;
import hospicloud.repositories.OrdonnanceRepository;
import hospicloud.repositories.PatientRepository;
import hospicloud.security.CurrentUserContext;
import hospicloud.security.TenantAuthorization;
import hospicloud.security.TenantContext;
import hospicloud.services.OrdonnanceService;
import hospicloud.services.RealtimeNotificationService;
import hospicloud.services.reporting.ReportGenerator;
import hospicloud.utils.NotificationService;
import hospicloud.utils.QrCodeService;
import hospicloud.utils.TenantReportParamsHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.awt.image.BufferedImage;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class OrdonnanceServiceImpl implements OrdonnanceService {

    private static final DateTimeFormatter EXPIRATION_FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private final OrdonnanceRepository repository;
    private final MedecinRepository medecinRepository;
    private final HopitalRepository hopitalRepository;
    private final PatientRepository patientRepository;
    private final ReportGenerator reportGenerator;
    private final NotificationService notificationService;
    private final RealtimeNotificationService realtimeNotificationService;

    @Autowired
    public OrdonnanceServiceImpl(
            OrdonnanceRepository repository,
            MedecinRepository medecinRepository,
            HopitalRepository hopitalRepository,
            PatientRepository patientRepository,
            ReportGenerator reportGenerator,
            NotificationService notificationService,
            RealtimeNotificationService realtimeNotificationService) {
        this.repository = repository;
        this.medecinRepository = medecinRepository;
        this.hopitalRepository = hopitalRepository;
        this.patientRepository = patientRepository;
        this.reportGenerator = reportGenerator;
        this.notificationService = notificationService;
        this.realtimeNotificationService = realtimeNotificationService;
    }

    @Override
    @Transactional
    public void creerOrdonnance(OrdonnanceRequest request) {
        Ordonnance ordonnance = new Ordonnance();
        ordonnance.setIdPatient(request.getIdPatient());
        ordonnance.setIdMedecin(request.getIdMedecin());
        ordonnance.setContenuOrdonnance(request.getContenuOrdonnance());
        ordonnance.setDiagnostic(request.getDiagnostic());
        ordonnance.setObservations(request.getObservations());
        ordonnance.setDateExpiration(request.getDateExpiration());
        ordonnance.setHospitalId(TenantContext.getRequiredHopitalId());
        ordonnance.setDatePrescription(LocalDateTime.now());
        ordonnance.setStatut("ACTIVE");
        repository.creerOrdonnance(ordonnance);
    }

    @Override
    public Optional<Ordonnance> trouverParId(Long idOrdonnance) {
        return repository.trouverParId(idOrdonnance);
    }

    @Override
    public Map<String, Object> getOrdonnanceParams(Long idOrdonnance) {
        return getOrdonnanceParamsFromOrdonnance(idOrdonnance);
    }

    @Override
    public Map<String, Object> getOrdonnanceParamsFromOrdonnance(Long idOrdonnance) {
        Ordonnance ordonnance = repository.trouverParId(idOrdonnance)
                .orElseThrow(() -> new IllegalArgumentException("Ordonnance non trouvée: " + idOrdonnance));

        TenantAuthorization.assertSameTenant(ordonnance.getHospitalId());

        var medecin = medecinRepository.trouverParId(ordonnance.getIdMedecin())
                .orElseThrow(() -> new IllegalArgumentException("Médecin introuvable: " + ordonnance.getIdMedecin()));
        Patient patient = patientRepository.trouverPatientParId((long) ordonnance.getIdPatient())
                .orElseThrow(() -> new IllegalArgumentException("Patient introuvable: " + ordonnance.getIdPatient()));

        Hopital hopital = TenantReportParamsHelper.resolveActiveHopital(
                hopitalRepository,
                ordonnance.getHospitalId());

        String prenom = medecin.getPrenom() != null ? medecin.getPrenom().trim() : "";
        String nom = medecin.getNom() != null ? medecin.getNom().trim() : "";
        String nomMedecin;
        if (!prenom.isEmpty() && !nom.isEmpty()) {
            nomMedecin = "Dr " + prenom + " " + nom;
        } else if (!nom.isEmpty()) {
            nomMedecin = "Dr " + nom;
        } else {
            nomMedecin = "Dr Médecin";
        }

        String nomPatient = Stream.of(patient.getPrenom(), patient.getNom())
                .filter(s -> s != null && !s.isBlank())
                .collect(Collectors.joining(" "))
                .trim();
        if (nomPatient.isBlank()) {
            nomPatient = "Patient";
        }

        String ref = "ORD-" + ordonnance.getHospitalId() + "-" + ordonnance.getIdOrdonnance();
        String qrPayload = "SHAMBUA|ORD|" + ordonnance.getHospitalId() + "|" + ordonnance.getIdOrdonnance() + "|" + ref;

        Map<String, Object> params = new HashMap<>();
        TenantReportParamsHelper.applyTenantBranding(params, hopital, ordonnance.getHospitalId());
        params.put("NOM_PATIENT", nomPatient);
        params.put("AGE_PATIENT", formatAgePatient(patient.getDateNaissance()));
        params.put("SEXE_PATIENT", patient.getSexe() != null && !patient.getSexe().isBlank() ? patient.getSexe() : "—");
        params.put("CODE_PATIENT", patient.getIdPatient() != null ? "PAT-" + patient.getIdPatient() : "—");
        params.put("NOM_MEDECIN", nomMedecin);
        params.put(
                "SPECIALITE_MEDECIN",
                medecin.getSpecialite() != null && !medecin.getSpecialite().isBlank()
                        ? medecin.getSpecialite()
                        : "—");
        params.put(
                "NUMERO_ORDRE_MEDECIN",
                medecin.getNumeroOrdre() != null && !medecin.getNumeroOrdre().isBlank()
                        ? medecin.getNumeroOrdre()
                        : "—");
        params.put("REF_ORDONNANCE", ref);
        params.put("STATUT_ORDONNANCE", ordonnance.getStatut() != null ? ordonnance.getStatut() : "ACTIVE");
        params.put(
                "DIAGNOSTIC",
                ordonnance.getDiagnostic() != null && !ordonnance.getDiagnostic().isBlank()
                        ? ordonnance.getDiagnostic()
                        : "—");
        params.put(
                "OBSERVATIONS",
                ordonnance.getObservations() != null && !ordonnance.getObservations().isBlank()
                        ? ordonnance.getObservations()
                        : "—");
        params.put(
                "DATE_EXPIRATION",
                ordonnance.getDateExpiration() != null
                        ? ordonnance.getDateExpiration().format(EXPIRATION_FMT)
                        : "Selon durée du traitement");

        java.util.Date datePrescription;
        if (ordonnance.getDatePrescription() != null) {
            datePrescription = java.sql.Timestamp.valueOf(ordonnance.getDatePrescription());
        } else {
            datePrescription = new java.util.Date();
        }
        params.put("DATE_PRESCRIPTION", datePrescription);
        params.put(
                "DATE_PRESCRIPTION_TEXTE",
                new java.text.SimpleDateFormat("dd/MM/yyyy 'à' HH:mm").format(datePrescription));

        params.put(
                "TELEPHONE_HOPITAL",
                hopital.getTelephone() != null && !hopital.getTelephone().isBlank()
                        ? hopital.getTelephone().trim()
                        : "—");
        params.put(
                "EMAIL_HOPITAL",
                hopital.getEmail() != null && !hopital.getEmail().isBlank()
                        ? hopital.getEmail().trim()
                        : "—");
        params.put(
                "TELEPHONE_MEDECIN",
                medecin.getTelephonePro() != null && !medecin.getTelephonePro().isBlank()
                        ? medecin.getTelephonePro().trim()
                        : "—");
        params.put(
                "MENTIONS_LEGALES",
                "Document médical confidentiel multi-tenant. Réservé au patient nommé et au professionnel de santé destinataire. "
                        + "Toute falsification, reproduction non autorisée ou usage hors établissement émetteur (tenant #"
                        + ordonnance.getHospitalId()
                        + ") est interdite. Conservez l'original ou le PDF authentifié par QR.");

        String contenu = ordonnance.getContenuOrdonnance();
        if (contenu == null || contenu.isBlank()) {
            contenu = ordonnance.getDiagnostic() != null ? ordonnance.getDiagnostic() : "";
        }
        params.put("contenuOrdonnance", contenu);
        params.put("LOGO_HOPITAL", loadLogoInputStream(hopital));
        params.put("QR_CODE_TEXTE", qrPayload);

        try {
            BufferedImage qrImage = QrCodeService.generateBufferedImage(qrPayload, 220);
            params.put("QR_CODE_IMAGE", qrImage);
        } catch (Exception e) {
            params.put("QR_CODE_IMAGE", null);
        }

        return params;
    }

    @Override
    public byte[] genererPdfOrdonnance(Long idOrdonnance) {
        Ordonnance ordonnance = repository.trouverParId(idOrdonnance)
                .orElseThrow(() -> new IllegalArgumentException("Ordonnance non trouvée: " + idOrdonnance));

        Authentication auth = org.springframework.security.core.context.SecurityContextHolder
                .getContext().getAuthentication();
        boolean asyncWorker = TenantContext.getHopitalId() != null
                && (auth == null || !(auth.getPrincipal() instanceof hospicloud.security.UtilisateurPrincipal));

        if (!asyncWorker) {
            var role = CurrentUserContext.getRole();
            if (role == hospicloud.model.Role.PATIENT) {
                TenantAuthorization.assertPatientOwns(ordonnance.getIdPatient());
            } else {
                TenantAuthorization.assertStaffRole();
            }
        }

        Map<String, Object> params = getOrdonnanceParamsFromOrdonnance(idOrdonnance);
        try {
            return reportGenerator.generate("Ordonnance.jasper", params, null);
        } catch (Exception e) {
            throw new IllegalStateException("Impossible de générer l'ordonnance PDF (JasperReports).", e);
        }
    }

    @Override
    public List<Ordonnance> listerParPatient(Integer idPatient) {
        return repository.listerParPatient(idPatient);
    }

    @Override
    public List<Ordonnance> listerParMedecin(Integer idMedecin) {
        return repository.listerParMedecin(idMedecin);
    }

    @Override
    public void renouvelerOrdonnance(Long idAncienne, OrdonnanceRequest nouvelleReq) {
        Optional<Ordonnance> ancienne = repository.trouverParId(idAncienne);
        if (ancienne.isEmpty()) {
            throw new IllegalArgumentException("Ordonnance inexistante");
        }
        repository.mettreAJourStatut(idAncienne, "RENOUVELEE");
        creerOrdonnance(nouvelleReq);
    }

    @Override
    public void annulerOrdonnance(Long idOrdonnance) {
        Ordonnance ordonnance = repository.trouverParId(idOrdonnance)
                .orElseThrow(() -> new IllegalArgumentException("Ordonnance introuvable"));
        if ("RENOUVELEE".equals(ordonnance.getStatut())) {
            throw new IllegalStateException("Impossible d'annuler une ordonnance déjà renouvelée.");
        }
        repository.mettreAJourStatut(idOrdonnance, "ANNULEE");
    }

    @Override
    @Transactional(readOnly = true)
    public OrdonnanceEnvoiResponse envoyerAuPatient(Long idOrdonnance, Integer idMedecinConnecte) {
        if (idMedecinConnecte == null) {
            throw new ForbiddenException(
                    "Aucun profil médecin n'est associé à votre compte. Contactez l'administrateur.");
        }

        Ordonnance ordonnance = repository.trouverParId(idOrdonnance)
                .orElseThrow(() -> new BadRequestException("Ordonnance introuvable."));

        TenantAuthorization.assertSameTenant(ordonnance.getHospitalId());
        TenantAuthorization.assertMedecinScope(ordonnance.getIdMedecin());

        if (!idMedecinConnecte.equals(ordonnance.getIdMedecin())) {
            throw new ForbiddenException("Vous ne pouvez envoyer que vos propres ordonnances.");
        }

        String statut = ordonnance.getStatut() != null ? ordonnance.getStatut().toUpperCase() : "ACTIVE";
        if ("ANNULEE".equals(statut)) {
            throw new BadRequestException("Impossible d'envoyer une ordonnance annulée.");
        }

        Patient patient = patientRepository.trouverPatientParId((long) ordonnance.getIdPatient())
                .orElseThrow(() -> new BadRequestException("Patient introuvable pour cette ordonnance."));

        String email = patient.getEmail() != null ? patient.getEmail().trim() : "";
        if (!StringUtils.hasText(email) || !email.contains("@")) {
            throw new BadRequestException(
                    "Ce patient n'a pas d'adresse e-mail renseignée. Mettez à jour sa fiche puis réessayez.");
        }

        var medecin = medecinRepository.trouverParId(ordonnance.getIdMedecin())
                .orElseThrow(() -> new BadRequestException("Médecin prescripteur introuvable."));

        Hopital hopital = TenantReportParamsHelper.resolveActiveHopital(
                hopitalRepository,
                ordonnance.getHospitalId());

        String prenom = medecin.getPrenom() != null ? medecin.getPrenom().trim() : "";
        String nom = medecin.getNom() != null ? medecin.getNom().trim() : "";
        String nomMedecin;
        if (!prenom.isEmpty() && !nom.isEmpty()) {
            nomMedecin = "Dr " + prenom + " " + nom;
        } else if (!nom.isEmpty()) {
            nomMedecin = "Dr " + nom;
        } else {
            nomMedecin = "Dr Médecin";
        }

        String nomPatient = Stream.of(patient.getPrenom(), patient.getNom())
                .filter(s -> s != null && !s.isBlank())
                .collect(Collectors.joining(" "))
                .trim();
        if (nomPatient.isBlank()) {
            nomPatient = ordonnance.getNomPatient() != null ? ordonnance.getNomPatient() : "Patient";
        }

        String ref = ordonnance.getNumeroOrdonnance();
        if (ref == null || ref.isBlank()) {
            ref = "ORD-" + ordonnance.getHospitalId() + "-" + ordonnance.getIdOrdonnance();
        }

        String dateLabel = "—";
        if (ordonnance.getDatePrescription() != null) {
            dateLabel = ordonnance.getDatePrescription()
                    .format(DateTimeFormatter.ofPattern("dd/MM/yyyy 'à' HH:mm"));
        }

        byte[] pdf = genererPdfOrdonnance(idOrdonnance);
        if (pdf == null || pdf.length == 0) {
            throw new IllegalStateException("Le PDF de l'ordonnance n'a pas pu être généré.");
        }

        String nomHopital = hopital != null && StringUtils.hasText(hopital.getNom())
                ? hopital.getNom().trim()
                : "Shambua Santé";

        try {
            notificationService.notifierOrdonnancePatient(
                    email,
                    nomPatient,
                    nomMedecin,
                    nomHopital,
                    ref,
                    dateLabel,
                    ordonnance.getDiagnostic(),
                    pdf);
        } catch (RuntimeException e) {
            throw new IllegalStateException(
                    "L'envoi de l'ordonnance a échoué. Vérifiez la configuration e-mail de la plateforme.",
                    e);
        }

        try {
            realtimeNotificationService.notifyOrdonnanceEnvoyeeAuPatient(
                    ordonnance.getHospitalId(),
                    ordonnance.getIdPatient(),
                    idOrdonnance,
                    ref,
                    nomMedecin);
        } catch (Exception e) {
            // L'e-mail a déjà été envoyé : ne pas faire échouer l'opération pour une notif live.
        }

        OrdonnanceEnvoiResponse response = new OrdonnanceEnvoiResponse();
        response.setIdOrdonnance(idOrdonnance);
        response.setNumeroOrdonnance(ref);
        response.setNomPatient(nomPatient);
        response.setEmailMasque(maskEmail(email));
        response.setEnvoyeLe(LocalDateTime.now());
        response.setMessage("Ordonnance transmise avec succès au patient.");
        return response;
    }

    private static String maskEmail(String email) {
        if (email == null || !email.contains("@")) {
            return "***";
        }
        String[] parts = email.split("@", 2);
        String local = parts[0];
        String domain = parts[1];
        if (local.length() <= 1) {
            return "*@" + domain;
        }
        if (local.length() == 2) {
            return local.charAt(0) + "*@" + domain;
        }
        return local.charAt(0) + "***" + local.charAt(local.length() - 1) + "@" + domain;
    }

    private String formatAgePatient(LocalDate dateNaissance) {
        if (dateNaissance == null) {
            return "—";
        }
        return Period.between(dateNaissance, LocalDate.now()).getYears() + " ans";
    }

    private java.io.InputStream loadLogoInputStream(Hopital hopital) {
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
            return null;
        } catch (Exception e) {
            return null;
        }
    }
}
