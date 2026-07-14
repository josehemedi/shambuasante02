package hospicloud.servicesImpl;

import hospicloud.dtos.reporting.SubscriptionPaymentReportRowDTO;
import hospicloud.servicesImpl.reportingimpl.JasperReportServiceImpl;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TenantSubscriptionPaymentReportPdfTest {

    @Test
    void shouldGenerateSubscriptionPaymentsReportPdf() throws Exception {
        JasperReportServiceImpl jasper = new JasperReportServiceImpl();

        Map<String, Object> params = new HashMap<>();
        params.put("NOM_HOPITAL", "Clinique Shambua");
        params.put("LOGO_HOPITAL", null);
        params.put("ID_TENANT", "1");
        params.put("SOUS_DOMAINE", "shambua-guinee");
        params.put("INFOS_ETABLISSEMENT", "Conakry · Guinée");
        params.put("DATE_GENERATION", new java.sql.Timestamp(System.currentTimeMillis()));
        params.put("REFERENCE_RAPPORT", "ABO-H1-20260709000000");
        params.put("GENERE_PAR", "admin.demo");
        params.put("PLAN_ACTUEL", "Growth");
        params.put("NB_PAIEMENTS", "3");
        params.put("TOTAL_PAYE", "5,600 USD");
        params.put("PERIODE_RAPPORT", "Historique complet");

        List<SubscriptionPaymentReportRowDTO> rows = List.of(
                new SubscriptionPaymentReportRowDTO(
                        "1", "INV-0103", "Growth", "2,400 USD", "Payé / Actif",
                        "01/07/2026 09:15", "31/07/2026", "Abonnement actif"),
                new SubscriptionPaymentReportRowDTO(
                        "2", "INV-0088", "Starter", "800 USD", "Annulé",
                        "01/06/2026 14:20", "30/06/2026", "Renouvellement"),
                new SubscriptionPaymentReportRowDTO(
                        "3", "INV-0071", "Starter", "800 USD", "Annulé",
                        "01/05/2026 11:00", "31/05/2026", "Cycle clôturé"));

        byte[] pdf = jasper.generate("Abonnements_Paiements.jasper", params, new JRBeanCollectionDataSource(rows));

        assertNotNull(pdf);
        assertTrue(pdf.length > 2500, "PDF rapport abonnement trop petit");
        assertTrue(pdf[0] == '%' && pdf[1] == 'P' && pdf[2] == 'D' && pdf[3] == 'F');
    }
}
