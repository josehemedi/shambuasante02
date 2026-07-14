package hospicloud.servicesImpl;

import hospicloud.dtos.LabResultatSubmitDTO;
import hospicloud.dtos.MedecinDemandeAnalyseResponseDTO;
import hospicloud.exceptions.ForbiddenException;
import hospicloud.exceptions.ResourceNotFoundException;
import hospicloud.model.Role;
import hospicloud.repositories.LaboratoryRepository;
import hospicloud.security.CurrentUserService;
import hospicloud.security.MedecinQueueTopics;
import hospicloud.security.ReceptionLiveTopics;
import hospicloud.security.TenantAuthorization;
import hospicloud.services.BillingCompositionService;
import hospicloud.services.LaborantinLabService;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Service
public class LaborantinLabServiceImpl implements LaborantinLabService {

    private final LaboratoryRepository laboratoryRepository;
    private final CurrentUserService currentUserService;
    private final SimpMessagingTemplate messagingTemplate;
    private final JdbcTemplate jdbcTemplate;
    private final BillingCompositionService billingCompositionService;

    public LaborantinLabServiceImpl(
            LaboratoryRepository laboratoryRepository,
            CurrentUserService currentUserService,
            SimpMessagingTemplate messagingTemplate,
            JdbcTemplate jdbcTemplate,
            BillingCompositionService billingCompositionService) {
        this.laboratoryRepository = laboratoryRepository;
        this.currentUserService = currentUserService;
        this.messagingTemplate = messagingTemplate;
        this.jdbcTemplate = jdbcTemplate;
        this.billingCompositionService = billingCompositionService;
    }

    @Override
    public List<MedecinDemandeAnalyseResponseDTO> listerFileHopital() {
        TenantAuthorization.assertStaffRole();
        Role role = currentUserService.getCurrentRole();
        if (role != Role.LABORANTIN && role != Role.TENANT_ADMIN) {
            throw new ForbiddenException("Accès réservé au laboratoire de votre établissement.");
        }
        Integer hopitalId = currentUserService.getCurrentHopitalId();
        return laboratoryRepository.listDemandesHopital(hopitalId);
    }

    @Override
    @Transactional
    public MedecinDemandeAnalyseResponseDTO soumettreResultat(Integer idAnalyse, LabResultatSubmitDTO request) {
        TenantAuthorization.assertStaffRole();
        Role role = currentUserService.getCurrentRole();
        if (role != Role.LABORANTIN && role != Role.TENANT_ADMIN) {
            throw new ForbiddenException("Seul un laborantin peut saisir un résultat.");
        }
        if (request == null || request.getResultatTexte() == null || request.getResultatTexte().isBlank()) {
            throw new IllegalArgumentException("Le résultat est obligatoire.");
        }

        Integer hopitalId = currentUserService.getCurrentHopitalId();
        Integer laborantinId = currentUserService.getCurrentUtilisateurId();
        MedecinDemandeAnalyseResponseDTO existing = laboratoryRepository.trouverDemande(idAnalyse, hopitalId);
        if (existing == null) {
            throw new ResourceNotFoundException("Demande d'analyse introuvable dans votre établissement.");
        }

        String statut = request.getStatut() != null && !request.getStatut().isBlank()
                ? request.getStatut().trim().toUpperCase()
                : "TERMINE";
        if (!statut.equals("EN_COURS") && !statut.equals("TERMINE") && !statut.equals("PRELEVE")) {
            statut = "TERMINE";
        }

        laboratoryRepository.soumettreResultat(
                idAnalyse,
                hopitalId,
                laborantinId,
                request.getResultatTexte().trim(),
                request.getInterpretation(),
                request.getValeursReference(),
                statut
        );

        MedecinDemandeAnalyseResponseDTO updated = laboratoryRepository.trouverDemande(idAnalyse, hopitalId);
        syncResultatVersConsultation(hopitalId, existing, updated);
        notifyMedecin(hopitalId, idAnalyse, existing, updated);
        messagingTemplate.convertAndSend(ReceptionLiveTopics.destination(hopitalId), "LAB_RESULT_READY");

        // Examen réellement terminé → taxe le prix catalogue admin (types_analyses.prix_analyse)
        if ("TERMINE".equalsIgnoreCase(statut) && existing.getIdPatient() != null) {
            billingCompositionService.chargePatientConsumptions(
                    hopitalId, existing.getIdPatient(), laborantinId);
        }

        return updated;
    }

    private void syncResultatVersConsultation(
            Integer hopitalId,
            MedecinDemandeAnalyseResponseDTO before,
            MedecinDemandeAnalyseResponseDTO updated) {
        if (before == null || before.getIdConsultation() == null || updated == null) {
            return;
        }
        try {
            String testName = updated.getTestName() != null ? updated.getTestName() : "Analyse";
            String resultat = updated.getResultatTexte() != null ? updated.getResultatTexte() : "";
            String interpretation = updated.getInterpretation() != null ? updated.getInterpretation() : "";
            String note = interpretation.isBlank() ? resultat : (resultat + " (" + interpretation + ")");
            // Met à jour analyses_prescrites JSON si présent, sinon append simple
            jdbcTemplate.update(
                    """
                    UPDATE consultations_medicales
                    SET analyses_prescrites = CASE
                        WHEN analyses_prescrites IS NULL OR TRIM(analyses_prescrites) = '' OR TRIM(analyses_prescrites) = '[]'
                        THEN CONCAT('[',
                            JSON_OBJECT('typeAnalyse', ?, 'resultat', ?, 'notes', ?),
                            ']')
                        ELSE CONCAT(
                            TRIM(TRAILING ']' FROM analyses_prescrites),
                            ',',
                            JSON_OBJECT('typeAnalyse', ?, 'resultat', ?, 'notes', ?),
                            ']')
                    END
                    WHERE id_consultation = ? AND id_hopital = ?
                    """,
                    testName, note, "Résultat labo",
                    testName, note, "Résultat labo",
                    before.getIdConsultation().longValue(),
                    hopitalId
            );
        } catch (Exception ignored) {
            // best-effort: certaines BDD n'ont pas JSON_OBJECT
            try {
                jdbcTemplate.update(
                        """
                        UPDATE consultations_medicales
                        SET observations = CONCAT(
                            COALESCE(observations, ''),
                            CASE WHEN observations IS NULL OR observations = '' THEN '' ELSE '\\n' END,
                            '[LABO] ', ?, ' → ', ?,
                            CASE WHEN ? IS NULL OR ? = '' THEN '' ELSE CONCAT(' (', ?, ')') END
                        )
                        WHERE id_consultation = ? AND id_hopital = ?
                        """,
                        updated.getTestName(),
                        updated.getResultatTexte(),
                        updated.getInterpretation(),
                        updated.getInterpretation(),
                        updated.getInterpretation(),
                        before.getIdConsultation().longValue(),
                        hopitalId
                );
            } catch (Exception ignored2) {
                // ignore
            }
        }
    }

    private void notifyMedecin(
            Integer hopitalId,
            Integer idAnalyse,
            MedecinDemandeAnalyseResponseDTO existing,
            MedecinDemandeAnalyseResponseDTO updated) {
        try {
            Integer idUtilisateurMedecin = jdbcTemplate.queryForObject(
                    "SELECT id_medecin FROM analyses_laboratoire WHERE id_analyse = ? AND id_hopital = ?",
                    Integer.class,
                    idAnalyse,
                    hopitalId
            );
            Integer idMedecinProfil = null;
            if (idUtilisateurMedecin != null) {
                List<Integer> linked = jdbcTemplate.query(
                        "SELECT id_medecin FROM utilisateurs WHERE id_utilisateur = ? AND id_hopital = ? LIMIT 1",
                        (rs, rowNum) -> rs.getObject("id_medecin") != null ? rs.getInt("id_medecin") : null,
                        idUtilisateurMedecin,
                        hopitalId
                );
                if (!linked.isEmpty()) {
                    idMedecinProfil = linked.get(0);
                }
            }

            Map<String, Object> payload = Map.of(
                    "type", "LAB_RESULT_READY",
                    "idAnalyse", idAnalyse,
                    "idConsultation", existing != null && existing.getIdConsultation() != null
                            ? existing.getIdConsultation() : 0,
                    "status", updated != null ? updated.getStatus() : "Completed",
                    "testName", updated != null && updated.getTestName() != null ? updated.getTestName() : "",
                    "patientName", updated != null && updated.getPatientName() != null ? updated.getPatientName() : "",
                    "resultatTexte", updated != null && updated.getResultatTexte() != null ? updated.getResultatTexte() : ""
            );

            if (idMedecinProfil != null) {
                messagingTemplate.convertAndSend(
                        MedecinQueueTopics.destination(hopitalId, idMedecinProfil),
                        payload
                );
            }
            // Filet de sécurité : topic réception / refresh médecin via réception live
            messagingTemplate.convertAndSend(ReceptionLiveTopics.destination(hopitalId), payload);

            // Notification persistée pour l'utilisateur médecin
            if (idUtilisateurMedecin != null) {
                try {
                    jdbcTemplate.update(
                            """
                            INSERT INTO notifications (id_utilisateur, id_hopital, titre, message, type_notification, lue, date_creation)
                            VALUES (?, ?, ?, ?, 'LAB_RESULT', FALSE, CURRENT_TIMESTAMP)
                            """,
                            idUtilisateurMedecin,
                            hopitalId,
                            "Résultat laboratoire disponible",
                            (updated != null ? updated.getPatientName() : "Patient")
                                    + " — "
                                    + (updated != null ? updated.getTestName() : "Analyse")
                                    + " : "
                                    + (updated != null ? updated.getResultatTexte() : "")
                    );
                } catch (Exception ignoredNotif) {
                    // schéma notifications variable
                }
            }
        } catch (Exception ignored) {
            // notification best-effort
        }
    }
}
