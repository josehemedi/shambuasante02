package hospicloud.servicesImpl;

import hospicloud.dtos.reporting.PlatformInvoiceReportRowDTO;
import hospicloud.servicesImpl.reportingimpl.JasperReportServiceImpl;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PlatformSubscriptionInvoicesReportPdfTest {

    @Test
    void shouldGeneratePlatformInvoicesReportPdf() throws Exception {
        JasperReportServiceImpl jasper = new JasperReportServiceImpl();

        Map<String, Object> params = new HashMap<>();
        params.put("NOM_PLATEFORME", "Shambua Santé — Plateforme");
        params.put("DATE_GENERATION", new java.sql.Timestamp(System.currentTimeMillis()));
        params.put("REFERENCE_RAPPORT", "PLT-INV-20260717000000");
        params.put("GENERE_PAR", "super.admin");
        params.put("NB_FACTURES", "3");
        params.put("NB_HOPITAUX", "3");
        params.put("TOTAL_MONTANT", "5,600 USD");
        params.put("PERIODE_RAPPORT", "Historique complet");

        List<PlatformInvoiceReportRowDTO> rows = List.of(
                new PlatformInvoiceReportRowDTO(
                        "1", "Clinique Conakry", "INV-0103", "Growth", "2,400 USD", "Payée",
                        "01/07/2026", "31/07/2026"),
                new PlatformInvoiceReportRowDTO(
                        "2", "Hôpital Labé", "INV-0088", "Starter", "800 USD", "En attente",
                        "01/06/2026", "30/06/2026"),
                new PlatformInvoiceReportRowDTO(
                        "3", "Centre Kindia", "INV-0071", "Enterprise", "2,400 USD", "En retard",
                        "01/05/2026", "31/05/2026"));

        byte[] pdf = jasper.generate(
                "Factures_Abonnements_Plateforme.jasper",
                params,
                new JRBeanCollectionDataSource(rows));

        assertNotNull(pdf);
        assertTrue(pdf.length > 2500, "PDF export factures plateforme trop petit");
        assertTrue(pdf[0] == '%' && pdf[1] == 'P' && pdf[2] == 'D' && pdf[3] == 'F');
    }
}
