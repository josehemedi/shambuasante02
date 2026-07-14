package hospicloud.repositoriesImpl;

import com.fasterxml.jackson.databind.ObjectMapper;
import hospicloud.enumeration.StatutAntecedent;
import hospicloud.model.Antecedent;
import hospicloud.repositories.AntecedentRepository;
import hospicloud.security.TenantContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import java.sql.*;
import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Repository
public class AntecedentRepositoryImpl implements AntecedentRepository {

    private static final Logger logger = LoggerFactory.getLogger(AntecedentRepositoryImpl.class);

    private final JdbcTemplate jdbcTemplate;
    private final JedisPool redisPool;
    private final ObjectMapper objectMapper = new ObjectMapper();

    private static final String CACHE_PREFIX = "hosp:%d:patient:%d:antecedents";
    private static final String DEFAULT_STATUT = "ACTIF";

    @Autowired
    public AntecedentRepositoryImpl(JdbcTemplate jdbcTemplate,
                                    @Autowired(required = false) JedisPool redisPool) {
        this.jdbcTemplate = jdbcTemplate;
        this.redisPool = redisPool;
    }

    private Integer currentTenant() {
        return TenantContext.getRequiredHopitalId();
    }

    private String cacheKey(int idPatient, int idHopital) {
        return String.format(CACHE_PREFIX, idHopital, idPatient);
    }

    private void invalidateCache(int idPatient, int idHopital) {
        if (redisPool == null) return;

        try (Jedis jedis = redisPool.getResource()) {
            jedis.del(cacheKey(idPatient, idHopital));
        } catch (Exception e) {
            logger.error("Erreur Redis invalidation", e);
        }
    }

    // ===================== MAPPING =====================
    private Antecedent mapRow(ResultSet rs, int rowNum) throws SQLException {

        Antecedent a = new Antecedent();

        a.setIdAntecendent(rs.getInt("id_antecedent"));
        a.setIdPatient(rs.getInt("id_patient"));

        Integer hopitalId = TenantContext.getRequiredHopitalId();
        a.setIdHopiatl(hopitalId);

        a.setTypeAntecedent(rs.getString("type_antecedent"));
        a.setLibelle(rs.getString("libelle"));
        a.setDescription(rs.getString("description"));
        a.setEst_critique(rs.getBoolean("est_critique"));

        Date diagDate = rs.getDate("date_diagnostic");
        if (diagDate != null) {
            a.setDateDiagnostic(diagDate.toLocalDate());
        }

        // ✅ ENUM SAFE
        String statutDb = rs.getString("statut");

        a.setStatut(
        	    statutDb != null
        	        ? StatutAntecedent.valueOf(statutDb)
        	        : StatutAntecedent.valueOf(DEFAULT_STATUT)
        	);

        Date enregDate = rs.getDate("date_enregistrement");
        if (enregDate != null) {
            a.setDateEnregistrement(enregDate.toLocalDate());
        }

        return a;
    }

    // ===================== INSERT =====================
    @Override
    @Transactional
    public void enregistrerAntecedent(Antecedent ant) {

        Objects.requireNonNull(ant);

        Integer hopitalId = currentTenant();

        String sql = """
            INSERT INTO antecedents
            (id_patient, id_hopital, type_antecedent, libelle, description,
             est_critique, date_diagnostic, statut, date_enregistrement)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
            """;

        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(conn -> {
            PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);

            ps.setInt(1, ant.getIdPatient());
            ps.setInt(2, hopitalId);
            ps.setString(3, ant.getTypeAntecedent());
            ps.setString(4, ant.getLibelle());
            ps.setString(5, ant.getDescription());
            ps.setBoolean(6, ant.isEst_critique());

            if (ant.getDateDiagnostic() != null) {
                ps.setDate(7, Date.valueOf(ant.getDateDiagnostic()));
            } else {
                ps.setNull(7, Types.DATE);
            }

            ps.setString(
            	    8,
            	    ant.getStatut() != null
            	        ? ant.getStatut().name()
            	        : DEFAULT_STATUT
            	);

            ps.setDate(9,
                    Date.valueOf(
                            ant.getDateEnregistrement() != null
                                    ? ant.getDateEnregistrement()
                                    : LocalDate.now()
                    )
            );

            return ps;

        }, keyHolder);

        if (keyHolder.getKey() != null) {
            ant.setIdAntecendent(keyHolder.getKey().intValue());
        }

        invalidateCache(ant.getIdPatient(), hopitalId);
    }

    // ===================== UPDATE =====================
    @Override
    @Transactional
    public void modifierAntecedent(Antecedent ant) {

        Integer hopitalId = currentTenant();

        String sql = """
            UPDATE antecedents
            SET type_antecedent=?, libelle=?, description=?, est_critique=?, statut=?
            WHERE id_antecedent=? AND id_hopital=?
            """;

        jdbcTemplate.update(sql,
                ant.getTypeAntecedent(),
                ant.getLibelle(),
                ant.getDescription(),
                ant.isEst_critique(),
                ant.getStatut(),
                ant.getIdAntecendent(),
                hopitalId
        );

        invalidateCache(ant.getIdPatient(), hopitalId);
    }

    // ===================== LISTE AVEC REDIS =====================
    @Override
    public List<Antecedent> listerParPatient(int idPatient, int page, int size) {

        Integer hopitalId = currentTenant();
        String key = cacheKey(idPatient, hopitalId);

        // 🔵 CACHE READ
        if (redisPool != null) {
            try (Jedis jedis = redisPool.getResource()) {
                String json = jedis.get(key);
                if (json != null) {
                    return objectMapper.readValue(
                            json,
                            objectMapper.getTypeFactory()
                                    .constructCollectionType(List.class, Antecedent.class)
                    );
                }
            } catch (Exception e) {
                logger.error("Redis read error", e);
            }
        }

        // 🔴 DB
        String sql = """
            SELECT * FROM antecedents
            WHERE id_patient=? AND id_hopital=?
            ORDER BY date_enregistrement DESC
            LIMIT ? OFFSET ?
            """;

        List<Antecedent> result = jdbcTemplate.query(
                sql,
                this::mapRow,
                idPatient,
                hopitalId,
                size,
                page * size
        );

        // 🟢 CACHE WRITE
        if (redisPool != null) {
            try (Jedis jedis = redisPool.getResource()) {
                jedis.setex(key, 300, objectMapper.writeValueAsString(result));
            } catch (Exception e) {
                logger.error("Redis write error", e);
            }
        }

        return result;
    }

    // ===================== DELETE =====================
    @Override
    public void supprimerAntecedent(int idAntecedent) {

        Integer hopitalId = currentTenant();

        jdbcTemplate.update("""
            DELETE FROM antecedents
            WHERE id_antecedent=? AND id_hopital=?
        """, idAntecedent, hopitalId);
    }

    // ===================== STATUT =====================
    @Override
    public void changerStatutAntecedent(int id, String statut) {

        Integer hopitalId = currentTenant();

        jdbcTemplate.update("""
            UPDATE antecedents
            SET statut=?
            WHERE id_antecedent=? AND id_hopital=?
        """, statut, id, hopitalId);
    }

    @Override
    public Optional<Antecedent> trouverParId(int id) {

        Integer hopitalId = currentTenant();

        try {
            return Optional.ofNullable(
                    jdbcTemplate.queryForObject(
                            "SELECT * FROM antecedents WHERE id_antecedent=? AND id_hopital=?",
                            this::mapRow,
                            id,
                            hopitalId
                    )
            );
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

	@Override
	public List<Antecedent> listerParPatient(int idPatient) {
		// TODO Auto-generated method stub
		return listerParPatient(idPatient, 0, 50);
	}

	@Override
	public List<Antecedent> preparerDonneesPourSynthese(
	        int idPatient,
	        List<String> priorityLibelles,
	        int page,
	        int size) {

	    Integer hopitalId = currentTenant();

	    StringBuilder sql = new StringBuilder("""
	        SELECT *
	        FROM antecedents
	        WHERE id_patient = ?
	        AND id_hopital = ?
	    """);

	    Object[] params;

	    // ================= PRIORITY FILTER =================
	    if (priorityLibelles != null && !priorityLibelles.isEmpty()) {

	        sql.append(" AND type_antecedent IN (");

	        for (int i = 0; i < priorityLibelles.size(); i++) {
	            sql.append("?");
	            if (i < priorityLibelles.size() - 1) {
	                sql.append(",");
	            }
	        }

	        sql.append(") ");

	        sql.append("""
	            ORDER BY est_critique DESC, date_enregistrement DESC
	            LIMIT ? OFFSET ?
	        """);

	        params = new Object[2 + priorityLibelles.size() + 2];

	        params[0] = idPatient;
	        params[1] = hopitalId;

	        for (int i = 0; i < priorityLibelles.size(); i++) {
	            params[2 + i] = priorityLibelles.get(i);
	        }

	        params[2 + priorityLibelles.size()] = size;
	        params[3 + priorityLibelles.size()] = page * size;

	    } else {

	        sql.append("""
	            ORDER BY est_critique DESC, date_enregistrement DESC
	            LIMIT ? OFFSET ?
	        """);

	        params = new Object[]{
	                idPatient,
	                hopitalId,
	                size,
	                page * size
	        };
	    }

	    try {
	        return jdbcTemplate.query(sql.toString(), this::mapRow, params);
	    } catch (Exception e) {
	        logger.error("Erreur synthèse patient {}", idPatient, e);
	        return List.of();
	    }
	}
}