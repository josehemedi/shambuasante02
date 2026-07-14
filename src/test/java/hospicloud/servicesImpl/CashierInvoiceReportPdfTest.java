package hospicloud.servicesImpl;

import hospicloud.dtos.reporting.CashierInvoiceLineRowDTO;
import hospicloud.servicesImpl.reportingimpl.JasperReportServiceImpl;
import hospicloud.utils.QrCodeService;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CashierInvoiceReportPdfTest {

    @Test
    void shouldGeneratePatientInvoicePdfWithQrCode() throws Exception {
        JasperReportServiceImpl jasper = new JasperReportServiceImpl();

        Map<String, Object> params = new HashMap<>();
        params.put("NOM_HOPITAL", "Clinique Shambua");
        params.put("LOGO_HOPITAL", null);
        params.put("ID_TENANT", "1");
        params.put("SOUS_DOMAINE", "shambua-guinee");
        params.put("INFOS_ETABLISSEMENT", "Conakry · Guinée");
        params.put("NUMERO_FACTURE", "FAC-2026-0101");
        params.put("DATE_FACTURE", "09/07/2026");
        params.put("REFERENCE_FACTURE", "FAC-H1-101");
        params.put("DATE_GENERATION", new java.sql.Timestamp(System.currentTimeMillis()));
        params.put("GENERE_PAR", "caissier.demo");
        params.put("NOM_PATIENT", "Amara Diallo");
        params.put("CODE_PATIENT", "PAT-001");
        params.put("TELEPHONE_PATIENT", "620000001");
        params.put("SEXE_PATIENT", "Femme");
        params.put("AGE_PATIENT", "36 ans");
        params.put("MONTANT_HT", "180000 GNF");
        params.put("TVA", "20000 GNF");
        params.put("MONTANT_TTC", "200000 GNF");
        params.put("MONTANT_PAYE", "50000 GNF");
        params.put("SOLDE", "150000 GNF");
        params.put("STATUT_PAIEMENT", "Paiement partiel");
        params.put("QR_CODE_TEXTE", "SHAMBUA|FAC|1|101|FAC-2026-0101");
        params.put("QR_CODE_IMAGE", QrCodeService.generateBufferedImage("SHAMBUA|FAC|1|101|FAC-2026-0101", 180));

        List<CashierInvoiceLineRowDTO> lines = List.of(
                new CashierInvoiceLineRowDTO("1", "Consultation générale", "1", "80000 GNF", "80000 GNF", "Consultation"),
                new CashierInvoiceLineRowDTO("2", "NFS complète", "1", "45000 GNF", "45000 GNF", "Laboratoire"),
                new CashierInvoiceLineRowDTO("3", "Paracétamol 500mg", "2", "12500 GNF", "25000 GNF", "Pharmacie"));

        byte[] pdf = jasper.generate("Facture_Patient.jasper", params, new JRBeanCollectionDataSource(lines));

        assertNotNull(pdf);
        assertTrue(pdf.length > 2500, "PDF facture trop petit");
        assertTrue(pdf[0] == '%' && pdf[1] == 'P' && pdf[2] == 'D' && pdf[3] == 'F');
    }
}
