package hospicloud.servicesImpl;

import hospicloud.dtos.TeleconsultationChatEventDTO;
import hospicloud.dtos.TeleconsultationChatMessageDTO;
import hospicloud.dtos.TeleconsultationChatReadReceiptDTO;
import hospicloud.exceptions.ForbiddenException;
import hospicloud.model.RendezVous;
import hospicloud.repositories.TeleconsultationChatRepository;
import hospicloud.security.CurrentUserContext;
import hospicloud.security.TeleconsultationChatGuard;
import hospicloud.security.TeleconsultationChatTopics;
import hospicloud.security.UtilisateurPrincipal;
import hospicloud.services.TeleconsultationChatService;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class TeleconsultationChatServiceImpl implements TeleconsultationChatService {

    private static final int MAX_CONTENT_LENGTH = 2000;
    private static final DateTimeFormatter ISO = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    private final TeleconsultationChatRepository repository;
    private final TeleconsultationChatGuard guard;
    private final SimpMessagingTemplate messagingTemplate;

    public TeleconsultationChatServiceImpl(TeleconsultationChatRepository repository,
                                           TeleconsultationChatGuard guard,
                                           SimpMessagingTemplate messagingTemplate) {
        this.repository = repository;
        this.guard = guard;
        this.messagingTemplate = messagingTemplate;
    }

    @Override
    public List<TeleconsultationChatMessageDTO> listMessages(Integer idRdv) {
        RendezVous rdv = guard.requireParticipant(idRdv);
        List<TeleconsultationChatMessageDTO> messages =
                repository.findByRdv(rdv.getIdHopital(), idRdv);
        Integer currentUserId = requireUserId();
        messages.forEach(msg -> enrichMessage(msg, currentUserId));

        TeleconsultationChatReadReceiptDTO receipt = markMessagesAsReadInternal(rdv, idRdv);
        if (receipt != null) {
            broadcastReadReceipt(receipt);
        }

        return messages;
    }

    @Override
    public TeleconsultationChatMessageDTO sendMessage(Integer idRdv, String content) {
        RendezVous rdv = guard.requireParticipant(idRdv);
        String sanitized = sanitizeContent(content);

        Integer medecinId = CurrentUserContext.getMedecinId();
        boolean isDoctor = medecinId != null && medecinId.equals(rdv.getIdMedecin());

        TeleconsultationChatMessageDTO message = new TeleconsultationChatMessageDTO();
        message.setIdHopital(rdv.getIdHopital());
        message.setIdRdv(idRdv);
        message.setIdEmetteur(requireUserId());
        message.setSenderRole(isDoctor ? "doctor" : "patient");
        message.setSenderName(resolveSenderName(rdv, isDoctor));
        message.setContent(sanitized);
        message.setCreatedAt(LocalDateTime.now().format(ISO));

        TeleconsultationChatMessageDTO saved = repository.save(message);
        saved.setSenderName(message.getSenderName());
        enrichMessage(saved, message.getIdEmetteur());
        broadcastMessage(saved);
        return saved;
    }

    private void broadcastMessage(TeleconsultationChatMessageDTO saved) {
        String destination = TeleconsultationChatTopics.destination(saved.getIdHopital(), saved.getIdRdv());
        messagingTemplate.convertAndSend(destination, TeleconsultationChatEventDTO.message(saved));
    }

    @Override
    public TeleconsultationChatReadReceiptDTO markMessagesAsRead(Integer idRdv) {
        RendezVous rdv = guard.requireParticipant(idRdv);
        TeleconsultationChatReadReceiptDTO receipt = markMessagesAsReadInternal(rdv, idRdv);
        if (receipt != null) {
            broadcastReadReceipt(receipt);
        }
        return receipt;
    }

    private TeleconsultationChatReadReceiptDTO markMessagesAsReadInternal(RendezVous rdv, Integer idRdv) {
        Integer medecinId = CurrentUserContext.getMedecinId();
        boolean isDoctor = medecinId != null && medecinId.equals(rdv.getIdMedecin());

        List<Long> markedIds = repository.markAsReadByRecipient(rdv.getIdHopital(), idRdv, isDoctor);
        if (markedIds.isEmpty()) {
            return null;
        }

        String readAt = LocalDateTime.now().format(ISO);
        TeleconsultationChatReadReceiptDTO receipt = new TeleconsultationChatReadReceiptDTO();
        receipt.setIdRdv(idRdv);
        receipt.setIdHopital(rdv.getIdHopital());
        receipt.setReaderRole(isDoctor ? "doctor" : "patient");
        receipt.setReadAt(readAt);
        receipt.setMessageIds(markedIds);
        return receipt;
    }

    private void broadcastReadReceipt(TeleconsultationChatReadReceiptDTO receipt) {
        String destination = TeleconsultationChatTopics.destination(receipt.getIdHopital(), receipt.getIdRdv());
        messagingTemplate.convertAndSend(destination, TeleconsultationChatEventDTO.readReceipt(receipt));
    }

    private void enrichMessage(TeleconsultationChatMessageDTO message, Integer currentUserId) {
        if (message.getSenderName() == null || message.getSenderName().isBlank()) {
            if ("doctor".equals(message.getSenderRole())) {
                message.setSenderName("Médecin");
            } else {
                message.setSenderName("Patient");
            }
        }

        if (currentUserId != null && currentUserId.equals(message.getIdEmetteur())) {
            if ("doctor".equals(message.getSenderRole())) {
                boolean read = message.getReadByPatientAt() != null;
                message.setReadByRecipient(read);
                message.setReadAt(message.getReadByPatientAt());
            } else {
                boolean read = message.getReadByDoctorAt() != null;
                message.setReadByRecipient(read);
                message.setReadAt(message.getReadByDoctorAt());
            }
        } else {
            message.setReadByRecipient(null);
            message.setReadAt(null);
        }
    }

    private String resolveSenderName(RendezVous rdv, boolean isDoctor) {
        if (isDoctor) {
            return rdv.getNomMedecin() != null ? rdv.getNomMedecin() : "Médecin";
        }
        return rdv.getNomPatient() != null ? rdv.getNomPatient() : "Patient";
    }

    private Integer requireUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof UtilisateurPrincipal principal) {
            if (principal.getIdUtilisateur() != null) {
                return principal.getIdUtilisateur();
            }
        }
        throw new ForbiddenException("Utilisateur non authentifié.");
    }

    private String sanitizeContent(String content) {
        if (content == null) {
            throw new IllegalArgumentException("Le message ne peut pas être vide.");
        }
        String trimmed = content.replace("\0", "").trim();
        if (trimmed.isEmpty()) {
            throw new IllegalArgumentException("Le message ne peut pas être vide.");
        }
        if (trimmed.length() > MAX_CONTENT_LENGTH) {
            throw new IllegalArgumentException("Le message dépasse la taille maximale autorisée.");
        }
        return trimmed;
    }
}
