package hospicloud.servicesImpl;

import com.fasterxml.jackson.databind.ObjectMapper;
import hospicloud.async.AsyncJobMessage;
import hospicloud.async.AsyncJobStatus;
import hospicloud.async.AsyncJobType;
import hospicloud.repositories.AsyncJobRepository;
import hospicloud.security.TenantContext;
import hospicloud.servicesImpl.reportingimpl.JasperReportServiceImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

/**
 * Consommateur asynchrone des rapports PDF.
 * Les paramètres Jasper sont préparés à l'enqueue (HTTP), la génération lourde se fait ici.
 */
@Component
public class AsyncReportJobListener {

    private static final Logger log = LoggerFactory.getLogger(AsyncReportJobListener.class);

    private final AsyncJobRepository asyncJobRepository;
    private final AsyncJobServiceImpl asyncJobService;
    private final JasperReportServiceImpl jasperReportService;
    private final ObjectMapper objectMapper;
    private final CashierInvoiceReportService cashierInvoiceReportService;
    private final OrdonnanceServiceImpl ordonnanceService;

    public AsyncReportJobListener(
            AsyncJobRepository asyncJobRepository,
            AsyncJobServiceImpl asyncJobService,
            JasperReportServiceImpl jasperReportService,
            ObjectMapper objectMapper,
            @Lazy CashierInvoiceReportService cashierInvoiceReportService,
            @Lazy OrdonnanceServiceImpl ordonnanceService) {
        this.asyncJobRepository = asyncJobRepository;
        this.asyncJobService = asyncJobService;
        this.jasperReportService = jasperReportService;
        this.objectMapper = objectMapper;
        this.cashierInvoiceReportService = cashierInvoiceReportService;
        this.ordonnanceService = ordonnanceService;
    }

    @RabbitListener(queues = "${app.rabbit.rapport.queue:rapport.queue}")
    public void onReportJob(AsyncJobMessage message) {
        if (message == null || message.getJobId() == null) {
            return;
        }
        String jobId = message.getJobId();
        try {
            asyncJobRepository.updateStatus(jobId, AsyncJobStatus.RUNNING, null);
            if (message.getIdHopital() != null) {
                TenantContext.setHopitalId(message.getIdHopital());
            }

            byte[] pdf = generatePdf(message);
            Path dir = Path.of(asyncJobService.resolveStorageDir(), "reports");
            Files.createDirectories(dir);
            Path file = dir.resolve(jobId + ".pdf");
            Files.write(file, pdf);

            Map<String, Object> result = new HashMap<>();
            result.put("bytes", pdf.length);
            result.put("path", file.toAbsolutePath().toString());
            if (message.getEntityId() != null) {
                result.put("entityId", message.getEntityId());
            }
            asyncJobRepository.markSucceeded(
                    jobId, objectMapper.writeValueAsString(result), file.toAbsolutePath().toString());
            log.info("Rapport async OK job={} type={} size={}", jobId, message.getType(), pdf.length);
        } catch (Exception e) {
            log.error("Rapport async KO job={}: {}", jobId, e.getMessage(), e);
            asyncJobRepository.updateStatus(jobId, AsyncJobStatus.FAILED, e.getMessage());
        } finally {
            TenantContext.clear();
        }
    }

    private byte[] generatePdf(AsyncJobMessage message) throws Exception {
        AsyncJobType type = message.getType();
        Map<String, Object> payload = message.getPayload() != null ? message.getPayload() : Map.of();

        if (type == AsyncJobType.REPORT_CAISSE_FACTURE || type == AsyncJobType.REPORT_FACTURE) {
            Integer idFacture = message.getEntityId() != null
                    ? message.getEntityId().intValue()
                    : payload.get("idFacture") != null ? ((Number) payload.get("idFacture")).intValue() : null;
            return cashierInvoiceReportService.genererPdf(idFacture, message.getIdHopital());
        }

        if (type == AsyncJobType.REPORT_ORDONNANCE && message.getEntityId() != null) {
            return ordonnanceService.genererPdfOrdonnance(message.getEntityId());
        }

        String reportName = String.valueOf(payload.getOrDefault("reportName", defaultReportName(type)));
        Map<String, Object> params = new HashMap<>(payload);
        params.remove("reportName");
        return jasperReportService.generate(reportName, params, null);
    }

    private String defaultReportName(AsyncJobType type) {
        return switch (type) {
            case REPORT_ORDONNANCE -> "Ordonnance.jasper";
            case REPORT_CONSULTATION -> "Fiche_Consultation.jasper";
            case REPORT_DOSSIER_PATIENT -> "Dossier_Patient.jrxml";
            case REPORT_LISTE_PATIENTS -> "Liste_Patients.jrxml";
            case REPORT_CAISSE_DASHBOARD -> "Dashboard_Caissier.jrxml";
            case REPORT_MEDECIN_DASHBOARD -> "Dashboard_Medecin.jrxml";
            case REPORT_ABONNEMENTS -> "Abonnements_Paiements.jrxml";
            case REPORT_BULLETIN_SORTIE -> "Bulletin_Sortie.jasper";
            default -> "Facture_Patient.jrxml";
        };
    }
}
