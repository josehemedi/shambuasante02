package hospicloud.servicesImpl.rag;

import hospicloud.dtos.ConsultationResponseDTO;
import hospicloud.dtos.PatientDossierDTO;
import hospicloud.dtos.rag.RagContextBundle;
import hospicloud.exceptions.ForbiddenException;
import hospicloud.model.Antecedent;
import hospicloud.model.Ordonnance;
import hospicloud.model.Patient;
import hospicloud.model.Role;
import hospicloud.model.rag.RagDocument;
import hospicloud.repositories.rag.RagDocumentRepository;
import hospicloud.security.CurrentUserContext;
import hospicloud.security.TenantAuthorization;
import hospicloud.security.TenantContext;
import hospicloud.services.OrdonnanceService;
import hospicloud.services.PatientService;
import hospicloud.services.rag.RagRetrievalService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class RagRetrievalServiceImpl implements RagRetrievalService {

    private static final Logger log = LoggerFactory.getLogger(RagRetrievalServiceImpl.class);
    private static final int MAX_CONTEXT_CHARS = 12_000;

    private final PatientService patientService;
    private final OrdonnanceService ordonnanceService;
    private final RagDocumentRepository ragDocumentRepository;
    private final JdbcTemplate jdbcTemplate;

    public RagRetrievalServiceImpl(PatientService patientService,
                                   OrdonnanceService ordonnanceService,
                                   RagDocumentRepository ragDocumentRepository,
                                   JdbcTemplate jdbcTemplate) {
        this.patientService = patientService;
        this.ordonnanceService = ordonnanceService;
        this.ragDocumentRepository = ragDocumentRepository;
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public RagContextBundle buildContext(Long patientId, String analysisType, String userMessage) {
        Role role = CurrentUserContext.getRole();
        Integer hopitalId = TenantContext.getHopitalId();
        RagContextBundle bundle = new RagContextBundle();

        if (role == Role.SUPER_ADMIN) {
            bundle.setScope("SUPER_ADMIN");
            return buildSuperAdminContext(bundle, userMessage);
        }
        if (role == Role.TENANT_ADMIN) {
            bundle.setScope("ADMIN");
            return buildAdminContext(bundle, hopitalId, userMessage);
        }

        // Médecin (et défaut clinique)
        bundle.setScope("MEDECIN");
        if (patientId != null) {
            appendClinicalPatientContext(bundle, patientId);
        } else {
            bundle.getWarnings().add("Aucun patient sélectionné — réponse limitée aux protocoles et consignes.");
            bundle.getMissingFields().add("patientId");
        }
        appendKnowledgeDocs(bundle, hopitalId, "MEDECIN", analysisType, userMessage);
        truncate(bundle);
        return bundle;
    }

    private void appendClinicalPatientContext(RagContextBundle bundle, Long patientId) {
        try {
            PatientDossierDTO dossier = patientService.obtenirDossierComplet(patientId);
            Patient patient = dossier.getPatient();
            if (patient == null) {
                bundle.getWarnings().add("Patient introuvable.");
                return;
            }
            TenantAuthorization.assertSameTenant(patient.getIdHopital());

            StringBuilder sb = new StringBuilder();
            sb.append("=== DOSSIER PATIENT (tenant courant uniquement) ===\n");
            sb.append("Patient #").append(patient.getIdPatient());
            if (patient.getCodePatient() != null) sb.append(" · ").append(patient.getCodePatient());
            sb.append(" · ").append(nullToDash(patient.getNom())).append(" ").append(nullToDash(patient.getPrenom()));
            sb.append("\nSexe: ").append(nullToDash(patient.getSexe()));
            sb.append(" · Naissance: ").append(patient.getDateNaissance() != null ? patient.getDateNaissance() : "—");
            sb.append(" · Groupe sanguin: ").append(nullToDash(patient.getGroupeSanguin()));
            sb.append("\n");

            // Antécédents / allergies
            List<Antecedent> antecedents = dossier.getAntecedents() != null ? dossier.getAntecedents() : List.of();
            List<Antecedent> allergies = antecedents.stream()
                    .filter(a -> a.getTypeAntecedent() != null
                            && a.getTypeAntecedent().toLowerCase(Locale.ROOT).contains("allerg"))
                    .toList();
            List<Antecedent> medicaux = antecedents.stream()
                    .filter(a -> !allergies.contains(a))
                    .toList();

            sb.append("\n--- Allergies ---\n");
            if (allergies.isEmpty()) {
                sb.append("(aucune allergie enregistrée — à confirmer cliniquement)\n");
                bundle.getMissingFields().add("allergies");
                bundle.getWarnings().add("Aucune allergie enregistrée dans le dossier.");
            } else {
                for (Antecedent a : allergies) {
                    sb.append("⚠ ").append(nullToDash(a.getLibelle())).append(" (").append(a.getTypeAntecedent()).append(")\n");
                }
                bundle.getSources().add("Allergies patient");
            }

            sb.append("\n--- Antécédents médicaux / chirurgicaux ---\n");
            if (medicaux.isEmpty()) {
                sb.append("(aucun antécédent)\n");
                bundle.getMissingFields().add("antecedents");
            } else {
                for (Antecedent a : medicaux) {
                    sb.append("- [").append(nullToDash(a.getTypeAntecedent())).append("] ")
                            .append(nullToDash(a.getLibelle())).append("\n");
                }
                bundle.getSources().add("Antécédents");
            }

            // Consultations
            List<ConsultationResponseDTO> consultations = dossier.getConsultations() != null
                    ? dossier.getConsultations() : List.of();
            consultations = consultations.stream()
                    .sorted(Comparator.comparing(ConsultationResponseDTO::getDateConsultation,
                            Comparator.nullsLast(Comparator.reverseOrder())))
                    .limit(8)
                    .toList();

            sb.append("\n--- Consultations (récentes) ---\n");
            if (consultations.isEmpty()) {
                sb.append("(aucune consultation)\n");
                bundle.getMissingFields().add("consultations");
            } else {
                for (ConsultationResponseDTO c : consultations) {
                    sb.append("• ").append(nullToDash(c.getDateConsultation()))
                            .append(" | Motif: ").append(nullToDash(c.getMotifVisite()))
                            .append(" | Diag: ").append(nullToDash(c.getDiagnostic()))
                            .append(" | TA: ").append(nullToDash(c.getTensionArterielle()))
                            .append(" T°: ").append(c.getTemperature() != null ? c.getTemperature() : "—")
                            .append(" FC: ").append(c.getFrequenceCardiaque() != null ? c.getFrequenceCardiaque() : "—")
                            .append("\n");
                    if (c.getObservations() != null && !c.getObservations().isBlank()) {
                        sb.append("  Obs: ").append(trim(c.getObservations(), 240)).append("\n");
                    }
                }
                bundle.getSources().add("Consultations");
                bundle.getSources().add("Constantes vitales");
                bundle.getSources().add("Diagnostics validés");
            }

            // Ordonnances / traitements
            try {
                List<Ordonnance> ordos = ordonnanceService.listerParPatient(patientId.intValue());
                sb.append("\n--- Prescriptions / traitements ---\n");
                if (ordos == null || ordos.isEmpty()) {
                    sb.append("(aucune ordonnance)\n");
                    bundle.getMissingFields().add("prescriptions");
                } else {
                    ordos.stream().limit(10).forEach(o ->
                            sb.append("- ").append(nullToDash(o.getNumeroOrdonnance()))
                                    .append(" · ").append(o.getDatePrescription() != null ? o.getDatePrescription() : "—")
                                    .append(" · statut=").append(nullToDash(o.getStatut()))
                                    .append(o.getContenuOrdonnance() != null ? " · " + trim(o.getContenuOrdonnance(), 120) : "")
                                    .append("\n"));
                    bundle.getSources().add("Prescriptions");
                }
            } catch (Exception e) {
                log.debug("Ordonnances RAG: {}", e.getMessage());
            }

            // Labo validé (best effort)
            appendLabResults(sb, bundle, patientId);

            // Hospitalisations / sorties (best effort)
            appendAdmissionsAndDischarge(sb, bundle, patientId);

            bundle.setContextText(bundle.getContextText() + sb);
            bundle.getSources().add("Dossier patient #" + patientId);
        } catch (ForbiddenException e) {
            throw e;
        } catch (Exception e) {
            log.warn("Contexte clinique RAG: {}", e.getMessage());
            bundle.getWarnings().add("Impossible de charger le dossier patient complet.");
        }
    }

    private void appendLabResults(StringBuilder sb, RagContextBundle bundle, Long patientId) {
        try {
            List<Map<String, Object>> rows = jdbcTemplate.queryForList("""
                    SELECT a.id_analyse, a.statut, a.date_demande, a.date_resultat,
                           COALESCE(ta.nom_analyse, 'Analyse') AS type_analyse,
                           a.resultat_texte, a.interpretation, a.valeurs_reference
                    FROM analyses_laboratoire a
                    LEFT JOIN types_analyses ta ON a.id_type_analyse = ta.id_type_analyse
                    WHERE a.id_patient = ?
                      AND a.statut = 'TERMINE'
                    ORDER BY COALESCE(a.date_resultat, a.date_demande) DESC
                    LIMIT 12
                    """, patientId.intValue());
            sb.append("\n--- Résultats de laboratoire validés ---\n");
            if (rows.isEmpty()) {
                sb.append("(aucun résultat validé récent)\n");
                bundle.getMissingFields().add("laboratoire");
            } else {
                for (Map<String, Object> r : rows) {
                    sb.append("- ").append(r.get("type_analyse"))
                            .append(" = ").append(r.get("resultat_texte") != null ? r.get("resultat_texte") : "—")
                            .append(r.get("interpretation") != null ? " · " + r.get("interpretation") : "")
                            .append(" (").append(r.get("date_resultat") != null ? r.get("date_resultat") : r.get("date_demande")).append(")\n");
                }
                bundle.getSources().add("Résultats laboratoire");
                if (rows.size() >= 2) {
                    bundle.getSources().add("Comparaison résultats récents / anciens");
                }
            }
        } catch (Exception e) {
            log.debug("Labo RAG ignoré: {}", e.getMessage());
        }
    }

    private void appendAdmissionsAndDischarge(StringBuilder sb, RagContextBundle bundle, Long patientId) {
        try {
            List<Map<String, Object>> admissions = jdbcTemplate.queryForList("""
                    SELECT id_admission, statut, temps_arrivee, salle, niveau_priorite
                    FROM admission
                    WHERE id_patient = ?
                    ORDER BY temps_arrivee DESC
                    LIMIT 5
                    """, patientId.intValue());
            sb.append("\n--- Hospitalisations / passages ---\n");
            if (admissions.isEmpty()) {
                sb.append("(aucun)\n");
            } else {
                for (Map<String, Object> a : admissions) {
                    sb.append("- ").append(a.get("temps_arrivee"))
                            .append(" · salle=").append(a.get("salle"))
                            .append(" · priorité=").append(a.get("niveau_priorite"))
                            .append(" · ").append(a.get("statut")).append("\n");
                }
                bundle.getSources().add("Hospitalisations");
            }
        } catch (Exception e) {
            log.debug("Admissions RAG ignoré: {}", e.getMessage());
        }
        try {
            List<Map<String, Object>> bons = jdbcTemplate.queryForList("""
                    SELECT id_bon_sortie, date_sortie, diagnostic_final, etat_sortie
                    FROM bons_sortie
                    WHERE id_patient = ?
                    ORDER BY date_sortie DESC
                    LIMIT 5
                    """, patientId.intValue());
            sb.append("\n--- Comptes rendus de sortie ---\n");
            if (bons.isEmpty()) {
                sb.append("(aucun)\n");
            } else {
                for (Map<String, Object> b : bons) {
                    sb.append("- ").append(b.get("date_sortie"))
                            .append(" · ").append(b.get("diagnostic_final"))
                            .append(" · ").append(b.get("etat_sortie")).append("\n");
                }
                bundle.getSources().add("Comptes rendus de sortie");
            }
        } catch (Exception e) {
            log.debug("Bons sortie RAG ignoré: {}", e.getMessage());
        }
    }

    private RagContextBundle buildAdminContext(RagContextBundle bundle, Integer hopitalId, String userMessage) {
        StringBuilder sb = new StringBuilder();
        sb.append("=== CONTEXTE ADMINISTRATEUR HÔPITAL (sans données cliniques patient) ===\n");
        sb.append("Établissement #").append(hopitalId).append("\n");
        sb.append("Périmètre autorisé : utilisateurs, rôles, services, documents RAG, catégories, versions protocoles,\n");
        sb.append("documents actifs/expirés, modèles IA, quotas, sécurité, journal d'usage, erreurs assistant, coûts.\n");
        sb.append("INTERDIT : diagnostic patient, notes confidentielles, résultats médicaux détaillés,\n");
        sb.append("conversations privées médecin-assistant, dossiers d'un autre établissement.\n");

        try {
            Integer users = jdbcTemplate.queryForObject(
                    "SELECT COUNT(*) FROM utilisateurs WHERE id_hopital = ?", Integer.class, hopitalId);
            sb.append("\nUtilisateurs actifs (approx): ").append(users).append("\n");
            bundle.getSources().add("Utilisateurs");
        } catch (Exception ignored) { }

        try {
            List<RagDocument> docs = ragDocumentRepository.listByHopital(hopitalId);
            long actifs = docs.stream().filter(d -> "ACTIF".equalsIgnoreCase(d.getStatut())).count();
            long expires = docs.stream().filter(d -> "EXPIRE".equalsIgnoreCase(d.getStatut())).count();
            Map<String, Long> byCat = docs.stream()
                    .collect(Collectors.groupingBy(d -> d.getCategorie() != null ? d.getCategorie() : "?", Collectors.counting()));
            sb.append("Documents RAG : ").append(docs.size())
                    .append(" (actifs=").append(actifs).append(", expirés=").append(expires).append(")\n");
            sb.append("Catégories : ").append(byCat).append("\n");
            docs.stream().limit(15).forEach(d ->
                    sb.append("- [").append(d.getCategorie()).append(" v").append(d.getVersionLabel())
                            .append("] ").append(d.getTitre()).append(" (").append(d.getStatut()).append(")\n"));
            bundle.getSources().add("Documents RAG");
            bundle.getSources().add("Catégories / versions");
        } catch (Exception e) {
            log.debug("Docs admin RAG: {}", e.getMessage());
        }

        appendKnowledgeDocs(bundle, hopitalId, "ADMIN", "admin", userMessage);
        bundle.setContextText(sb + "\n" + bundle.getContextText());
        truncate(bundle);
        return bundle;
    }

    private RagContextBundle buildSuperAdminContext(RagContextBundle bundle, String userMessage) {
        StringBuilder sb = new StringBuilder();
        sb.append("=== CONTEXTE SUPER ADMINISTRATEUR (plateforme multi-tenant) ===\n");
        sb.append("Périmètre : hôpitaux, abonnements, plans, MRR/ARPU, utilisateurs, état services,\n");
        sb.append("consommation API OpenAI, quotas, erreurs techniques, stockage, disponibilité, config RAG.\n");
        sb.append("Ne jamais exposer de données cliniques patient d'un établissement.\n");

        try {
            Integer hospitals = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM hopitaux", Integer.class);
            sb.append("Hôpitaux: ").append(hospitals).append("\n");
            bundle.getSources().add("Liste des hôpitaux");
        } catch (Exception ignored) { }

        try {
            Integer users = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM utilisateurs", Integer.class);
            sb.append("Utilisateurs plateforme: ").append(users).append("\n");
            bundle.getSources().add("Nombre d'utilisateurs");
        } catch (Exception ignored) { }

        try {
            List<Map<String, Object>> subs = jdbcTemplate.queryForList("""
                    SELECT plan_code, COUNT(*) AS cnt
                    FROM saas_subscriptions
                    GROUP BY plan_code
                    """);
            sb.append("Plans souscrits: ").append(subs).append("\n");
            bundle.getSources().add("Abonnements / plans");
        } catch (Exception e) {
            try {
                List<Map<String, Object>> subs = jdbcTemplate.queryForList("""
                        SELECT plan, COUNT(*) AS cnt FROM abonnements GROUP BY plan
                        """);
                sb.append("Plans: ").append(subs).append("\n");
                bundle.getSources().add("Abonnements / plans");
            } catch (Exception ignored) { }
        }

        try {
            Map<String, Object> usage = jdbcTemplate.queryForMap("""
                    SELECT COUNT(*) AS calls,
                           COALESCE(SUM(estimated_cost_usd),0) AS cost,
                           SUM(CASE WHEN success=0 THEN 1 ELSE 0 END) AS errors
                    FROM rag_usage_logs
                    WHERE created_at >= DATE_SUB(NOW(), INTERVAL 30 DAY)
                    """);
            sb.append("Consommation RAG 30j: ").append(usage).append("\n");
            bundle.getSources().add("Consommation API OpenAI");
            bundle.getSources().add("Erreurs techniques");
        } catch (Exception ignored) { }

        appendKnowledgeDocs(bundle, null, "SUPER_ADMIN", "platform", userMessage);
        bundle.setContextText(sb + "\n" + bundle.getContextText());
        truncate(bundle);
        return bundle;
    }

    private void appendKnowledgeDocs(RagContextBundle bundle, Integer hopitalId, String audience,
                                     String analysisType, String userMessage) {
        List<RagDocument> docs = ragDocumentRepository.listForAudience(hopitalId, audience, false);
        String query = ((analysisType != null ? analysisType : "") + " " + (userMessage != null ? userMessage : ""))
                .toLowerCase(Locale.ROOT);

        List<RagDocument> ranked = new ArrayList<>(docs);
        ranked.sort((a, b) -> Integer.compare(score(b, query), score(a, query)));

        StringBuilder sb = new StringBuilder("\n=== PROTOCOLES / GUIDES / PROCEDURES (RAG) ===\n");
        int added = 0;
        for (RagDocument d : ranked) {
            if (added >= 8) break;
            if (score(d, query) <= 0 && added >= 3) continue;
            sb.append("\n## ").append(d.getTitre())
                    .append(" [").append(d.getCategorie()).append(" v").append(d.getVersionLabel()).append("]\n")
                    .append(trim(d.getContenu(), 900)).append("\n");
            bundle.getSources().add(d.getCategorie() + " · " + d.getTitre());
            added++;
        }
        if (added == 0) {
            sb.append("(aucun document de connaissance pertinent)\n");
        }
        bundle.setContextText(bundle.getContextText() + sb);
    }

    private int score(RagDocument d, String query) {
        if (query == null || query.isBlank()) return 1;
        String hay = ((d.getTitre() != null ? d.getTitre() : "") + " "
                + (d.getTags() != null ? d.getTags() : "") + " "
                + (d.getCategorie() != null ? d.getCategorie() : "") + " "
                + (d.getContenu() != null ? d.getContenu() : "")).toLowerCase(Locale.ROOT);
        int s = 0;
        for (String token : query.split("\\W+")) {
            if (token.length() < 3) continue;
            if (hay.contains(token)) s += 2;
        }
        if (query.contains("allerg") && hay.contains("allerg")) s += 5;
        if (query.contains("protocole") && hay.contains("protocole")) s += 4;
        if (query.contains("labo") && hay.contains("laboratoire")) s += 4;
        if (query.contains("urgence") && hay.contains("urgence")) s += 4;
        if (query.contains("admission") || query.contains("sortie")) {
            if (hay.contains("admission") || hay.contains("sortie")) s += 4;
        }
        return s;
    }

    private void truncate(RagContextBundle bundle) {
        String text = bundle.getContextText();
        if (text != null && text.length() > MAX_CONTEXT_CHARS) {
            bundle.setContextText(text.substring(0, MAX_CONTEXT_CHARS) + "\n…[contexte tronqué]");
        }
    }

    private static String nullToDash(String v) {
        return v == null || v.isBlank() ? "—" : v;
    }

    private static String trim(String v, int max) {
        if (v == null) return "";
        return v.length() <= max ? v : v.substring(0, max) + "…";
    }
}
