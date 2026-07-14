package hospicloud.controlleurs;

import hospicloud.dtos.archive.*;
import hospicloud.services.archive.ArchiveClassementService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/archives/classement")
public class ArchiveClassementController {

    private final ArchiveClassementService classementService;

    public ArchiveClassementController(ArchiveClassementService classementService) {
        this.classementService = classementService;
    }

    /** Contenu explorateur : dossiers + fichiers dans un dossier (null = racine). */
    @GetMapping("/explorer")
    public ArchiveExplorerContentDto explorer(@RequestParam(required = false) Long folderId) {
        return classementService.explorer(folderId);
    }

    @GetMapping("/arbre")
    public List<ArchiveDossierVirtuelDto> arbre() {
        return classementService.arbre();
    }

    @PostMapping("/dossiers")
    public ArchiveDossierVirtuelDto creer(@RequestBody CreateDossierVirtuelRequest request) {
        return classementService.creerDossier(request);
    }

    @PutMapping("/dossiers/{folderId}/renommer")
    public ArchiveDossierVirtuelDto renommer(@PathVariable Long folderId,
                                             @RequestBody RenameDossierVirtuelRequest request) {
        return classementService.renommer(folderId, request);
    }

    @PutMapping("/dossiers/{folderId}/deplacer")
    public ArchiveDossierVirtuelDto deplacerDossier(@PathVariable Long folderId,
                                                    @RequestBody MoveDossierVirtuelRequest request) {
        return classementService.deplacerDossierVirtuel(folderId, request);
    }

    @DeleteMapping("/dossiers/{folderId}")
    public void supprimer(@PathVariable Long folderId) {
        classementService.supprimerDossier(folderId);
    }

    /** Déplacer un dossier patient (archive) dans un dossier virtuel (null = racine). */
    @PutMapping("/archives/{archiveId}/deplacer")
    public ArchiveDossierResponseDto deplacerArchive(@PathVariable Long archiveId,
                                                     @RequestBody MoveArchiveDossierRequest request) {
        return classementService.deplacerArchive(archiveId, request);
    }
}
