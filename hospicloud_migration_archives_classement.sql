-- Classement type explorateur pour archives (dossiers virtuels)
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
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Relie un dossier médical à un dossier virtuel (NULL = racine / à classer)
ALTER TABLE archives_dossiers
    ADD COLUMN IF NOT EXISTS dossier_virtuel_id BIGINT NULL;

CREATE INDEX IF NOT EXISTS idx_archive_dossier_virtuel
    ON archives_dossiers (hopital_id, dossier_virtuel_id);
