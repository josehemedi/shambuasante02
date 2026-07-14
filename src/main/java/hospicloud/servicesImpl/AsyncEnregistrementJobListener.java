package hospicloud.servicesImpl;

import com.fasterxml.jackson.databind.ObjectMapper;
import hospicloud.async.AsyncJobMessage;
import hospicloud.async.AsyncJobStatus;
import hospicloud.async.AsyncJobType;
import hospicloud.model.Patient;
import hospicloud.repositories.AsyncJobRepository;
import hospicloud.repositories.PatientRepository;
import hospicloud.security.TenantContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Traitement asynchrone des enregistrements (patients / arrivées).
 */
@Component
public class AsyncEnregistrementJobListener {

    private static final Logger log = LoggerFactory.getLogger(AsyncEnregistrementJobListener.class);

    private final AsyncJobRepository asyncJobRepository;
    private final PatientRepository patientRepository;
    private final ObjectMapper objectMapper;

    public AsyncEnregistrementJobListener(
            AsyncJobRepository asyncJobRepository,
            PatientRepository patientRepository,
            ObjectMapper objectMapper) {
        this.asyncJobRepository = asyncJobRepository;
        this.patientRepository = patientRepository;
        this.objectMapper = objectMapper;
    }

    @RabbitListener(queues = "${app.rabbit.enregistrement.queue:enregistrement.queue}")
    public void onEnregistrement(AsyncJobMessage message) {
        if (message == null || message.getJobId() == null) {
            return;
        }
        String jobId = message.getJobId();
        try {
            asyncJobRepository.updateStatus(jobId, AsyncJobStatus.RUNNING, null);
            if (message.getIdHopital() != null) {
                TenantContext.setHopitalId(message.getIdHopital());
            }

            Map<String, Object> result = switch (message.getType()) {
                case ENREGISTREMENT_PATIENT -> registerPatient(message);
                case ENREGISTREMENT_ARRIVEE -> registerArrivee(message);
                case ENREGISTREMENT_UTILISATEUR -> Map.of("note", "Délégué au service utilisateurs sync + file d'audit");
                default -> throw new IllegalArgumentException("Type enregistrement non supporté: " + message.getType());
            };

            asyncJobRepository.markSucceeded(jobId, objectMapper.writeValueAsString(result), null);
            log.info("Enregistrement async OK job={} type={}", jobId, message.getType());
        } catch (Exception e) {
            log.error("Enregistrement async KO job={}: {}", jobId, e.getMessage(), e);
            asyncJobRepository.updateStatus(jobId, AsyncJobStatus.FAILED, e.getMessage());
        } finally {
            TenantContext.clear();
        }
    }

    private Map<String, Object> registerPatient(AsyncJobMessage message) {
        Map<String, Object> p = message.getPayload() != null ? message.getPayload() : Map.of();
        Patient patient = new Patient();
        patient.setNom(asString(p.get("nom")));
        patient.setPrenom(asString(p.get("prenom")));
        patient.setSexe(asString(p.get("sexe")));
        if (p.get("dateNaissance") != null) {
            patient.setDateNaissance(LocalDate.parse(String.valueOf(p.get("dateNaissance"))));
        }
        patient.setTelephone(asString(p.get("telephone")));
        patient.setEmail(asString(p.get("email")));
        patient.setAdresse(asString(p.get("adresse")));
        patient.setDateEnregistrement(LocalDateTime.now());
        if (message.getActorUserId() != null) {
            patient.setCreePar(message.getActorUserId());
        }
        if (message.getIdHopital() != null) {
            patient.setIdHopital(message.getIdHopital());
        }

        patientRepository.enregistrerPatient(patient);

        Map<String, Object> result = new HashMap<>();
        result.put("idPatient", patient.getIdPatient());
        result.put("codePatient", patient.getCodePatient());
        result.put("nomComplet", patient.getPrenom() + " " + patient.getNom());
        return result;
    }

    private Map<String, Object> registerArrivee(AsyncJobMessage message) {
        // L'arrivée walk-in reste orchestrée côté réception ; ici on journalise l'async
        // et on crée le patient si payload.creerPatient=true.
        Map<String, Object> p = message.getPayload() != null ? message.getPayload() : Map.of();
        if (Boolean.TRUE.equals(p.get("creerPatient")) || "true".equalsIgnoreCase(String.valueOf(p.get("creerPatient")))) {
            return registerPatient(message);
        }
        Map<String, Object> result = new HashMap<>();
        result.put("idPatient", p.get("idPatient"));
        result.put("recorded", true);
        result.put("at", LocalDateTime.now().toString());
        return result;
    }

    private String asString(Object value) {
        return value == null ? null : String.valueOf(value);
    }
}
