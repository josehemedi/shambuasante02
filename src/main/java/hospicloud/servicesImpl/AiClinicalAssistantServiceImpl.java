package hospicloud.servicesImpl;

import hospicloud.dtos.AiChatMessageDTO;
import hospicloud.dtos.AiChatRequestDTO;
import hospicloud.dtos.AiChatResponseDTO;
import hospicloud.dtos.AiStatusDTO;
import hospicloud.exceptions.BadRequestException;
import hospicloud.exceptions.ResourceNotFoundException;
import hospicloud.model.Role;
import hospicloud.security.CurrentUserContext;
import hospicloud.security.TenantContext;
import hospicloud.services.AiClinicalAssistantService;
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

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Service
public class AiClinicalAssistantServiceImpl implements AiClinicalAssistantService {

    private static final Logger logger = LoggerFactory.getLogger(AiClinicalAssistantServiceImpl.class);

    private static final List<String> SUGGESTED_PROMPTS_FR = List.of(
            "Résumer un patient de mon établissement",
            "Vérifier les interactions médicamenteuses",
            "Suggérer un protocole de traitement",
            "Analyser les derniers résultats de labo"
    );

    private final ChatClient chatClient;
    private final String apiKey;
    private final String model;
    private final boolean enabled;

    public AiClinicalAssistantServiceImpl(
            @Autowired(required = false) ChatClient.Builder chatClientBuilder,
            @Value("${spring.ai.openai.api-key:}") String apiKey,
            @Value("${spring.ai.openai.chat.options.model:gpt-4o-mini}") String model,
            @Value("${app.ai.enabled:true}") boolean enabled) {
        this.apiKey = apiKey != null ? apiKey.trim() : "";
        this.model = model;
        this.enabled = enabled;
        this.chatClient = chatClientBuilder != null && isConfigured() ? chatClientBuilder.build() : null;
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
        return SUGGESTED_PROMPTS_FR;
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

        List<Message> messages = new ArrayList<>();
        messages.add(new SystemMessage(buildSystemPrompt(hopitalId, role, analysisType)));

        if (request.getHistory() != null) {
            for (AiChatMessageDTO item : request.getHistory()) {
                if (item == null || item.getContent() == null || item.getContent().isBlank()) {
                    continue;
                }
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
            String content = chatClient.prompt()
                    .messages(messages)
                    .call()
                    .content();

            AiChatResponseDTO response = new AiChatResponseDTO();
            response.setContent(content != null ? content.trim() : "");
            response.setModel(model);
            response.setConfigured(true);
            response.setConfidence(88);
            if (hopitalId != null) {
                response.getSources().add("Dossiers médicaux · établissement #" + hopitalId);
            }
            response.getSources().add("ShambuaSante AI · OpenAI");
            return response;
        } catch (Exception ex) {
            logger.error("Erreur appel OpenAI: {}", ex.getMessage());
            throw new BadRequestException("Impossible de contacter l'assistant IA : " + ex.getMessage());
        }
    }

    private boolean isConfigured() {
        return enabled && apiKey != null && !apiKey.isBlank();
    }

    private String normalizeAnalysisType(String analysisType) {
        if (analysisType == null || analysisType.isBlank()) {
            return "general";
        }
        return analysisType.trim().toLowerCase(Locale.ROOT);
    }

    private String buildSystemPrompt(Integer hopitalId, Role role, String analysisType) {
        String tenantScope = hopitalId != null
                ? "L'utilisateur appartient à l'établissement (tenant) #" + hopitalId
                  + ". Ne citez jamais de données d'un autre établissement."
                : "Contexte plateforme SaaS (super administrateur).";

        String roleLabel = role != null ? role.name() : "CLINICIEN";

        String taskFocus = switch (analysisType) {
            case "diagnosis" -> "Concentrez-vous sur l'aide au diagnostic différentiel, les signes d'alerte et les examens complémentaires pertinents.";
            case "druginteraction", "drug_interaction" -> "Concentrez-vous sur les interactions médicamenteuses, contre-indications, surveillance et alternatives thérapeutiques.";
            case "summarize" -> "Concentrez-vous sur la synthèse structurée d'un dossier clinique (antécédents, motif, traitements, résultats, plan de suivi).";
            case "protocols" -> "Concentrez-vous sur les protocoles de prise en charge fondés sur les bonnes pratiques cliniques.";
            default -> "Répondez de manière clinique, structurée et actionnable.";
        };

        return """
                Vous êtes l'assistant IA clinique de Shambua Santé, une plateforme hospitalière multi-établissements.
                Rôle utilisateur : %s. %s
                %s

                Règles impératives :
                - Répondez en français, avec un ton professionnel et concis.
                - Vous fournissez une AIDE À LA DÉCISION, jamais un diagnostic définitif ni une prescription finale.
                - Mentionnez les limites de l'analyse quand les données sont insuffisantes.
                - Structurez la réponse avec des puces ou sections courtes si utile.
                - Ne inventez pas de données patient : si des informations manquent, demandez-les ou indiquez les hypothèses.
                - Rappelez brièvement que la validation par un clinicien agréé est obligatoire.
                """.formatted(roleLabel, tenantScope, taskFocus);
    }
}
