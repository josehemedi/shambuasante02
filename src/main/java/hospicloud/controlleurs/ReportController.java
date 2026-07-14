package hospicloud.controlleurs;

import hospicloud.services.ConsultationMedicaleService;
import hospicloud.services.OrdonnanceService;
import hospicloud.servicesImpl.reportingimpl.JasperReportServiceImpl;
import net.sf.jasperreports.engine.JRDataSource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/reports")
public class ReportController {

    private final JasperReportServiceImpl jasperService;
    private final ConsultationMedicaleService consultationService;
    private final OrdonnanceService ordonnanceService;

    public ReportController(JasperReportServiceImpl jasperService,
                            ConsultationMedicaleService consultationService,
                            OrdonnanceService ordonnanceService) {
        this.jasperService = jasperService;
        this.consultationService = consultationService;
        this.ordonnanceService = ordonnanceService;
    }

    @GetMapping("/{type}/{id}")
    public ResponseEntity<byte[]> getReport(@PathVariable String type,
                                            @PathVariable Long id) {
        try {

            // 1. Déterminer le fichier Jasper
            String reportName = determineReportName(type);

            // 2. Charger les paramètres Jasper
            Map<String, Object> params = fetchDataForReport(type, id);

            // 3. Créer un DataSource avec le champ contenuOrdonnance (ordonnance uniquement)
            JRDataSource dataSource;
            if ("ordonnance".equalsIgnoreCase(type)) {
                String contenuOrdonnance = (String) params.get("contenuOrdonnance");
                Map<String, Object> fieldData = new HashMap<>();
                fieldData.put("contenuOrdonnance", contenuOrdonnance);
                List<Map<String, Object>> dataList = new ArrayList<>();
                dataList.add(fieldData);
                dataSource = new net.sf.jasperreports.engine.data.JRBeanCollectionDataSource(dataList);
            } else {
                dataSource = null;
            }

            // 4. Génération PDF
            byte[] pdfContent = jasperService.generate(reportName, params, dataSource);

            // 5. Retour HTTP
            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_PDF)
                    .header(HttpHeaders.CONTENT_DISPOSITION,
                            "inline; filename=" + type + "_" + id + ".pdf")
                    .body(pdfContent);

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(null);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body(null);
        }
    }

    private String determineReportName(String type) {
        return switch (type.toLowerCase()) {
            case "ordonnance" -> "Ordonnance.jasper";
            case "labo" -> "Laboratoire_Bon_Examen.jasper";
            case "certificat" -> "Certificat_Medical.jasper";
            case "consultation" -> "Fiche_Consultation.jasper";
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