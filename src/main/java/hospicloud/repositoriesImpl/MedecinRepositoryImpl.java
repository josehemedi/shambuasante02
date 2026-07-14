package hospicloud.repositoriesImpl;

import hospicloud.dtos.StatistiqueMedecinDTO;
import hospicloud.model.Medecin;
import hospicloud.repositories.MedecinRepository;
import hospicloud.security.TenantContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.type.TypeReference;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

@Repository
public class MedecinRepositoryImpl implements MedecinRepository {

    private final JdbcTemplate jdbcTemplate;
    private final JedisPool redisPool;
    private final ObjectMapper objectMapper;

    private static final int CACHE_TTL = 1800; // 30 min
    private static final Logger logger = LoggerFactory.getLogger(MedecinRepositoryImpl.class);

    public MedecinRepositoryImpl(JdbcTemplate jdbcTemplate,
                                  @Autowired(required = false) JedisPool redisPool,
                                  ObjectMapper objectMapper) {
        this.jdbcTemplate = jdbcTemplate;
        this.redisPool = redisPool;
        this.objectMapper = objectMapper != null ? objectMapper : new ObjectMapper();
    }

private long getFromCacheOrCompute(String key, int ttl, Supplier<Long> dbOperation) {
    if (redisPool != null) {
        try (Jedis jedis = redisPool.getResource()) {
            String cached = jedis.get(key);
            if (cached != null) return Long.parseLong(cached);
        } catch (Exception e) {
            logger.error("Erreur lecture cache Redis pour clé {}", key, e);
        }
    }

    long value = dbOperation.get();

    if (redisPool != null) {
        try (Jedis jedis = redisPool.getResource()) {
            jedis.setex(key, ttl, String.valueOf(value));
        } catch (Exception e) {
            logger.error("Erreur écriture cache Redis pour clé {}", key, e);
        }
    }
    return value;
}

    // =========================
    // CREATE
    // =========================
    @Override
    public void creer(Medecin m) {
        creerEtRetournerId(m);
    }

    @Override
    public Integer creerEtRetournerId(Medecin m) {
        Integer hopitalId = m.getIdHopital() != null
                ? m.getIdHopital()
                : TenantContext.getRequiredHopitalId();

        String sql = """
            INSERT INTO medecin
            (id_hopital, nom, prenom, email, specialite, numero_ordre, telephone_pro, disponibilite_status)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?)
        """;

        org.springframework.jdbc.support.KeyHolder keyHolder =
                new org.springframework.jdbc.support.GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            var ps = connection.prepareStatement(sql, java.sql.Statement.RETURN_GENERATED_KEYS);
            ps.setInt(1, hopitalId);
            ps.setString(2, m.getNom());
            ps.setString(3, m.getPrenom());
            ps.setString(4, m.getEmail());
            ps.setString(5, m.getSpecialite());
            ps.setString(6, m.getNumeroOrdre());
            ps.setString(7, m.getTelephonePro());
            ps.setObject(8, m.getDisponibiliteStatus() != null ? m.getDisponibiliteStatus() : Boolean.TRUE);
            return ps;
        }, keyHolder);

        Number key = keyHolder.getKey();
        Integer id = key != null ? key.intValue() : null;
        if (id != null) {
            m.setIdMedecin(id);
            m.setIdHopital(hopitalId);
        }
        invalidateCache();
        return id;
    }

    // =========================
    // FIND BY ID (CACHE)
    // =========================
    @Override
    public Optional<Medecin> trouverParId(Integer idMedecin) {

        Integer hopitalId = TenantContext.getRequiredHopitalId();
        String key = "hopital:" + hopitalId + ":medecin:id:" + idMedecin;

        // 🔥 CACHE READ
        if (redisPool != null) {
            try (Jedis jedis = redisPool.getResource()) {
                String cached = jedis.get(key);
                if (cached != null) {
                    return Optional.of(objectMapper.readValue(cached, Medecin.class));
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        String sql = """
            SELECT * FROM medecin
            WHERE id_medecin = ?
            AND id_hopital = ?
        """;

        List<Medecin> list = jdbcTemplate.query(sql,
                new Object[]{idMedecin, hopitalId},
                (rs, rowNum) -> {

                    Medecin m = new Medecin();
                    m.setIdMedecin(rs.getInt("id_medecin"));
                    m.setIdHopital(rs.getInt("id_hopital"));
                    m.setNom(rs.getString("nom"));
                    m.setPrenom(rs.getString("prenom"));
                    m.setEmail(rs.getString("email"));
                    m.setSpecialite(rs.getString("specialite"));
                    m.setNumeroOrdre(rs.getString("numero_ordre"));
                    m.setTelephonePro(rs.getString("telephone_pro"));
                    m.setDisponibiliteStatus(rs.getBoolean("disponibilite_status"));
                    return m;
                });

        Optional<Medecin> result = list.stream().findFirst();

        // 🔥 CACHE WRITE
        result.ifPresent(this::saveToCache);

        return result;
    }

    @Override
    public Optional<Medecin> trouverParEmail(String email) {
        Integer hopitalId = TenantContext.getRequiredHopitalId();

        String sql = """
            SELECT * FROM medecin
            WHERE LOWER(TRIM(email)) = LOWER(TRIM(?))
            AND id_hopital = ?
            LIMIT 1
        """;

        List<Medecin> list = jdbcTemplate.query(sql,
                new Object[]{email, hopitalId},
                (rs, rowNum) -> {
                    Medecin m = new Medecin();
                    m.setIdMedecin(rs.getInt("id_medecin"));
                    m.setIdHopital(rs.getInt("id_hopital"));
                    m.setNom(rs.getString("nom"));
                    m.setPrenom(rs.getString("prenom"));
                    m.setEmail(rs.getString("email"));
                    m.setSpecialite(rs.getString("specialite"));
                    m.setNumeroOrdre(rs.getString("numero_ordre"));
                    m.setTelephonePro(rs.getString("telephone_pro"));
                    m.setDisponibiliteStatus(rs.getBoolean("disponibilite_status"));
                    return m;
                });

        return list.stream().findFirst();
    }

    // =========================
    // LIST BY HOPITAL (CACHE)
    // =========================
    @Override
    public List<Medecin> listerParHopital(Integer idHopital) {

        String key = "hopital:" + idHopital + ":medecins";

        if (redisPool != null) {
            try (Jedis jedis = redisPool.getResource()) {
                String cached = jedis.get(key);
                if (cached != null) {
                    return objectMapper.readValue(
                            cached,
                            new TypeReference<List<Medecin>>() {}
                    );
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        String sql = """
            SELECT * FROM medecin
            WHERE id_hopital = ?
        """;

        List<Medecin> list = jdbcTemplate.query(sql,
                new Object[]{idHopital},
                (rs, rowNum) -> {

                    Medecin m = new Medecin();
                    m.setIdMedecin(rs.getInt("id_medecin"));
                    m.setIdHopital(rs.getInt("id_hopital"));
                    m.setNom(rs.getString("nom"));
                    m.setPrenom(rs.getString("prenom"));
                    m.setEmail(rs.getString("email"));
                    m.setSpecialite(rs.getString("specialite"));
                    m.setNumeroOrdre(rs.getString("numero_ordre"));
                    m.setTelephonePro(rs.getString("telephone_pro"));
                    m.setDisponibiliteStatus(rs.getBoolean("disponibilite_status"));
                    return m;
                });

        saveListToCache(idHopital, list);

        return list;
    }

    // =========================
    // UPDATE
    // =========================
    @Override
    public void mettreAJour(Medecin m) {

        String sql = """
            UPDATE medecin
            SET nom = ?, prenom = ?, email = ?, specialite = ?,
                numero_ordre = ?, telephone_pro = ?, disponibilite_status = ?
            WHERE id_medecin = ?
            AND id_hopital = ?
        """;

        jdbcTemplate.update(sql,
                m.getNom(),
                m.getPrenom(),
                m.getEmail(),
                m.getSpecialite(),
                m.getNumeroOrdre(),
                m.getTelephonePro(),
                m.getDisponibiliteStatus(),
                m.getIdMedecin(),
                TenantContext.getRequiredHopitalId()
        );

        invalidateCache();
    }

    // =========================
    // CHANGE STATUS
    // =========================
    @Override
    public void changerDisponibilite(Integer idMedecin, Boolean status) {

        String sql = """
            UPDATE medecin
            SET disponibilite_status = ?
            WHERE id_medecin = ?
            AND id_hopital = ?
        """;

        jdbcTemplate.update(sql,
                status,
                idMedecin,
                TenantContext.getRequiredHopitalId()
        );

        invalidateCache();
    }

    // =========================
    // CACHE HELPERS
    // =========================

    private void saveToCache(Medecin m) {
        if (redisPool == null || m == null) return;

        Integer hopitalId = TenantContext.getRequiredHopitalId();
        String key = "hopital:" + hopitalId + ":medecin:id:" + m.getIdMedecin();

        try (Jedis jedis = redisPool.getResource()) {
            jedis.setex(key, CACHE_TTL, objectMapper.writeValueAsString(m));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void saveListToCache(Integer hopitalId, List<Medecin> list) {
        if (redisPool == null) return;

        String key = "hopital:" + hopitalId + ":medecins";

        try (Jedis jedis = redisPool.getResource()) {
            jedis.setex(key, CACHE_TTL, objectMapper.writeValueAsString(list));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void invalidateCache() {

        Integer hopitalId = TenantContext.getRequiredHopitalId();

        if (redisPool == null) return;

        try (Jedis jedis = redisPool.getResource()) {

            jedis.del("hopital:" + hopitalId + ":medecins");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

	@Override
    public long getNombrePatients(Integer medecinId, Integer hopitalId) {
        String key = "stat:patients:" + hopitalId + ":" + medecinId;

        return getFromCacheOrCompute(key, CACHE_TTL, () -> {
            String sql = "SELECT SUM(nombre_nouveaux_patients) FROM statistiques_frequentation WHERE id_hopital = ? AND id_medecin = ?";
            Long result = jdbcTemplate.queryForObject(sql, Long.class, hopitalId, medecinId);
            return result != null ? result : 0L;
        });
    }

@Override
    public long getConsultationsAujourdhui(Integer medecinId, Integer hopitalId) {
        String key = "stat:consult:" + hopitalId + ":" + medecinId;

        return getFromCacheOrCompute(key, 300, () -> {
            String sql = "SELECT SUM(nombre_consultations) FROM statistiques_frequentation " +
                     "WHERE id_hopital = ? AND id_medecin = ? AND date_stat = CURRENT_DATE";
            Long result = jdbcTemplate.queryForObject(sql, Long.class, hopitalId, medecinId);
            return result != null ? result : 0L;
        });
    }
    @Override
    public long getHospitalisationsEncours(Integer medecinId, Integer hopitalId) {
        String key = "stat:hosp:" + hopitalId + ":" + medecinId;

        return getFromCacheOrCompute(key, 300, () -> {
            String sql = "SELECT SUM(nombre_hospitalisations) FROM statistiques_frequentation WHERE id_hopital = ? AND id_medecin = ? AND date_stat = CURRENT_DATE";
            Long result = jdbcTemplate.queryForObject(sql, Long.class, hopitalId, medecinId);
            return result != null ? result : 0L;
        });
    }

    @Override
    public long getRendezVousAujourdhui(Integer medecinId, Integer hopitalId) {
        String key = "stat:rdv:" + hopitalId + ":" + medecinId;

        return getFromCacheOrCompute(key, 300, () -> {
            String sql = "SELECT COUNT(*) FROM rendez_vous01 WHERE id_hopital = ? AND id_medecin = ? AND DATE(date_heure_rdv) = CURRENT_DATE";
            Long count = jdbcTemplate.queryForObject(sql, Long.class, hopitalId, medecinId);
            return count != null ? count : 0L;
        });
    }

    @Override
    public long getExamensEnAttente(Integer medecinId, Integer hopitalId) {
        String key = "stat:exam:" + hopitalId + ":" + medecinId;

        return getFromCacheOrCompute(key, 300, () -> {
            String sql = "SELECT COUNT(*) FROM analyses_laboratoire " +
                    "WHERE id_medecin = ? AND id_hopital = ? " +
                    "AND statut = 'EN_ATTENTE'";
            Long count = jdbcTemplate.queryForObject(sql, Long.class, medecinId, hopitalId);
            return count != null ? count : 0L;
        });
    }

    @Override
    public long getNotificationsNonLues(Integer medecinId, Integer hopitalId) {
        String key = "stat:notif:" + hopitalId + ":" + medecinId;

        return getFromCacheOrCompute(key, 300, () -> {
            String sql = "SELECT COUNT(*) FROM notifications " +
                    "WHERE id_hopital = ? " +
                    "AND id_medecin = ? " +
                    "AND est_lu = 0";
            Long count = jdbcTemplate.queryForObject(sql, Long.class, hopitalId, medecinId);
            return count != null ? count : 0L;
        });
    }
    
    @Override
    public StatistiqueMedecinDTO getDashboardStats(Integer medecinId, Integer hopitalId) {
        return new StatistiqueMedecinDTO(
            getConsultationsAujourdhui(medecinId, hopitalId),
            getHospitalisationsEncours(medecinId, hopitalId),
            getNombrePatients(medecinId, hopitalId),
            getRendezVousAujourdhui(medecinId, hopitalId),
            getExamensEnAttente(medecinId, hopitalId),
            getNotificationsNonLues(medecinId, hopitalId)
        );
    }
}