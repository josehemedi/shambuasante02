package hospicloud.servicesImpl;

import com.fasterxml.jackson.databind.ObjectMapper;
import hospicloud.dtos.AiChatMessageDTO;
import hospicloud.dtos.AiChatRequestDTO;
import hospicloud.dtos.AiChatResponseDTO;
import hospicloud.dtos.AiStatusDTO;
import hospicloud.dtos.rag.RagContextBundle;
import hospicloud.exceptions.BadRequestException;
import hospicloud.exceptions.ResourceNotFoundException;
import hospicloud.model.Role;
import hospicloud.repositories.rag.RagUsageRepository;
import hospicloud.security.CurrentUserContext;
import hospicloud.security.CurrentUserService;
import hospicloud.security.TenantContext;
import hospicloud.services.AiClinicalAssistantService;
import hospicloud.services.rag.RagRetrievalService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Service
public class AiClinicalAssistantServiceImpl implements AiClinicalAssistantService {

    private static final Logger logger = LoggerFactory.getLogger(AiClinicalAssistantServiceImpl.class);

    private final ChatClient chatClient;
    private final String apiKey;
    private final String model;
    private final boolean enabled;
    private final RagRetrievalService ragRetrievalService;
    private final RagUsageRepository ragUsageRepository;
    private final CurrentUserService currentUserService;
    private final ObjectMapper objectMapper;

    public AiClinicalAssistantServiceImpl(
            @Autowired(required = false) ChatClient.Builder chatClientBuilder,
            @Value("${spring.ai.openai.api-key:}") String apiKey,
            @Value("${spring.ai.openai.chat.options.model:gpt-4o-mini}") String model,
            @Value("${app.ai.enabled:true}") boolean enabled,
            RagRetrievalService ragRetrievalService,
            RagUsageRepository ragUsageRepository,
            CurrentUserService currentUserService,
            ObjectMapper objectMapper) {
        this.apiKey = apiKey != null ? apiKey.trim() : "";
        this.model = model;
        this.enabled = enabled;
        this.ragRetrievalService = ragRetrievalService;
        this.ragUsageRepository = ragUsageRepository;
        this.currentUserService = currentUserService;
        this.objectMapper = objectMapper;
        this.chatClient = chatClientBuilder != null && isConfigured() ? chatClientBuilder.build() : null;
        if (!isConfigured()) {
            logger.warn("Assistant IA désactivé : OPENAI_API_KEY absente ou invalide.");
        } else {
            logger.info("Assistant IA OpenAI + RAG prêt (modèle {}).", model);
        }
    }

    @Override
    public AiStatusDTO getStatus() {
        AiStatusDTO status = new AiStatusDTO();
        status.setConfigured(isConfigured());
        status.setAvailable(chatClient != null);
        status.setModel(model);
        return status;
    }

    @Override
    public List<String> getSuggestedPrompts() {
        Role role = CurrentUserContext.getRole();
        if (role == Role.SUPER_ADMIN) {
            return List.of(
                    "Résumer l'état de la plateforme et la disponibilité",
                    "Quels sont les plans souscrits et le volume d'usage RAG ?",
                    "Quelles erreurs techniques RAG récentes ?",
                    "Quelle est la consommation API OpenAI du mois ?"
            );
        }
        if (role == Role.TENANT_ADMIN) {
            return List.of(
                    "Lister les documents RAG actifs et expirés",
                    "Quels modèles IA et quotas sont configurés ?",
                    "Résumer le journal d'utilisation de l'assistant",
                    "Quelles erreurs de l'assistant ce mois-ci ?"
            );
        }
        return List.of(
                "Résumer le dossier du patient sélectionné",
                "Signaler les allergies enregistrées",
                "Comparer les résultats de laboratoire récents aux anciens",
                "Préparer un résumé de consultation",
                "Quels examens mentionne le protocole d'urgence ?",
                "Quelles informations importantes manquent dans le dossier ?"
        );
    }

    @Override
    public AiChatResponseDTO chat(AiChatRequestDTO request) {
        if (!isConfigured()) {
            throw new ResourceNotFoundException(
                    "Assistant IA non configuré. Définissez OPENAI_API_KEY sur le serveur.");
        }
        if (chatClient == null) {
            throw new BadRequestException("Le service OpenAI n'est pas disponible pour le moment.");
        }

        Integer hopitalId = TenantContext.getHopitalId();
        Role role = CurrentUserContext.getRole();
        String analysisType = normalizeAnalysisType(request.getAnalysisType());
        Long patientId = request.getPatientId();

        // Isolation : super-admin / admin hôpital ne reçoivent jamais le dossier clinique via patientId
        if (role == Role.SUPER_ADMIN || role == Role.TENANT_ADMIN) {
            patientId = null;
        }

        RagContextBundle rag = ragRetrievalService.buildContext(patientId, analysisType, request.getMessage());
        int contextChars = rag.getContextText() != null ? rag.getContextText().length() : 0;
        int promptChars = request.getMessage() != null ? request.getMessage().length() : 0;

        List<Message> messages = new ArrayList<>();
        messages.add(new SystemMessage(buildSystemPrompt(hopitalId, role, analysisType, rag)));
        if (rag.getContextText() != null && !rag.getContextText().isBlank()) {
            messages.add(new SystemMessage(
                    "CONTEXTE RAG RÉCUPÉRÉ (source de vérité pour cet établissement) :\n" + rag.getContextText()));
        }

        if (request.getHistory() != null) {
            for (AiChatMessageDTO item : request.getHistory()) {
                if (item == null || item.getContent() == null || item.getContent().isBlank()) continue;
                String itemRole = item.getRole() != null ? item.getRole().toLowerCase(Locale.ROOT) : "user";
                switch (itemRole) {
                    case "assistant" -> messages.add(new AssistantMessage(item.getContent().trim()));
                    case "system" -> messages.add(new SystemMessage(item.getContent().trim()));
                    default -> messages.add(new UserMessage(item.getContent().trim()));
                }
            }
        }
        messages.add(new UserMessage(request.getMessage().trim()));

        try {
            String content = chatClient.prompt().messages(messages).call().content();
            AiChatResponseDTO response = new AiChatResponseDTO();
            response.setContent(content != null ? content.trim() : "");
            response.setModel(model);
            response.setConfigured(true);
            response.setConfidence(rag.getSources().isEmpty() ? 70 : 90);
            response.setRagScope(rag.getScope());
            response.setSources(new ArrayList<>(rag.getSources()));
            response.setWarnings(new ArrayList<>(rag.getWarnings()));
            response.setMissingFields(new ArrayList<>(rag.getMissingFields()));
            if (!response.getSources().contains("ShambuaSante RAG · OpenAI")) {
                response.getSources().add("ShambuaSante RAG · OpenAI");
            }

            logUsage(hopitalId, role, rag.getScope(), patientId, analysisType, promptChars, contextChars,
                    response.getContent().length(), response.getSources(), true, null);
            return response;
        } catch (Exception ex) {
            logger.error("Erreur appel OpenAI/RAG: {}", ex.getMessage());
            logUsage(hopitalId, role, rag.getScope(), patientId, analysisType, promptChars, contextChars,
                    0, List.copyOf(rag.getSources()), false, ex.getMessage());
            throw new BadRequestException("Impossible de contacter l'assistant IA : " + ex.getMessage());
        }
    }

    private void logUsage(Integer hopitalId, Role role, String scope, Long patientId, String analysisType,
                          int promptChars, int contextChars, int responseChars, List<String> sources,
                          boolean success, String error) {
        try {
            String sourcesJson = objectMapper.writeValueAsString(sources);
            // Estimation grossière : ~4 chars / token, $0.15 / 1M tokens input-ish
            double tokens = (promptChars + contextChars + responseChars) / 4.0;
            BigDecimal cost = BigDecimal.valueOf(tokens * 0.00000015).setScale(6, RoundingMode.HALF_UP);
            ragUsageRepository.insert(
                    hopitalId,
                    currentUserService.getCurrentUtilisateurId(),
                    role != null ? role.name() : null,
                    scope != null ? scope : "UNKNOWN",
                    patientId,
                    model,
                    analysisType,
                    promptChars,
                    contextChars,
                    responseChars,
                    sourcesJson,
                    success,
                    error != null && error.length() > 480 ? error.substring(0, 480) : error,
                    cost
            );
        } catch (Exception e) {
            logger.debug("Journal RAG non enregistré: {}", e.getMessage());
        }
    }

    private boolean isConfigured() {
        if (!enabled || apiKey == null || apiKey.isBlank()) return false;
        String normalized = apiKey.toLowerCase(Locale.ROOT);
        return !normalized.contains("your-openai") && !normalized.equals("sk-xxx") && apiKey.startsWith("sk-");
    }

    private String normalizeAnalysisType(String analysisType) {
        if (analysisType == null || analysisType.isBlank()) return "general";
        return analysisType.trim().toLowerCase(Locale.ROOT);
    }

    private String buildSystemPrompt(Integer hopitalId, Role role, String analysisType, RagContextBundle rag) {
        String tenantScope = hopitalId != null
                ? "Établissement (tenant) #" + hopitalId + ". Isolation multi-tenant stricte."
                : "Contexte plateforme SaaS (super administrateur) — aucune donnée clinique patient.";

        String roleLabel = role != null ? role.name() : "CLINICIEN";

        String taskFocus = switch (analysisType) {
            case "diagnosis" -> "Aide au diagnostic différentiel, signes d'alerte, examens complémentaires.";
            case "druginteraction", "drug_interaction" -> "Interactions médicamenteuses, contre-indications, alternatives.";
            case "summarize" -> "Synthèse structurée du dossier (motif, antécédents, allergies, constantes, diagnostics, traitements, labo, plan). Signalez les informations manquantes.";
            case "protocols" -> "Protocoles, guides, procédures labo, urgences, critères admission/sortie. Présentez les examens mentionnés.";
            case "allergies" -> "Signalez clairement les allergies enregistrées et les absences d'allergies documentées.";
            case "compare_lab" -> "Comparez les résultats de laboratoire récents aux anciens.";
            case "missing" -> "Identifiez les informations importantes manquantes dans le dossier.";
            case "admin" -> "Gouvernance RAG / utilisateurs / quotas / journal — sans données cliniques.";
            case "platform" -> "Pilotage SaaS : hôpitaux, abonnements, MRR/ARPU, consommation API, quotas, disponibilité.";
            default -> "Réponse structurée et actionnable selon le périmètre du rôle.";
        };

        String missing = rag.getMissingFields().isEmpty()
                ? ""
                : "Champs potentiellement manquants détectés : " + String.join(", ", rag.getMissingFields()) + ".";

        return """
                Vous êtes l'assistant IA Shambua Santé avec RAG multi-tenant.
                Rôle utilisateur : %s. %s
                Scope RAG : %s.
                %s
                %s

                Règles impératives :
                - Répondez en français, professionnel et concis.
                - Basez-vous UNIQUEMENT sur le contexte RAG fourni + la question. N'inventez pas de données.
                - Ne citez jamais un autre établissement.
                - Si le rôle est ADMIN ou SUPER_ADMIN : n'exposez aucune donnée clinique patient.
                - Si médecin : signalez allergies, éléments manquants, et rappelez la validation clinique obligatoire.
                - Structurez avec sections / puces. Aide à la décision, pas diagnostic définitif.
                """.formatted(roleLabel, tenantScope, rag.getScope(), taskFocus, missing);
    }
}
