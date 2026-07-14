package hospicloud.security;

import hospicloud.exceptions.ForbiddenException;
import hospicloud.exceptions.ResourceNotFoundException;
import hospicloud.model.RendezVous;
import hospicloud.model.Role;
import hospicloud.repositories.RendezVousRepository;
import org.springframework.stereotype.Component;

@Component
public class TeleconsultationChatGuard {

    private final RendezVousRepository rendezVousRepository;

    public TeleconsultationChatGuard(RendezVousRepository rendezVousRepository) {
        this.rendezVousRepository = rendezVousRepository;
    }

    public RendezVous requireParticipant(Integer idRdv) {
        Role role = CurrentUserContext.getRole();
        if (role != Role.MEDECIN && role != Role.PATIENT) {
            throw new ForbiddenException("Seuls le médecin et le patient peuvent accéder au chat.");
        }

        RendezVous rdv = rendezVousRepository.trouverParId(idRdv);
        if (rdv == null) {
            throw new ResourceNotFoundException("Rendez-vous introuvable dans votre établissement.");
        }

        TenantAuthorization.assertSameTenant(rdv.getIdHopital());

        if (!"TELECONSULTATION".equalsIgnoreCase(rdv.getCanal())) {
            throw new ForbiddenException("Ce rendez-vous n'est pas une téléconsultation.");
        }

        String statut = rdv.getStatutRdv() != null ? rdv.getStatutRdv().toUpperCase() : "";
        if ("ANNULE".equals(statut) || "ABSENT".equals(statut)) {
            throw new ForbiddenException("Ce rendez-vous n'est plus actif.");
        }

        Integer medecinId = CurrentUserContext.getMedecinId();
        Integer patientId = CurrentUserContext.getPatientId();
        boolean isAssignedMedecin = medecinId != null && medecinId.equals(rdv.getIdMedecin());
        boolean isAssignedPatient = patientId != null && patientId.equals(rdv.getIdPatient());

        if (!isAssignedMedecin && !isAssignedPatient) {
            throw new ForbiddenException("Seuls le médecin et le patient de ce rendez-vous peuvent accéder au chat.");
        }

        return rdv;
    }

    public void assertTenantMatchesTopic(Integer requestedTenantId) {
        Integer tenantId = TenantContext.getRequiredHopitalId();
        if (!tenantId.equals(requestedTenantId)) {
            throw new ForbiddenException("Accès interdit à ce canal de discussion.");
        }
    }
}
