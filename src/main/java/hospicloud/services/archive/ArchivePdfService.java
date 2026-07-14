package hospicloud.services.archive;

import hospicloud.dtos.archive.ArchiveFichierDto;
import hospicloud.model.archive.ArchiveDossier;
import hospicloud.model.archive.ArchiveFichier;
import org.springframework.web.multipart.MultipartFile;

import java.util.Collection;
import java.util.List;
import java.util.Map;

public interface ArchivePdfService {

    /**
     * Génère / régénère tous les PDF du dossier patient pour l'archive courante
     * (dossier, ordonnances, consultations, bulletins de sortie) — strictement multi-tenant.
     */
    List<ArchiveFichierDto> genererTousDocuments(ArchiveDossier archive);

    /** Raccourci : génère tout et renvoie le PDF dossier patient (ou le premier créé). */
    ArchiveFichierDto genererEtAttacher(ArchiveDossier archive);

    /** Ajoute une pièce jointe tierce (PDF ou autre) à l'archive. */
    ArchiveFichierDto uploaderPieceJointe(ArchiveDossier archive, MultipartFile file, String libelle);

    /** Supprime une pièce jointe uploadée (pas les PDF auto-générés système). */
    void supprimerPieceJointe(ArchiveDossier archive, Long fichierId);

    List<ArchiveFichierDto> lister(Integer hopitalId, Long archiveId);

    Map<Long, List<ArchiveFichierDto>> listerParArchives(Integer hopitalId, Collection<Long> archiveIds);

    ArchiveFichier getFichierOuThrow(Integer hopitalId, Long fichierId);

    byte[] lireContenu(ArchiveFichier fichier);
}
