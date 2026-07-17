package hospicloud.utils;

public interface EmailService {

    void envoyerEmail(String destinataire,
                       String sujet,
                       String contenu);

    void envoyerEmailHtml(String destinataire,
                          String sujet,
                          String contenuHtml);

    /**
     * E-mail HTML avec pièce jointe (ex. ordonnance PDF).
     */
    void envoyerEmailHtmlAvecPieceJointe(
            String destinataire,
            String sujet,
            String contenuHtml,
            String nomFichier,
            byte[] pieceJointe,
            String mimeType);
}