package hospicloud.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;

/**
 * Schéma RAG multi-tenant : documents de connaissance + journal d'usage.
 * hopital_id NULL = document plateforme (super-admin) ; sinon scoped à l'établissement.
 */
@Component
public class RagSchemaMigration {

    private static final Logger log = LoggerFactory.getLogger(RagSchemaMigration.class);
    private final JdbcTemplate jdbcTemplate;

    public RagSchemaMigration(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @PostConstruct
    public void migrate() {
        try {
            jdbcTemplate.execute("""
                    CREATE TABLE IF NOT EXISTS rag_documents (
                        id BIGINT AUTO_INCREMENT PRIMARY KEY,
                        hopital_id INT NULL,
                        categorie VARCHAR(80) NOT NULL,
                        titre VARCHAR(255) NOT NULL,
                        contenu MEDIUMTEXT NOT NULL,
                        version_label VARCHAR(40) NOT NULL DEFAULT '1.0',
                        statut VARCHAR(20) NOT NULL DEFAULT 'ACTIF',
                        audience VARCHAR(40) NOT NULL DEFAULT 'MEDECIN',
                        tags VARCHAR(500) NULL,
                        expire_at DATETIME NULL,
                        created_by INT NULL,
                        updated_by INT NULL,
                        created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
                        updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                        KEY idx_rag_docs_hopital (hopital_id, statut, categorie),
                        KEY idx_rag_docs_audience (audience, statut)
                    )
                    """);

            jdbcTemplate.execute("""
                    CREATE TABLE IF NOT EXISTS rag_usage_logs (
                        id BIGINT AUTO_INCREMENT PRIMARY KEY,
                        hopital_id INT NULL,
                        user_id INT NULL,
                        role_code VARCHAR(40) NULL,
                        scope_code VARCHAR(40) NOT NULL,
                        patient_id BIGINT NULL,
                        model_name VARCHAR(80) NULL,
                        analysis_type VARCHAR(60) NULL,
                        prompt_chars INT NULL,
                        context_chars INT NULL,
                        response_chars INT NULL,
                        sources_json TEXT NULL,
                        success TINYINT(1) NOT NULL DEFAULT 1,
                        error_message VARCHAR(500) NULL,
                        estimated_cost_usd DECIMAL(12,6) NULL,
                        created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
                        KEY idx_rag_usage_hopital (hopital_id, created_at),
                        KEY idx_rag_usage_scope (scope_code, created_at)
                    )
                    """);

            jdbcTemplate.execute("""
                    CREATE TABLE IF NOT EXISTS rag_config (
                        id BIGINT AUTO_INCREMENT PRIMARY KEY,
                        hopital_id INT NULL,
                        model_name VARCHAR(80) NOT NULL DEFAULT 'gpt-4o-mini',
                        monthly_token_quota INT NOT NULL DEFAULT 500000,
                        max_context_chars INT NOT NULL DEFAULT 12000,
                        allow_patient_context TINYINT(1) NOT NULL DEFAULT 1,
                        security_notes TEXT NULL,
                        updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                        UNIQUE KEY uk_rag_config_hopital (hopital_id)
                    )
                    """);

            seedPlatformDocumentsIfEmpty();
            log.info("Schéma RAG prêt (documents, usage, config)");
        } catch (Exception e) {
            log.warn("Migration RAG ignorée: {}", e.getMessage());
        }
    }

    private void seedPlatformDocumentsIfEmpty() {
        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM rag_documents WHERE hopital_id IS NULL", Integer.class);
        if (count != null && count > 0) {
            return;
        }
        insertSeed(
                "PROTOCOLE",
                "Protocole triage urgences",
                """
                Triage ABCDE. Signes vitaux critiques : SpO2 < 90 %, PAS < 90, FC > 130, T° > 39.5.
                Critères d'admission urgences : détresse respiratoire, douleur thoracique, AVC suspecté, sepsis.
                Critères de sortie : constantes stables, diagnostic clair, plan de suivi documenté.
                """,
                "MEDECIN",
                "urgence,admission,sortie");
        insertSeed(
                "GUIDE",
                "Guide allergies et prescription",
                """
                Toujours vérifier allergies avant prescription. Contre-indications AINS si ulcère / IRC sévère.
                Signaler toute allergie médicamenteuse dans le résumé. Demander confirmation clinique.
                """,
                "MEDECIN",
                "allergie,prescription");
        insertSeed(
                "PROCEDURE_LABO",
                "Procédure laboratoire — résultats critiques",
                """
                Résultats critiques à notifier immédiatement au médecin : K+ > 6.5, Hb < 7, glucose < 0.5 g/L,
                tropinine élevée, INR > 5. Comparer toujours aux résultats précédents du même patient.
                """,
                "MEDECIN",
                "laboratoire,critique");
        insertSeed(
                "RECOMMANDATION",
                "Recommandations internes — résumé consultation",
                """
                Un résumé de consultation doit contenir : motif, symptômes, antécédents pertinents, allergies,
                constantes, diagnostics validés, examens, traitements en cours, plan et éléments manquants.
                """,
                "MEDECIN",
                "resume,consultation");
        insertSeed(
                "ADMIN",
                "Gouvernance documents RAG établissement",
                """
                L'administrateur d'hôpital gère : utilisateurs, rôles, services, documents RAG, catégories,
                versions de protocoles, documents actifs/expirés, modèles IA, quotas, sécurité, journal d'usage.
                Interdit : diagnostics patients détaillés, notes confidentielles, conversations privées médecin-IA,
                dossiers d'un autre établissement.
                """,
                "ADMIN",
                "gouvernance,rag");
        insertSeed(
                "PLATEFORME",
                "Pilotage SaaS RAG multi-tenant",
                """
                Le super administrateur voit : hôpitaux, abonnements, plans, MRR/ARPU, utilisateurs, état services,
                consommation API OpenAI, quotas, erreurs techniques, stockage, disponibilité, config RAG globale.
                Isolation stricte multi-tenant obligatoire.
                """,
                "SUPER_ADMIN",
                "saas,mrr,quota");
    }

    private void insertSeed(String categorie, String titre, String contenu, String audience, String tags) {
        jdbcTemplate.update("""
                INSERT INTO rag_documents
                (hopital_id, categorie, titre, contenu, version_label, statut, audience, tags)
                VALUES (NULL, ?, ?, ?, '1.0', 'ACTIF', ?, ?)
                """, categorie, titre, contenu, audience, tags);
    }
}
