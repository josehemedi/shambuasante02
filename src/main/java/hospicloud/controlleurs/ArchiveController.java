package hospicloud.controlleurs;

import hospicloud.dtos.archive.*;
import hospicloud.model.archive.ReglesArchivageHopital;
import hospicloud.model.archive.StatutArchive;
import hospicloud.model.archive.TypeEpisode;
import hospicloud.services.archive.ArchivageService;
import hospicloud.services.archive.DemandeAccesArchiveService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/archives")
public class ArchiveController {

    private final ArchivageService archivageService;
    private final DemandeAccesArchiveService demandeAccesService;

    public ArchiveController(ArchivageService archivageService,
                             DemandeAccesArchiveService demandeAccesService) {
        this.archivageService = archivageService;
        this.demandeAccesService = demandeAccesService;
    }

    @GetMapping
    public ArchivePageResponseDto lister(
            @RequestParam(required = false) StatutArchive statut,
            @RequestParam(required = false) TypeEpisode typeEpisode,
            @RequestParam(required = false) Long patientId,
            @RequestParam(required = false) Integer idMedecin,
            @RequestParam(required = false) Integer idService,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime dateFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime dateTo,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "date_fin_episode") String sort,
            @RequestParam(defaultValue = "DESC") String direction) {
        ArchiveSearchFilter filter = buildFilter(statut, typeEpisode, patientId, idMedecin,
                idService, search, dateFrom, dateTo, page, size, sort, direction);
        return archivageService.rechercher(filter);
    }

    @GetMapping("/patient/{patientId}")
    public List<ArchiveDossierResponseDto> parPatient(@PathVariable Long patientId) {
        return archivageService.listerParPatient(patientId);
    }

    @GetMapping("/a-verifier")
    public ArchivePageResponseDto aVerifier(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        ArchiveSearchFilter filter = new ArchiveSearchFilter();
        filter.setStatut(StatutArchive.A_VERIFIER);
        filter.setPage(page);
        filter.setSize(size);
        return archivageService.rechercher(filter);
    }

    @GetMapping("/incomplets")
    public ArchivePageResponseDto incomplets(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        ArchiveSearchFilter filter = new ArchiveSearchFilter();
        filter.setStatut(StatutArchive.INCOMPLET);
        filter.setPage(page);
        filter.setSize(size);
        return archivageService.rechercher(filter);
    }

    @GetMapping("/pret-a-archiver")
    public ArchivePageResponseDto pretAArchiver(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        ArchiveSearchFilter filter = new ArchiveSearchFilter();
        filter.setStatut(StatutArchive.PRET_A_ARCHIVER);
        filter.setPage(page);
        filter.setSize(size);
        return archivageService.rechercher(filter);
    }

    @GetMapping("/statistiques")
    public ArchiveStatistiquesDto statistiques() {
        return archivageService.statistiques();
    }

    @GetMapping("/regles")
    public ReglesArchivageHopital getRegles() {
        return archivageService.getRegles();
    }

    @PutMapping("/regles")
    public ReglesArchivageHopital updateRegles(@RequestBody ReglesArchivageHopital regles) {
        return archivageService.updateRegles(regles);
    }

    @GetMapping("/demandes-acces/en-attente")
    public List<DemandeAccesArchiveDto> demandesEnAttente() {
        return demandeAccesService.listerEnAttente();
    }

    @GetMapping("/{id}")
    public ArchiveDossierResponseDto consulter(@PathVariable Long id) {
        return archivageService.consulter(id);
    }

    @PostMapping("/verifier")
    public VerificationDossierResultDto verifier(@RequestBody VerifierDossierRequestDto request) {
        return archivageService.verifierDossier(request);
    }

    @PostMapping("/enregistrer")
    public ArchiveDossierResponseDto enregistrer(@RequestBody EnregistrerEpisodeRequestDto request) {
        return archivageService.enregistrerEpisode(request);
    }

    @PostMapping("/{id}/marquer-incomplet")
    public ArchiveDossierResponseDto marquerIncomplet(@PathVariable Long id,
                                                      @RequestBody TransitionArchiveRequestDto request) {
        return archivageService.marquerCommeIncomplet(id, request);
    }

    @PostMapping("/{id}/pret-a-archiver")
    public ArchiveDossierResponseDto pretAArchiver(@PathVariable Long id,
                                                   @RequestBody TransitionArchiveRequestDto request) {
        return archivageService.marquerCommePretAArchiver(id, request);
    }

    @PostMapping("/{id}/archiver")
    public ArchiveDossierResponseDto archiver(@PathVariable Long id,
                                              @RequestBody TransitionArchiveRequestDto request) {
        return archivageService.archiverEpisode(id, request);
    }

    @PostMapping("/{id}/restaurer")
    public ArchiveDossierResponseDto restaurer(@PathVariable Long id,
                                               @RequestBody TransitionArchiveRequestDto request) {
        return archivageService.restaurerArchive(id, request);
    }

    @GetMapping("/{id}/historique")
    public List<HistoriqueArchivageDto> historique(@PathVariable Long id) {
        return archivageService.historique(id);
    }

    @GetMapping("/{id}/fichiers")
    public List<ArchiveFichierDto> listerFichiers(@PathVariable Long id) {
        return archivageService.listerFichiers(id);
    }

    @GetMapping("/{id}/fichiers/{fichierId}/download")
    public ResponseEntity<byte[]> telechargerFichier(@PathVariable Long id, @PathVariable Long fichierId) {
        byte[] content = archivageService.telechargerFichier(id, fichierId);
        ArchiveFichierDto meta = archivageService.getFichierMeta(id, fichierId);
        String filename = meta.getNomFichier() != null ? meta.getNomFichier() : ("fichier_" + fichierId);
        MediaType mediaType = MediaType.APPLICATION_OCTET_STREAM;
        try {
            if (meta.getMimeType() != null && !meta.getMimeType().isBlank()) {
                mediaType = MediaType.parseMediaType(meta.getMimeType());
            }
        } catch (Exception ignored) {
            mediaType = MediaType.APPLICATION_OCTET_STREAM;
        }
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + filename.replace("\"", "") + "\"")
                .contentType(mediaType)
                .body(content);
    }

    @PostMapping("/{id}/fichiers/pdf")
    public ArchiveFichierDto regenererPdf(@PathVariable Long id) {
        return archivageService.regenererPdf(id);
    }

    @PostMapping(value = "/{id}/fichiers/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ArchiveFichierDto uploaderPieceJointe(
            @PathVariable Long id,
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "libelle", required = false) String libelle) {
        return archivageService.uploaderPieceJointe(id, file, libelle);
    }

    @DeleteMapping("/{id}/fichiers/{fichierId}")
    public void supprimerPieceJointe(@PathVariable Long id, @PathVariable Long fichierId) {
        archivageService.supprimerPieceJointe(id, fichierId);
    }

    @PostMapping("/{id}/demandes-acces")
    public DemandeAccesArchiveDto creerDemande(@PathVariable Long id,
                                               @RequestBody DemandeAccesRequestDto request) {
        return demandeAccesService.creerDemande(id, request);
    }

    @PutMapping("/demandes-acces/{demandeId}/accepter")
    public DemandeAccesArchiveDto accepterDemande(@PathVariable Long demandeId,
                                                  @RequestBody(required = false) TransitionArchiveRequestDto request) {
        String observation = request != null ? request.getObservation() : null;
        return demandeAccesService.accepter(demandeId, observation);
    }

    @PutMapping("/demandes-acces/{demandeId}/refuser")
    public DemandeAccesArchiveDto refuserDemande(@PathVariable Long demandeId,
                                                 @RequestBody(required = false) TransitionArchiveRequestDto request) {
        String observation = request != null ? request.getObservation() : null;
        return demandeAccesService.refuser(demandeId, observation);
    }

    private ArchiveSearchFilter buildFilter(StatutArchive statut, TypeEpisode typeEpisode,
                                            Long patientId, Integer idMedecin, Integer idService,
                                            String search, LocalDateTime dateFrom, LocalDateTime dateTo,
                                            int page, int size, String sort, String direction) {
        ArchiveSearchFilter filter = new ArchiveSearchFilter();
        filter.setStatut(statut);
        filter.setTypeEpisode(typeEpisode);
        filter.setPatientId(patientId);
        filter.setIdMedecin(idMedecin);
        filter.setIdService(idService);
        filter.setSearch(search);
        filter.setDateFrom(dateFrom);
        filter.setDateTo(dateTo);
        filter.setPage(page);
        filter.setSize(size);
        filter.setSort(sort);
        filter.setDirection(direction);
        return filter;
    }
}
