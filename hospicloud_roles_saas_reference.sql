-- =============================================================================
-- Shambua Santé / Hospicloud — Référence rôles SaaS multi-tenant
-- Base : hospicloud
-- Aligné sur : hospicloud.model.Role (Spring Boot)
-- =============================================================================

-- -----------------------------------------------------------------------------
-- 1. Catalogue des rôles (table roles)
-- -----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS roles (
    id_role INT NOT NULL AUTO_INCREMENT,
    nom_role VARCHAR(50) NOT NULL,
    description TEXT,
    PRIMARY KEY (id_role),
    UNIQUE KEY nom_role (nom_role)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

INSERT INTO roles (id_role, nom_role, description) VALUES
(1, 'SUPER_ADMIN',  'Administrateur plateforme SaaS (sans tenant)'),
(2, 'TENANT_ADMIN', 'Administrateur d''établissement / hôpital'),
(3, 'MEDECIN',      'Praticien médical'),
(4, 'RECEPTION',    'Réceptionniste / accueil'),
(5, 'PATIENT',      'Patient portail'),
(6, 'LABORANTIN',   'Technicien laboratoire'),
(7, 'CAISSIER',     'Caissier / encaissement'),
(8, 'USER',         'Utilisateur générique'),
(9, 'PHARMACIEN',   'Gestionnaire de pharmacie (legacy)'),
(10, 'ARCHIVISTE',   'Archiviste médical — gestion des dossiers archivés')
ON DUPLICATE KEY UPDATE description = VALUES(description);

-- -----------------------------------------------------------------------------
-- 2. Rôle principal par utilisateur (colonne utilisateurs.role — modèle actuel)
-- -----------------------------------------------------------------------------
-- CREATE TABLE utilisateurs (..., role VARCHAR(50) NOT NULL, ...);

-- Exemples de comptes démo Shambua (mot de passe : shambua123, hash BCrypt ci-dessous)
-- INSERT INTO utilisateurs (id_hopital, nom, prenom, email, mot_de_passe, role, est_actif)
-- VALUES
-- (NULL, 'Okonkwo', 'Adaeze', 'adaeze@shambua.cloud', '$2a$10$...', 'SUPER_ADMIN', 1),
-- (1, 'Mensah', 'Kwame', 'kwame.mensah@shambua.health', '$2a$10$...', 'TENANT_ADMIN', 1),
-- (1, 'Achebe', 'Ngozi', 'ngozi.achebe@shambua.health', '$2a$10$...', 'MEDECIN', 1),
-- (1, 'Diallo', 'Amara', 'amara.diallo@gmail.com', '$2a$10$...', 'PATIENT', 1),
-- (1, 'Cisse', 'Ibrahim', 'ibrahim.cisse@shambua.health', '$2a$10$...', 'LABORANTIN', 1),
-- (1, 'Ndiaye', 'Fatou', 'fatou.ndiaye@shambua.health', '$2a$10$...', 'RECEPTION', 1),
-- (1, 'Kouassi', 'Marie', 'marie.kouassi@shambua.health', '$2a$10$...', 'CAISSIER', 1);

-- -----------------------------------------------------------------------------
-- 3. Liaison pivot utilisateurs_roles (optionnel, RBAC avancé)
-- -----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS utilisateurs_roles (
    id_utilisateur INT NOT NULL,
    id_role INT NOT NULL,
    PRIMARY KEY (id_utilisateur, id_role),
    KEY id_role (id_role),
    CONSTRAINT utilisateurs_roles_ibfk_1 FOREIGN KEY (id_utilisateur) REFERENCES utilisateurs (id_utilisateur) ON DELETE CASCADE,
    CONSTRAINT utilisateurs_roles_ibfk_2 FOREIGN KEY (id_role) REFERENCES roles (id_role) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- Comptes démo -> rôles catalogue
INSERT IGNORE INTO utilisateurs_roles (id_utilisateur, id_role) VALUES
(3, 1),   -- SUPER_ADMIN
(4, 2),   -- TENANT_ADMIN
(5, 3),   -- MEDECIN
(6, 5),   -- PATIENT
(7, 6),   -- LABORANTIN
(8, 4),   -- RECEPTION
(12, 7),  -- CAISSIER
(1, 4), (2, 4), (9, 4), (10, 4),  -- RECEPTION
(11, 3);  -- MEDECIN

-- -----------------------------------------------------------------------------
-- 4. Migration des anciens libellés (exécution idempotente)
-- -----------------------------------------------------------------------------
UPDATE utilisateurs SET role = 'RECEPTION'    WHERE UPPER(role) IN ('RECEPTIONNISTE', 'RECEPTIONIST');
UPDATE utilisateurs SET role = 'MEDECIN'       WHERE UPPER(role) = 'DOCTOR';
UPDATE utilisateurs SET role = 'LABORANTIN'    WHERE UPPER(role) IN ('LAB_TECH', 'LABORATOIRE');
UPDATE utilisateurs SET role = 'TENANT_ADMIN'  WHERE UPPER(role) IN ('HOSPITAL_ADMIN', 'ADMIN');
UPDATE utilisateurs SET role = 'CAISSIER'      WHERE UPPER(role) IN ('CASHIER');
UPDATE utilisateurs SET role = 'ARCHIVISTE'    WHERE UPPER(role) IN ('ARCHIVIST');
