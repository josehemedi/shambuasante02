package hospicloud.servicesImpl;

import hospicloud.dtos.lab.LabDashboardStatsDTO;
import hospicloud.dtos.lab.ResultatAnalyseCritiqueDTO;
import hospicloud.model.lab.CommandeAnalyse;
import hospicloud.repositories.LabDashboardRepository;
import hospicloud.security.TenantContext;
import hospicloud.services.LabDashboardService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class LabDashboardServiceImpl implements LabDashboardService {

    private final LabDashboardRepository labDashboardRepository;
    private final org.springframework.messaging.simp.SimpMessagingTemplate messagingTemplate;

    public LabDashboardServiceImpl(LabDashboardRepository labDashboardRepository, org.springframework.messaging.simp.SimpMessagingTemplate messagingTemplate) {
        this.labDashboardRepository = labDashboardRepository;
        this.messagingTemplate = messagingTemplate;
    }

    private String getTenantId() {
        Integer hopitalId = TenantContext.getRequiredHopitalId();
        return String.valueOf(hopitalId); // Conversion String pour id_locataire CHAR(36)
    }

    @Override
    @Transactional(readOnly = true)
    public LabDashboardStatsDTO getDashboardStats() {
        return labDashboardRepository.getStatsByLocataire(getTenantId());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ResultatAnalyseCritiqueDTO> getResultatsCritiques(int page, int size) {
        int offset = page * size;
        return labDashboardRepository.getResultatsCritiquesNonAcquittes(getTenantId(), size, offset);
    }

    @Override
    @Transactional
    public void acquitterResultat(String idResultat) {
        String tenantId = getTenantId();
        labDashboardRepository.acquitterResultatCritique(idResultat, tenantId);
        
        // Push websocket event to update technician dashboard in real-time
        messagingTemplate.convertAndSend("/topic/lab-updates/" + tenantId, "CRITICAL_RESULT_ACKNOWLEDGED");
    }

    @Override
    @Transactional(readOnly = true)
    public List<CommandeAnalyse> listerCommandes(String statut, int page, int size) {
        int offset = page * size;
        return labDashboardRepository.getCommandesAnalyses(getTenantId(), statut, size, offset);
    }
}
