package hospicloud.servicesImpl;

import hospicloud.dtos.PatientDossierDTO;
import hospicloud.dtos.reporting.DossierConsultationRowDTO;
import hospicloud.dtos.reporting.ReportChartRowDTO;
import hospicloud.dtos.reporting.ReportNumericChartRowDTO;
import hospicloud.dtos.ConsultationResponseDTO;
import hospicloud.exceptions.ForbiddenException;
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

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

class PatientDossierReportPdfTest {

    @BeforeEach
    void setUp() {
        TenantContext.setHopitalId(1);
    }

    @AfterEach
    void tearDown() {
        TenantContext.clear();
    }

    @Test
    void shouldGeneratePatientDossierPdfWithCharts() throws Exception {
        JasperReportServiceImpl jasper = new JasperReportServiceImpl();
        HopitalRepository hopitalRepository = Mockito.mock(HopitalRepository.class);
        PatientDossierReportService service = new PatientDossierReportService(jasper, hopitalRepository);

        Hopital hopital = new Hopital();
        hopital.setIdHopital(1);
        hopital.setNomCommercial("Clinique Shambua");
        hopital.setSousDomaine("shambua-guinee");
        hopital.setVille("Conakry");
        hopital.setPays("Guinée");
        hopital.setEstActif(true);
        when(hopitalRepository.rechercherhopitalParId(1L)).thenReturn(hopital);

        Patient patient = new Patient();
        patient.setIdPatient(1L);
        patient.setIdHopital(1);
        patient.setNom("Diallo");
        patient.setPrenom("Amara");
        patient.setSexe("F");
        patient.setDateNaissance(LocalDate.of(1990, 5, 12));
        patient.setCodePatient("PAT-001");
        patient.setTelephone("620000001");
        patient.setEmail("amara.diallo@gmail.com");

        PatientDossierDTO dossier = new PatientDossierDTO();
        dossier.setPatient(patient);
        dossier.setConsultations(List.of());
        dossier.setAntecedents(List.of());
        dossier.setRendezVous(List.of());

        byte[] pdf = service.genererPdf(dossier);

        assertNotNull(pdf);
        assertTrue(pdf.length > 500, "PDF dossier trop petit");
        assertTrue(pdf[0] == '%' && pdf[1] == 'P' && pdf[2] == 'D' && pdf[3] == 'F');
    }

    @Test
    void shouldRejectCrossTenantDossier() {
        JasperReportServiceImpl jasper = new JasperReportServiceImpl();
        HopitalRepository hopitalRepository = Mockito.mock(HopitalRepository.class);
        PatientDossierReportService service = new PatientDossierReportService(jasper, hopitalRepository);

        TenantContext.setHopitalId(2);

        Patient patient = new Patient();
        patient.setIdPatient(1L);
        patient.setIdHopital(1);

        PatientDossierDTO dossier = new PatientDossierDTO();
        dossier.setPatient(patient);

        assertThrows(ForbiddenException.class, () -> service.genererPdf(dossier));
    }

    @Test
    void shouldGenerateDossierPdfWithVitalCharts() throws Exception {
        JasperReportServiceImpl jasper = new JasperReportServiceImpl();
        HopitalRepository hopitalRepository = Mockito.mock(HopitalRepository.class);
        PatientDossierReportService service = new PatientDossierReportService(jasper, hopitalRepository);

        Hopital hopital = new Hopital();
        hopital.setIdHopital(1);
        hopital.setNomCommercial("Clinique Shambua");
        hopital.setSousDomaine("shambua-guinee");
        hopital.setVille("Conakry");
        hopital.setPays("Guinée");
        hopital.setEstActif(true);
        when(hopitalRepository.rechercherhopitalParId(1L)).thenReturn(hopital);

        Patient patient = new Patient();
        patient.setIdPatient(1L);
        patient.setIdHopital(1);
        patient.setNom("Diallo");
        patient.setPrenom("Amara");
        patient.setSexe("F");
        patient.setDateNaissance(LocalDate.of(1990, 5, 12));
        patient.setCodePatient("PAT-001");

        ConsultationResponseDTO c1 = new ConsultationResponseDTO();
        c1.setIdHopital(1);
        c1.setDateConsultation("01/06/2026 10:00");
        c1.setPoids(new BigDecimal("72.5"));
        c1.setTemperature(new BigDecimal("37.1"));
        c1.setFrequenceCardiaque(72);

        ConsultationResponseDTO c2 = new ConsultationResponseDTO();
        c2.setIdHopital(1);
        c2.setDateConsultation("05/07/2026 14:30");
        c2.setPoids(new BigDecimal("71.0"));
        c2.setTemperature(new BigDecimal("36.8"));
        c2.setFrequenceCardiaque(68);

        PatientDossierDTO dossier = new PatientDossierDTO();
        dossier.setPatient(patient);
        dossier.setConsultations(List.of(c1, c2));
        dossier.setAntecedents(List.of());
        dossier.setRendezVous(List.of());

        byte[] pdf = service.genererPdf(dossier);

        assertNotNull(pdf);
        assertTrue(pdf.length > 1500, "PDF avec histogrammes vitaux trop petit");
    }

    @Test
    void shouldCompileDossierJrxmlDirectlyWithSampleData() throws Exception {
        JasperReportServiceImpl jasper = new JasperReportServiceImpl();

        Map<String, Object> params = new HashMap<>();
        params.put("NOM_HOPITAL", "Clinique Shambua");
        params.put("LOGO_HOPITAL", null);
        params.put("ID_TENANT", "1");
        params.put("SOUS_DOMAINE", "shambua-guinee");
        params.put("INFOS_ETABLISSEMENT", "Conakry · Guinée");
        params.put("NOM_PATIENT", "Amara Diallo");
        params.put("CODE_PATIENT", "PAT-001");
        params.put("AGE_PATIENT", "35 ans");
        params.put("SEXE_PATIENT", "F");
        params.put("GROUPE_SANGUIN", "O+");
        params.put("TELEPHONE_PATIENT", "620000001");
        params.put("EMAIL_PATIENT", "amara.diallo@gmail.com");
        params.put("DATE_GENERATION", new java.sql.Timestamp(System.currentTimeMillis()));
        params.put("NB_CONSULTATIONS", "2");
        params.put("NB_ANTECEDENTS", "3");
        params.put("NB_RDV", "1");
        params.put("ANTECEDENT_CHART_DS", new JRBeanCollectionDataSource(List.of(
                new ReportChartRowDTO("Allergie", 1L),
                new ReportChartRowDTO("Chirurgie", 2L))));
        params.put("CONSULTATIONS_BAR_DS", new JRBeanCollectionDataSource(List.of(
                new ReportChartRowDTO("juil. 2026", 2L),
                new ReportChartRowDTO("juin. 2026", 1L))));
        params.put("CONSULTATIONS_COLUMN_DS", new JRBeanCollectionDataSource(List.of(
                new ReportChartRowDTO("juil. 2026", 2L),
                new ReportChartRowDTO("juin. 2026", 1L))));
        params.put("KPI_COLUMN_DS", new JRBeanCollectionDataSource(List.of(
                new ReportChartRowDTO("Consultations", 2L),
                new ReportChartRowDTO("Antécédents", 3L),
                new ReportChartRowDTO("Rendez-vous", 1L))));
        params.put("RDV_STATUS_DS", new JRBeanCollectionDataSource(List.of(
                new ReportChartRowDTO("Confirmé", 1L))));
        params.put("POIDS_CHART_DS", new JRBeanCollectionDataSource(List.of(
                new ReportNumericChartRowDTO("01/06/26", 72.5),
                new ReportNumericChartRowDTO("05/07/26", 71.0))));
        params.put("TEMPERATURE_CHART_DS", new JRBeanCollectionDataSource(List.of(
                new ReportNumericChartRowDTO("01/06/26", 37.1),
                new ReportNumericChartRowDTO("05/07/26", 36.8))));
        params.put("FREQUENCE_CHART_DS", new JRBeanCollectionDataSource(List.of(
                new ReportNumericChartRowDTO("01/06/26", 72.0),
                new ReportNumericChartRowDTO("05/07/26", 68.0))));

        List<DossierConsultationRowDTO> rows = List.of(
                new DossierConsultationRowDTO("05/07/2026 10:00", "Contrôle", "Stable", "Dr Ngozi Achebe"),
                new DossierConsultationRowDTO("01/06/2026 14:30", "Suivi", "Amélioration", "Dr Kwame Mensah"));

        byte[] pdf = jasper.generate("Dossier_Patient.jasper", params, new JRBeanCollectionDataSource(rows));

        assertNotNull(pdf);
        assertTrue(pdf.length > 1000, "PDF avec graphiques trop petit");
    }
}
