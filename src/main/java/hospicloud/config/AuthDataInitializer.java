package hospicloud.config;

import hospicloud.repositories.ConsultationMedicaleRepository;
import hospicloud.repositories.PasswordResetTokenRepository;
import hospicloud.repositories.PatientRepository;
import hospicloud.repositories.PharmacieMedicamentRepository;
import hospicloud.repositories.SignatureDocumentRepository;
import hospicloud.repositories.TeleconsultationChatRepository;
import hospicloud.repositories.UtilisateurRepository;
import hospicloud.servicesImpl.LiveKitService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
public class AuthDataInitializer implements ApplicationRunner {

    private static final Logger logger = LoggerFactory.getLogger(AuthDataInitializer.class);

    private final UtilisateurRepository utilisateurRepository;
    private final PatientRepository patientRepository;
    private final PharmacieMedicamentRepository pharmacieMedicamentRepository;
    private final ConsultationMedicaleRepository consultationMedicaleRepository;
    private final SignatureDocumentRepository signatureDocumentRepository;
    private final TeleconsultationChatRepository teleconsultationChatRepository;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final LiveKitService liveKitService;

    public AuthDataInitializer(UtilisateurRepository utilisateurRepository,
                               PatientRepository patientRepository,
                               PharmacieMedicamentRepository pharmacieMedicamentRepository,
                               ConsultationMedicaleRepository consultationMedicaleRepository,
                               SignatureDocumentRepository signatureDocumentRepository,
                               TeleconsultationChatRepository teleconsultationChatRepository,
                               PasswordResetTokenRepository passwordResetTokenRepository,
                               LiveKitService liveKitService) {
        this.utilisateurRepository = utilisateurRepository;
        this.patientRepository = patientRepository;
        this.pharmacieMedicamentRepository = pharmacieMedicamentRepository;
        this.consultationMedicaleRepository = consultationMedicaleRepository;
        this.signatureDocumentRepository = signatureDocumentRepository;
        this.teleconsultationChatRepository = teleconsultationChatRepository;
        this.passwordResetTokenRepository = passwordResetTokenRepository;
        this.liveKitService = liveKitService;
    }

    @Override
    public void run(ApplicationArguments args) {
        utilisateurRepository.ensureSchema();
        pharmacieMedicamentRepository.ensureSchema();
        signatureDocumentRepository.ensureSchema();
        consultationMedicaleRepository.ensureSchema();
        teleconsultationChatRepository.ensureSchema();
        passwordResetTokenRepository.ensureSchema();
        utilisateurRepository.seedIfEmpty();
        utilisateurRepository.syncDemoUsers();
        try {
            patientRepository.syncDemoPatients();
        } catch (Exception ex) {
            logger.error("Échec sync données patients démo (connexion possible): {}", ex.getMessage(), ex);
        }
        liveKitService.logConfigurationHint();
    }
}
