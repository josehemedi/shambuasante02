package hospicloud.servicesImpl.archive;

import hospicloud.dtos.archive.ArchiveDossierResponseDto;
import hospicloud.dtos.archive.ArchiveExportFormat;
import hospicloud.dtos.archive.ArchiveExportResultDto;
import hospicloud.dtos.archive.ArchiveFichierDto;
import hospicloud.exceptions.BadRequestException;
import hospicloud.exceptions.ForbiddenException;
import hospicloud.exceptions.ResourceNotFoundException;
import hospicloud.model.archive.ArchiveFichier;
import hospicloud.security.archive.ArchivePermissionService;
import hospicloud.services.archive.ArchivageService;
import hospicloud.services.archive.ArchiveExportService;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.rendering.ImageType;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageOutputStream;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.zip.Deflater;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Service
public class ArchiveExportServiceImpl implements ArchiveExportService {

    private static final Logger log = LoggerFactory.getLogger(ArchiveExportServiceImpl.class);
    private static final float RENDER_DPI = 150f;
    private static final DateTimeFormatter TS = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");

    private final ArchivageService archivageService;
    private final ArchivePermissionService permissionService;
    private final ArchiveAuditHelper auditHelper;

    public ArchiveExportServiceImpl(ArchivageService archivageService,
                                    ArchivePermissionService permissionService,
                                    ArchiveAuditHelper auditHelper) {
        this.archivageService = archivageService;
        this.permissionService = permissionService;
        this.auditHelper = auditHelper;
    }

    @Override
    @Transactional(readOnly = true)
    public ArchiveExportResultDto exporter(Long archiveId, ArchiveExportFormat format) {
        permissionService.require(ArchivePermissionService.ARCHIVE_VOIR);
        if (permissionService.isSuperAdminTechnicalOnly()) {
            throw new ForbiddenException("Le super administrateur n'a pas accès au contenu médical des archives.");
        }
        if (archiveId == null || format == null) {
            throw new BadRequestException("Identifiant d'archive et format d'export requis.");
        }

        ArchiveDossierResponseDto archive = archivageService.consulter(archiveId);
        ArchiveExportResultDto result;
        try {
            result = switch (format) {
                case ZIP -> buildZip(archive);
                case PDF_OPTIMIZED -> buildOptimizedPdf(archive);
                case PNG -> buildPngPackage(archive);
                case TIFF -> buildLosslessTiff(archive);
            };
        } catch (BadRequestException | ForbiddenException | ResourceNotFoundException e) {
            throw e;
        } catch (Exception e) {
            log.error("Export archive {} format {} échoué: {}", archiveId, format, e.getMessage(), e);
            throw new IllegalStateException("Échec de la compression du dossier patient: " + e.getMessage(), e);
        }

        auditHelper.log(
                "ARCHIVE_EXPORT_" + format.name(),
                "SUCCESS",
                "Export " + format.name() + " — " + result.getFilename() + " (" + result.getSizeBytes() + " o)",
                archiveId,
                null,
                null,
                null
        );
        return result;
    }

    private ArchiveExportResultDto buildZip(ArchiveDossierResponseDto archive) throws IOException {
        List<ArchiveFichierDto> fichiers = archive.getFichiers();
        if (fichiers == null || fichiers.isEmpty()) {
            throw new BadRequestException("Aucun fichier à compresser. Générez d'abord les PDF du dossier.");
        }

        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try (ZipOutputStream zos = new ZipOutputStream(bos)) {
            zos.setLevel(Deflater.BEST_COMPRESSION);
            int index = 1;
            for (ArchiveFichierDto fichier : fichiers) {
                if (fichier.getId() == null) continue;
                byte[] content = archivageService.telechargerFichier(archive.getId(), fichier.getId());
                String entryName = safeZipName(index++, fichier.getNomFichier(), fichier.getTypeFichier());
                ZipEntry entry = new ZipEntry(entryName);
                entry.setSize(content.length);
                zos.putNextEntry(entry);
                zos.write(content);
                zos.closeEntry();
            }

            String manifest = buildManifest(archive, fichiers);
            ZipEntry meta = new ZipEntry("MANIFEST.txt");
            zos.putNextEntry(meta);
            zos.write(manifest.getBytes(StandardCharsets.UTF_8));
            zos.closeEntry();
        }

        return resultOf(
                ArchiveExportFormat.ZIP,
                bos.toByteArray(),
                baseName(archive) + "_dossier_complet.zip",
                "application/zip",
                fichiers.size()
        );
    }

    private ArchiveExportResultDto buildOptimizedPdf(ArchiveDossierResponseDto archive) throws IOException {
        byte[] source = resolveDossierPdf(archive);
        byte[] optimized = optimizePdf(source);
        return resultOf(
                ArchiveExportFormat.PDF_OPTIMIZED,
                optimized,
                baseName(archive) + "_dossier_optimise.pdf",
                "application/pdf",
                countPdfPages(optimized)
        );
    }

    private ArchiveExportResultDto buildPngPackage(ArchiveDossierResponseDto archive) throws IOException {
        byte[] source = resolveDossierPdf(archive);
        try (PDDocument document = Loader.loadPDF(source)) {
            PDFRenderer renderer = new PDFRenderer(document);
            int pages = document.getNumberOfPages();
            if (pages <= 0) {
                throw new BadRequestException("Le PDF du dossier ne contient aucune page.");
            }

            if (pages == 1) {
                BufferedImage image = renderer.renderImageWithDPI(0, RENDER_DPI, ImageType.RGB);
                byte[] png = toPngBytes(image);
                return resultOf(
                        ArchiveExportFormat.PNG,
                        png,
                        baseName(archive) + "_page_001.png",
                        "image/png",
                        1
                );
            }

            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            try (ZipOutputStream zos = new ZipOutputStream(bos)) {
                zos.setLevel(Deflater.BEST_COMPRESSION);
                for (int i = 0; i < pages; i++) {
                    BufferedImage image = renderer.renderImageWithDPI(i, RENDER_DPI, ImageType.RGB);
                    byte[] png = toPngBytes(image);
                    String name = String.format(Locale.ROOT, "page_%03d.png", i + 1);
                    ZipEntry entry = new ZipEntry(name);
                    zos.putNextEntry(entry);
                    zos.write(png);
                    zos.closeEntry();
                }
            }
            return resultOf(
                    ArchiveExportFormat.PNG,
                    bos.toByteArray(),
                    baseName(archive) + "_pages_png.zip",
                    "application/zip",
                    pages
            );
        }
    }

    private ArchiveExportResultDto buildLosslessTiff(ArchiveDossierResponseDto archive) throws IOException {
        byte[] source = resolveDossierPdf(archive);
        try (PDDocument document = Loader.loadPDF(source)) {
            PDFRenderer renderer = new PDFRenderer(document);
            int pages = document.getNumberOfPages();
            if (pages <= 0) {
                throw new BadRequestException("Le PDF du dossier ne contient aucune page.");
            }

            Iterator<ImageWriter> writers = ImageIO.getImageWritersByFormatName("TIFF");
            if (!writers.hasNext()) {
                throw new IllegalStateException("Aucun encodeur TIFF disponible sur le serveur.");
            }
            ImageWriter writer = writers.next();
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            try (ImageOutputStream ios = ImageIO.createImageOutputStream(bos)) {
                writer.setOutput(ios);
                ImageWriteParam param = writer.getDefaultWriteParam();
                if (param.canWriteCompressed()) {
                    param.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
                    String[] types = param.getCompressionTypes();
                    String compression = pickLosslessTiffCompression(types);
                    if (compression != null) {
                        param.setCompressionType(compression);
                    }
                }
                writer.prepareWriteSequence(null);
                for (int i = 0; i < pages; i++) {
                    BufferedImage image = renderer.renderImageWithDPI(i, RENDER_DPI, ImageType.RGB);
                    writer.writeToSequence(new IIOImage(image, null, null), param);
                }
                writer.endWriteSequence();
            } finally {
                writer.dispose();
            }

            return resultOf(
                    ArchiveExportFormat.TIFF,
                    bos.toByteArray(),
                    baseName(archive) + "_dossier_lzw.tiff",
                    "image/tiff",
                    pages
            );
        }
    }

    private byte[] resolveDossierPdf(ArchiveDossierResponseDto archive) {
        List<ArchiveFichierDto> fichiers = archive.getFichiers();
        if (fichiers == null || fichiers.isEmpty()) {
            throw new BadRequestException("Aucun PDF disponible. Cliquez sur « Régénérer les PDF » puis réessayez.");
        }

        ArchiveFichierDto dossier = fichiers.stream()
                .filter(f -> ArchiveFichier.TYPE_DOSSIER_PATIENT.equalsIgnoreCase(safe(f.getTypeFichier())))
                .findFirst()
                .orElseGet(() -> fichiers.stream()
                        .filter(f -> {
                            String mime = safe(f.getMimeType()).toLowerCase(Locale.ROOT);
                            String name = safe(f.getNomFichier()).toLowerCase(Locale.ROOT);
                            return mime.contains("pdf") || name.endsWith(".pdf");
                        })
                        .findFirst()
                        .orElse(null));

        if (dossier == null || dossier.getId() == null) {
            throw new BadRequestException("PDF dossier introuvable. Régénérez d'abord les documents de l'archive.");
        }
        return archivageService.telechargerFichier(archive.getId(), dossier.getId());
    }

    private byte[] optimizePdf(byte[] source) throws IOException {
        try (PDDocument document = Loader.loadPDF(source);
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            // Normalise la géométrie et force la réécriture compressée des flux COS.
            for (PDPage page : document.getPages()) {
                PDRectangle box = page.getMediaBox();
                if (box != null && (box.getWidth() <= 0 || box.getHeight() <= 0)) {
                    page.setMediaBox(PDRectangle.A4);
                }
            }
            document.getDocumentInformation().setProducer("Shambua Santé — Archive PDF Optimizer");
            document.getDocumentInformation().setModificationDate(java.util.Calendar.getInstance());
            document.setAllSecurityToBeRemoved(true);
            document.save(out);
            byte[] optimized = out.toByteArray();
            // Garde le plus compact des deux (réécriture vs original).
            return optimized.length > 0 && optimized.length < source.length ? optimized : source;
        }
    }

    private int countPdfPages(byte[] pdf) {
        try (PDDocument document = Loader.loadPDF(pdf)) {
            return document.getNumberOfPages();
        } catch (Exception e) {
            return 0;
        }
    }

    private byte[] toPngBytes(BufferedImage image) throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        if (!ImageIO.write(image, "PNG", bos)) {
            throw new IllegalStateException("Impossible d'encoder l'image PNG.");
        }
        return bos.toByteArray();
    }

    private String pickLosslessTiffCompression(String[] types) {
        if (types == null || types.length == 0) return null;
        for (String candidate : List.of("LZW", "Deflate", "ZLib", "PackBits")) {
            for (String type : types) {
                if (candidate.equalsIgnoreCase(type)) {
                    return type;
                }
            }
        }
        return types[0];
    }

    private ArchiveExportResultDto resultOf(ArchiveExportFormat format,
                                            byte[] content,
                                            String filename,
                                            String contentType,
                                            int pageCount) {
        ArchiveExportResultDto dto = new ArchiveExportResultDto();
        dto.setFormat(format);
        dto.setContent(content);
        dto.setFilename(filename);
        dto.setContentType(contentType);
        dto.setSizeBytes(content != null ? content.length : 0);
        dto.setPageCount(pageCount);
        return dto;
    }

    private String baseName(ArchiveDossierResponseDto archive) {
        String code = StringUtils.hasText(archive.getNumeroDossier())
                ? archive.getNumeroDossier()
                : ("ARC-" + archive.getId());
        String patient = StringUtils.hasText(archive.getNomPatient())
                ? archive.getNomPatient()
                : "patient";
        String stamp = LocalDateTime.now().format(TS);
        return sanitize(code) + "_" + sanitize(patient) + "_" + stamp;
    }

    private String buildManifest(ArchiveDossierResponseDto archive, List<ArchiveFichierDto> fichiers) {
        StringBuilder sb = new StringBuilder();
        sb.append("Shambua Santé — Export ZIP dossier patient\n");
        sb.append("Archive ID: ").append(archive.getId()).append('\n');
        sb.append("N° dossier: ").append(nullToDash(archive.getNumeroDossier())).append('\n');
        sb.append("Patient: ").append(nullToDash(archive.getNomPatient())).append('\n');
        sb.append("Médecin: ").append(nullToDash(archive.getNomMedecin())).append('\n');
        sb.append("Statut: ").append(archive.getStatutArchive() != null ? archive.getStatutArchive().name() : "—").append('\n');
        sb.append("Généré le: ").append(LocalDateTime.now()).append('\n');
        sb.append("Fichiers: ").append(fichiers.size()).append("\n\n");
        for (ArchiveFichierDto f : fichiers) {
            sb.append("- ")
                    .append(nullToDash(f.getNomFichier()))
                    .append(" [")
                    .append(nullToDash(f.getTypeFichier()))
                    .append("] ")
                    .append(f.getTailleOctets() != null ? f.getTailleOctets() + " o" : "")
                    .append('\n');
        }
        return sb.toString();
    }

    private String safeZipName(int index, String nomFichier, String typeFichier) {
        String base = StringUtils.hasText(nomFichier) ? nomFichier : (safe(typeFichier) + "_" + index);
        base = sanitize(base);
        if (!base.contains(".")) {
            base = base + ".bin";
        }
        return String.format(Locale.ROOT, "%02d_%s", index, base);
    }

    private String sanitize(String input) {
        String value = safe(input).trim();
        if (value.isEmpty()) return "dossier";
        return value.replaceAll("[\\\\/:*?\"<>|]+", "_")
                .replaceAll("\\s+", "_")
                .replaceAll("_+", "_");
    }

    private String safe(String value) {
        return value == null ? "" : value;
    }

    private String nullToDash(String value) {
        return StringUtils.hasText(value) ? value : "—";
    }
}
