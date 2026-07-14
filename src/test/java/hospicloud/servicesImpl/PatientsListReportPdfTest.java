package hospicloud.servicesImpl;

import hospicloud.dtos.reporting.PatientListReportRowDTO;
import hospicloud.dtos.reporting.ReportChartRowDTO;
import hospicloud.model.Hopital;
import hospicloud.model.Patient;
import hospicloud.repositories.HopitalRepository;
import hospicloud.security.TenantContext;
import hospicloud.servicesImpl.reportingimpl.JasperReportServiceImpl;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

class PatientsListReportPdfTest {

    @BeforeEach
    void setUp() {
        TenantContext.setHopitalId(1);
    }

    @AfterEach
    void tearDown() {
        TenantContext.clear();
    }

    @Test
    void shouldGeneratePatientsListPdfWithProfessionalTable() throws Exception {
        JasperReportServiceImpl jasper = new JasperReportServiceImpl();
        HopitalRepository hopitalRepository = Mockito.mock(HopitalRepository.class);

        Hopital hopital = new Hopital();
        hopital.setIdHopital(1);
        hopital.setNomCommercial("Clinique Shambua");
        hopital.setSousDomaine("shambua-guinee");
        hopital.setVille("Conakry");
        hopital.setPays("Guinée");
        hopital.setEstActif(true);
        when(hopitalRepository.rechercherhopitalParId(1L)).thenReturn(hopital);

        Patient p1 = new Patient();
        p1.setIdPatient(1L);
        p1.setIdHopital(1);
        p1.setCodePatient("PAT-001");
        p1.setNom("Diallo");
        p1.setPrenom("Amara");
        p1.setSexe("F");
        p1.setDateNaissance(LocalDate.of(1990, 5, 12));
        p1.setGroupeSanguin("O+");
        p1.setTelephone("620000001");
        p1.setEmail("amara@example.com");
        p1.setEstActif(true);
        p1.setStatutClinique("AMBULATOIRE");
        p1.setDateEnregistrement(LocalDateTime.of(2026, 1, 15, 10, 30));

        Patient p2 = new Patient();
        p2.setIdPatient(2L);
        p2.setIdHopital(1);
        p2.setCodePatient("PAT-002");
        p2.setNom("Camara");
        p2.setPrenom("Ibrahima");
        p2.setSexe("M");
        p2.setDateNaissance(LocalDate.of(1985, 3, 20));
        p2.setEstActif(true);
        p2.setStatutClinique("ADMIS");
        p2.setDateEnregistrement(LocalDateTime.of(2026, 2, 1, 9, 0));

        List<PatientListReportRowDTO> rows = List.of(
                new PatientListReportRowDTO("1", "PAT-001", "Amara Diallo", "Femme", "12/05/1990",
                        "35 ans", "620000001", "amara@example.com", "O+", "Actif", "Ambulatoire",
                        "15/01/2026 10:30", "—", "—"),
                new PatientListReportRowDTO("2", "PAT-002", "Ibrahima Camara", "Homme", "20/03/1985",
                        "41 ans", "—", "—", "—", "Actif", "Admis",
                        "01/02/2026 09:00", "—", "—"));

        Map<String, Object> params = new HashMap<>();
        params.put("NOM_HOPITAL", "Clinique Shambua");
        params.put("ID_TENANT", "1");
        params.put("SOUS_DOMAINE", "shambua-guinee");
        params.put("INFOS_ETABLISSEMENT", "Conakry · Guinée");
        params.put("DATE_GENERATION", java.sql.Timestamp.valueOf(LocalDateTime.now()));
        params.put("DATE_RAPPORT", "09/07/2026");
        params.put("REFERENCE_RAPPORT", "PAT-LIST-H1-TEST");
        params.put("GENERE_PAR", "Dr Test");
        params.put("PERIMETRE_RAPPORT", "Patients suivis par le médecin connecté");
        params.put("KPI_TOTAL", "2");
        params.put("KPI_ACTIFS", "2");
        params.put("KPI_ADMIS", "1");
        params.put("KPI_AMBULATOIRES", "1");
        params.put("KPI_COLUMN_DS", new JRBeanCollectionDataSource(List.of(
                new ReportChartRowDTO("Total", 2L),
                new ReportChartRowDTO("Actifs", 2L),
                new ReportChartRowDTO("Admis", 1L),
                new ReportChartRowDTO("Ambulatoires", 1L))));
        params.put("STATUT_CLINIQUE_DS", new JRBeanCollectionDataSource(List.of(
                new ReportChartRowDTO("Ambulatoire", 1L),
                new ReportChartRowDTO("Admis", 1L))));
        params.put("ACTIF_DS", new JRBeanCollectionDataSource(List.of(
                new ReportChartRowDTO("Actifs", 2L),
                new ReportChartRowDTO("Inactifs", 0L))));
        params.put("SEXE_DS", new JRBeanCollectionDataSource(List.of(
                new ReportChartRowDTO("Femme", 1L),
                new ReportChartRowDTO("Homme", 1L))));
        params.put("CODE_BARRE_TEXTE", "SHAMBUA|PAT-LIST-TEST");

        byte[] pdf = jasper.generate("Liste_Patients.jasper", params, new JRBeanCollectionDataSource(rows));

        assertNotNull(pdf);
        assertTrue(pdf.length > 500, "PDF liste patients trop petit");
        assertTrue(pdf[0] == '%' && pdf[1] == 'P' && pdf[2] == 'D' && pdf[3] == 'F');
    }
}
