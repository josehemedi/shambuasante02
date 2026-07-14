-- Migration de référence — module archivage des dossiers hospitaliers
-- Appliquée aussi via ArchivesSchemaMigration.java (@PostConstruct)

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
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

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
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

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
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

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
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
