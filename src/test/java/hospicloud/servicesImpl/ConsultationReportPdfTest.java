package hospicloud.servicesImpl;

import hospicloud.servicesImpl.reportingimpl.JasperReportServiceImpl;
import org.junit.jupiter.api.Test;

import java.sql.Timestamp;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ConsultationReportPdfTest {

    @Test
    void shouldGenerateConsultationPdfFromJrxml() throws Exception {
        JasperReportServiceImpl service = new JasperReportServiceImpl();

        Map<String, Object> params = new HashMap<>();
        params.put("NOM_HOPITAL", "Hôpital Test");
        params.put("LOGO_HOPITAL", null);
        params.put("NOM_PATIENT", "Amara Diallo");
        params.put("AGE_PATIENT", "35 ans");
        params.put("NOM_MEDECIN", "Dr Ngozi Achebe");
        params.put("DATE_CONSULTATION", new Timestamp(System.currentTimeMillis()));
        params.put("ID_RDV", "#12");
        params.put("MOTIF_VISITE", "Contrôle de routine");
        params.put("POIDS", "68 kg");
        params.put("TAILLE", "170 cm");
        params.put("TENSION_ARTERIELLE", "120/80");
        params.put("TEMPERATURE", "36.8 °C");
        params.put("FREQUENCE_CARDIAQUE", "72 bpm");
        params.put("OBSERVATIONS", "Patient en bon état général.");
        params.put("DIAGNOSTIC", "État stable");
        params.put("ANALYSES_TEXTE", "1. NFS — Résultat : normal");
        params.put("QR_CODE_IMAGE", null);
        params.put("REF_CONSULTATION", "CONS-1");
        params.put("ID_TENANT", "1");
        params.put("SOUS_DOMAINE", "demo");
        params.put("INFOS_ETABLISSEMENT", "123 Avenue Test · Dakar · Sénégal");
        params.put("DOCUMENT_SIGNE", Boolean.TRUE);
        params.put("DATE_SIGNATURE_TEXTE", "11/07/2026 10:30");
        params.put("REFERENCE_SIGNATURE", "SIG-CONS-1-1-20260711103000");
        params.put("NUMERO_ORDRE_MEDECIN", "ORD-12345");
        params.put("HASH_ABREGE", "a1b2c3d4…9f8e7d6c");

        byte[] pdf = service.generate("Fiche_Consultation.jasper", params, null);

        assertNotNull(pdf);
        assertTrue(pdf.length > 100, "PDF trop petit");
        assertTrue(pdf[0] == '%' && pdf[1] == 'P' && pdf[2] == 'D' && pdf[3] == 'F', "En-tête PDF invalide");
    }
}
