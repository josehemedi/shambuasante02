package hospicloud.servicesImpl;

import hospicloud.dtos.SignatureConsultationResponseDTO;
import hospicloud.dtos.SignerConsultationRequestDTO;
import hospicloud.dtos.events.TechnicalLogEvent;
import hospicloud.exceptions.ConsultationBusinessException;
import hospicloud.exceptions.DisabledAccountException;
import hospicloud.exceptions.ForbiddenException;
import hospicloud.exceptions.ResourceNotFoundException;
import hospicloud.model.ConsultationMedicale;
import hospicloud.model.Medecin;
import hospicloud.model.Role;
import hospicloud.model.SignatureDocument;
import hospicloud.model.Utilisateur;
import hospicloud.model.enums.ConsultationStatut;
import hospicloud.model.enums.StatutSignature;
import hospicloud.model.enums.TypeDocument;
import hospicloud.repositories.ConsultationMedicaleRepository;
import hospicloud.repositories.MedecinRepository;
import hospicloud.repositories.SignatureDocumentRepository;
import hospicloud.repositories.UtilisateurRepository;
import hospicloud.security.CurrentUserService;
import hospicloud.security.TenantAuthorization;
import hospicloud.services.ConsultationSignatureService;
import hospicloud.services.TechnicalLogService;
import hospicloud.utils.DocumentHashUtil;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class ConsultationSignatureServiceImpl implements ConsultationSignatureService {

    private static final String METHODE_MOT_DE_PASSE = "MOT_DE_PASSE";
    private static final DateTimeFormatter RESPONSE_DATE = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private final ConsultationMedicaleRepository consultationRepository;
    private final SignatureDocumentRepository signatureRepository;
    private final UtilisateurRepository utilisateurRepository;
    private final MedecinRepository medecinRepository;
    private final CurrentUserService currentUserService;
    private final PasswordEncoder passwordEncoder;
    private final TechnicalLogService technicalLogService;

    public ConsultationSignatureServiceImpl(
            ConsultationMedicaleRepository consultationRepository,
            SignatureDocumentRepository signatureRepository,
            UtilisateurRepository utilisateurRepository,
            MedecinRepository medecinRepository,
            CurrentUserService currentUserService,
            PasswordEncoder passwordEncoder,
            TechnicalLogService technicalLogService) {
        this.consultationRepository = consultationRepository;
        this.signatureRepository = signatureRepository;
        this.utilisateurRepository = utilisateurRepository;
        this.medecinRepository = medecinRepository;
        this.currentUserService = currentUserService;
        this.passwordEncoder = passwordEncoder;
        this.technicalLogService = technicalLogService;
    }

    @Override
    @Transactional
    public SignatureConsultationResponseDTO signerConsultation(
            Long consultationId,
            SignerConsultationRequestDTO request,
            HttpServletRequest httpRequest) {

        Integer hopitalId = currentUserService.getCurrentHopitalId();
        Integer medecinId = currentUserService.getCurrentMedecinId();
        Integer utilisateurId = currentUserService.getCurrentUtilisateurId();
        Role role = currentUserService.getCurrentRole();
        String ip = resolveClientIp(httpRequest);

        audit(hopitalId, utilisateurId, role, consultationId, ip,
                "TENTATIVE_SIGNATURE_CONSULTATION", "INFO", "Tentative de signature de consultation.");

        try {
            assertMedecinActif(role, medecinId, utilisateurId, hopitalId);

            if (request == null || !Boolean.TRUE.equals(request.getConfirmation())) {
                throw new ConsultationBusinessException(
                        "CONFIRMATION_REQUISE",
                        "La confirmation explicite est requise pour signer la consultation.");
            }
            if (request.getMotDePasse() == null || request.getMotDePasse().isBlank()) {
                throw new ConsultationBusinessException(
                        "MOT_DE_PASSE_REQUIS",
                        "Le mot de passe est requis pour confirmer la signature.");
            }

            ConsultationMedicale consultation = consultationRepository.findById(consultationId)
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Consultation introuvable dans votre établissement."));
            TenantAuthorization.assertSameTenant(consultation.getIdHopital());
            TenantAuthorization.assertMedecinScope(consultation.getIdMedecin());

            if (consultation.getStatut() == ConsultationStatut.SIGNEE) {
                throw new ConsultationBusinessException(
                        "CONSULTATION_DEJA_SIGNEE",
                        "Cette consultation est signée et ne peut plus être modifiée directement.",
                        HttpStatus.CONFLICT);
            }
            if (consultation.getStatut() == ConsultationStatut.ANNULEE) {
                throw new ConsultationBusinessException(
                        "CONSULTATION_ANNULEE",
                        "Cette consultation est annulée et ne peut pas être signée.");
            }

            assertConsultationComplete(consultation);

            signatureRepository.findActiveByDocument(
                    TypeDocument.CONSULTATION, consultationId, hopitalId).ifPresent(existing -> {
                throw new ConsultationBusinessException(
                        "CONSULTATION_DEJA_SIGNEE",
                        "Cette consultation possède déjà une signature active.",
                        HttpStatus.CONFLICT);
            });

            Utilisateur utilisateur = utilisateurRepository.findByIdAndHopitalId(utilisateurId, hopitalId)
                    .orElseThrow(() -> new ForbiddenException("Utilisateur introuvable dans votre établissement."));
            if (!utilisateur.isEstActif()) {
                throw new DisabledAccountException();
            }
            if (!passwordEncoder.matches(request.getMotDePasse(), utilisateur.getMotDePasse())) {
                audit(hopitalId, utilisateurId, role, consultationId, ip,
                        "SIGNATURE_CONSULTATION_ECHOUEE", "ECHEC", "Mot de passe incorrect.");
                throw new ConsultationBusinessException(
                        "MOT_DE_PASSE_INVALIDE",
                        "Mot de passe incorrect.",
                        HttpStatus.UNAUTHORIZED);
            }

            Medecin medecin = medecinRepository.trouverParId(medecinId)
                    .orElseThrow(() -> new ResourceNotFoundException("Médecin introuvable."));
            String nomMedecin = formatNomMedecin(medecin);

            String canonicalPayload = DocumentHashUtil.buildConsultationCanonicalPayload(consultation);
            String hash = DocumentHashUtil.sha256Hex(canonicalPayload);
            LocalDateTime signedAt = LocalDateTime.now();
            String reference = buildReference(hopitalId, consultationId, signedAt);

            SignatureDocument signature = new SignatureDocument();
            signature.setDocumentId(consultationId);
            signature.setTypeDocument(TypeDocument.CONSULTATION);
            signature.setMedecinId(medecinId);
            signature.setUtilisateurId(utilisateurId);
            signature.setNomMedecin(nomMedecin);
            signature.setHashDocument(hash);
            signature.setAdresseIp(ip);
            signature.setMethodeAuthentification(METHODE_MOT_DE_PASSE);
            signature.setDateSignature(signedAt);
            signature.setStatut(StatutSignature.VALIDEE);
            signature.setReferenceSignature(reference);

            signatureRepository.save(signature);
            consultationRepository.signerConsultation(consultationId, signedAt);

            audit(hopitalId, utilisateurId, role, consultationId, ip,
                    "SIGNATURE_CONSULTATION_REUSSIE", "SUCCES",
                    "Signature créée avec la référence " + reference + ".");

            SignatureConsultationResponseDTO response = new SignatureConsultationResponseDTO();
            response.setIdConsultation(consultationId);
            response.setStatut(ConsultationStatut.SIGNEE.name());
            response.setNomMedecin(nomMedecin);
            response.setNumeroOrdre(medecin.getNumeroOrdre());
            response.setDateSignature(RESPONSE_DATE.format(signedAt));
            response.setReferenceSignature(reference);
            response.setHashAbrege(DocumentHashUtil.abbreviateHash(hash));
            return response;

        } catch (ConsultationBusinessException | ForbiddenException | DisabledAccountException
                 | ResourceNotFoundException ex) {
            if (!(ex instanceof ConsultationBusinessException cbe
                  && "MOT_DE_PASSE_INVALIDE".equals(cbe.getCode()))) {
                audit(hopitalId, utilisateurId, role, consultationId, ip,
                        "SIGNATURE_CONSULTATION_ECHOUEE", "ECHEC", safeMessage(ex));
            }
            throw ex;
        } catch (RuntimeException ex) {
            audit(hopitalId, utilisateurId, role, consultationId, ip,
                    "SIGNATURE_CONSULTATION_ECHOUEE", "ECHEC", safeMessage(ex));
            throw ex;
        }
    }

    private void assertMedecinActif(Role role, Integer medecinId, Integer utilisateurId, Integer hopitalId) {
        if (role != Role.MEDECIN) {
            throw new ForbiddenException("Seul un médecin peut signer une consultation.");
        }
        if (medecinId == null || utilisateurId == null || hopitalId == null) {
            throw new ForbiddenException("Contexte médecin incomplet.");
        }
    }

    private void assertConsultationComplete(ConsultationMedicale consultation) {
        if (consultation.getDiagnostic() == null || consultation.getDiagnostic().isBlank()) {
            throw new ConsultationBusinessException(
                    "CONSULTATION_INCOMPLETE",
                    "Le diagnostic est obligatoire avant la signature.");
        }
        if (consultation.getIdPatient() == null || consultation.getIdMedecin() == null) {
            throw new ConsultationBusinessException(
                    "CONSULTATION_INCOMPLETE",
                    "La consultation est incomplète (patient ou médecin manquant).");
        }
    }

    private String buildReference(Integer hopitalId, Long consultationId, LocalDateTime signedAt) {
        return "SIG-CONS-" + hopitalId + "-" + consultationId + "-"
                + signedAt.format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
    }

    private String formatNomMedecin(Medecin medecin) {
        String nom = Stream.of(medecin.getPrenom(), medecin.getNom())
                .filter(Objects::nonNull)
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.joining(" "));
        if (nom.isBlank()) {
            return "Dr Médecin";
        }
        return nom.startsWith("Dr") ? nom : "Dr " + nom;
    }

    private void audit(Integer hopitalId, Integer utilisateurId, Role role, Long consultationId,
                       String ip, String action, String resultat, String details) {
        TechnicalLogEvent event = new TechnicalLogEvent();
        event.setHopitalId(hopitalId);
        event.setUserId(utilisateurId != null ? utilisateurId.longValue() : null);
        event.setUserRole(role != null ? role.name() : null);
        event.setModule("signatures");
        event.setAction(action);
        event.setEndpoint("/api/medecin/consultations/" + consultationId + "/signer");
        event.setHttpMethod("POST");
        event.setStatus(resultat);
        event.setMessage("typeDocument=CONSULTATION;documentId=" + consultationId + ";resultat=" + resultat
                + (details != null ? ";details=" + details : ""));
        event.setIpAddress(ip);
        technicalLogService.record(event);
    }

    private String resolveClientIp(HttpServletRequest request) {
        if (request == null) {
            return null;
        }
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) {
            return forwarded.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }

    private String safeMessage(Exception ex) {
        return ex.getMessage() != null ? ex.getMessage() : ex.getClass().getSimpleName();
    }
}
