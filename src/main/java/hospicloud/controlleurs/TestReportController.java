package hospicloud.controlleurs;

import hospicloud.servicesImpl.TestOrdonnanceReportService;
import hospicloud.servicesImpl.reportingimpl.JasperReportServiceImpl;
import net.sf.jasperreports.engine.JRDataSource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/test-reports")
public class TestReportController {

    private final JasperReportServiceImpl jasperService;
    private final TestOrdonnanceReportService testService;

    public TestReportController(JasperReportServiceImpl jasperService,
                               TestOrdonnanceReportService testService) {
        this.jasperService = jasperService;
        this.testService = testService;
    }

    @GetMapping("/ordonnance/test")
    public ResponseEntity<byte[]> getTestOrdonnanceReport() {
        try {
            // 1. Déterminer le fichier Jasper
            String reportName = "Ordonnance.jasper";

            // 2. Charger les paramètres de test
            Map<String, Object> params = testService.getTestOrdonnanceParams();
            
            // DEBUG: Vérifier tous les paramètres
            System.out.println("=== TEST REPORT DEBUG ===");
            for (Map.Entry<String, Object> entry : params.entrySet()) {
                System.out.println("Paramètre " + entry.getKey() + " = " + entry.getValue());
            }
            
            // DEBUG: Vérifier tous les paramètres
            System.out.println("=== TEST REPORT DEBUG ===");
            for (Map.Entry<String, Object> entry : params.entrySet()) {
                System.out.println("Paramètre " + entry.getKey() + " = " + entry.getValue());
            }

            // 3. Créer un DataSource avec le contenu de l'ordonnance
            JRDataSource dataSource = testService.createTestDataSource();

            // 4. Génération PDF
            byte[] pdfContent = jasperService.generate(reportName, params, dataSource);

            // 5. Retour HTTP
            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_PDF)
                    .header(HttpHeaders.CONTENT_DISPOSITION,
                            "inline; filename=test_ordonnance.pdf")
                    .body(pdfContent);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body(e.getMessage().getBytes());
        }
    }
}