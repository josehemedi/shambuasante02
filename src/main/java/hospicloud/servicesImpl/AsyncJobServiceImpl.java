package hospicloud.servicesImpl;

import com.fasterxml.jackson.databind.ObjectMapper;
import hospicloud.async.AsyncJobMessage;
import hospicloud.async.AsyncJobResponse;
import hospicloud.async.AsyncJobStatus;
import hospicloud.async.AsyncJobType;
import hospicloud.exceptions.BadRequestException;
import hospicloud.exceptions.ResourceNotFoundException;
import hospicloud.repositories.AsyncJobRepository;
import hospicloud.services.AsyncJobService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@Service
public class AsyncJobServiceImpl implements AsyncJobService {

    private static final Logger log = LoggerFactory.getLogger(AsyncJobServiceImpl.class);

    private final AsyncJobRepository asyncJobRepository;
    private final RabbitTemplate rabbitTemplate;
    private final ObjectMapper objectMapper;

    @Value("${app.rabbit.exchange:rendezvous.exchange}")
    private String exchange;

    @Value("${app.async.storage-dir:}")
    private String storageDir;

    public AsyncJobServiceImpl(
            AsyncJobRepository asyncJobRepository,
            RabbitTemplate rabbitTemplate,
            ObjectMapper objectMapper) {
        this.asyncJobRepository = asyncJobRepository;
        this.rabbitTemplate = rabbitTemplate;
        this.objectMapper = objectMapper;
    }

    @Override
    @Transactional
    public AsyncJobResponse enqueueReport(AsyncJobType type, Integer idHopital, Integer actorUserId,
                                          Long entityId, Map<String, Object> payload) {
        return enqueue(type, "rapport." + type.name().toLowerCase(), idHopital, actorUserId, entityId, payload);
    }

    @Override
    @Transactional
    public AsyncJobResponse enqueueEnregistrement(AsyncJobType type, Integer idHopital, Integer actorUserId,
                                                  Map<String, Object> payload) {
        return enqueue(type, "enregistrement." + type.name().toLowerCase(), idHopital, actorUserId, null, payload);
    }

    private AsyncJobResponse enqueue(AsyncJobType type, String routingKey, Integer idHopital,
                                     Integer actorUserId, Long entityId, Map<String, Object> payload) {
        String jobId = UUID.randomUUID().toString().replace("-", "");
        String payloadJson;
        try {
            payloadJson = objectMapper.writeValueAsString(payload != null ? payload : Map.of());
        } catch (Exception e) {
            throw new BadRequestException("Payload invalide");
        }

        asyncJobRepository.insert(jobId, type, AsyncJobStatus.QUEUED, idHopital, actorUserId, entityId, payloadJson);

        AsyncJobMessage message = new AsyncJobMessage();
        message.setJobId(jobId);
        message.setType(type);
        message.setIdHopital(idHopital);
        message.setActorUserId(actorUserId);
        message.setEntityId(entityId);
        message.setPayload(payload);
        message.setRequestedAt(LocalDateTime.now());

        try {
            rabbitTemplate.convertAndSend(exchange, routingKey, message);
        } catch (Exception e) {
            asyncJobRepository.updateStatus(jobId, AsyncJobStatus.FAILED, "Impossible de publier le job: " + e.getMessage());
            log.error("Echec publication job {}: {}", jobId, e.getMessage());
            throw new BadRequestException("Broker indisponible — réessayez plus tard");
        }

        AsyncJobResponse response = new AsyncJobResponse();
        response.setJobId(jobId);
        response.setType(type);
        response.setStatus(AsyncJobStatus.QUEUED);
        response.setStatusUrl("/api/async/jobs/" + jobId);
        response.setDownloadUrl("/api/async/jobs/" + jobId + "/download");
        response.setCreatedAt(LocalDateTime.now());
        return response;
    }

    @Override
    public AsyncJobResponse getJob(String jobId) {
        AsyncJobResponse job = asyncJobRepository.findById(jobId)
                .orElseThrow(() -> new ResourceNotFoundException("Job introuvable: " + jobId));
        job.setStatusUrl("/api/async/jobs/" + jobId);
        if (job.getStatus() == AsyncJobStatus.SUCCEEDED && job.getResultRef() != null) {
            job.setDownloadUrl("/api/async/jobs/" + jobId + "/download");
        }
        return job;
    }

    @Override
    public byte[] loadReportBytes(String jobId) {
        AsyncJobResponse job = getJob(jobId);
        if (job.getStatus() != AsyncJobStatus.SUCCEEDED) {
            throw new BadRequestException("Le rapport n'est pas encore prêt (statut=" + job.getStatus() + ")");
        }
        String path = job.getResultRef();
        if (path == null || path.isBlank()) {
            throw new ResourceNotFoundException("Aucun fichier associé à ce job");
        }
        try {
            return Files.readAllBytes(Path.of(path));
        } catch (Exception e) {
            throw new ResourceNotFoundException("Fichier rapport introuvable");
        }
    }

    public String resolveStorageDir() {
        if (storageDir != null && !storageDir.isBlank()) {
            return storageDir;
        }
        String sys = System.getProperty("app.async.storage-dir");
        if (sys != null && !sys.isBlank()) {
            return sys;
        }
        return Path.of(System.getProperty("java.io.tmpdir"), "hospicloud-async").toString();
    }
}
