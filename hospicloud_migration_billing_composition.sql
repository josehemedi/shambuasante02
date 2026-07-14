-- Migration facturation compositionnelle (Shambua Santé)
-- Total patient = soins − assurance − remises − avances

CREATE TABLE IF NOT EXISTS tarifs_hopital (
  id_tarif INT AUTO_INCREMENT PRIMARY KEY,
  id_hopital INT NOT NULL,
  code VARCHAR(40) NOT NULL,
  libelle VARCHAR(255) NOT NULL,
  categorie ENUM('CONSULTATION','EXAMEN','MEDICAMENT','HOSPITALISATION','ACTE_MEDICAL','AUTRE') NOT NULL,
  prix_unitaire DECIMAL(10,2) NOT NULL DEFAULT 0,
  actif TINYINT(1) NOT NULL DEFAULT 1,
  UNIQUE KEY uk_tarif_hopital_code (id_hopital, code)
);

CREATE TABLE IF NOT EXISTS sejours_hospitalisation (
  id_sejour INT AUTO_INCREMENT PRIMARY KEY,
  id_hopital INT NOT NULL,
  id_patient INT NOT NULL,
  id_chambre INT NULL,
  type_chambre VARCHAR(40) NULL,
  date_entree DATETIME NOT NULL,
  date_sortie DATETIME NULL,
  prix_journalier DECIMAL(10,2) NOT NULL DEFAULT 0,
  notes VARCHAR(255) NULL
);

CREATE TABLE IF NOT EXISTS actes_realises (
  id_acte_realise INT AUTO_INCREMENT PRIMARY KEY,
  id_hopital INT NOT NULL,
  id_patient INT NOT NULL,
  id_tarif INT NULL,
  designation VARCHAR(255) NOT NULL,
  quantite INT NOT NULL DEFAULT 1,
  prix_unitaire DECIMAL(10,2) NOT NULL DEFAULT 0,
  date_acte DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
);

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
  appliquee TINYINT(1) NOT NULL DEFAULT 0
);

-- Colonnes factures / lignes : appliquées aussi par BillingSchemaMigration.java au démarrage.
