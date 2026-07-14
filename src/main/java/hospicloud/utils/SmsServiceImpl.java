package hospicloud.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

@Service
public class SmsServiceImpl implements SmsService {

    private static final Logger log = LoggerFactory.getLogger(SmsServiceImpl.class);

    private final RestTemplate restTemplate = new RestTemplate();
    private final boolean enabled;
    private final String accountSid;
    private final String authToken;
    private final String fromNumber;
    private final String defaultCountryCode;

    public SmsServiceImpl(
            @Value("${app.sms.enabled:false}") boolean enabled,
            @Value("${app.sms.twilio.account-sid:}") String accountSid,
            @Value("${app.sms.twilio.auth-token:}") String authToken,
            @Value("${app.sms.twilio.from-number:}") String fromNumber,
            @Value("${app.sms.default-country-code:+224}") String defaultCountryCode) {
        this.enabled = enabled;
        this.accountSid = accountSid;
        this.authToken = authToken;
        this.fromNumber = fromNumber;
        this.defaultCountryCode = defaultCountryCode;
    }

    @Override
    public boolean isEnabled() {
        return enabled
                && StringUtils.hasText(accountSid)
                && StringUtils.hasText(authToken)
                && StringUtils.hasText(fromNumber);
    }

    @Override
    public void envoyerSms(String numero, String message) {
        if (!StringUtils.hasText(numero) || !StringUtils.hasText(message)) {
            throw new IllegalArgumentException("Numéro et message SMS obligatoires");
        }

        String numeroNormalise = PhoneNormalizer.normalize(numero, defaultCountryCode);
        if (!StringUtils.hasText(numeroNormalise)) {
            throw new IllegalArgumentException("Numéro de téléphone invalide : " + numero);
        }

        if (!isEnabled()) {
            log.info("[SMS simulé] {} → {}", numeroNormalise, message);
            return;
        }

        String url = "https://api.twilio.com/2010-04-01/Accounts/" + accountSid + "/Messages.json";

        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("To", numeroNormalise);
        body.add("From", fromNumber);
        body.add("Body", message);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        headers.setBasicAuth(accountSid, authToken);

        try {
            ResponseEntity<String> response = restTemplate.postForEntity(
                    url,
                    new HttpEntity<>(body, headers),
                    String.class);
            log.info("SMS envoyé à {} (statut {})", numeroNormalise, response.getStatusCode().value());
        } catch (RestClientException ex) {
            throw new RuntimeException("Erreur envoi SMS Twilio vers " + numeroNormalise, ex);
        }
    }
}
