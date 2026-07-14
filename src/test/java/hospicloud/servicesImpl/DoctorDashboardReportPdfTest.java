package hospicloud.servicesImpl;

import hospicloud.dtos.DashboardDTO;
import hospicloud.dtos.DoctorConsultationActiveDTO;
import hospicloud.dtos.MedecinFileItemDTO;
import hospicloud.dtos.DoctorPendingNoteDTO;
import hospicloud.dtos.RendezVousJourDTO;
import hospicloud.dtos.StatistiqueMedecinDTO;
import hospicloud.dtos.reporting.DoctorDashboardScheduleRowDTO;
import hospicloud.dtos.reporting.ReportChartRowDTO;
import hospicloud.model.Hopital;
import hospicloud.model.Medecin;
import hospicloud.repositories.HopitalRepository;
import hospicloud.repositories.MedecinRepository;
import hospicloud.repositories.UtilisateurRepository;
import hospicloud.security.CurrentUserService;
import hospicloud.security.TenantContext;
import hospicloud.services.DashboardService;
import hospicloud.servicesImpl.reportingimpl.JasperReportServiceImpl;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

class DoctorDashboardReportPdfTest {

    @BeforeEach
    void setUp() {
        TenantContext.setHopitalId(1);
    }

    @AfterEach
    void tearDown() {
        TenantContext.clear();
    }

    @Test
    void shouldGenerateDoctorDashboardPdfWithChartsAndBarcode() throws Exception {
        JasperReportServiceImpl jasper = new JasperReportServiceImpl();
        DashboardService dashboardService = Mockito.mock(DashboardService.class);
        HopitalRepository hopitalRepository = Mockito.mock(HopitalRepository.class);
        CurrentUserService currentUserService = Mockito.mock(CurrentUserService.class);
        MedecinRepository medecinRepository = Mockito.mock(MedecinRepository.class);
        UtilisateurRepository utilisateurRepository = Mockito.mock(UtilisateurRepository.class);

        DoctorDashboardReportService service = new DoctorDashboardReportService(
                dashboardService,
                jasper,
                hopitalRepository,
                currentUserService,
                medecinRepository,
                utilisateurRepository);

        when(currentUserService.getCurrentHopitalId()).thenReturn(1);
        when(currentUserService.getCurrentMedecinId()).thenReturn(5);

        Medecin medecin = new Medecin();
        medecin.setIdMedecin(5);
        medecin.setIdHopital(1);
        medecin.setNom("Achebe");
        medecin.setPrenom("Ngozi");
        when(medecinRepository.trouverParId(5)).thenReturn(Optional.of(medecin));

        Hopital hopital = new Hopital();
        hopital.setIdHopital(1);
        hopital.setNomCommercial("Clinique Shambua");
        hopital.setSousDomaine("shambua-guinee");
        hopital.setVille("Conakry");
        hopital.setPays("Guinée");
        hopital.setEstActif(true);
        when(hopitalRepository.rechercherhopitalParId(1L)).thenReturn(hopital);

        DashboardDTO dashboard = new DashboardDTO();
        dashboard.setStatistiques(new StatistiqueMedecinDTO(3, 1, 120, 4, 2, 0));
        dashboard.setRendezVousDuJour(List.of(
                new RendezVousJourDTO(1, "Amara Diallo", "Contrôle", LocalDateTime.now(), 30, "CONFIRME", "PHYSIQUE"),
                new RendezVousJourDTO(2, "Tunde Bakare", "Suivi", LocalDateTime.now().plusHours(1), 20, "PROGRAMME", "TELECONSULTATION")));
        dashboard.setFilePatients(List.of(new MedecinFileItemDTO() {{
            setPatientName("Patient A");
            setSalle("Salle 2");
            setWaited("15 min");
            setPriority("high");
        }}));
        dashboard.setConsultationsActives(List.of(new DoctorConsultationActiveDTO() {{
            setPatientName("Patient B");
            setMotif("Consultation en cours");
            setCanal("PHYSIQUE");
            setStartedAt(LocalDateTime.now().minusMinutes(20));
        }}));
        dashboard.setNotesEnAttente(List.of(new DoctorPendingNoteDTO() {{
            setPatientName("Patient C");
            setMotif("Note à compléter");
            setConsultationDate(LocalDateTime.now().minusHours(2));
        }}));
        when(dashboardService.getDashboardData()).thenReturn(dashboard);

        byte[] pdf = service.genererPdf();

        assertNotNull(pdf);
        assertTrue(pdf.length > 1500, "PDF tableau de bord trop petit");
        assertTrue(pdf[0] == '%' && pdf[1] == 'P' && pdf[2] == 'D' && pdf[3] == 'F');
    }

    @Test
    void shouldCompileDashboardJrxmlDirectlyWithSampleData() throws Exception {
        JasperReportServiceImpl jasper = new JasperReportServiceImpl();

        Map<String, Object> params = new HashMap<>();
        params.put("NOM_HOPITAL", "Clinique Shambua");
        params.put("LOGO_HOPITAL", null);
        params.put("ID_TENANT", "1");
        params.put("SOUS_DOMAINE", "shambua-guinee");
        params.put("INFOS_ETABLISSEMENT", "Conakry · Guinée");
        params.put("NOM_MEDECIN", "Dr Ngozi Achebe");
        params.put("ID_MEDECIN", "5");
        params.put("DATE_GENERATION", new java.sql.Timestamp(System.currentTimeMillis()));
        params.put("DATE_RAPPORT", "09/07/2026");
        params.put("REFERENCE_RAPPORT", "DASH-H1-M5-20260709000000");
        params.put("KPI_RDV", "4");
        params.put("KPI_FILE", "2");
        params.put("KPI_ACTIVES", "1");
        params.put("KPI_NOTES", "3");
        params.put("STATS_CONSULTATIONS", "3");
        params.put("STATS_PATIENTS", "120");
        params.put("STATS_EXAMENS", "2");
        params.put("FILE_ATTENTE_RESUME", "1. Patient A · Salle 2");
        params.put("CONSULTATIONS_ACTIVES_RESUME", "1. Patient B · Consultation");
        params.put("NOTES_EN_ATTENTE_RESUME", "1. Patient C · Note");
        params.put("KPI_COLUMN_DS", new JRBeanCollectionDataSource(List.of(
                new ReportChartRowDTO("RDV", 4L),
                new ReportChartRowDTO("File", 2L))));
        params.put("RDV_STATUS_DS", new JRBeanCollectionDataSource(List.of(
                new ReportChartRowDTO("Confirmé", 2L),
                new ReportChartRowDTO("Programmé", 2L))));
        params.put("QUEUE_PRIORITY_DS", new JRBeanCollectionDataSource(List.of(
                new ReportChartRowDTO("Haute", 1L))));
        params.put("CANAL_DS", new JRBeanCollectionDataSource(List.of(
                new ReportChartRowDTO("Présentiel", 2L),
                new ReportChartRowDTO("Téléconsultation", 2L))));
        params.put("WORKLOAD_BAR_DS", new JRBeanCollectionDataSource(List.of(
                new ReportChartRowDTO("Agenda", 4L),
                new ReportChartRowDTO("Notes", 3L))));
        params.put("CODE_BARRE_TEXTE", "SHAMBUA|DASH-H1-M5-TEST");
        params.put("BARCODE_IMAGE", hospicloud.utils.BarcodeService.generateCode128Image("SHAMBUA|DASH-H1-M5-TEST", 420, 72));

        List<DoctorDashboardScheduleRowDTO> rows = List.of(
                new DoctorDashboardScheduleRowDTO("09:00", "Amara Diallo", "Contrôle", "Confirmé", "Présentiel"),
                new DoctorDashboardScheduleRowDTO("10:30", "Tunde Bakare", "Suivi", "Programmé", "Téléconsultation"));

        byte[] pdf = jasper.generate("Dashboard_Medecin.jasper", params, new JRBeanCollectionDataSource(rows));

        assertNotNull(pdf);
        assertTrue(pdf.length > 2000, "PDF avec graphiques et code-barres trop petit");
    }
}
