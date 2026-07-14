package hospicloud.servicesImpl.reportingimpl;

import hospicloud.services.reporting.ReportGenerator;
import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.engine.xml.JRXmlLoader;
import org.springframework.stereotype.Service;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.Map;

@Service
public class JasperReportServiceImpl implements ReportGenerator {

    @Override
    public byte[] generate(String reportName,
                           Map<String, Object> params,
                           JRDataSource dataSource) throws Exception {

        JasperReport jasperReport = null;
        
        // 1. Charger le fichier .jasper existant
        InputStream jasperStream = getClass().getClassLoader()
                .getResourceAsStream("reports/" + reportName);
        
        if (jasperStream != null) {
            jasperReport = (JasperReport) net.sf.jasperreports.engine.util.JRLoader.loadObject(jasperStream);
        } else {
            // 2. Essayer avec .jrxml si .jasper n'existe pas
            String jrxmlName = reportName.replace(".jasper", ".jrxml");
            InputStream jrxmlStream = getClass().getClassLoader()
                    .getResourceAsStream("reports/" + jrxmlName);
            if (jrxmlStream == null) {
                throw new FileNotFoundException("Rapport non trouvé : " + reportName);
            }
            jasperReport = JasperCompileManager.compileReport(jrxmlStream);
        }

        // 3. DataSource par défaut (IMPORTANT pour les rapports simples)
        JRDataSource jrDataSource = (dataSource == null)
                ? new JREmptyDataSource()
                : dataSource;

        // 4. Remplissage du rapport Jasper
        JasperPrint jasperPrint = JasperFillManager.fillReport(
                jasperReport,
                params,
                jrDataSource
        );

        // 5. Export PDF
        return JasperExportManager.exportReportToPdf(jasperPrint);
    }
}