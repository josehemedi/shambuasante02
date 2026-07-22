package hospicloud.servicesImpl;

import hospicloud.servicesImpl.reportingimpl.JasperReportServiceImpl;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ReceptionTicketReportPdfTest {

    @Test
    void shouldGenerateProfessionalPassageTicketPdf() throws Exception {
        JasperReportServiceImpl jasper = new JasperReportServiceImpl();

        Map<String, Object> params = new HashMap<>();
        params.put("NOM_HOPITAL", "Clinique Shambua Conakry");
        params.put("LOGO_HOPITAL", null);
        params.put("INFOS_ETABLISSEMENT", "Conakry · Guinée");
        params.put("DATE_GENERATION", new java.sql.Timestamp(System.currentTimeMillis()));
        params.put("GENERE_PAR", "reception.demo");
        params.put("REFERENCE_TICKET", "TKT-H1-A42-20260722080000");
        params.put("NUMERO_PASSAGE", "042");
        params.put("NOM_PATIENT", "Amina Diallo");
        params.put("CODE_PATIENT", "PT-0042");
        params.put("NOM_MEDECIN", "Dr Kwame Mensah");
        params.put("SPECIALITE", "Médecine générale");
        params.put("SERVICE", "Consultations");
        params.put("MOTIF_VISITE", "Douleurs abdominales");
        params.put("PRIORITE", "Priorité normale");
        params.put("STATUT", "Orienté");
        params.put("HEURE_ARRIVEE", "08:15");
        params.put("DATE_JOUR", "22/07/2026");
        params.put("QR_CODE_IMAGE", null);

        byte[] pdf = jasper.generate("Ticket_Passage_Accueil.jasper", params, null);

        assertNotNull(pdf);
        assertTrue(pdf.length > 1500, "PDF ticket trop petit");
        assertTrue(pdf[0] == '%' && pdf[1] == 'P' && pdf[2] == 'D' && pdf[3] == 'F');
    }
}
