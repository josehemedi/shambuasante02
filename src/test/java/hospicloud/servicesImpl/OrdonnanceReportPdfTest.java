package hospicloud.servicesImpl;

import hospicloud.servicesImpl.reportingimpl.JasperReportServiceImpl;
import hospicloud.utils.QrCodeService;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class OrdonnanceReportPdfTest {

    @Test
    void genereOrdonnancePdfAvecQr() throws Exception {
        JasperReportServiceImpl service = new JasperReportServiceImpl();
        Map<String, Object> params = new HashMap<>();
        params.put("NOM_HOPITAL", "Clinique Demo Shambua");
        params.put("ID_TENANT", "1");
        params.put("SOUS_DOMAINE", "demo");
        params.put("INFOS_ETABLISSEMENT", "Kinshasa · RDC");
        params.put("NOM_PATIENT", "Patient Test");
        params.put("AGE_PATIENT", "34 ans");
        params.put("SEXE_PATIENT", "M");
        params.put("CODE_PATIENT", "PAT-101");
        params.put("NOM_MEDECIN", "Dr Ngozi Achebe");
        params.put("SPECIALITE_MEDECIN", "Médecine générale");
        params.put("NUMERO_ORDRE_MEDECIN", "ORD-225");
        params.put("DATE_PRESCRIPTION", new java.util.Date());
        params.put("DATE_PRESCRIPTION_TEXTE", new java.text.SimpleDateFormat("dd/MM/yyyy 'à' HH:mm").format(new java.util.Date()));
        params.put("REF_ORDONNANCE", "ORD-1-99");
        params.put("STATUT_ORDONNANCE", "ACTIVE");
        params.put("DIAGNOSTIC", "Infection respiratoire aiguë — suivi post-analyse labo");
        params.put("contenuOrdonnance", "1) Amoxicilline 500 mg — 1 gélule — 3 fois / jour pendant 7 jours\n2) Paracétamol 1000 mg — 1 comprimé — si fièvre / douleur (max 3 / jour)\n3) Repos relatif — hydratation abondante — contrôle clinique à J5");
        params.put("OBSERVATIONS", "Boire beaucoup d'eau. Éviter l'automédication. Consulter en urgence si dyspnée ou fièvre > 39°C.");
        params.put("DATE_EXPIRATION", "Selon durée du traitement");
        params.put("TELEPHONE_HOPITAL", "+243 81 000 0000");
        params.put("EMAIL_HOPITAL", "contact@demo.shambua.health");
        params.put("TELEPHONE_MEDECIN", "+243 89 111 2233");
        params.put("MENTIONS_LEGALES", "Document médical confidentiel multi-tenant. Falsification interdite.");
        params.put("LOGO_HOPITAL", null);
        String qr = "SHAMBUA|ORD|1|99|ORD-1-99";
        params.put("QR_CODE_TEXTE", qr);
        params.put("QR_CODE_IMAGE", QrCodeService.generateBufferedImage(qr, 200));

        byte[] pdf = service.generate("Ordonnance.jasper", params, null);
        assertNotNull(pdf);
        assertTrue(pdf.length > 500);
        assertTrue(new String(pdf, 0, 4).startsWith("%PDF"));
    }
}
