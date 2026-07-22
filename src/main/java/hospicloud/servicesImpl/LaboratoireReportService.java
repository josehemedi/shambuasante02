package hospicloud.servicesImpl;

import hospicloud.dtos.MedecinDemandeAnalyseResponseDTO;
import hospicloud.exceptions.BadRequestException;
import hospicloud.exceptions.ResourceNotFoundException;
import hospicloud.model.Hopital;
import hospicloud.model.Patient;
import hospicloud.repositories.HopitalRepository;
import hospicloud.repositories.LaboratoryRepository;
import hospicloud.repositories.PatientRepository;
import hospicloud.security.TenantContext;
import hospicloud.services.reporting.ReportGenerator;
import hospicloud.utils.TenantReportParamsHelper;
import net.sf.jasperreports.engine.JREmptyDataSource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Génération PDF JasperReports des résultats / bons d'examen de laboratoire.
 */
@Service
public class LaboratoireReportService {

    private static final String REPORT = "Laboratoire_Bon_Examen.jasper";
    private static final DateTimeFormatter DT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private final ReportGenerator reportGenerator;
    private final LaboratoryRepository laboratoryRepository;
    private final PatientRepository patientRepository;
    private final HopitalRepository hopitalRepository;

    public LaboratoireReportService(
            ReportGenerator reportGenerator,
            LaboratoryRepository laboratoryRepository,
            PatientRepository patientRepository,
            HopitalRepository hopitalRepository) {
        this.reportGenerator = reportGenerator;
        this.laboratoryRepository = laboratoryRepository;
        this.patientRepository = patientRepository;
        this.hopitalRepository = hopitalRepository;
    }

    @Transactional(readOnly = true)
    public byte[] genererPdf(Integer idAnalyse) {
        Integer hopitalId = TenantContext.getRequiredHopitalId();
        MedecinDemandeAnalyseResponseDTO analyse = laboratoryRepository.trouverDemande(idAnalyse, hopitalId);
        if (analyse == null) {
            throw new ResourceNotFoundException("Analyse introuvable.");
        }
        try {
            return reportGenerator.generate(REPORT, buildParams(analyse, hopitalId), new JREmptyDataSource());
        } catch (ResourceNotFoundException | BadRequestException e) {
            throw e;
        } catch (Exception e) {
            throw new IllegalStateException("Échec génération PDF labo Jasper : " + e.getMessage(), e);
        }
    }

    public Map<String, Object> buildParams(MedecinDemandeAnalyseResponseDTO analyse, Integer hopitalId) {
        Hopital hopital = TenantReportParamsHelper.resolveActiveHopital(hopitalRepository, hopitalId);
        Map<String, Object> params = new HashMap<>();
        TenantReportParamsHelper.applyTenantBranding(params, hopital, hopitalId);

        Patient patient = analyse.getIdPatient() != null
                ? patientRepository.trouverPatientParId(analyse.getIdPatient().longValue()).orElse(null)
                : null;
        String nomPatient = analyse.getPatientName();
        if (!StringUtils.hasText(nomPatient) && patient != null) {
            nomPatient = ((patient.getPrenom() != null ? patient.getPrenom() : "") + " "
                    + (patient.getNom() != null ? patient.getNom() : "")).trim();
        }
        params.put("NOM_PATIENT", StringUtils.hasText(nomPatient) ? nomPatient : "—");
        params.put("CODE_PATIENT", analyse.getPatientId() != null ? analyse.getPatientId()
                : (patient != null && patient.getIdPatient() != null ? "PT-" + patient.getIdPatient() : "—"));

        params.put("NOM_MEDECIN", StringUtils.hasText(analyse.getRequestedBy()) ? analyse.getRequestedBy() : "—");
        params.put("REF_ANALYSE", analyse.getId() != null ? analyse.getId()
                : (analyse.getIdAnalyse() != null ? "LAB-" + analyse.getIdAnalyse() : "—"));
        params.put("NOM_EXAMEN", StringUtils.hasText(analyse.getTestName()) ? analyse.getTestName() : "Analyse");
        params.put("DATE_DEMANDE", formatDate(analyse.getDate()));
        params.put("STATUT", analyse.getStatus() != null ? analyse.getStatus() : "—");
        params.put("PRIORITE", analyse.getPriority() != null ? analyse.getPriority() : "Normale");
        params.put("RESULTAT", StringUtils.hasText(analyse.getResultatTexte())
                ? analyse.getResultatTexte() : "Résultat non disponible.");
        params.put("VALEURS_REFERENCE", coalesce(analyse.getValeursReference()));
        params.put("INTERPRETATION", coalesce(analyse.getInterpretation()));
        params.put("OBSERVATIONS", coalesce(analyse.getObservationsMedecin()));
        params.put("NOTES", coalesce(analyse.getNotes()));
        params.put("DATE_GENERATION", new Date());
        return params;
    }

    private static String formatDate(LocalDateTime value) {
        return value != null ? value.format(DT) : "—";
    }

    private static String coalesce(String value) {
        return StringUtils.hasText(value) ? value : "—";
    }
}
