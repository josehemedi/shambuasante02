package hospicloud.tools;

import net.sf.jasperreports.engine.JasperCompileManager;

import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Compile tous les {@code .jrxml} en {@code .jasper} dans {@code src/main/resources/reports}.
 * <p>
 * Usage manuel :
 * {@code mvn -q -DskipTests exec:java -Dexec.mainClass=hospicloud.tools.CompileJasperReports -Dexec.classpathScope=compile}
 * <p>
 * Aussi lancé automatiquement en phase {@code process-resources} (voir pom.xml).
 */
public final class CompileJasperReports {

    private CompileJasperReports() {}

    public static void main(String[] args) throws Exception {
        Path reportsDir = args.length > 0
                ? Path.of(args[0])
                : Path.of("src/main/resources/reports");
        if (!Files.isDirectory(reportsDir)) {
            throw new IllegalStateException("Dossier reports introuvable: " + reportsDir.toAbsolutePath());
        }
        int ok = 0;
        int fail = 0;
        try (var stream = Files.list(reportsDir)) {
            var files = stream.filter(p -> p.toString().endsWith(".jrxml")).sorted().toList();
            for (Path jrxml : files) {
                Path jasper = jrxml.resolveSibling(
                        jrxml.getFileName().toString().replace(".jrxml", ".jasper"));
                try {
                    JasperCompileManager.compileReportToFile(jrxml.toString(), jasper.toString());
                    System.out.println("OK  " + jasper.getFileName());
                    ok++;
                } catch (Exception e) {
                    System.err.println("FAIL " + jrxml.getFileName() + " : " + e.getMessage());
                    fail++;
                }
            }
        }
        System.out.println("Jasper compile terminé : " + ok + " OK, " + fail + " échec(s).");
        if (fail > 0 && ok == 0) {
            System.exit(1);
        }
        // Échecs partiels : on garde les .jasper déjà valides (warning seulement).
    }
}
