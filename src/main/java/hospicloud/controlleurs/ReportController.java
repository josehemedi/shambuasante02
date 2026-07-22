package hospicloud.controlleurs;

import hospicloud.services.ConsultationMedicaleService;
import hospicloud.services.OrdonnanceService;
import hospicloud.services.reporting.ReportGenerator;
import hospicloud.servicesImpl.LaboratoireReportService;
import net.sf.jasperreports.engine.JRDataSource;
import net.sf.jasperreports.engine.JREmptyDataSource;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * API générique des documents médicaux — tous générés en JasperReports (.jasper).
 */
@RestController
@RequestMapping("/api/reports")
public class ReportController {

    private final ReportGenerator reportGenerator;
    private final ConsultationMedicaleService consultationService;
    private final OrdonnanceService ordonnanceService;
    private final LaboratoireReportService laboratoireReportService;

    public ReportController(ReportGenerator reportGenerator,
                            ConsultationMedicaleService consultationService,
                            OrdonnanceService ordonnanceService,
                            LaboratoireReportService laboratoireReportService) {
        this.reportGenerator = reportGenerator;
        this.consultationService = consultationService;
        this.ordonnanceService = ordonnanceService;
        this.laboratoireReportService = laboratoireReportService;
    }

    @GetMapping("/{type}/{id}")
    public ResponseEntity<byte[]> getReport(@PathVariable String type,
                                            @PathVariable Long id) {
        try {
            if ("labo".equalsIgnoreCase(type)) {
                byte[] pdf = laboratoireReportService.genererPdf(id.intValue());
                return pdfResponse(pdf, "labo_" + id + ".pdf");
            }

            String reportName = determineReportName(type);
            Map<String, Object> params = fetchDataForReport(type, id);

            JRDataSource dataSource;
            if ("ordonnance".equalsIgnoreCase(type)) {
                String contenuOrdonnance = (String) params.get("contenuOrdonnance");
                Map<String, Object> fieldData = new HashMap<>();
                fieldData.put("contenuOrdonnance", contenuOrdonnance);
                List<Map<String, Object>> dataList = new ArrayList<>();
                dataList.add(fieldData);
                dataSource = new JRBeanCollectionDataSource(dataList);
            } else {
                dataSource = new JREmptyDataSource();
            }

            byte[] pdfContent = reportGenerator.generate(reportName, params, dataSource);
            return pdfResponse(pdfContent, type + "_" + id + ".pdf");

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    private ResponseEntity<byte[]> pdfResponse(byte[] pdf, String filename) {
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=" + filename)
                .body(pdf);
    }

    private String determineReportName(String type) {
        return switch (type.toLowerCase()) {
            case "ordonnance" -> "Ordonnance.jasper";
            case "labo" -> "Laboratoire_Bon_Examen.jasper";
            case "certificat" -> "Certificat_Medical.jasper";
            case "consultation" -> "Fiche_Consultation.jasper";
            case "bulletin" -> "Bulletin_Sortie.jasper";
            default -> throw new IllegalArgumentException("Type de rapport invalide : " + type);
        };
    }

    private Map<String, Object> fetchDataForReport(String type, Long id) {
        return switch (type.toLowerCase()) {
            case "ordonnance" -> ordonnanceService.getOrdonnanceParams(id);
            case "consultation" -> consultationService.getOrdonnanceParams(id);
            default -> throw new IllegalArgumentException("Service non trouvé pour : " + type);
        };
    }
}
