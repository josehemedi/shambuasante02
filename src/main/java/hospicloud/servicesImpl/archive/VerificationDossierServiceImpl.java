package hospicloud.servicesImpl.archive;

import hospicloud.dtos.archive.VerificationDossierResultDto;
import hospicloud.exceptions.BadRequestException;
import hospicloud.model.archive.ReglesArchivageHopital;
import hospicloud.model.archive.TypeEpisode;
import hospicloud.services.archive.VerificationDossierService;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Map;

@Service
@Transactional(readOnly = true)
public class VerificationDossierServiceImpl implements VerificationDossierService {

    private final JdbcTemplate jdbcTemplate;

    public VerificationDossierServiceImpl(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public VerificationDossierResultDto verifier(Integer hopitalId, TypeEpisode typeEpisode,
                                                 Long episodeId, Long patientId) {
        return verifierAvecRegles(hopitalId, typeEpisode, episodeId, patientId, null);
    }

    @Override
    public VerificationDossierResultDto verifierAvecRegles(Integer hopitalId, TypeEpisode typeEpisode,
                                                           Long episodeId, Long patientId,
                                                           ReglesArchivageHopital regles) {
        if (typeEpisode == null || episodeId == null) {
            throw new BadRequestException("Type d'épisode et identifiant requis pour la vérification.");
        }

        ReglesArchivageHopital rules = regles != null ? regles
                : defaultRegles();

        VerificationDossierResultDto result = new VerificationDossierResultDto();

        switch (typeEpisode) {
            case CONSULTATION -> verifierConsultation(hopitalId, episodeId, patientId, rules, result);
            case HOSPITALISATION, URGENCE -> verifierHospitalisation(hopitalId, episodeId, patientId, rules, result);
            case ADMINISTRATIF -> verifierAdministratif(hopitalId, episodeId, patientId, result);
        }

        result.setComplet(result.getManquants().isEmpty());
        result.setPeutArchiver(result.isComplet());
        return result;
    }

    private void verifierConsultation(Integer hopitalId, Long episodeId, Long patientId,
                                      ReglesArchivageHopital rules,
                                      VerificationDossierResultDto result) {
        List<Map<String, Object>> rows = jdbcTemplate.queryForList("""
                SELECT statut, diagnostic, fiche_finalisee, id_patient, id_hopital
                FROM consultations_medicales
                WHERE id_consultation = ? AND id_hopital = ?
                """, episodeId, hopitalId);

        if (rows.isEmpty()) {
            result.addManquant("Consultation introuvable pour cet hôpital.");
            return;
        }

        Map<String, Object> row = rows.get(0);
        if (patientId != null && !patientId.equals(toLong(row.get("id_patient")))) {
            result.addManquant("La consultation ne correspond pas au patient indiqué.");
        }

        String statut = String.valueOf(row.get("statut"));
        if (!"SIGNEE".equalsIgnoreCase(statut)) {
            result.addManquant("La consultation n'est pas terminée et signée.");
        }

        if (rules.isExigerClotureMedicale()) {
            if (!StringUtils.hasText((String) row.get("diagnostic"))) {
                result.addManquant("Diagnostic non enregistré.");
            }
            Object fiche = row.get("fiche_finalisee");
            boolean finalisee = fiche instanceof Boolean b ? b
                    : fiche != null && ("1".equals(String.valueOf(fiche)) || Boolean.parseBoolean(String.valueOf(fiche)));
            if (!finalisee) {
                result.addManquant("Fiche de consultation non finalisée.");
            }
        }

        verifierAnalysesEnAttente(hopitalId, toLong(row.get("id_patient")), result);
        verifierPrescriptions(hopitalId, toLong(row.get("id_patient")), rules, result);
    }

    private void verifierHospitalisation(Integer hopitalId, Long episodeId, Long patientId,
                                         ReglesArchivageHopital rules,
                                         VerificationDossierResultDto result) {
        List<Map<String, Object>> rows = jdbcTemplate.queryForList("""
                SELECT statut, id_patient FROM admission
                WHERE id_admission = ? AND id_hopital = ?
                """, episodeId.intValue(), hopitalId);

        if (rows.isEmpty()) {
            result.addManquant("Hospitalisation introuvable pour cet hôpital.");
            return;
        }

        Map<String, Object> row = rows.get(0);
        if (patientId != null && !patientId.equals(toLong(row.get("id_patient")))) {
            result.addManquant("L'hospitalisation ne correspond pas au patient indiqué.");
        }

        String statut = String.valueOf(row.get("statut"));
        if (!List.of("SORTI", "TERMINE", "SORTIE_AUTORISEE").contains(statut.toUpperCase())) {
            result.addManquant("L'hospitalisation n'est pas clôturée.");
        }

        if (rules.isExigerClotureMedicale()) {
            Integer count = jdbcTemplate.queryForObject("""
                    SELECT COUNT(1) FROM bons_sortie
                    WHERE id_admission = ? AND id_hopital = ?
                      AND statut_workflow IN ('DELIVRE', 'AUTORISE_MEDICALEMENT')
                    """, Integer.class, episodeId.intValue(), hopitalId);
            if (count == null || count == 0) {
                result.addManquant("Compte rendu de sortie absent ou non validé.");
            }
        }

        if (rules.isExigerClotureAdministrative()) {
            result.addAvertissement("Clôture administrative non vérifiée automatiquement.");
        }

        if (rules.isExigerClotureFinanciere()) {
            result.addAvertissement("Clôture financière non bloquante par défaut — vérification manuelle recommandée.");
        }
    }

    private void verifierAdministratif(Integer hopitalId, Long episodeId, Long patientId,
                                       VerificationDossierResultDto result) {
        List<Map<String, Object>> rows = jdbcTemplate.queryForList("""
                SELECT statut, id_patient FROM admission
                WHERE id_admission = ? AND id_hopital = ?
                """, episodeId.intValue(), hopitalId);

        if (rows.isEmpty()) {
            result.addManquant("Épisode administratif introuvable.");
            return;
        }

        Map<String, Object> row = rows.get(0);
        if (patientId != null && !patientId.equals(toLong(row.get("id_patient")))) {
            result.addManquant("L'épisode ne correspond pas au patient indiqué.");
        }

        String statut = String.valueOf(row.get("statut"));
        if (!"TERMINE".equalsIgnoreCase(statut)) {
            result.addManquant("Dossier administratif non clôturé.");
        }
    }

    private void verifierAnalysesEnAttente(Integer hopitalId, Long patientId,
                                           VerificationDossierResultDto result) {
        if (patientId == null) return;
        Integer pending = jdbcTemplate.queryForObject("""
                SELECT COUNT(1) FROM analyses_laboratoire
                WHERE id_hopital = ? AND id_patient = ?
                  AND statut IN ('EN_ATTENTE', 'PRELEVE', 'EN_COURS')
                """, Integer.class, hopitalId, patientId);
        if (pending != null && pending > 0) {
            result.addManquant("Analyses de laboratoire encore en attente (" + pending + ").");
        }
    }

    private void verifierPrescriptions(Integer hopitalId, Long patientId,
                                       ReglesArchivageHopital rules,
                                       VerificationDossierResultDto result) {
        if (!rules.isExigerClotureMedicale() || patientId == null) return;
        Integer count = jdbcTemplate.queryForObject("""
                SELECT COUNT(1) FROM ordonnances_medicales
                WHERE hospital_id = ? AND id_patient = ?
                """, Integer.class, hopitalId, patientId);
        if (count == null || count == 0) {
            result.addAvertissement("Aucune ordonnance enregistrée pour cet épisode.");
        }
    }

    private ReglesArchivageHopital defaultRegles() {
        ReglesArchivageHopital r = new ReglesArchivageHopital();
        r.setExigerClotureMedicale(true);
        return r;
    }

    private Long toLong(Object value) {
        if (value == null) return null;
        if (value instanceof Number n) return n.longValue();
        return Long.parseLong(String.valueOf(value));
    }
}
