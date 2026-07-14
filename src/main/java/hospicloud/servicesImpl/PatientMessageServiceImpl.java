package hospicloud.servicesImpl;

import hospicloud.dtos.patient.PatientMessageConversationDTO;
import hospicloud.exceptions.ForbiddenException;
import hospicloud.exceptions.patient.PatientNotFoundException;
import hospicloud.repositories.PatientMessageRepository;
import hospicloud.repositories.PatientRepository;
import hospicloud.security.CurrentUserContext;
import hospicloud.security.TenantAuthorization;
import hospicloud.security.TenantContext;
import hospicloud.services.PatientMessageService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class PatientMessageServiceImpl implements PatientMessageService {

    private final PatientMessageRepository patientMessageRepository;
    private final PatientRepository patientRepository;

    public PatientMessageServiceImpl(PatientMessageRepository patientMessageRepository,
                                     PatientRepository patientRepository) {
        this.patientMessageRepository = patientMessageRepository;
        this.patientRepository = patientRepository;
    }

    @Override
    public List<PatientMessageConversationDTO> listConversations() {
        Integer idPatient = CurrentUserContext.getPatientId();
        if (idPatient == null) {
            throw new ForbiddenException("Profil patient requis.");
        }
        TenantAuthorization.assertPatientOwns(idPatient);

        Integer hopitalId = TenantContext.getRequiredHopitalId();
        patientRepository.trouverPatientParId(idPatient.longValue())
                .orElseThrow(() -> new PatientNotFoundException(idPatient));

        return patientMessageRepository.listConversations(idPatient, hopitalId);
    }
}
