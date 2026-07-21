package hospicloud.tools;

import net.sf.jasperreports.engine.JasperCompileManager;

import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Compile les .jrxml en .jasper dans src/main/resources/reports.
 * Usage: mvn -q -DskipTests exec:java -Dexec.mainClass=hospicloud.tools.CompileJasperReports -Dexec.classpathScope=compile
 */
public final class CompileJasperReports {

    private CompileJasperReports() {}

    public static void main(String[] args) throws Exception {
        Path reportsDir = Path.of("src/main/resources/reports");
        if (!Files.isDirectory(reportsDir)) {
            throw new IllegalStateException("Dossier reports introuvable: " + reportsDir.toAbsolutePath());
        }
        try (var stream = Files.list(reportsDir)) {
            stream.filter(p -> p.toString().endsWith(".jrxml")).forEach(jrxml -> {
                Path jasper = jrxml.resolveSibling(
                        jrxml.getFileName().toString().replace(".jrxml", ".jasper"));
                try {
                    JasperCompileManager.compileReportToFile(jrxml.toString(), jasper.toString());
                    System.out.println("OK " + jasper.getFileName());
                } catch (Exception e) {
                    System.err.println("SKIP " + jrxml.getFileName() + " : " + e.getMessage());
                }
            });
        }
    }
}
