-- Chat sécurisé téléconsultation : chiffrement au repos + accusés de lecture
CREATE TABLE IF NOT EXISTS teleconsultation_chat_messages (
  id BIGINT NOT NULL AUTO_INCREMENT,
  id_hopital INT NOT NULL,
  id_rdv INT NOT NULL,
  id_emetteur INT NOT NULL,
  sender_role VARCHAR(20) NOT NULL,
  contenu TEXT NOT NULL,
  created_at TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP,
  read_by_doctor_at TIMESTAMP NULL DEFAULT NULL,
  read_by_patient_at TIMESTAMP NULL DEFAULT NULL,
  PRIMARY KEY (id),
  KEY idx_tele_chat_tenant_rdv (id_hopital, id_rdv, created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
