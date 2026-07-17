-- Migration RAG multi-tenant (documents, usage, config)
-- Base : hospicloud
-- Exécutable dans MySQL Workbench

CREATE TABLE IF NOT EXISTS rag_documents (
    id BIGINT NOT NULL AUTO_INCREMENT,
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
    PRIMARY KEY (id),
    KEY idx_rag_docs_hopital (hopital_id, statut, categorie),
    KEY idx_rag_docs_audience (audience, statut)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS rag_usage_logs (
    id BIGINT NOT NULL AUTO_INCREMENT,
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
    PRIMARY KEY (id),
    KEY idx_rag_usage_hopital (hopital_id, created_at),
    KEY idx_rag_usage_scope (scope_code, created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS rag_config (
    id BIGINT NOT NULL AUTO_INCREMENT,
    hopital_id INT NULL,
    model_name VARCHAR(80) NOT NULL DEFAULT 'gpt-4o-mini',
    monthly_token_quota INT NOT NULL DEFAULT 500000,
    max_context_chars INT NOT NULL DEFAULT 12000,
    allow_patient_context TINYINT(1) NOT NULL DEFAULT 1,
    security_notes TEXT NULL,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uk_rag_config_hopital (hopital_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

INSERT INTO rag_config (hopital_id, model_name, monthly_token_quota, max_context_chars, allow_patient_context, security_notes)
SELECT NULL, 'gpt-4o-mini', 5000000, 12000, 0, 'Config plateforme : pas de contexte patient pour SUPER_ADMIN'
WHERE NOT EXISTS (SELECT 1 FROM rag_config WHERE hopital_id IS NULL);
