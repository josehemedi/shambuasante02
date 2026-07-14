package hospicloud.servicesImpl.reportingimpl;

import hospicloud.dtos.RapportRequestDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * @deprecated Remplacé par {@link hospicloud.servicesImpl.AsyncReportJobListener}.
 * Conservé pour compatibilité de bean ; ne consomme plus RabbitMQ.
 */
@Component
@Deprecated
public class RapportRabbitListener {

    private static final Logger log = LoggerFactory.getLogger(RapportRabbitListener.class);

    /** Ancien handler — désactivé (plus de @RabbitListener). Utiliser /api/async/reports. */
    public void genererRapport(RapportRequestDto request) {
        log.warn("RapportRabbitListener legacy ignoré — utiliser AsyncReportJobListener /api/async/reports");
    }
}
