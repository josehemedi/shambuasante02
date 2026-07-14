-- Migration signatures électroniques + statut consultation
-- Compatible avec la table signatures_documents existante

ALTER TABLE consultations_medicales
    ADD COLUMN IF NOT EXISTS statut VARCHAR(20) NOT NULL DEFAULT 'BROUILLON',
    ADD COLUMN IF NOT EXISTS date_signature DATETIME NULL;

CREATE TABLE IF NOT EXISTS signatures_documents (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    id_hopital INT NOT NULL,
    document_id BIGINT NOT NULL,
    type_document VARCHAR(50) NOT NULL,
    medecin_id INT NOT NULL,
    utilisateur_id BIGINT NOT NULL,
    nom_medecin VARCHAR(255) NOT NULL,
    image_signature LONGTEXT NULL,
    hash_document VARCHAR(64) NOT NULL,
    adresse_ip VARCHAR(45) NULL,
    methode_authentification VARCHAR(50) NOT NULL,
    date_signature DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    reference_signature VARCHAR(64) NOT NULL,
    statut ENUM('SIGNE', 'ANNULE', 'INVALIDE') NOT NULL DEFAULT 'SIGNE',
    KEY idx_sig_doc (id_hopital, type_document, document_id),
    UNIQUE KEY uk_sig_reference (reference_signature)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

UPDATE consultations_medicales
SET statut = 'SIGNEE', fiche_finalisee = 1
WHERE (statut IS NULL OR statut = 'BROUILLON') AND fiche_finalisee = 1;
