package hospicloud.repositoriesImpl;

import hospicloud.model.Role;
import hospicloud.model.Role;
import hospicloud.model.Utilisateur;
import hospicloud.repositories.UtilisateurRepository;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public class UtilisateurRepositoryImpl implements UtilisateurRepository {

    private final JdbcTemplate jdbcTemplate;
    private final PasswordEncoder passwordEncoder;

    public UtilisateurRepositoryImpl(JdbcTemplate jdbcTemplate, PasswordEncoder passwordEncoder) {
        this.jdbcTemplate = jdbcTemplate;
        this.passwordEncoder = passwordEncoder;
    }

    private final RowMapper<Utilisateur> rowMapper = new RowMapper<>() {
        @Override
        public Utilisateur mapRow(ResultSet rs, int rowNum) throws SQLException {
            Utilisateur u = new Utilisateur();
            u.setIdUtilisateur(rs.getInt("id_utilisateur"));
            int hopitalId = rs.getInt("id_hopital");
            u.setIdHopital(rs.wasNull() ? null : hopitalId);
            int medecinId = rs.getInt("id_medecin");
            u.setIdMedecin(rs.wasNull() ? null : medecinId);
            long patientId = rs.getLong("id_patient");
            u.setIdPatient(rs.wasNull() ? null : patientId);
            u.setNom(rs.getString("nom"));
            u.setPrenom(rs.getString("prenom"));
            u.setEmail(rs.getString("email"));
            u.setMotDePasse(rs.getString("mot_de_passe"));
            u.setTelephone(rs.getString("telephone"));
            u.setRole(Role.fromDatabaseValue(rs.getString("role")));
            u.setEstActif(rs.getBoolean("est_actif"));
            if (rs.getTimestamp("date_creation") != null) {
                u.setDateCreation(rs.getTimestamp("date_creation").toLocalDateTime());
            }
            return u;
        }
    };

    @Override
    public void ensureSchema() {
        jdbcTemplate.execute("""
            CREATE TABLE IF NOT EXISTS utilisateurs (
                id_utilisateur INT AUTO_INCREMENT PRIMARY KEY,
                id_hopital INT NULL,
                id_medecin INT NULL,
                id_patient BIGINT NULL,
                nom VARCHAR(100) NOT NULL,
                prenom VARCHAR(100) NOT NULL,
                email VARCHAR(255) NOT NULL UNIQUE,
                mot_de_passe VARCHAR(255) NOT NULL,
                telephone VARCHAR(30),
                role VARCHAR(50) NOT NULL,
                est_actif BOOLEAN DEFAULT TRUE,
                date_creation DATETIME DEFAULT CURRENT_TIMESTAMP
            )
            """);

        addColumnIfMissing("utilisateurs", "id_medecin", "INT NULL");
        addColumnIfMissing("utilisateurs", "id_patient", "BIGINT NULL");
        migrateRoleColumnToVarchar();
        migrateLegacyRoleValues();
    }

    private void migrateLegacyRoleValues() {
        jdbcTemplate.update("""
            UPDATE utilisateurs
            SET role = 'RECEPTION'
            WHERE UPPER(role) IN ('RECEPTIONNISTE', 'RECEPTIONIST')
            """);
        jdbcTemplate.update("""
            UPDATE utilisateurs
            SET role = 'MEDECIN'
            WHERE UPPER(role) = 'DOCTOR'
            """);
        jdbcTemplate.update("""
            UPDATE utilisateurs
            SET role = 'LABORANTIN'
            WHERE UPPER(role) IN ('LAB_TECH', 'LABORATOIRE')
            """);
        jdbcTemplate.update("""
            UPDATE utilisateurs
            SET role = 'TENANT_ADMIN'
            WHERE UPPER(role) IN ('HOSPITAL_ADMIN', 'ADMIN')
            """);
        jdbcTemplate.update("""
            UPDATE utilisateurs
            SET role = 'CAISSIER'
            WHERE UPPER(role) IN ('CASHIER')
            """);
        jdbcTemplate.update("""
            UPDATE utilisateurs
            SET role = 'ARCHIVISTE'
            WHERE UPPER(role) = 'ARCHIVIST'
            """);
    }

    private void migrateRoleColumnToVarchar() {
        String dataType = jdbcTemplate.queryForObject(
                """
                SELECT DATA_TYPE FROM information_schema.columns
                WHERE table_schema = DATABASE() AND table_name = 'utilisateurs' AND column_name = 'role'
                """,
                String.class
        );
        if ("enum".equalsIgnoreCase(dataType)) {
            jdbcTemplate.execute("ALTER TABLE utilisateurs MODIFY COLUMN role VARCHAR(50) NOT NULL");
        }
    }

    private void addColumnIfMissing(String table, String column, String definition) {
        Integer count = jdbcTemplate.queryForObject(
                """
                SELECT COUNT(*) FROM information_schema.columns
                WHERE table_schema = DATABASE() AND table_name = ? AND column_name = ?
                """,
                Integer.class,
                table,
                column
        );
        if (count != null && count == 0) {
            jdbcTemplate.execute("ALTER TABLE " + table + " ADD COLUMN " + column + " " + definition);
        }
    }

    @Override
    public void seedIfEmpty() {
        Integer count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM utilisateurs", Integer.class);
        if (count == null || count == 0) {
            syncDemoUsers();
        }
    }

    @Override
    public void syncDemoUsers() {
        String encodedPassword = passwordEncoder.encode("shambua123");
        LocalDateTime now = LocalDateTime.now();

        upsertUser("Adaeze", "Okonkwo", "adaeze@shambua.cloud", encodedPassword, Role.SUPER_ADMIN, null, null, null, now);
        upsertUser("Kwame", "Mensah", "kwame.mensah@shambua.health", encodedPassword, Role.TENANT_ADMIN, 1, null, null, now);
        upsertUser("Ngozi", "Achebe", "ngozi.achebe@shambua.health", encodedPassword, Role.MEDECIN, 1, 1, null, now);
        upsertUser("Amara", "Diallo", "amara.diallo@gmail.com", encodedPassword, Role.PATIENT, 1, null, 1L, now);
        upsertUser("Ibrahim", "Cisse", "ibrahim.cisse@shambua.health", encodedPassword, Role.LABORANTIN, 1, null, null, now);
        upsertUser("Fatou", "Ndiaye", "fatou.ndiaye@shambua.health", encodedPassword, Role.RECEPTION, 1, null, null, now);
        upsertUser("Marie", "Kouassi", "marie.kouassi@shambua.health", encodedPassword, Role.CAISSIER, 1, null, null, now);
        upsertUser("Amina", "Diallo", "amina.diallo@shambua.health", encodedPassword, Role.ARCHIVISTE, 1, null, null, now);
    }

    private void upsertUser(String prenom, String nom, String email, String password, Role role,
                            Integer hopitalId, Integer medecinId, Long patientId, LocalDateTime now) {
        Optional<Utilisateur> existing = findByEmailAnyStatus(email);
        if (existing.isPresent()) {
            jdbcTemplate.update(
                    """
                    UPDATE utilisateurs
                    SET id_hopital = ?, id_medecin = ?, id_patient = ?, nom = ?, prenom = ?,
                        mot_de_passe = ?, role = ?, est_actif = TRUE
                    WHERE LOWER(email) = LOWER(?)
                    """,
                    hopitalId, medecinId, patientId, nom, prenom, password, role.name(), email
            );
            return;
        }

        insertUser(prenom, nom, email, password, role, hopitalId, medecinId, patientId, now);
    }

    private void insertUser(String prenom, String nom, String email, String password, Role role,
                            Integer hopitalId, Integer medecinId, Long patientId, LocalDateTime now) {
        jdbcTemplate.update(
                """
                INSERT INTO utilisateurs
                (id_hopital, id_medecin, id_patient, nom, prenom, email, mot_de_passe, role, est_actif, date_creation)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, TRUE, ?)
                """,
                hopitalId, medecinId, patientId, nom, prenom, email, password, role.name(), now
        );
    }

    @Override
    public Optional<Utilisateur> findByEmail(String email) {
        List<Utilisateur> results = jdbcTemplate.query(
                "SELECT * FROM utilisateurs WHERE LOWER(email) = LOWER(?) AND est_actif = TRUE",
                rowMapper,
                email
        );
        return results.stream().findFirst();
    }

    @Override
    public Optional<Utilisateur> findByEmailAnyStatus(String email) {
        List<Utilisateur> results = jdbcTemplate.query(
                "SELECT * FROM utilisateurs WHERE LOWER(email) = LOWER(?)",
                rowMapper,
                email
        );
        return results.stream().findFirst();
    }

    @Override
    public Optional<Utilisateur> findById(Integer id) {
        List<Utilisateur> results = jdbcTemplate.query(
                "SELECT * FROM utilisateurs WHERE id_utilisateur = ?",
                rowMapper,
                id
        );
        return results.stream().findFirst();
    }

    @Override
    public Optional<Utilisateur> findByIdAndHopitalId(Integer id, Integer idHopital) {
        List<Utilisateur> results = jdbcTemplate.query(
                "SELECT * FROM utilisateurs WHERE id_utilisateur = ? AND id_hopital = ?",
                rowMapper,
                id,
                idHopital
        );
        return results.stream().findFirst();
    }

    @Override
    public List<Utilisateur> findAllByHopitalId(Integer idHopital) {
        return jdbcTemplate.query(
                "SELECT * FROM utilisateurs WHERE id_hopital = ? AND est_actif = TRUE ORDER BY nom, prenom",
                rowMapper,
                idHopital
        );
    }

    @Override
    public List<Utilisateur> findAllByHopitalIdIncludingInactive(Integer idHopital) {
        return jdbcTemplate.query(
                "SELECT * FROM utilisateurs WHERE id_hopital = ? ORDER BY nom, prenom",
                rowMapper,
                idHopital
        );
    }

    @Override
    public boolean existsByEmail(String email) {
        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM utilisateurs WHERE LOWER(email) = LOWER(?)",
                Integer.class,
                email
        );
        return count != null && count > 0;
    }

    @Override
    public boolean existsByEmailExcludingId(String email, Integer excludeId) {
        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM utilisateurs WHERE LOWER(email) = LOWER(?) AND id_utilisateur <> ?",
                Integer.class,
                email,
                excludeId
        );
        return count != null && count > 0;
    }

    @Override
    public Utilisateur insert(Utilisateur utilisateur) {
        LocalDateTime now = utilisateur.getDateCreation() != null
                ? utilisateur.getDateCreation()
                : LocalDateTime.now();

        org.springframework.jdbc.support.KeyHolder keyHolder =
                new org.springframework.jdbc.support.GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            var ps = connection.prepareStatement(
                    """
                    INSERT INTO utilisateurs
                    (id_hopital, id_medecin, id_patient, nom, prenom, email, mot_de_passe, telephone, role, est_actif, date_creation)
                    VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                    """,
                    java.sql.Statement.RETURN_GENERATED_KEYS
            );
            ps.setObject(1, utilisateur.getIdHopital());
            ps.setObject(2, utilisateur.getIdMedecin());
            ps.setObject(3, utilisateur.getIdPatient());
            ps.setString(4, utilisateur.getNom());
            ps.setString(5, utilisateur.getPrenom());
            ps.setString(6, utilisateur.getEmail());
            ps.setString(7, utilisateur.getMotDePasse());
            ps.setString(8, utilisateur.getTelephone());
            ps.setString(9, utilisateur.getRole().name());
            ps.setBoolean(10, utilisateur.isEstActif());
            ps.setObject(11, now);
            return ps;
        }, keyHolder);

        Number key = keyHolder.getKey();
        if (key != null) {
            utilisateur.setIdUtilisateur(key.intValue());
        }
        utilisateur.setDateCreation(now);
        return utilisateur;
    }

    @Override
    public void updateProfile(Utilisateur utilisateur) {
        int updated = jdbcTemplate.update(
                """
                UPDATE utilisateurs
                SET nom = ?, prenom = ?, email = ?, telephone = ?, role = ?, id_medecin = ?
                WHERE id_utilisateur = ? AND id_hopital = ?
                """,
                utilisateur.getNom(),
                utilisateur.getPrenom(),
                utilisateur.getEmail(),
                utilisateur.getTelephone(),
                utilisateur.getRole().name(),
                utilisateur.getIdMedecin(),
                utilisateur.getIdUtilisateur(),
                utilisateur.getIdHopital()
        );
        if (updated == 0) {
            throw new IllegalStateException("Utilisateur introuvable pour la mise à jour");
        }
    }

    @Override
    public void updateMedecinLink(Integer idUtilisateur, Integer idHopital, Integer idMedecin) {
        int updated = jdbcTemplate.update(
                """
                UPDATE utilisateurs
                SET id_medecin = ?
                WHERE id_utilisateur = ? AND id_hopital = ?
                """,
                idMedecin,
                idUtilisateur,
                idHopital
        );
        if (updated == 0) {
            throw new IllegalStateException("Utilisateur introuvable pour le lien médecin");
        }
    }

    @Override
    public void updatePassword(Integer idUtilisateur, String encodedPassword) {
        int updated = jdbcTemplate.update(
                "UPDATE utilisateurs SET mot_de_passe = ? WHERE id_utilisateur = ?",
                encodedPassword,
                idUtilisateur
        );
        if (updated == 0) {
            throw new IllegalStateException("Utilisateur introuvable pour la mise à jour du mot de passe");
        }
    }

    @Override
    public boolean setActive(Integer id, Integer idHopital, boolean active) {
        int updated = jdbcTemplate.update(
                "UPDATE utilisateurs SET est_actif = ? WHERE id_utilisateur = ? AND id_hopital = ?",
                active,
                id,
                idHopital
        );
        return updated > 0;
    }

    @Override
    public boolean setActiveById(Integer id, boolean active) {
        int updated = jdbcTemplate.update(
                "UPDATE utilisateurs SET est_actif = ? WHERE id_utilisateur = ?",
                active,
                id
        );
        return updated > 0;
    }

    @Override
    public Long countAllActive() {
        return jdbcTemplate.queryForObject(
                "SELECT COUNT(1) FROM utilisateurs WHERE est_actif = TRUE", Long.class);
    }

    @Override
    public Long countAllActiveExistingBefore(java.time.LocalDate date) {
        return jdbcTemplate.queryForObject(
                "SELECT COUNT(1) FROM utilisateurs WHERE est_actif = TRUE AND date_creation < ?",
                Long.class, date.atStartOfDay());
    }

    @Override
    public Optional<Integer> findUtilisateurIdByPatient(Integer idPatient, Integer idHopital) {
        if (idPatient == null || idHopital == null) {
            return Optional.empty();
        }
        try {
            Integer id = jdbcTemplate.queryForObject(
                    """
                    SELECT id_utilisateur FROM utilisateurs
                    WHERE id_patient = ? AND id_hopital = ? AND est_actif = TRUE
                    LIMIT 1
                    """,
                    Integer.class,
                    idPatient,
                    idHopital);
            if (id != null) {
                return Optional.of(id);
            }
        } catch (Exception ignored) {
            // fallback email ci-dessous
        }
        try {
            Integer id = jdbcTemplate.queryForObject(
                    """
                    SELECT u.id_utilisateur
                    FROM utilisateurs u
                    INNER JOIN patients p ON p.id_hopital = u.id_hopital
                      AND LOWER(TRIM(p.email)) = LOWER(TRIM(u.email))
                    WHERE p.id_patient = ?
                      AND p.id_hopital = ?
                      AND UPPER(u.role) = 'PATIENT'
                      AND u.est_actif = TRUE
                    LIMIT 1
                    """,
                    Integer.class,
                    idPatient,
                    idHopital);
            if (id != null) {
                // Auto-répare le lien id_patient pour les prochains appels
                jdbcTemplate.update(
                        """
                        UPDATE utilisateurs SET id_patient = ?
                        WHERE id_utilisateur = ? AND (id_patient IS NULL OR id_patient = ?)
                        """,
                        idPatient.longValue(),
                        id,
                        idPatient.longValue());
                return Optional.of(id);
            }
        } catch (Exception ignored) {
            // aucun compte
        }
        return Optional.empty();
    }

    @Override
    public Optional<String> findEmailByPatient(Integer idPatient, Integer idHopital) {
        if (idPatient == null || idHopital == null) {
            return Optional.empty();
        }
        try {
            String email = jdbcTemplate.queryForObject(
                    """
                    SELECT email FROM utilisateurs
                    WHERE id_patient = ? AND id_hopital = ? AND est_actif = TRUE
                    LIMIT 1
                    """,
                    String.class,
                    idPatient,
                    idHopital);
            if (email == null || email.isBlank()) {
                return Optional.empty();
            }
            return Optional.of(email.trim());
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    @Override
    public boolean linkPatientAccountByEmail(Integer idPatient, Integer idHopital, String email) {
        if (idPatient == null || idHopital == null || email == null || email.isBlank()) {
            return false;
        }
        int updated = jdbcTemplate.update(
                """
                UPDATE utilisateurs
                SET id_patient = ?
                WHERE id_hopital = ?
                  AND LOWER(TRIM(email)) = LOWER(TRIM(?))
                  AND UPPER(role) = 'PATIENT'
                  AND est_actif = TRUE
                  AND (id_patient IS NULL OR id_patient = ?)
                """,
                idPatient.longValue(),
                idHopital,
                email.trim(),
                idPatient.longValue());
        return updated > 0;
    }

    @Override
    public Optional<Integer> findUtilisateurIdByMedecin(Integer idMedecin, Integer idHopital) {
        if (idMedecin == null || idHopital == null) {
            return Optional.empty();
        }
        try {
            Integer id = jdbcTemplate.queryForObject(
                    """
                    SELECT id_utilisateur FROM utilisateurs
                    WHERE id_medecin = ? AND id_hopital = ? AND est_actif = TRUE
                    LIMIT 1
                    """,
                    Integer.class,
                    idMedecin,
                    idHopital);
            return Optional.ofNullable(id);
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    @Override
    public List<Integer> findActiveUtilisateurIdsByRole(Integer idHopital, Role role) {
        if (idHopital == null || role == null) {
            return List.of();
        }
        try {
            return jdbcTemplate.query(
                    """
                    SELECT id_utilisateur FROM utilisateurs
                    WHERE id_hopital = ? AND role = ? AND est_actif = TRUE
                    ORDER BY id_utilisateur
                    """,
                    (rs, rowNum) -> rs.getInt("id_utilisateur"),
                    idHopital,
                    role.name());
        } catch (Exception e) {
            return List.of();
        }
    }
}
