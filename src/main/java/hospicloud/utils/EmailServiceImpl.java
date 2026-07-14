package hospicloud.utils;

import hospicloud.utils.EmailService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import jakarta.mail.internet.MimeMessage;

@Service
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender mailSender;
    private final String fromEmail;

    public EmailServiceImpl(JavaMailSender mailSender,
                            @Value("${spring.mail.username:hospicloud@gmail.com}") String fromEmail) {
        this.mailSender = mailSender;
        this.fromEmail = fromEmail;
    }

    @Override
    public void envoyerEmail(String destinataire, String sujet, String contenu) {
        envoyerEmailHtml(destinataire, sujet, "<pre>" + contenu + "</pre>");
    }

    @Override
    public void envoyerEmailHtml(String destinataire, String sujet, String contenuHtml) {

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(destinataire);
            helper.setSubject(sujet);
            helper.setText(contenuHtml, true);
            helper.setFrom(fromEmail);

            mailSender.send(message);

        } catch (Exception e) {
            throw new RuntimeException("Erreur envoi email HTML", e);
        }
    }
}