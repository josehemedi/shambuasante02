package hospicloud.repositoriesImpl;

import java.sql.*;
import java.util.*;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import hospicloud.model.HoraireTravail;
import hospicloud.repositories.HoraireTravailRepository;
import hospicloud.security.TenantContext;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

@Repository
public class HoraireTravailRepositoryImpl implements HoraireTravailRepository {

    private final JdbcTemplate jdbcTemplate;
    private final JedisPool redisPool;
    private final ObjectMapper objectMapper;

    private static final int CACHE_TTL = 1800;

    // ================= ROW MAPPER FIX =================
    private final RowMapper<HoraireTravail> rowMapper = (rs, rowNum) -> {

        HoraireTravail h = new HoraireTravail();

        h.setId(rs.getLong("id"));
        h.setHopitalId(rs.getObject("hopital_id", Integer.class));
        h.setMedecinId(rs.getObject("medecin_id", Integer.class));
        h.setJourSemaine(rs.getString("jour_semaine"));

        Time debut = rs.getTime("heure_debut");
        Time fin = rs.getTime("heure_fin");

        h.setHeureDebut(debut != null ? debut.toLocalTime() : null);
        h.setHeureFin(fin != null ? fin.toLocalTime() : null);

        h.setPasConsultation(rs.getObject("pas_consultation", Integer.class));

        return h;
    };

    @Autowired
    public HoraireTravailRepositoryImpl(
            JdbcTemplate jdbcTemplate,
            @Autowired(required = false) JedisPool redisPool,
            @Autowired(required = false) ObjectMapper objectMapper
    ) {
        this.jdbcTemplate = jdbcTemplate;
        this.redisPool = redisPool;
        this.objectMapper = objectMapper != null ? objectMapper : new ObjectMapper();
    }

    // =====================================================
    // CACHE CLEAN
    // =====================================================
    private void evictCache(Integer hopitalId, Integer medecinId) {

        if (redisPool == null || hopitalId == null || medecinId == null) return;

        try (Jedis jedis = redisPool.getResource()) {

            String pattern = "hopital:" + hopitalId + ":medecin:" + medecinId + ":jour:*";
            Set<String> keys = jedis.keys(pattern);

            if (keys != null && !keys.isEmpty()) {
                jedis.del(keys.toArray(new String[0]));
            }

        } catch (Exception e) {
            System.err.println("Redis eviction error: " + e.getMessage());
        }
    }

    // =====================================================
    // CREATE
    // =====================================================
    @Override
    public HoraireTravail enregistrer(HoraireTravail h) {

        Integer hopitalId = TenantContext.getRequiredHopitalId();

        if (h == null) {
            throw new IllegalArgumentException("Horaire null");
        }

        String sql =
                "INSERT INTO horaire_travaille " +
                "(hopital_id, medecin_id, jour_semaine, heure_debut, heure_fin, pas_consultation) " +
                "VALUES (?, ?, ?, ?, ?, ?)";

        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {

            PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);

            ps.setInt(1, hopitalId);
            ps.setInt(2, h.getMedecinId());
            ps.setString(3, h.getJourSemaine());

            ps.setTime(4, h.getHeureDebut() != null ? Time.valueOf(h.getHeureDebut()) : null);
            ps.setTime(5, h.getHeureFin() != null ? Time.valueOf(h.getHeureFin()) : null);

            ps.setObject(6, h.getPasConsultation());

            return ps;

        }, keyHolder);

        h.setId(keyHolder.getKey().longValue());

        // ✔ SaaS SAFE : backend only
        h.setHopitalId(hopitalId);

        evictCache(hopitalId, h.getMedecinId());

        return h;
    }

    // =====================================================
    // UPDATE
    // =====================================================
    @Override
    public int modifier(HoraireTravail h) {

        Integer hopitalId = TenantContext.getRequiredHopitalId();

        String sql =
                "UPDATE horaire_travaille SET medecin_id=?, jour_semaine=?, heure_debut=?, heure_fin=?, pas_consultation=? " +
                "WHERE id=? AND hopital_id=?";

        int res = jdbcTemplate.update(sql,
                h.getMedecinId(),
                h.getJourSemaine(),
                h.getHeureDebut() != null ? Time.valueOf(h.getHeureDebut()) : null,
                h.getHeureFin() != null ? Time.valueOf(h.getHeureFin()) : null,
                h.getPasConsultation(),
                h.getId(),
                hopitalId
        );

        if (res > 0) {
            evictCache(hopitalId, h.getMedecinId());
        }

        return res;
    }

    // =====================================================
    // DELETE
    // =====================================================
    @Override
    public int supprimerParId(Long id) {

        Integer hopitalId = TenantContext.getRequiredHopitalId();

        Optional<HoraireTravail> existing = trouverParId(id);

        int res = jdbcTemplate.update(
                "DELETE FROM horaire_travaille WHERE id=? AND hopital_id=?",
                id,
                hopitalId
        );

        existing.ifPresent(h ->
                evictCache(hopitalId, h.getMedecinId())
        );

        return res;
    }

    // =====================================================
    // FIND BY ID
    // =====================================================
    @Override
    public Optional<HoraireTravail> trouverParId(Long id) {

        Integer hopitalId = TenantContext.getRequiredHopitalId();

        try {
            return Optional.ofNullable(
                    jdbcTemplate.queryForObject(
                            "SELECT * FROM horaire_travaille WHERE id=? AND hopital_id=?",
                            rowMapper,
                            id,
                            hopitalId
                    )
            );
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    // =====================================================
    // FIND BY MEDECIN
    // =====================================================
    @Override
    public List<HoraireTravail> trouverParMedecinId(Integer medecinId) {

        Integer hopitalId = TenantContext.getRequiredHopitalId();

        String sql =
                "SELECT * FROM horaire_travaille WHERE medecin_id=? AND hopital_id=? " +
                "ORDER BY FIELD(jour_semaine,'Lundi','Mardi','Mercredi','Jeudi','Vendredi','Samedi','Dimanche'), heure_debut";

        return jdbcTemplate.query(sql, rowMapper, medecinId, hopitalId);
    }

    // =====================================================
    // FIND BY MEDECIN + JOUR (CACHE REDIS)
    // =====================================================
    @Override
    public List<HoraireTravail> trouverParMedecinIdEtJour(Integer medecinId, String jourSemaine) {

        Integer hopitalId = TenantContext.getRequiredHopitalId();

        if (medecinId == null || jourSemaine == null) return Collections.emptyList();

        String key = "hopital:" + hopitalId + ":medecin:" + medecinId + ":jour:" + jourSemaine.toLowerCase();

        // CACHE READ
        if (redisPool != null) {
            try (Jedis jedis = redisPool.getResource()) {

                String cached = jedis.get(key);

                if (cached != null) {
                    return objectMapper.readValue(
                            cached,
                            new TypeReference<List<HoraireTravail>>() {}
                    );
                }
            } catch (Exception ignored) {}
        }

        // DB
        List<HoraireTravail> result = jdbcTemplate.query(
                "SELECT * FROM horaire_travaille WHERE hopital_id=? AND medecin_id=? AND jour_semaine=? ORDER BY heure_debut",
                rowMapper,
                hopitalId,
                medecinId,
                jourSemaine
        );

        // CACHE WRITE
        if (!result.isEmpty() && redisPool != null) {
            try (Jedis jedis = redisPool.getResource()) {
                jedis.setex(key, CACHE_TTL, objectMapper.writeValueAsString(result));
            } catch (Exception ignored) {}
        }

        return result;
    }
}