-- Réinitialisation de mot de passe (tokens sécurisés, multi-tenant)
CREATE TABLE IF NOT EXISTS password_reset_tokens (
  id BIGINT NOT NULL AUTO_INCREMENT,
  id_utilisateur INT NOT NULL,
  id_hopital INT NULL,
  token_hash VARCHAR(128) NOT NULL,
  expires_at TIMESTAMP NOT NULL,
  used_at TIMESTAMP NULL DEFAULT NULL,
  created_at TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  UNIQUE KEY uk_password_reset_hash (token_hash),
  KEY idx_password_reset_user (id_utilisateur, created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
