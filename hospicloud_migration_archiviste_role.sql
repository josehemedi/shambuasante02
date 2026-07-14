-- =============================================================================
-- Shambua Santé — Rôle ARCHIVISTE (utilisateurs + catalogue roles)
-- Valeur officielle en base : ARCHIVISTE (hospicloud.model.Role.ARCHIVISTE)
-- =============================================================================

-- 1. Catalogue des rôles (si table roles utilisée)
INSERT INTO roles (nom_role, description) VALUES
('ARCHIVISTE', 'Archiviste médical — vérification et archivage des dossiers')
ON DUPLICATE KEY UPDATE description = VALUES(description);

-- 2. Migration des anciens libellés
UPDATE utilisateurs SET role = 'ARCHIVISTE' WHERE UPPER(role) IN ('ARCHIVIST');

-- 3. Compte démo archiviste (mot de passe : shambua123)
-- Hash BCrypt identique aux autres comptes démo Shambua
INSERT INTO utilisateurs (id_hopital, nom, prenom, email, mot_de_passe, role, est_actif)
SELECT 1, 'Diallo', 'Amina', 'amina.diallo@shambua.health',
       '$2a$10$9kpfWmL.GtgpmvE2Y58eCuQC/4CeUlTKLoSR/fkvs7BijNBGeksry',
       'ARCHIVISTE', 1
WHERE NOT EXISTS (
    SELECT 1 FROM utilisateurs WHERE email = 'amina.diallo@shambua.health'
);
