package hospicloud.servicesImpl;

import hospicloud.dtos.SubscriptionKpiMetricDTO;
import hospicloud.dtos.TechnicalLogDTO;
import hospicloud.dtos.TechnicalLogKpisDTO;
import hospicloud.dtos.events.TechnicalLogEvent;
import hospicloud.messaging.RabbitProducer;
import hospicloud.repositories.SupportTicketRepository;
import hospicloud.repositories.TechnicalLogRepository;
import hospicloud.security.CurrentUserService;
import hospicloud.security.RequestContext;
import hospicloud.security.TenantContext;
import hospicloud.security.UtilisateurPrincipal;
import hospicloud.services.TechnicalLogService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class TechnicalLogServiceImpl implements TechnicalLogService {

    private static final Logger log = LoggerFactory.getLogger(TechnicalLogServiceImpl.class);

    private final TechnicalLogRepository technicalLogRepository;
    private final SupportTicketRepository supportTicketRepository;
    private final RabbitProducer rabbitProducer;
    private final CurrentUserService currentUserService;
    private final String routingKey;

    public TechnicalLogServiceImpl(TechnicalLogRepository technicalLogRepository,
                                   SupportTicketRepository supportTicketRepository,
                                   RabbitProducer rabbitProducer,
                                   CurrentUserService currentUserService,
                                   @Value("${app.rabbit.technical.routing-key:technical.log}") String routingKey) {
        this.technicalLogRepository = technicalLogRepository;
        this.supportTicketRepository = supportTicketRepository;
        this.rabbitProducer = rabbitProducer;
        this.currentUserService = currentUserService;
        this.routingKey = routingKey;
    }

    @Override
    public void record(TechnicalLogEvent event) {
        enrichFromSecurityContext(event);
        event.setStatus(normalizeStatus(event.getStatus()));
        try {
            rabbitProducer.send(routingKey, event);
        } catch (Exception e) {
            log.warn("Publication RabbitMQ échouée, écriture synchrone: {}", e.getMessage());
            insertSafely(event);
        }
    }

    private void insertSafely(TechnicalLogEvent event) {
        try {
            technicalLogRepository.insert(event);
        } catch (Exception e) {
            log.warn("Impossible d'enregistrer le journal technique (action={}, module={}): {}",
                    event.getAction(), event.getModule(), e.getMessage());
        }
    }

    /**
     * La colonne technical_logs.status est un ENUM('INFO','WARNING','ERROR').
     * Les événements métier (SUCCES, ECHEC, REFUSE…) sont mappés ici.
     */
    public static String normalizeStatus(String status) {
        if (status == null || status.isBlank()) {
            return "INFO";
        }
        return switch (status.trim().toUpperCase()) {
            case "INFO", "WARNING", "ERROR" -> status.trim().toUpperCase();
            case "SUCCES", "SUCCESS", "OK" -> "INFO";
            case "ECHEC", "FAIL", "FAILED", "REFUSE", "REFUSED", "DENIED" -> "WARNING";
            default -> "INFO";
        };
    }

    @Override
    public void recordApiError(String module, String action, String endpoint, String httpMethod,
                               int httpStatus, String message, String errorDetails,
                               String ipAddress, String userAgent) {
        TechnicalLogEvent event = new TechnicalLogEvent();
        event.setHopitalId(resolveHopitalId());
        event.setModule(module != null ? module : "api");
        event.setAction(action != null ? action : "HTTP_" + httpStatus);
        event.setEndpoint(endpoint);
        event.setHttpMethod(httpMethod);
        event.setStatus(httpStatus >= 500 ? "ERROR" : "WARNING");
        event.setMessage(message);
        event.setErrorDetails(errorDetails);
        event.setRequestId(RequestContext.getRequestId());
        event.setIpAddress(ipAddress);
        event.setUserAgent(userAgent);
        record(event);
    }

    @Override
    public void recordAuthEvent(String action, String message, String status,
                                Integer hopitalId, Integer userId, String userEmail, String userRole,
                                String ipAddress, String userAgent) {
        TechnicalLogEvent event = new TechnicalLogEvent();
        event.setHopitalId(hopitalId);
        event.setUserId(userId != null ? userId.longValue() : null);
        event.setUserEmail(userEmail);
        event.setUserRole(userRole);
        event.setModule("auth");
        event.setAction(action);
        event.setEndpoint("/api/auth/login");
        event.setHttpMethod("POST");
        event.setStatus(status);
        event.setMessage(message);
        event.setRequestId(RequestContext.getRequestId());
        event.setIpAddress(ipAddress);
        event.setUserAgent(userAgent);
        record(event);
    }

    @Override
    public List<TechnicalLogDTO> search(Integer hopitalId, Long userId, String userEmail, String module,
                                        String action, String status, String requestId, String endpoint,
                                        String search, LocalDateTime dateFrom, LocalDateTime dateTo, int limit) {
        return technicalLogRepository.search(hopitalId, userId, userEmail, module, action, status,
                requestId, endpoint, search, dateFrom, dateTo, limit);
    }

    @Override
    public TechnicalLogKpisDTO getKpis() {
        LocalDateTime since = LocalDateTime.now().minusDays(7);
        TechnicalLogKpisDTO kpis = technicalLogRepository.getKpis(since);
        long openTickets = supportTicketRepository.countOpenTickets();
        SubscriptionKpiMetricDTO ticketMetric = new SubscriptionKpiMetricDTO();
        ticketMetric.setValue(BigDecimal.valueOf(openTickets));
        ticketMetric.setDelta(BigDecimal.ZERO);
        kpis.setOpenTickets(ticketMetric);
        return kpis;
    }

    @Override
    public List<String> listModules() {
        return technicalLogRepository.listDistinctModules();
    }

    @Override
    public List<String> listActions() {
        return technicalLogRepository.listDistinctActions();
    }

    private void enrichFromSecurityContext(TechnicalLogEvent event) {
        if (event.getHopitalId() == null) {
            event.setHopitalId(resolveHopitalId());
        }
        if (event.getRequestId() == null) {
            event.setRequestId(RequestContext.getRequestId());
        }
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof UtilisateurPrincipal principal) {
            if (event.getUserId() == null && principal.getIdUtilisateur() != null) {
                event.setUserId(principal.getIdUtilisateur().longValue());
            }
            if (event.getUserEmail() == null) {
                event.setUserEmail(principal.getUsername());
            }
            if (event.getUserRole() == null && principal.getAppRole() != null) {
                event.setUserRole(principal.getAppRole().name());
            }
        }
    }

    private Integer resolveHopitalId() {
        Integer tenantId = TenantContext.getHopitalId();
        if (tenantId != null) {
            return tenantId;
        }
        try {
            return currentUserService.getCurrentHopitalId();
        } catch (Exception ignored) {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth != null && auth.getPrincipal() instanceof UtilisateurPrincipal principal) {
                return principal.getIdHopital();
            }
            return null;
        }
    }
}
