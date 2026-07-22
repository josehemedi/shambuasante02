package hospicloud.tools;

import net.sf.jasperreports.engine.JasperCompileManager;

public final class CompileTicketPassageReport {
    private CompileTicketPassageReport() {}

    public static void main(String[] args) throws Exception {
        String jrxml = "src/main/resources/reports/Ticket_Passage_Accueil.jrxml";
        String jasper = "src/main/resources/reports/Ticket_Passage_Accueil.jasper";
        JasperCompileManager.compileReportToFile(jrxml, jasper);
        System.out.println("OK " + jasper);
    }
}
