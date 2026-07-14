package hospicloud.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;

@Component
public class ArchivesSchemaMigration {

    private static final Logger log = LoggerFactory.getLogger(ArchivesSchemaMigration.class);
    private final JdbcTemplate jdbcTemplate;

    public ArchivesSchemaMigration(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @PostConstruct
    public void migrate() {
        try {
            jdbcTemplate.execute("""
                CREATE TABLE IF NOT EXISTS archives_dossiers (
                    id BIGINT NOT NULL AUTO_INCREMENT,
                    hopital_id INT NOT NULL,
                    patient_id BIGINT NOT NULL,
                    type_episode VARCHAR(40) NOT NULL,
                    episode_id BIGINT NOT NULL,
                    statut_archive VARCHAR(40) NOT NULL DEFAULT 'A_VERIFIER',
                    date_fin_episode DATETIME NULL,
                    date_demande_archivage DATETIME NULL,
                    date_archivage DATETIME NULL,
                    archive_par INT NULL,
                    verifie_par INT NULL,
                    motif_archivage VARCHAR(500) NULL,
                    observation TEXT NULL,
                    dossier_complet TINYINT(1) NOT NULL DEFAULT 0,
                    emplacement_physique VARCHAR(255) NULL,
                    numero_boite_archive VARCHAR(80) NULL,
                    numero_rayon VARCHAR(80) NULL,
                    date_restauration DATETIME NULL,
                    restaure_par INT NULL,
                    motif_restauration VARCHAR(500) NULL,
                    version INT NOT NULL DEFAULT 1,
                    id_medecin INT NULL,
                    id_service INT NULL,
                    created_at TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP,
                    updated_at TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                    PRIMARY KEY (id),
                    UNIQUE KEY uk_archive_episode (hopital_id, type_episode, episode_id),
                    KEY idx_archive_hopital (hopital_id),
                    KEY idx_archive_patient (hopital_id, patient_id),
                    KEY idx_archive_statut (hopital_id, statut_archive),
                    KEY idx_archive_date (hopital_id, date_archivage),
                    KEY idx_archive_par (hopital_id, archive_par)
                ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4
                """);

            jdbcTemplate.execute("""
                CREATE TABLE IF NOT EXISTS historique_archivage (
                    id BIGINT NOT NULL AUTO_INCREMENT,
                    hopital_id INT NOT NULL,
                    archive_id BIGINT NOT NULL,
                    ancien_statut VARCHAR(40) NULL,
                    nouveau_statut VARCHAR(40) NOT NULL,
                    action VARCHAR(80) NOT NULL,
                    motif VARCHAR(500) NULL,
                    observation TEXT NULL,
                    effectue_par INT NULL,
                    date_action TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP,
                    adresse_ip VARCHAR(45) NULL,
                    user_agent VARCHAR(255) NULL,
                    PRIMARY KEY (id),
                    KEY idx_hist_archive (hopital_id, archive_id),
                    KEY idx_hist_date (hopital_id, date_action)
                ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4
                """);

            jdbcTemplate.execute("""
                CREATE TABLE IF NOT EXISTS demandes_acces_archive (
                    id BIGINT NOT NULL AUTO_INCREMENT,
                    hopital_id INT NOT NULL,
                    archive_id BIGINT NOT NULL,
                    demandeur_id INT NOT NULL,
                    motif VARCHAR(500) NOT NULL,
                    statut VARCHAR(40) NOT NULL DEFAULT 'EN_ATTENTE',
                    date_demande TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP,
                    traite_par INT NULL,
                    date_traitement DATETIME NULL,
                    observation TEXT NULL,
                    PRIMARY KEY (id),
                    KEY idx_demande_archive (hopital_id, archive_id),
                    KEY idx_demande_statut (hopital_id, statut)
                ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4
                """);

            jdbcTemplate.execute("""
                CREATE TABLE IF NOT EXISTS regles_archivage_hopital (
                    id INT NOT NULL AUTO_INCREMENT,
                    hopital_id INT NOT NULL,
                    exiger_cloture_medicale TINYINT(1) NOT NULL DEFAULT 1,
                    exiger_cloture_administrative TINYINT(1) NOT NULL DEFAULT 0,
                    exiger_cloture_financiere TINYINT(1) NOT NULL DEFAULT 0,
                    created_at TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP,
                    updated_at TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                    PRIMARY KEY (id),
                    UNIQUE KEY uk_regles_hopital (hopital_id)
                ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4
                """);

            jdbcTemplate.execute("""
                CREATE TABLE IF NOT EXISTS archives_dossiers_virtuels (
                    id BIGINT NOT NULL AUTO_INCREMENT,
                    hopital_id INT NOT NULL,
                    parent_id BIGINT NULL,
                    nom VARCHAR(180) NOT NULL,
                    created_by INT NULL,
                    created_at TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP,
                    updated_at TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                    PRIMARY KEY (id),
                    KEY idx_adv_hopital_parent (hopital_id, parent_id),
                    UNIQUE KEY uk_adv_nom_parent (hopital_id, parent_id, nom)
                ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4
                """);

            ensureColumn("archives_dossiers", "dossier_virtuel_id", "BIGINT NULL");
            ensureColumn("archives_dossiers", "contenu_snapshot", "LONGTEXT NULL");
            ensureColumn("archives_dossiers", "snapshot_at", "DATETIME NULL");
            ensureColumn("archives_dossiers", "nom_patient_fige", "VARCHAR(255) NULL");
            ensureColumn("archives_dossiers", "numero_dossier_fige", "VARCHAR(80) NULL");
            ensureIndex("archives_dossiers", "idx_archive_dossier_virtuel",
                    "CREATE INDEX idx_archive_dossier_virtuel ON archives_dossiers (hopital_id, dossier_virtuel_id)");

            jdbcTemplate.execute("""
                CREATE TABLE IF NOT EXISTS archives_fichiers (
                    id BIGINT NOT NULL AUTO_INCREMENT,
                    hopital_id INT NOT NULL,
                    archive_id BIGINT NOT NULL,
                    type_fichier VARCHAR(60) NOT NULL DEFAULT 'DOSSIER_PATIENT',
                    nom_fichier VARCHAR(255) NOT NULL,
                    chemin_stockage VARCHAR(1000) NOT NULL,
                    mime_type VARCHAR(120) NOT NULL DEFAULT 'application/pdf',
                    taille_octets BIGINT NULL,
                    genere_at DATETIME NULL,
                    genere_par INT NULL,
                    created_at TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP,
                    updated_at TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                    PRIMARY KEY (id),
                    UNIQUE KEY uk_af_archive_type (hopital_id, archive_id, type_fichier),
                    KEY idx_af_archive (hopital_id, archive_id),
                    CONSTRAINT fk_af_archive FOREIGN KEY (archive_id)
                        REFERENCES archives_dossiers (id) ON DELETE CASCADE
                ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4
                """);

            log.info("Migration module archivage appliquée");
        } catch (Exception e) {
            log.warn("Migration module archivage ignorée: {}", e.getMessage());
        }
    }

    private void ensureColumn(String table, String column, String definition) {
        try {
            Integer count = jdbcTemplate.queryForObject(
                    """
                    SELECT COUNT(*) FROM information_schema.COLUMNS
                    WHERE TABLE_SCHEMA = DATABASE()
                      AND TABLE_NAME = ?
                      AND COLUMN_NAME = ?
                    """,
                    Integer.class,
                    table,
                    column
            );
            if (count != null && count == 0) {
                jdbcTemplate.execute("ALTER TABLE " + table + " ADD COLUMN " + column + " " + definition);
                log.info("Colonne {}.{} ajoutée", table, column);
            }
        } catch (Exception e) {
            log.warn("Colonne {}.{}: {}", table, column, e.getMessage());
        }
    }

    private void ensureIndex(String table, String indexName, String createSql) {
        try {
            Integer count = jdbcTemplate.queryForObject(
                    """
                    SELECT COUNT(*) FROM information_schema.STATISTICS
                    WHERE TABLE_SCHEMA = DATABASE()
                      AND TABLE_NAME = ?
                      AND INDEX_NAME = ?
                    """,
                    Integer.class,
                    table,
                    indexName
            );
            if (count != null && count == 0) {
                jdbcTemplate.execute(createSql);
                log.info("Index {}.{} ajouté", table, indexName);
            }
        } catch (Exception e) {
            log.warn("Index {}.{}: {}", table, indexName, e.getMessage());
        }
    }
}
