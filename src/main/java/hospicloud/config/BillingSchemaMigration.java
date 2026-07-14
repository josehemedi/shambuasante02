package hospicloud.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;

/**
 * Schéma de facturation composée :
 * Total patient = soins consommés − assurance − remise − avances.
 */
@Component
public class BillingSchemaMigration {

    private static final Logger log = LoggerFactory.getLogger(BillingSchemaMigration.class);
    private final JdbcTemplate jdbcTemplate;

    public BillingSchemaMigration(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @PostConstruct
    public void migrate() {
        try {
            ensureTarifsHopital();
            ensureSejours();
            ensureActesRealises();
            ensureAvances();
            ensurePharmacieDelivrances();
            ensureFactureColumns();
            ensureFactureItemColumns();
            seedDefaultTarifs();
            log.info("Schéma facturation compositionnelle prêt");
        } catch (Exception e) {
            log.warn("Migration facturation ignorée: {}", e.getMessage());
        }
    }

    private void ensureTarifsHopital() {
        jdbcTemplate.execute("""
                CREATE TABLE IF NOT EXISTS tarifs_hopital (
                    id_tarif INT AUTO_INCREMENT PRIMARY KEY,
                    id_hopital INT NOT NULL,
                    code VARCHAR(40) NOT NULL,
                    libelle VARCHAR(255) NOT NULL,
                    categorie ENUM(
                        'CONSULTATION','EXAMEN','MEDICAMENT','HOSPITALISATION',
                        'ACTE_MEDICAL','AUTRE'
                    ) NOT NULL,
                    prix_unitaire DECIMAL(10,2) NOT NULL DEFAULT 0,
                    actif TINYINT(1) NOT NULL DEFAULT 1,
                    UNIQUE KEY uk_tarif_hopital_code (id_hopital, code),
                    KEY idx_tarif_hopital_cat (id_hopital, categorie)
                )
                """);
    }

    private void ensureSejours() {
        jdbcTemplate.execute("""
                CREATE TABLE IF NOT EXISTS sejours_hospitalisation (
                    id_sejour INT AUTO_INCREMENT PRIMARY KEY,
                    id_hopital INT NOT NULL,
                    id_patient INT NOT NULL,
                    id_chambre INT NULL,
                    type_chambre VARCHAR(40) NULL,
                    date_entree DATETIME NOT NULL,
                    date_sortie DATETIME NULL,
                    prix_journalier DECIMAL(10,2) NOT NULL DEFAULT 0,
                    notes VARCHAR(255) NULL,
                    KEY idx_sejour_patient (id_hopital, id_patient),
                    KEY idx_sejour_dates (date_entree, date_sortie)
                )
                """);
    }

    private void ensureActesRealises() {
        jdbcTemplate.execute("""
                CREATE TABLE IF NOT EXISTS actes_realises (
                    id_acte_realise INT AUTO_INCREMENT PRIMARY KEY,
                    id_hopital INT NOT NULL,
                    id_patient INT NOT NULL,
                    id_tarif INT NULL,
                    designation VARCHAR(255) NOT NULL,
                    quantite INT NOT NULL DEFAULT 1,
                    prix_unitaire DECIMAL(10,2) NOT NULL DEFAULT 0,
                    date_acte DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
                    KEY idx_acte_patient (id_hopital, id_patient)
                )
                """);
    }

    private void ensureAvances() {
        jdbcTemplate.execute("""
                CREATE TABLE IF NOT EXISTS avances_patient (
                    id_avance INT AUTO_INCREMENT PRIMARY KEY,
                    id_hopital INT NOT NULL,
                    id_patient INT NOT NULL,
                    id_facture INT NULL,
                    montant DECIMAL(10,2) NOT NULL,
                    date_avance DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
                    mode_paiement VARCHAR(40) NULL,
                    reference_transaction VARCHAR(100) NULL,
                    notes VARCHAR(255) NULL,
                    appliquee TINYINT(1) NOT NULL DEFAULT 0,
                    KEY idx_avance_patient (id_hopital, id_patient, appliquee),
                    KEY idx_avance_facture (id_facture)
                )
                """);
    }

    private void ensurePharmacieDelivrances() {
        jdbcTemplate.execute("""
                CREATE TABLE IF NOT EXISTS pharmacie_delivrances (
                    id BIGINT AUTO_INCREMENT PRIMARY KEY,
                    hopital_id INT NOT NULL,
                    id_patient INT NOT NULL,
                    medicament_id BIGINT NOT NULL,
                    quantite INT NOT NULL DEFAULT 1,
                    prix_unitaire DECIMAL(10,2) NOT NULL DEFAULT 0,
                    designation VARCHAR(255) NULL,
                    date_delivrance DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
                    delivre_par INT NULL,
                    KEY idx_deliv_patient (hopital_id, id_patient),
                    KEY idx_deliv_medicament (medicament_id)
                )
                """);
    }

    private void ensureFactureColumns() {
        ensureColumn("factures", "sous_total_soins", "DECIMAL(10,2) NULL DEFAULT 0");
        ensureColumn("factures", "montant_assurance", "DECIMAL(10,2) NULL DEFAULT 0");
        ensureColumn("factures", "montant_remise", "DECIMAL(10,2) NULL DEFAULT 0");
        ensureColumn("factures", "montant_avances", "DECIMAL(10,2) NULL DEFAULT 0");
        ensureColumn("factures", "taux_assurance", "DECIMAL(5,2) NULL DEFAULT 0");
        ensureColumn("factures", "composition_auto", "TINYINT(1) NULL DEFAULT 0");
    }

    private void ensureFactureItemColumns() {
        ensureColumn("facture_items", "categorie",
                "ENUM('CONSULTATION','EXAMEN','MEDICAMENT','HOSPITALISATION','ACTE_MEDICAL','AUTRE') NULL DEFAULT 'AUTRE'");
        ensureColumn("facture_items", "source_type", "VARCHAR(40) NULL");
        ensureColumn("facture_items", "source_id", "BIGINT NULL");
    }

    private void seedDefaultTarifs() {
        jdbcTemplate.update("""
                INSERT IGNORE INTO tarifs_hopital (id_hopital, code, libelle, categorie, prix_unitaire, actif)
                SELECT h.id_hopital, t.code, t.libelle, t.categorie, t.prix, 1
                FROM hopitaux h
                CROSS JOIN (
                    SELECT 'CONSULT_GEN' AS code, 'Consultation générale' AS libelle,
                           'CONSULTATION' AS categorie, 20.00 AS prix
                    UNION ALL SELECT 'CONSULT_SPE', 'Consultation spécialiste', 'CONSULTATION', 35.00
                    UNION ALL SELECT 'PANSEMENT', 'Pansement', 'ACTE_MEDICAL', 5.00
                    UNION ALL SELECT 'PETITE_CHIR', 'Petite chirurgie', 'ACTE_MEDICAL', 50.00
                    UNION ALL SELECT 'ANESTHESIE', 'Anesthésie', 'ACTE_MEDICAL', 40.00
                    UNION ALL SELECT 'KINE', 'Séance de kinésithérapie', 'ACTE_MEDICAL', 15.00
                    UNION ALL SELECT 'SOIN_INF', 'Soins infirmiers', 'ACTE_MEDICAL', 8.00
                    UNION ALL SELECT 'CHAMBRE_ORD', 'Chambre ordinaire / jour', 'HOSPITALISATION', 15.00
                    UNION ALL SELECT 'CHAMBRE_PRIV', 'Chambre privée / jour', 'HOSPITALISATION', 40.00
                ) t
                """);
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
}
