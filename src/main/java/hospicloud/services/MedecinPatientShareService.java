package hospicloud.services;

import hospicloud.dtos.DocumentEnvoiResponse;
import org.springframework.web.multipart.MultipartFile;

public interface MedecinPatientShareService {

    /** Transmet un résultat de laboratoire validé au patient (portail + e-mail + notif). */
    DocumentEnvoiResponse envoyerResultatLabo(Integer idAnalyse);

    /** Transmet le PDF d'une consultation au patient. */
    DocumentEnvoiResponse envoyerFicheConsultation(Long idConsultation);

    /** Dépose un document et le partage immédiatement avec le patient. */
    DocumentEnvoiResponse envoyerDocumentFichier(
            Integer idPatient,
            String typeDocument,
            String titre,
            MultipartFile fichier);
}
