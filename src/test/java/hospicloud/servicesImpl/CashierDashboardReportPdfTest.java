package hospicloud.servicesImpl;

import hospicloud.dtos.reporting.CashierHistoryReportRowDTO;
import hospicloud.dtos.reporting.CashierQueueReportRowDTO;
import hospicloud.dtos.reporting.ReportChartRowDTO;
import hospicloud.servicesImpl.reportingimpl.JasperReportServiceImpl;
import net.sf.jasperreports.engine.JasperCompileManager;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CashierDashboardReportPdfTest {

    @Test
    void shouldGenerateCashierDashboardPdfWithChartsSubreportAndBarcode() throws Exception {
        JasperReportServiceImpl jasper = new JasperReportServiceImpl();

        Map<String, Object> params = new HashMap<>();
        params.put("NOM_HOPITAL", "Clinique Shambua");
        params.put("LOGO_HOPITAL", null);
        params.put("ID_TENANT", "1");
        params.put("SOUS_DOMAINE", "shambua-guinee");
        params.put("INFOS_ETABLISSEMENT", "Conakry · Guinée");
        params.put("DATE_GENERATION", new java.sql.Timestamp(System.currentTimeMillis()));
        params.put("DATE_RAPPORT", "09/07/2026");
        params.put("REFERENCE_RAPPORT", "CAISSE-H1-20260709000000");
        params.put("GENERE_PAR", "caissier.demo");
        params.put("KPI_ATTENTE", "3");
        params.put("KPI_ENCAISSE", "450000 GNF");
        params.put("KPI_PARTIEL", "1");
        params.put("KPI_SORTIE", "2");
        params.put("TOTAL_SOLDE_ATTENTE", "320000 GNF");
        params.put("NB_FILE", "3");
        params.put("NB_HISTORIQUE", "2");
        params.put("KPI_COLUMN_DS", new JRBeanCollectionDataSource(List.of(
                new ReportChartRowDTO("En attente", 3L),
                new ReportChartRowDTO("Encaissé jour", 450000L),
                new ReportChartRowDTO("Partiels", 1L),
                new ReportChartRowDTO("Sorties admin", 2L))));
        params.put("STATUS_PIE_DS", new JRBeanCollectionDataSource(List.of(
                new ReportChartRowDTO("En attente", 2L),
                new ReportChartRowDTO("Paiement partiel", 1L))));
        params.put("PAYMENT_METHOD_DS", new JRBeanCollectionDataSource(List.of(
                new ReportChartRowDTO("Espèces", 1L),
                new ReportChartRowDTO("Mobile money", 1L))));
        params.put("BALANCE_BAR_DS", new JRBeanCollectionDataSource(List.of(
                new ReportChartRowDTO("Amara D.", 120000L),
                new ReportChartRowDTO("Tunde B.", 200000L))));
        params.put("HISTORY_DS", new JRBeanCollectionDataSource(List.of(
                new CashierHistoryReportRowDTO("1", "REC-001", "FAC-101", "Amara Diallo",
                        "150000 GNF", "Espèces", "09/07/2026 10:30", "Marie K.", "50000 GNF"),
                new CashierHistoryReportRowDTO("2", "REC-002", "FAC-102", "Tunde Bakare",
                        "300000 GNF", "Mobile money", "09/07/2026 11:15", "Marie K.", "0 GNF"))));
        params.put("SUBREPORT_HISTORY", JasperCompileManager.compileReport(
                getClass().getClassLoader().getResourceAsStream("reports/Caissier_Historique.jrxml")));
        params.put("CODE_BARRE_TEXTE", "SHAMBUA|CAISSE-H1-TEST");
        params.put("BARCODE_IMAGE", hospicloud.utils.BarcodeService.generateCode128Image("SHAMBUA|CAISSE-H1-TEST", 420, 72));

        List<CashierQueueReportRowDTO> rows = List.of(
                new CashierQueueReportRowDTO("1", "Amara Diallo", "PAT-001", "FAC-101",
                        "En attente", "200000 GNF", "80000 GNF", "120000 GNF", "Haute", "Consultation", "Dr Ngozi"),
                new CashierQueueReportRowDTO("2", "Tunde Bakare", "PAT-002", "FAC-102",
                        "Paiement partiel", "500000 GNF", "300000 GNF", "200000 GNF", "Normal", "Labo", "Dr Amadou"));

        byte[] pdf = jasper.generate("Dashboard_Caissier.jasper", params, new JRBeanCollectionDataSource(rows));

        assertNotNull(pdf);
        assertTrue(pdf.length > 2500, "PDF tableau de bord caissier trop petit");
        assertTrue(pdf[0] == '%' && pdf[1] == 'P' && pdf[2] == 'D' && pdf[3] == 'F');
    }
}
