package hospicloud.servicesImpl.archive;

import hospicloud.dtos.ConsultationResponseDTO;
import hospicloud.dtos.PatientDossierDTO;
import hospicloud.dtos.archive.ArchiveFichierDto;
import hospicloud.exceptions.BadRequestException;
import hospicloud.exceptions.ForbiddenException;
import hospicloud.exceptions.ResourceNotFoundException;
import hospicloud.model.BonSortie;
import hospicloud.model.Ordonnance;
import hospicloud.model.Patient;
import hospicloud.model.archive.ArchiveDossier;
import hospicloud.model.archive.ArchiveFichier;
import hospicloud.repositories.BonSortieRepository;
import hospicloud.repositories.OrdonnanceRepository;
import hospicloud.repositories.archive.ArchiveFichierRepository;
import hospicloud.security.CurrentUserService;
import hospicloud.security.TenantAuthorization;
import hospicloud.security.TenantContext;
import hospicloud.services.ConsultationMedicaleService;
import hospicloud.services.OrdonnanceService;
import hospicloud.services.PatientService;
import hospicloud.services.archive.ArchivePdfService;
import hospicloud.services.reporting.ReportGenerator;
import hospicloud.servicesImpl.AsyncJobServiceImpl;
import hospicloud.servicesImpl.PatientDossierReportService;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class ArchivePdfServiceImpl implements ArchivePdfService {

    private static final Logger log = LoggerFactory.getLogger(ArchivePdfServiceImpl.class);
    private static final long MAX_UPLOAD_BYTES = 25L * 1024 * 1024;
    private static final Set<String> ALLOWED_MIME = Set.of(
            "application/pdf",
            "image/jpeg",
            "image/png",
            "image/webp",
            "image/gif",
            "image/tiff",
            "text/plain",
            "application/msword",
            "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
            "application/vnd.ms-excel",
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
            "application/zip",
            "application/x-zip-compressed",
            "application/octet-stream"
    );
    private static final Set<String> ALLOWED_EXT = Set.of(
            "pdf", "jpg", "jpeg", "png", "webp", "gif", "tif", "tiff", "txt",
            "doc", "docx", "xls", "xlsx", "zip"
    );

    private final PatientService patientService;
    private final PatientDossierReportService patientDossierReportService;
    private final OrdonnanceService ordonnanceService;
    private final OrdonnanceRepository ordonnanceRepository;
    private final ConsultationMedicaleService consultationMedicaleService;
    private final BonSortieRepository bonSortieRepository;
    private final ReportGenerator reportGenerator;
    private final ArchiveFichierRepository fichierRepository;
    private final CurrentUserService currentUserService;
    private final AsyncJobServiceImpl asyncJobService;

    public ArchivePdfServiceImpl(PatientService patientService,
                                 PatientDossierReportService patientDossierReportService,
                                 OrdonnanceService ordonnanceService,
                                 OrdonnanceRepository ordonnanceRepository,
                                 ConsultationMedicaleService consultationMedicaleService,
                                 BonSortieRepository bonSortieRepository,
                                 ReportGenerator reportGenerator,
                                 ArchiveFichierRepository fichierRepository,
                                 CurrentUserService currentUserService,
                                 AsyncJobServiceImpl asyncJobService) {
        this.patientService = patientService;
        this.patientDossierReportService = patientDossierReportService;
        this.ordonnanceService = ordonnanceService;
        this.ordonnanceRepository = ordonnanceRepository;
        this.consultationMedicaleService = consultationMedicaleService;
        this.bonSortieRepository = bonSortieRepository;
        this.reportGenerator = reportGenerator;
        this.fichierRepository = fichierRepository;
        this.currentUserService = currentUserService;
        this.asyncJobService = asyncJobService;
    }

    @Override
    @Transactional
    public ArchiveFichierDto genererEtAttacher(ArchiveDossier archive) {
        List<ArchiveFichierDto> all = genererTousDocuments(archive);
        return all.stream()
                .filter(f -> ArchiveFichier.TYPE_DOSSIER_PATIENT.equals(f.getTypeFichier()))
                .findFirst()
                .orElse(all.isEmpty() ? null : all.get(0));
    }

    @Override
    @Transactional
    public List<ArchiveFichierDto> genererTousDocuments(ArchiveDossier archive) {
        assertArchiveTenant(archive);

        PatientDossierDTO dossier = patientService.obtenirDossierComplet(archive.getPatientId());
        Patient patient = dossier.getPatient();
        if (patient == null || patient.getIdHopital() == null
                || !Objects.equals(patient.getIdHopital(), archive.getHopitalId())) {
            throw new ForbiddenException("Patient hors périmètre de l'établissement de l'archive.");
        }
        TenantAuthorization.assertSameTenant(patient.getIdHopital());

        Integer patientIdInt = archive.getPatientId().intValue();
        Path dir = resolveArchiveDir(archive);
        List<ArchiveFichierDto> generes = new ArrayList<>();

        // 1) Dossier patient complet
        try {
            byte[] pdf = patientDossierReportService.genererPdf(dossier);
            String nom = "Dossier_" + sanitize(codePatient(archive, patient))
                    + "_" + sanitize(nomPatient(archive, patient))
                    + "_" + archive.getId() + ".pdf";
            generes.add(persistPdf(archive, ArchiveFichier.TYPE_DOSSIER_PATIENT, nom, dir.resolve("dossier_patient.pdf"), pdf));
        } catch (Exception e) {
            log.error("PDF dossier patient archive {}: {}", archive.getId(), e.getMessage());
            throw new IllegalStateException("Impossible de générer le PDF dossier patient.", e);
        }

        // 2) Ordonnances (tenant)
        List<Ordonnance> ordonnances = safeList(() -> ordonnanceRepository.listerParPatient(patientIdInt));
        for (Ordonnance o : ordonnances) {
            if (o == null || o.getIdOrdonnance() == null) continue;
            if (o.getHospitalId() != null && !Objects.equals(o.getHospitalId(), archive.getHopitalId())) {
                continue;
            }
            try {
                byte[] pdf = ordonnanceService.genererPdfOrdonnance(o.getIdOrdonnance());
                String type = ArchiveFichier.TYPE_ORDONNANCE_PREFIX + o.getIdOrdonnance();
                String nom = "Ordonnance_" + sanitize(o.getNumeroOrdonnance() != null
                        ? o.getNumeroOrdonnance()
                        : String.valueOf(o.getIdOrdonnance())) + ".pdf";
                generes.add(persistPdf(archive, type, nom, dir.resolve("ordonnance_" + o.getIdOrdonnance() + ".pdf"), pdf));
            } catch (Exception e) {
                log.warn("PDF ordonnance {} non archivé: {}", o.getIdOrdonnance(), e.getMessage());
            }
        }

        // 3) Fiches de consultation signées (tenant)
        List<ConsultationResponseDTO> consultations =
                dossier.getConsultations() != null ? dossier.getConsultations() : List.of();
        for (ConsultationResponseDTO c : consultations) {
            if (c == null || c.getIdConsultation() == null) continue;
            try {
                byte[] pdf = consultationMedicaleService.genererPdfFicheConsultation(c.getIdConsultation());
                String type = ArchiveFichier.TYPE_CONSULTATION_PREFIX + c.getIdConsultation();
                String nom = "Consultation_" + c.getIdConsultation() + ".pdf";
                generes.add(persistPdf(archive, type, nom, dir.resolve("consultation_" + c.getIdConsultation() + ".pdf"), pdf));
            } catch (Exception e) {
                log.warn("PDF consultation {} non archivé: {}", c.getIdConsultation(), e.getMessage());
            }
        }

        // 4) Bulletins de sortie (tenant) — best effort (paiement parfois requis)
        List<BonSortie> bons = safeList(() -> bonSortieRepository.findByPatientId(patientIdInt));
        for (BonSortie b : bons) {
            if (b == null || b.getIdBonSortie() == null) continue;
            if (b.getIdHopital() != null && !Objects.equals(b.getIdHopital(), archive.getHopitalId())) {
                continue;
            }
            try {
                Map<String, Object> params = buildBulletinParams(b, patient);
                byte[] pdf = reportGenerator.generate(
                        "Bulletin_Sortie.jasper",
                        params,
                        new JRBeanCollectionDataSource(List.of(params)));
                String type = ArchiveFichier.TYPE_BULLETIN_PREFIX + b.getIdBonSortie();
                String nom = "Bulletin_Sortie_" + sanitize(b.getNumeroBon() != null
                        ? b.getNumeroBon()
                        : String.valueOf(b.getIdBonSortie())) + ".pdf";
                generes.add(persistPdf(archive, type, nom, dir.resolve("bulletin_" + b.getIdBonSortie() + ".pdf"), pdf));
            } catch (Exception e) {
                log.warn("PDF bulletin sortie {} non archivé: {}", b.getIdBonSortie(), e.getMessage());
            }
        }

        log.info("Archive {} : {} PDF générés (tenant {})", archive.getId(), generes.size(), archive.getHopitalId());
        return generes;
    }

    @Override
    @Transactional
    public ArchiveFichierDto uploaderPieceJointe(ArchiveDossier archive, MultipartFile file, String libelle) {
        assertArchiveTenant(archive);
        if (file == null || file.isEmpty()) {
            throw new BadRequestException("Aucun fichier fourni.");
        }
        if (file.getSize() > MAX_UPLOAD_BYTES) {
            throw new BadRequestException("Fichier trop volumineux (max 25 Mo).");
        }

        String original = file.getOriginalFilename() != null ? file.getOriginalFilename() : "piece_jointe";
        String extension = extractExtension(original);
        String contentType = detectMime(file.getContentType(), extension);
        if (!isAllowedMime(contentType, extension)) {
            throw new BadRequestException(
                    "Type de fichier non autorisé. Formats acceptés : PDF, images, Word, Excel, texte, ZIP.");
        }

        String safeOriginal = sanitize(original);
        String displayName = (libelle != null && !libelle.isBlank())
                ? sanitize(libelle) + (extension.isEmpty() ? "" : "." + extension)
                : safeOriginal;

        String token = UUID.randomUUID().toString().replace("-", "");
        String typeFichier = ArchiveFichier.TYPE_UPLOAD_PREFIX + token;
        Path dir = resolveArchiveDir(archive);
        Path target = dir.resolve("upload_" + token + (extension.isEmpty() ? "" : "." + extension));

        try {
            byte[] bytes = file.getBytes();
            Files.createDirectories(target.getParent());
            Files.write(target, bytes);

            ArchiveFichier fichier = new ArchiveFichier();
            fichier.setHopitalId(archive.getHopitalId());
            fichier.setArchiveId(archive.getId());
            fichier.setTypeFichier(typeFichier);
            fichier.setNomFichier(displayName.endsWith("." + extension) || extension.isEmpty()
                    ? displayName
                    : displayName);
            if (!fichier.getNomFichier().contains(".") && !extension.isEmpty()) {
                fichier.setNomFichier(displayName + "." + extension);
            }
            fichier.setCheminStockage(target.toAbsolutePath().toString());
            fichier.setMimeType(contentType);
            fichier.setTailleOctets((long) bytes.length);
            fichier.setGenereAt(LocalDateTime.now());
            fichier.setGenerePar(safeUserId());

            Long id = fichierRepository.upsert(fichier);
            fichier.setId(id);
            log.info("Pièce jointe uploadée archive={} fichier={} bytes={} tenant={}",
                    archive.getId(), fichier.getNomFichier(), bytes.length, archive.getHopitalId());
            return toDto(fichier);
        } catch (BadRequestException e) {
            throw e;
        } catch (Exception e) {
            throw new IllegalStateException("Échec de l'enregistrement de la pièce jointe.", e);
        }
    }

    @Override
    @Transactional
    public void supprimerPieceJointe(ArchiveDossier archive, Long fichierId) {
        assertArchiveTenant(archive);
        ArchiveFichier fichier = getFichierOuThrow(archive.getHopitalId(), fichierId);
        if (!archive.getId().equals(fichier.getArchiveId())) {
            throw new ResourceNotFoundException("Fichier d'archive introuvable");
        }
        if (fichier.getTypeFichier() == null
                || !fichier.getTypeFichier().startsWith(ArchiveFichier.TYPE_UPLOAD_PREFIX)) {
            throw new BadRequestException("Seules les pièces jointes ajoutées manuellement peuvent être supprimées.");
        }

        try {
            Path path = Path.of(fichier.getCheminStockage()).toAbsolutePath().normalize();
            Path expectedRoot = Path.of(asyncJobService.resolveStorageDir(), "archives",
                    String.valueOf(archive.getHopitalId())).toAbsolutePath().normalize();
            if (path.startsWith(expectedRoot) && Files.exists(path)) {
                Files.deleteIfExists(path);
            }
        } catch (Exception e) {
            log.warn("Suppression physique fichier {}: {}", fichierId, e.getMessage());
        }

        if (!fichierRepository.deleteById(archive.getHopitalId(), fichierId)) {
            throw new ResourceNotFoundException("Fichier d'archive introuvable");
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<ArchiveFichierDto> lister(Integer hopitalId, Long archiveId) {
        assertHopital(hopitalId);
        return fichierRepository.findByArchiveId(hopitalId, archiveId).stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Map<Long, List<ArchiveFichierDto>> listerParArchives(Integer hopitalId, Collection<Long> archiveIds) {
        assertHopital(hopitalId);
        Map<Long, List<ArchiveFichierDto>> map = new LinkedHashMap<>();
        if (archiveIds == null || archiveIds.isEmpty()) {
            return map;
        }
        for (ArchiveFichier f : fichierRepository.findByArchiveIds(hopitalId, archiveIds)) {
            map.computeIfAbsent(f.getArchiveId(), k -> new ArrayList<>()).add(toDto(f));
        }
        return map;
    }

    @Override
    @Transactional(readOnly = true)
    public ArchiveFichier getFichierOuThrow(Integer hopitalId, Long fichierId) {
        assertHopital(hopitalId);
        return fichierRepository.findById(hopitalId, fichierId)
                .orElseThrow(() -> new ResourceNotFoundException("Fichier d'archive introuvable"));
    }

    @Override
    @Transactional(readOnly = true)
    public byte[] lireContenu(ArchiveFichier fichier) {
        if (fichier == null) {
            throw new ResourceNotFoundException("Fichier d'archive introuvable");
        }
        assertHopital(fichier.getHopitalId());
        // Empêche la lecture d'un chemin hors arborescence tenant
        Path expectedRoot = Path.of(asyncJobService.resolveStorageDir(), "archives",
                String.valueOf(fichier.getHopitalId())).toAbsolutePath().normalize();
        Path actual = Path.of(fichier.getCheminStockage()).toAbsolutePath().normalize();
        if (!actual.startsWith(expectedRoot)) {
            throw new ForbiddenException("Chemin de fichier hors périmètre tenant.");
        }
        try {
            if (!Files.exists(actual)) {
                throw new ResourceNotFoundException("Fichier PDF physique introuvable sur le serveur");
            }
            return Files.readAllBytes(actual);
        } catch (ResourceNotFoundException | ForbiddenException e) {
            throw e;
        } catch (Exception e) {
            throw new ResourceNotFoundException("Impossible de lire le PDF d'archive");
        }
    }

    private ArchiveFichierDto persistPdf(ArchiveDossier archive,
                                         String typeFichier,
                                         String nomFichier,
                                         Path target,
                                         byte[] pdf) throws Exception {
        Files.createDirectories(target.getParent());
        Files.write(target, pdf);

        ArchiveFichier fichier = new ArchiveFichier();
        fichier.setHopitalId(archive.getHopitalId());
        fichier.setArchiveId(archive.getId());
        fichier.setTypeFichier(typeFichier);
        fichier.setNomFichier(nomFichier);
        fichier.setCheminStockage(target.toAbsolutePath().toString());
        fichier.setMimeType("application/pdf");
        fichier.setTailleOctets((long) pdf.length);
        fichier.setGenereAt(LocalDateTime.now());
        fichier.setGenerePar(safeUserId());

        Long id = fichierRepository.upsert(fichier);
        fichier.setId(id);
        return toDto(fichier);
    }

    private Path resolveArchiveDir(ArchiveDossier archive) {
        Path dir = Path.of(asyncJobService.resolveStorageDir(),
                "archives",
                String.valueOf(archive.getHopitalId()),
                String.valueOf(archive.getId()));
        try {
            Files.createDirectories(dir);
        } catch (Exception e) {
            throw new IllegalStateException("Impossible de créer le dossier de stockage PDF archives", e);
        }
        return dir;
    }

    private Map<String, Object> buildBulletinParams(BonSortie b, Patient patient) {
        Map<String, Object> params = new HashMap<>();
        params.put("nomPatient", ((patient.getPrenom() != null ? patient.getPrenom() + " " : "")
                + (patient.getNom() != null ? patient.getNom() : "")).trim());
        params.put("dateSortie", b.getDateSortie() != null
                ? java.sql.Timestamp.valueOf(b.getDateSortie())
                : null);
        params.put("diagnosticFinal", b.getDiagnosticFinal());
        params.put("etatSortie", b.getEtatSortie());
        params.put("recommandations", b.getRecommandationsPostHospitalisation());
        params.put("numeroBon", b.getNumeroBon());
        return params;
    }

    private void assertArchiveTenant(ArchiveDossier archive) {
        if (archive == null || archive.getId() == null || archive.getPatientId() == null
                || archive.getHopitalId() == null) {
            throw new IllegalArgumentException("Archive invalide pour génération PDF");
        }
        Integer tenant = TenantContext.getRequiredHopitalId();
        if (!Objects.equals(tenant, archive.getHopitalId())) {
            throw new ForbiddenException("Violation multi-tenant : archive hors établissement courant.");
        }
        TenantAuthorization.assertSameTenant(archive.getHopitalId());
    }

    private void assertHopital(Integer hopitalId) {
        if (hopitalId == null) {
            throw new ForbiddenException("Hôpital requis.");
        }
        TenantAuthorization.assertSameTenant(hopitalId);
    }

    private ArchiveFichierDto toDto(ArchiveFichier f) {
        ArchiveFichierDto dto = new ArchiveFichierDto();
        dto.setId(f.getId());
        dto.setArchiveId(f.getArchiveId());
        dto.setTypeFichier(f.getTypeFichier());
        dto.setNomFichier(f.getNomFichier());
        dto.setMimeType(f.getMimeType());
        dto.setTailleOctets(f.getTailleOctets());
        dto.setGenereAt(f.getGenereAt());
        dto.setDownloadUrl("/api/archives/" + f.getArchiveId() + "/fichiers/" + f.getId() + "/download");
        return dto;
    }

    private Integer safeUserId() {
        try {
            return currentUserService.getCurrentUtilisateurId();
        } catch (Exception e) {
            return null;
        }
    }

    private static String codePatient(ArchiveDossier archive, Patient patient) {
        if (archive.getNumeroDossier() != null) return archive.getNumeroDossier();
        if (patient.getCodePatient() != null) return patient.getCodePatient();
        return "PT-" + archive.getPatientId();
    }

    private static String nomPatient(ArchiveDossier archive, Patient patient) {
        if (archive.getNomPatient() != null) return archive.getNomPatient();
        return ((patient.getPrenom() != null ? patient.getPrenom() + " " : "")
                + (patient.getNom() != null ? patient.getNom() : "")).trim();
    }

    private static String sanitize(String raw) {
        if (raw == null || raw.isBlank()) {
            return "x";
        }
        String cleaned = raw.trim()
                .replaceAll("[\\\\/:*?\"<>|]+", "_")
                .replaceAll("\\s+", "_");
        if (cleaned.length() > 80) {
            cleaned = cleaned.substring(0, 80);
        }
        return cleaned.isBlank() ? "x" : cleaned;
    }

    private static String extractExtension(String filename) {
        if (filename == null) return "";
        int dot = filename.lastIndexOf('.');
        if (dot < 0 || dot == filename.length() - 1) return "";
        return filename.substring(dot + 1).toLowerCase(Locale.ROOT);
    }

    private static String detectMime(String contentType, String extension) {
        if (contentType != null && !contentType.isBlank() && !"application/octet-stream".equals(contentType)) {
            return contentType.split(";")[0].trim().toLowerCase(Locale.ROOT);
        }
        return switch (extension) {
            case "pdf" -> "application/pdf";
            case "jpg", "jpeg" -> "image/jpeg";
            case "png" -> "image/png";
            case "tif", "tiff" -> "image/tiff";
            case "webp" -> "image/webp";
            case "gif" -> "image/gif";
            case "txt" -> "text/plain";
            case "doc" -> "application/msword";
            case "docx" -> "application/vnd.openxmlformats-officedocument.wordprocessingml.document";
            case "xls" -> "application/vnd.ms-excel";
            case "xlsx" -> "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
            case "zip" -> "application/zip";
            default -> contentType != null ? contentType : "application/octet-stream";
        };
    }

    private static boolean isAllowedMime(String mime, String extension) {
        if (extension != null && !extension.isBlank() && ALLOWED_EXT.contains(extension)) {
            return true;
        }
        return mime != null && ALLOWED_MIME.contains(mime);
    }

    private <T> List<T> safeList(java.util.concurrent.Callable<List<T>> supplier) {
        try {
            List<T> list = supplier.call();
            return list != null ? list : new ArrayList<>();
        } catch (Exception e) {
            log.warn("Lecture documents archive ignorée: {}", e.getMessage());
            return new ArrayList<>();
        }
    }
}
