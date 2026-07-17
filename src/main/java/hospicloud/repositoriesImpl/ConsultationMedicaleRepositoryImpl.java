package hospicloud.repositoriesImpl;

import com.fasterxml.jackson.databind.ObjectMapper;
import hospicloud.model.ConsultationMedicale;
import hospicloud.repositories.ConsultationMedicaleRepository;
import hospicloud.security.TenantContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public class ConsultationMedicaleRepositoryImpl implements ConsultationMedicaleRepository {

    private static final Logger logger = LoggerFactory.getLogger(ConsultationMedicaleRepositoryImpl.class);
    
    private final JdbcTemplate jdbcTemplate;
    private final JedisPool jedisPool;
    private final ObjectMapper objectMapper;

    private static final int CACHE_TTL = 3600;
    private volatile boolean schemaEnsured = false;

    public ConsultationMedicaleRepositoryImpl(JdbcTemplate jdbcTemplate,
            @Autowired(required = false) JedisPool jedisPool,
            ObjectMapper objectMapper) {
        this.jdbcTemplate = jdbcTemplate;
        this.jedisPool = jedisPool;
        this.objectMapper = objectMapper;
    }

    private void cacheConsultation(Integer hopitalId, ConsultationMedicale c) {
        if (jedisPool == null || c.getIdConsultation() == null) {
            return;
        }
        try (Jedis jedis = jedisPool.getResource()) {
            jedis.setex(buildCacheKey(hopitalId, c.getIdConsultation()), CACHE_TTL, objectMapper.writeValueAsString(c));
        } catch (Exception e) {
            logger.warn("Cache Redis indisponible lors de l'enregistrement de la consultation {}: {}",
                    c.getIdConsultation(), e.getMessage());
        }
    }

    private void invalidateCache(Integer hopitalId, Long consultationId) {
        if (jedisPool == null || consultationId == null) {
            return;
        }
        try (Jedis jedis = jedisPool.getResource()) {
            jedis.del(buildCacheKey(hopitalId, consultationId));
        } catch (Exception e) {
            logger.warn("Cache Redis indisponible lors de l'invalidation de la consultation {}: {}",
                    consultationId, e.getMessage());
        }
    }

    private String buildCacheKey(Integer hopitalId, Long consultationId) {
        return String.format("hopital:%d:consultation:%d", hopitalId, consultationId);
    }

    @Override
    public void ensureSchema() {
        jdbcTemplate.execute("""
            CREATE TABLE IF NOT EXISTS consultations_medicales (
                id_consultation BIGINT NOT NULL AUTO_INCREMENT,
                id_hopital INT NOT NULL,
                id_medecin INT NOT NULL,
                id_patient INT NOT NULL,
                id_rdv INT DEFAULT NULL,
                date_consultation TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP,
                motif_visite VARCHAR(255) DEFAULT NULL,
                poids DECIMAL(5,2) DEFAULT NULL,
                taille INT DEFAULT NULL,
                tension_arterielle VARCHAR(20) DEFAULT NULL,
                temperature DECIMAL(4,2) DEFAULT NULL,
                frequence_cardiaque INT DEFAULT NULL,
                observations TEXT,
                diagnostic TEXT,
                analyses_prescrites TEXT NULL,
                fiche_finalisee TINYINT(1) NOT NULL DEFAULT 0,
                PRIMARY KEY (id_consultation),
                KEY idx_consult_med_tenant (id_hopital, id_medecin),
                KEY idx_consult_med_patient (id_patient),
                KEY idx_consult_med_rdv (id_rdv),
                CONSTRAINT fk_consult_med_hopital FOREIGN KEY (id_hopital) REFERENCES hopitaux(id_hopital)
            ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4
            """);

        addColumnIfMissing("consultations_medicales", "analyses_prescrites", "TEXT NULL");
        addColumnIfMissing("consultations_medicales", "fiche_finalisee", "TINYINT(1) NOT NULL DEFAULT 0");
        addColumnIfMissing("consultations_medicales", "statut", "VARCHAR(20) NOT NULL DEFAULT 'BROUILLON'");
        addColumnIfMissing("consultations_medicales", "date_signature", "DATETIME NULL");
        addIndexIfMissing("consultations_medicales", "idx_consult_med_rdv", "id_rdv");
        jdbcTemplate.update("""
            UPDATE consultations_medicales
            SET statut = 'BROUILLON'
            WHERE statut = 'SIGNEE'
              AND NOT EXISTS (
                SELECT 1 FROM signatures_documents s
                WHERE s.document_id = consultations_medicales.id_consultation
                  AND s.type_document = 'CONSULTATION'
                  AND s.id_hopital = consultations_medicales.id_hopital
                  AND s.statut = 'SIGNE'
              )
            """);
        schemaEnsured = true;
    }

    private void ensureSchemaOnce() {
        if (!schemaEnsured) {
            synchronized (this) {
                if (!schemaEnsured) {
                    ensureSchema();
                }
            }
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
            logger.info("Colonne {}.{} ajoutée automatiquement.", table, column);
        }
    }

    private void addIndexIfMissing(String table, String indexName, String column) {
        Integer count = jdbcTemplate.queryForObject(
                """
                SELECT COUNT(*) FROM information_schema.statistics
                WHERE table_schema = DATABASE() AND table_name = ? AND index_name = ?
                """,
                Integer.class,
                table,
                indexName
        );
        if (count != null && count == 0) {
            jdbcTemplate.execute("CREATE INDEX " + indexName + " ON " + table + " (" + column + ")");
            logger.info("Index {} ajouté sur {}.{}.", indexName, table, column);
        }
    }

    @Override
    @Transactional
    public ConsultationMedicale save(ConsultationMedicale c) {
        ensureSchemaOnce();
        Integer hopitalId = TenantContext.getRequiredHopitalId();

        String sql = """
            INSERT INTO consultations_medicales (id_hopital, id_medecin, id_patient, id_rdv, motif_visite, 
            poids, taille, tension_arterielle, temperature, frequence_cardiaque, observations, diagnostic, analyses_prescrites) 
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            """;

        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setInt(1, hopitalId);
            ps.setInt(2, c.getIdMedecin());
            ps.setInt(3, c.getIdPatient());
            if (c.getIdRdv() != null) ps.setInt(4, c.getIdRdv()); else ps.setNull(4, Types.INTEGER);
            ps.setString(5, c.getMotifVisite());
            ps.setBigDecimal(6, c.getPoids());
            c.setDateConsultation(java.time.LocalDateTime.now());
            if (c.getTaille() != null) ps.setInt(7, c.getTaille()); else ps.setNull(7, Types.INTEGER);
            ps.setString(8, c.getTensionArterielle());
            ps.setBigDecimal(9, c.getTemperature());
            if (c.getFrequenceCardiaque() != null) ps.setInt(10, c.getFrequenceCardiaque()); else ps.setNull(10, Types.INTEGER);
            ps.setString(11, c.getObservations());
            ps.setString(12, c.getDiagnostic());
            ps.setString(13, c.getAnalysesPrescrites());
            return ps;
        }, keyHolder);

        if (keyHolder.getKey() != null) {
            c.setIdConsultation(keyHolder.getKey().longValue());
        }
        c.setIdHopital(hopitalId);

        cacheConsultation(hopitalId, c);

        return c;
    }

    @Override
    public List<ConsultationMedicale> findByPatient(Integer idPatient) {
        String sql = """
            SELECT c.*,
                   TRIM(CONCAT(COALESCE(m.prenom, ''), ' ', COALESCE(m.nom, ''))) AS nom_medecin,
                   h.nom AS nom_hopital
            FROM consultations_medicales c
            LEFT JOIN medecin m ON c.id_medecin = m.id_medecin AND c.id_hopital = m.id_hopital
            LEFT JOIN hopitaux h ON c.id_hopital = h.id_hopital
            WHERE c.id_patient = ? AND c.id_hopital = ? AND c.statut = 'SIGNEE'
            ORDER BY c.date_consultation DESC
            """;
        return jdbcTemplate.query(sql, this::mapRowToConsultationWithJoin, idPatient, TenantContext.getRequiredHopitalId());
    }

    @Override
    public List<ConsultationMedicale> findByMedecin(Integer idMedecin) {
        ensureSchemaOnce();
        String sql = """
            SELECT c.*,
                   TRIM(CONCAT(COALESCE(m.prenom, ''), ' ', COALESCE(m.nom, ''))) AS nom_medecin,
                   TRIM(CONCAT(COALESCE(p.prenom, ''), ' ', COALESCE(p.nom, ''))) AS nom_patient,
                   h.nom AS nom_hopital
            FROM consultations_medicales c
            LEFT JOIN medecin m ON c.id_medecin = m.id_medecin AND c.id_hopital = m.id_hopital
            LEFT JOIN patients p ON c.id_patient = p.id_patient AND c.id_hopital = p.id_hopital
            LEFT JOIN hopitaux h ON c.id_hopital = h.id_hopital
            WHERE c.id_medecin = ? AND c.id_hopital = ? AND c.statut = 'SIGNEE'
            ORDER BY c.date_consultation DESC
            """;
        return jdbcTemplate.query(sql, this::mapRowToConsultationWithJoin, idMedecin, TenantContext.getRequiredHopitalId());
    }

    @Override
    public List<ConsultationMedicale> findEnGeranceParMedecin(Integer idMedecin) {
        ensureSchemaOnce();
        if (idMedecin == null) {
            return List.of();
        }
        Integer hopitalId = TenantContext.getRequiredHopitalId();
        String sql = """
            SELECT c.*,
                   TRIM(CONCAT(COALESCE(m.prenom, ''), ' ', COALESCE(m.nom, ''))) AS nom_medecin,
                   TRIM(CONCAT(COALESCE(p.prenom, ''), ' ', COALESCE(p.nom, ''))) AS nom_patient,
                   h.nom AS nom_hopital
            FROM consultations_medicales c
            INNER JOIN medecin_patient mp
                    ON mp.id_patient = c.id_patient AND mp.id_medecin = c.id_medecin
            INNER JOIN patients p
                    ON p.id_patient = c.id_patient AND p.id_hopital = c.id_hopital
            LEFT JOIN medecin m ON c.id_medecin = m.id_medecin AND c.id_hopital = m.id_hopital
            LEFT JOIN hopitaux h ON c.id_hopital = h.id_hopital
            WHERE c.id_medecin = ?
              AND c.id_hopital = ?
              AND (c.statut IS NULL OR c.statut <> 'ANNULEE')
              AND UPPER(COALESCE(p.statut_clinique, 'AMBULATOIRE')) NOT IN ('SORTI', 'SORTIE_AUTORISEE')
            ORDER BY c.date_consultation DESC
            """;
        return jdbcTemplate.query(sql, this::mapRowToConsultationWithJoin, idMedecin, hopitalId);
    }

    @Override
    public List<ConsultationMedicale> findByHopital(Integer idHopital) {
        ensureSchemaOnce();
        String sql = """
            SELECT c.*,
                   TRIM(CONCAT(COALESCE(m.prenom, ''), ' ', COALESCE(m.nom, ''))) AS nom_medecin,
                   h.nom AS nom_hopital
            FROM consultations_medicales c
            LEFT JOIN medecin m ON c.id_medecin = m.id_medecin AND c.id_hopital = m.id_hopital
            LEFT JOIN hopitaux h ON c.id_hopital = h.id_hopital
            WHERE c.id_hopital = ? AND (c.statut IS NULL OR c.statut <> 'ANNULEE')
            ORDER BY c.date_consultation DESC
            """;
        return jdbcTemplate.query(sql, this::mapRowToConsultationWithJoin, idHopital);
    }

    @Override
    public Optional<ConsultationMedicale> findById(Long idConsultation) {
        Integer hopitalId = TenantContext.getRequiredHopitalId();
        String key = buildCacheKey(hopitalId, idConsultation);

        if (jedisPool != null) {
            try (Jedis jedis = jedisPool.getResource()) {
                String json = jedis.get(key);
                if (json != null) return Optional.of(objectMapper.readValue(json, ConsultationMedicale.class));
            } catch (Exception e) {
                logger.warn("Redis indisponible ou erreur de lecture pour id {}, fallback MySQL", idConsultation);
            }
        }

        String sql = "SELECT * FROM consultations_medicales WHERE id_consultation = ? AND id_hopital = ?";
        return jdbcTemplate.query(sql, this::mapRowToConsultation, idConsultation, hopitalId).stream().findFirst()
            .map(c -> {
                cacheConsultation(hopitalId, c);
                return c;
            });
    }

    @Override
    @Transactional
    public void updateObservationsEtDiagnostic(Long idConsultation, String obs, String diag) {
        Integer hopitalId = TenantContext.getRequiredHopitalId();
        jdbcTemplate.update("UPDATE consultations_medicales SET observations = ?, diagnostic = ? WHERE id_consultation = ? AND id_hopital = ?", 
                            obs, diag, idConsultation, hopitalId);

        invalidateCache(hopitalId, idConsultation);
    }

    @Override
    public Optional<ConsultationMedicale> findActiveForPatientAndMedecin(Integer idPatient, Integer idMedecin) {
        Integer hopitalId = TenantContext.getRequiredHopitalId();
        String sql = """
            SELECT * FROM consultations_medicales
            WHERE id_patient = ? AND id_medecin = ? AND id_hopital = ?
              AND date_consultation >= DATE_SUB(NOW(), INTERVAL 48 HOUR)
            ORDER BY date_consultation DESC
            LIMIT 1
            """;
        return jdbcTemplate.query(sql, this::mapRowToConsultation, idPatient, idMedecin, hopitalId)
                .stream().findFirst();
    }

    @Override
    public Optional<ConsultationMedicale> findByRdv(Integer idRdv) {
        Integer hopitalId = TenantContext.getRequiredHopitalId();
        String sql = """
            SELECT * FROM consultations_medicales
            WHERE id_rdv = ? AND id_hopital = ?
            ORDER BY date_consultation DESC
            LIMIT 1
            """;
        return jdbcTemplate.query(sql, this::mapRowToConsultation, idRdv, hopitalId).stream().findFirst();
    }

    @Override
    @Transactional
    public void updateFiche(ConsultationMedicale c) {
        ensureSchemaOnce();
        Integer hopitalId = TenantContext.getRequiredHopitalId();
        String sql = """
            UPDATE consultations_medicales
            SET poids = ?, taille = ?, tension_arterielle = ?, temperature = ?,
                frequence_cardiaque = ?, observations = ?, diagnostic = ?, analyses_prescrites = ?,
                fiche_finalisee = CASE WHEN ? = 1 THEN 1 ELSE fiche_finalisee END
            WHERE id_consultation = ? AND id_hopital = ?
            """;
        jdbcTemplate.update(sql,
                c.getPoids(),
                c.getTaille(),
                c.getTensionArterielle(),
                c.getTemperature(),
                c.getFrequenceCardiaque(),
                c.getObservations(),
                c.getDiagnostic(),
                c.getAnalysesPrescrites(),
                Boolean.TRUE.equals(c.getFicheFinalisee()) ? 1 : 0,
                c.getIdConsultation(),
                hopitalId);
        invalidateCache(hopitalId, c.getIdConsultation());
    }

    @Override
    @Transactional
    public void signerConsultation(Long idConsultation, LocalDateTime dateSignature) {
        ensureSchemaOnce();
        Integer hopitalId = TenantContext.getRequiredHopitalId();
        int updated = jdbcTemplate.update("""
            UPDATE consultations_medicales
            SET statut = 'SIGNEE', fiche_finalisee = 1, date_signature = ?
            WHERE id_consultation = ? AND id_hopital = ? AND statut = 'BROUILLON'
            """,
                Timestamp.valueOf(dateSignature),
                idConsultation,
                hopitalId);
        if (updated == 0) {
            throw new IllegalStateException("Impossible de signer la consultation : statut invalide ou déjà signée.");
        }
        invalidateCache(hopitalId, idConsultation);
    }

    private ConsultationMedicale mapRowToConsultationWithJoin(ResultSet rs, int rowNum) throws SQLException {
        ConsultationMedicale c = mapRowToConsultation(rs, rowNum);
        try {
            String nomMedecin = rs.getString("nom_medecin");
            if (nomMedecin != null && !nomMedecin.isBlank()) {
                c.setNomMedecin(nomMedecin.trim());
            }
            String nomHopital = rs.getString("nom_hopital");
            if (nomHopital != null && !nomHopital.isBlank()) {
                c.setNomHopital(nomHopital.trim());
            }
            try {
                String nomPatient = rs.getString("nom_patient");
                if (nomPatient != null && !nomPatient.isBlank()) {
                    c.setNomPatient(nomPatient.trim());
                }
            } catch (SQLException ignored) {
                // colonne absente sur certaines requêtes
            }
        } catch (SQLException ignored) {
            // colonnes absentes sur requêtes sans jointure
        }
        return c;
    }

    private ConsultationMedicale mapRowToConsultation(ResultSet rs, int rowNum) throws SQLException {
        ConsultationMedicale c = new ConsultationMedicale();
        c.setIdConsultation(rs.getLong("id_consultation"));
        c.setIdHopital(rs.getInt("id_hopital"));
        c.setIdMedecin(rs.getInt("id_medecin"));
        c.setIdPatient(rs.getInt("id_patient"));
        
        // Gestion sécurisée des champs nullables (Remplissage des entiers)
        int idRdv = rs.getInt("id_rdv");
        c.setIdRdv(rs.wasNull() ? null : idRdv);
        
        if (rs.getTimestamp("date_consultation") != null) {
            c.setDateConsultation(rs.getTimestamp("date_consultation").toLocalDateTime());
        }
        
        c.setMotifVisite(rs.getString("motif_visite"));
        c.setPoids(rs.getBigDecimal("poids"));
        
        int taille = rs.getInt("taille");
        c.setTaille(rs.wasNull() ? null : taille);
        
        c.setTensionArterielle(rs.getString("tension_arterielle"));
        c.setTemperature(rs.getBigDecimal("temperature"));
        
        int freq = rs.getInt("frequence_cardiaque");
        c.setFrequenceCardiaque(rs.wasNull() ? null : freq);
        
        c.setObservations(rs.getString("observations"));
        c.setDiagnostic(rs.getString("diagnostic"));
        try {
            c.setAnalysesPrescrites(rs.getString("analyses_prescrites"));
        } catch (SQLException ignored) {
            c.setAnalysesPrescrites(null);
        }
        try {
            c.setFicheFinalisee(rs.getBoolean("fiche_finalisee"));
        } catch (SQLException ignored) {
            c.setFicheFinalisee(false);
        }
        try {
            String statut = rs.getString("statut");
            c.setStatut(hospicloud.model.enums.ConsultationStatut.fromDb(statut));
        } catch (SQLException ignored) {
            c.setStatut(hospicloud.model.enums.ConsultationStatut.BROUILLON);
        }
        try {
            if (rs.getTimestamp("date_signature") != null) {
                c.setDateSignature(rs.getTimestamp("date_signature").toLocalDateTime());
            }
        } catch (SQLException ignored) {
            c.setDateSignature(null);
        }
        
        return c;
    }
}