package hospicloud.repositoriesImpl;

import com.fasterxml.jackson.databind.ObjectMapper;
import hospicloud.model.Ordonnance;
import hospicloud.repositories.OrdonnanceRepository;
import hospicloud.security.TenantContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

@Repository
public class OrdonnanceRepositoryImpl implements OrdonnanceRepository {

    private final JdbcTemplate jdbcTemplate;
    private final JedisPool jedisPool;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private static final String CACHE_PREFIX = "ordonnance:";

    public OrdonnanceRepositoryImpl(JdbcTemplate jdbcTemplate,
                                    @Autowired(required = false) JedisPool jedisPool) {
        this.jdbcTemplate = jdbcTemplate;
        this.jedisPool = jedisPool;
    }

    @Override
    public void creerOrdonnance(Ordonnance o) {
        Integer hopitalId = TenantContext.getRequiredHopitalId();
        try {
            String sql = """
                INSERT INTO ordonnances_medicales
                  (id_patient, hospital_id, id_medecin, contenu_ordonnance, diagnostic, observations,
                   date_expiration, date_prescription, statut)
                VALUES (?, ?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP, 'ACTIVE')
                """;
            jdbcTemplate.update(
                    sql,
                    o.getIdPatient(),
                    hopitalId,
                    o.getIdMedecin(),
                    o.getContenuOrdonnance(),
                    o.getDiagnostic(),
                    o.getObservations(),
                    o.getDateExpiration()
            );
        } catch (Exception ex) {
            // Fallback schéma minimal
            String sql = "INSERT INTO ordonnances_medicales (id_patient, hospital_id, id_medecin, contenu_ordonnance, date_expiration) VALUES (?, ?, ?, ?, ?)";
            jdbcTemplate.update(
                    sql,
                    o.getIdPatient(),
                    hopitalId,
                    o.getIdMedecin(),
                    o.getContenuOrdonnance(),
                    o.getDateExpiration()
            );
        }
    }

    @Override
    public List<Ordonnance> listerParPatient(Integer idPatient) {
        Integer hopitalId = TenantContext.getRequiredHopitalId();
        try {
            String sql = """
                SELECT * FROM ordonnances_medicales
                WHERE id_patient = ? AND hospital_id = ?
                ORDER BY date_prescription DESC, id_ordonnance DESC
                """;
            return jdbcTemplate.query(sql, new OrdonnanceRowMapper(), idPatient, hopitalId);
        } catch (Exception ex) {
            String sql = "SELECT * FROM ordonnances_medicales WHERE id_patient = ? AND hospital_id = ? ORDER BY id_ordonnance DESC";
            return jdbcTemplate.query(sql, new OrdonnanceRowMapper(), idPatient, hopitalId);
        }
    }

    @Override
    public List<Ordonnance> listerParMedecin(Integer idMedecin) {
        Integer hopitalId = TenantContext.getRequiredHopitalId();
        if (idMedecin == null) {
            return List.of();
        }
        try {
            String sql = """
                SELECT o.*,
                       TRIM(CONCAT(COALESCE(p.prenom, ''), ' ', COALESCE(p.nom, ''))) AS nom_patient
                FROM ordonnances_medicales o
                LEFT JOIN patients p ON o.id_patient = p.id_patient AND o.hospital_id = p.id_hopital
                WHERE o.id_medecin = ? AND o.hospital_id = ?
                ORDER BY o.date_prescription DESC, o.id_ordonnance DESC
                """;
            return jdbcTemplate.query(sql, new OrdonnanceWithPatientRowMapper(), idMedecin, hopitalId);
        } catch (Exception ex) {
            String sql = """
                SELECT o.* FROM ordonnances_medicales o
                WHERE o.id_medecin = ? AND o.hospital_id = ?
                ORDER BY o.id_ordonnance DESC
                """;
            return jdbcTemplate.query(sql, new OrdonnanceRowMapper(), idMedecin, hopitalId);
        }
    }

    @Override
    public Optional<Ordonnance> trouverParId(Long idOrdonnance) {
        String cacheKey = CACHE_PREFIX + TenantContext.getRequiredHopitalId() + ":" + idOrdonnance;
        
        if (jedisPool != null) {
            try (Jedis jedis = jedisPool.getResource()) {
                String json = jedis.get(cacheKey);
                if (json != null) {
                    return Optional.of(objectMapper.readValue(json, Ordonnance.class));
                }
            } catch (Exception e) { /* Log */ }
        }

        String sql = "SELECT * FROM ordonnances_medicales WHERE id_ordonnance = ? AND hospital_id = ?";
        List<Ordonnance> results = jdbcTemplate.query(sql, new OrdonnanceRowMapper(), idOrdonnance, TenantContext.getRequiredHopitalId());
        
        if (!results.isEmpty()) {
            Ordonnance o = results.get(0);
            if (jedisPool != null) {
                try (Jedis jedis = jedisPool.getResource()) {
                    jedis.set(cacheKey, objectMapper.writeValueAsString(o));
                } catch (Exception e) { /* Log */ }
            }
            return Optional.of(o);
        }
        return Optional.empty();
    }

    @Override
    public void mettreAJourStatut(Long idOrdonnance, String nouveauStatut) {
        String sql = "UPDATE ordonnances_medicales SET statut = ? WHERE id_ordonnance = ? AND hospital_id = ?";
        jdbcTemplate.update(sql, nouveauStatut, idOrdonnance, TenantContext.getRequiredHopitalId());
        
        if (jedisPool != null) {
            try (Jedis jedis = jedisPool.getResource()) {
                jedis.del(CACHE_PREFIX + TenantContext.getRequiredHopitalId() + ":" + idOrdonnance);
            } catch (Exception e) { /* Log */ }
        }
    }

    private static class OrdonnanceRowMapper implements RowMapper<Ordonnance> {
        @Override
        public Ordonnance mapRow(ResultSet rs, int rowNum) throws SQLException {
            Ordonnance o = new Ordonnance();
            o.setIdOrdonnance(rs.getLong("id_ordonnance"));
            o.setIdPatient(rs.getInt("id_patient"));
            o.setHospitalId(rs.getInt("hospital_id"));
            o.setIdMedecin(rs.getInt("id_medecin"));
            o.setContenuOrdonnance(rs.getString("contenu_ordonnance"));
            try {
                o.setStatut(rs.getString("statut"));
            } catch (SQLException ignored) {
                o.setStatut("ACTIVE");
            }
            try {
                o.setDiagnostic(rs.getString("diagnostic"));
            } catch (SQLException ignored) {
            }
            try {
                o.setObservations(rs.getString("observations"));
            } catch (SQLException ignored) {
            }
            try {
                if (rs.getDate("date_expiration") != null) {
                    o.setDateExpiration(rs.getDate("date_expiration").toLocalDate());
                }
            } catch (SQLException ignored) {
            }
            try {
                if (rs.getTimestamp("date_prescription") != null) {
                    o.setDatePrescription(rs.getTimestamp("date_prescription").toLocalDateTime());
                }
            } catch (SQLException ignored) {
            }
            return o;
        }
    }

    private static class OrdonnanceWithPatientRowMapper implements RowMapper<Ordonnance> {
        private final OrdonnanceRowMapper base = new OrdonnanceRowMapper();

        @Override
        public Ordonnance mapRow(ResultSet rs, int rowNum) throws SQLException {
            Ordonnance o = base.mapRow(rs, rowNum);
            try {
                String nom = rs.getString("nom_patient");
                if (nom != null && !nom.isBlank()) {
                    o.setNomPatient(nom.trim());
                }
            } catch (SQLException ignored) {
            }
            return o;
        }
    }

    @Override
    public void annulerOrdonnance(Long idOrdonnance) {
        // 1. Mise à jour du statut en base de données
        String sql = "UPDATE ordonnances_medicales SET statut = 'ANNULEE' WHERE id_ordonnance = ? AND hospital_id = ?";
        jdbcTemplate.update(sql, idOrdonnance, TenantContext.getRequiredHopitalId());
        
        // 2. Invalidation du cache Redis pour garantir la cohérence des données
        if (jedisPool != null) {
            try (Jedis jedis = jedisPool.getResource()) {
                jedis.del(CACHE_PREFIX + TenantContext.getRequiredHopitalId() + ":" + idOrdonnance);
            } catch (Exception e) {
                // Log de l'erreur d'invalidation cache si nécessaire
            }
        }
    }
}