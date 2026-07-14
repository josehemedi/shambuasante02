package hospicloud.utils;

public interface EmailService {

    void envoyerEmail(String destinataire,
                       String sujet,
                       String contenu);

    void envoyerEmailHtml(String destinataire,
                          String sujet,
                          String contenuHtml);
}