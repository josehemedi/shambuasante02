package hospicloud.servicesImpl.reportingimpl;

import hospicloud.services.reporting.ReportGenerator;
import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.engine.util.JRLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Point d'entrée unique pour tous les PDF métier Shambua Santé.
 * Convention : toujours charger un fichier compilé {@code reports/*.jasper}.
 * Si seul le {@code .jrxml} est présent, il est compilé à la volée (et mis en cache JVM).
 */
@Service
public class JasperReportServiceImpl implements ReportGenerator {

    private static final Logger log = LoggerFactory.getLogger(JasperReportServiceImpl.class);
    private static final String REPORTS_DIR = "reports/";

    private final ConcurrentHashMap<String, JasperReport> compiledCache = new ConcurrentHashMap<>();

    @Override
    public byte[] generate(String reportName,
                           Map<String, Object> params,
                           JRDataSource dataSource) throws Exception {

        String jasperName = normalizeJasperName(reportName);
        JasperReport jasperReport = loadCompiledReport(jasperName);

        JRDataSource jrDataSource = (dataSource == null)
                ? new JREmptyDataSource()
                : dataSource;

        JasperPrint jasperPrint = JasperFillManager.fillReport(
                jasperReport,
                params != null ? params : Map.of(),
                jrDataSource
        );

        return JasperExportManager.exportReportToPdf(jasperPrint);
    }

    /**
     * Force le suffixe {@code .jasper} (ex. {@code Ordonnance} → {@code Ordonnance.jasper}).
     */
    public static String normalizeJasperName(String reportName) {
        if (reportName == null || reportName.isBlank()) {
            throw new IllegalArgumentException("Nom de rapport Jasper obligatoire.");
        }
        String name = reportName.trim();
        if (name.endsWith(".jrxml")) {
            name = name.substring(0, name.length() - 6) + ".jasper";
        } else if (!name.endsWith(".jasper")) {
            name = name + ".jasper";
        }
        return name;
    }

    private JasperReport loadCompiledReport(String jasperName) throws Exception {
        JasperReport cached = compiledCache.get(jasperName);
        if (cached != null) {
            return cached;
        }

        InputStream jasperStream = getClass().getClassLoader()
                .getResourceAsStream(REPORTS_DIR + jasperName);
        if (jasperStream != null) {
            try (jasperStream) {
                JasperReport report = (JasperReport) JRLoader.loadObject(jasperStream);
                compiledCache.put(jasperName, report);
                return report;
            }
        }

        String jrxmlName = jasperName.replace(".jasper", ".jrxml");
        InputStream jrxmlStream = getClass().getClassLoader()
                .getResourceAsStream(REPORTS_DIR + jrxmlName);
        if (jrxmlStream == null) {
            throw new FileNotFoundException(
                    "Rapport Jasper introuvable : " + REPORTS_DIR + jasperName
                            + " (ni source " + jrxmlName + ")");
        }

        log.warn("Compilation à la volée de {} — préférez précompiler en .jasper (mvn process-resources).",
                jrxmlName);
        try (jrxmlStream) {
            JasperReport report = JasperCompileManager.compileReport(jrxmlStream);
            compiledCache.put(jasperName, report);
            return report;
        }
    }
}
