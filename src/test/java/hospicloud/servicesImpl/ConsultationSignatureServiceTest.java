package hospicloud.servicesImpl;

import hospicloud.dtos.SignatureConsultationResponseDTO;
import hospicloud.dtos.SignerConsultationRequestDTO;
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
import hospicloud.model.enums.TypeDocument;
import hospicloud.repositories.ConsultationMedicaleRepository;
import hospicloud.repositories.MedecinRepository;
import hospicloud.repositories.SignatureDocumentRepository;
import hospicloud.repositories.UtilisateurRepository;
import hospicloud.security.CurrentUserService;
import hospicloud.services.TechnicalLogService;
import hospicloud.utils.DocumentHashUtil;
import hospicloud.security.CurrentUserContext;
import hospicloud.security.TenantContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ConsultationSignatureServiceTest {

    @Mock private ConsultationMedicaleRepository consultationRepository;
    @Mock private SignatureDocumentRepository signatureRepository;
    @Mock private UtilisateurRepository utilisateurRepository;
    @Mock private MedecinRepository medecinRepository;
    @Mock private CurrentUserService currentUserService;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private TechnicalLogService technicalLogService;

    @InjectMocks
    private ConsultationSignatureServiceImpl service;

    private ConsultationMedicale consultation;
    private Utilisateur utilisateur;
    private Medecin medecin;

    @BeforeEach
    void setUp() {
        TenantContext.setHopitalId(1);
        CurrentUserContext.setMedecinId(5);
        consultation = new ConsultationMedicale();
        consultation.setIdConsultation(10L);
        consultation.setIdHopital(1);
        consultation.setIdMedecin(5);
        consultation.setIdPatient(20);
        consultation.setDiagnostic("Hypertension contrôlée");
        consultation.setObservations("Suivi régulier");
        consultation.setStatut(ConsultationStatut.BROUILLON);
        consultation.setDateConsultation(LocalDateTime.of(2026, 7, 11, 10, 0));

        utilisateur = new Utilisateur();
        utilisateur.setIdUtilisateur(99);
        utilisateur.setIdHopital(1);
        utilisateur.setIdMedecin(5);
        utilisateur.setMotDePasse("encoded");
        utilisateur.setEstActif(true);
        utilisateur.setRole(Role.MEDECIN);

        medecin = new Medecin();
        medecin.setIdMedecin(5);
        medecin.setPrenom("Ngozi");
        medecin.setNom("Achebe");
        medecin.setNumeroOrdre("ORD-12345");
    }

    @AfterEach
    void tearDown() {
        TenantContext.clear();
        CurrentUserContext.clear();
    }

    private SignerConsultationRequestDTO validRequest() {
        SignerConsultationRequestDTO dto = new SignerConsultationRequestDTO();
        dto.setMotDePasse("shambua123");
        dto.setConfirmation(true);
        return dto;
    }

    private void stubMedecinContext() {
        when(currentUserService.getCurrentHopitalId()).thenReturn(1);
        when(currentUserService.getCurrentMedecinId()).thenReturn(5);
        when(currentUserService.getCurrentUtilisateurId()).thenReturn(99);
        when(currentUserService.getCurrentRole()).thenReturn(Role.MEDECIN);
    }

    @Test
    void medecinCanSignOwnConsultation() {
        stubMedecinContext();
        when(consultationRepository.findById(10L)).thenReturn(Optional.of(consultation));
        when(signatureRepository.findActiveByDocument(TypeDocument.CONSULTATION, 10L, 1))
                .thenReturn(Optional.empty());
        when(utilisateurRepository.findByIdAndHopitalId(99, 1)).thenReturn(Optional.of(utilisateur));
        when(passwordEncoder.matches("shambua123", "encoded")).thenReturn(true);
        when(medecinRepository.trouverParId(5)).thenReturn(Optional.of(medecin));
        when(signatureRepository.save(any(SignatureDocument.class))).thenAnswer(inv -> {
            SignatureDocument s = inv.getArgument(0);
            s.setId(1L);
            return s;
        });

        SignatureConsultationResponseDTO response = service.signerConsultation(10L, validRequest(), null);

        assertEquals("SIGNEE", response.getStatut());
        assertNotNull(response.getReferenceSignature());
        assertNotNull(response.getHashAbrege());
        verify(consultationRepository).signerConsultation(eq(10L), any(LocalDateTime.class));
        verify(technicalLogService, atLeastOnce()).record(any());
    }

    @Test
    void cannotSignAnotherMedecinConsultation() {
        stubMedecinContext();
        CurrentUserContext.setMedecinId(999);
        when(consultationRepository.findById(10L)).thenReturn(Optional.of(consultation));

        assertThrows(ForbiddenException.class,
                () -> service.signerConsultation(10L, validRequest(), null));
    }

    @Test
    void receptionistCannotSign() {
        when(currentUserService.getCurrentHopitalId()).thenReturn(1);
        when(currentUserService.getCurrentMedecinId()).thenReturn(null);
        when(currentUserService.getCurrentUtilisateurId()).thenReturn(50);
        when(currentUserService.getCurrentRole()).thenReturn(Role.RECEPTION);

        assertThrows(ForbiddenException.class,
                () -> service.signerConsultation(10L, validRequest(), null));
    }

    @Test
    void incompleteConsultationCannotBeSigned() {
        stubMedecinContext();
        consultation.setDiagnostic("");
        when(consultationRepository.findById(10L)).thenReturn(Optional.of(consultation));

        ConsultationBusinessException ex = assertThrows(ConsultationBusinessException.class,
                () -> service.signerConsultation(10L, validRequest(), null));
        assertEquals("CONSULTATION_INCOMPLETE", ex.getCode());
    }

    @Test
    void wrongPasswordBlocksSignature() {
        stubMedecinContext();
        when(consultationRepository.findById(10L)).thenReturn(Optional.of(consultation));
        when(signatureRepository.findActiveByDocument(TypeDocument.CONSULTATION, 10L, 1))
                .thenReturn(Optional.empty());
        when(utilisateurRepository.findByIdAndHopitalId(99, 1)).thenReturn(Optional.of(utilisateur));
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(false);

        ConsultationBusinessException ex = assertThrows(ConsultationBusinessException.class,
                () -> service.signerConsultation(10L, validRequest(), null));
        assertEquals("MOT_DE_PASSE_INVALIDE", ex.getCode());
        verify(consultationRepository, never()).signerConsultation(anyLong(), any());
    }

    @Test
    void alreadySignedConsultationIsRejected() {
        stubMedecinContext();
        consultation.setStatut(ConsultationStatut.SIGNEE);
        when(consultationRepository.findById(10L)).thenReturn(Optional.of(consultation));

        ConsultationBusinessException ex = assertThrows(ConsultationBusinessException.class,
                () -> service.signerConsultation(10L, validRequest(), null));
        assertEquals("CONSULTATION_DEJA_SIGNEE", ex.getCode());
        assertEquals(HttpStatus.CONFLICT, ex.getStatus());
    }

    @Test
    void hashIsGeneratedOnSignature() {
        stubMedecinContext();
        when(consultationRepository.findById(10L)).thenReturn(Optional.of(consultation));
        when(signatureRepository.findActiveByDocument(TypeDocument.CONSULTATION, 10L, 1))
                .thenReturn(Optional.empty());
        when(utilisateurRepository.findByIdAndHopitalId(99, 1)).thenReturn(Optional.of(utilisateur));
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(true);
        when(medecinRepository.trouverParId(5)).thenReturn(Optional.of(medecin));
        when(signatureRepository.save(any(SignatureDocument.class))).thenAnswer(inv -> inv.getArgument(0));

        service.signerConsultation(10L, validRequest(), null);

        ArgumentCaptor<SignatureDocument> captor = ArgumentCaptor.forClass(SignatureDocument.class);
        verify(signatureRepository).save(captor.capture());
        String hash = captor.getValue().getHashDocument();
        assertNotNull(hash);
        assertEquals(64, hash.length());
        assertEquals(hash, DocumentHashUtil.sha256Hex(
                DocumentHashUtil.buildConsultationCanonicalPayload(consultation)));
    }
}
