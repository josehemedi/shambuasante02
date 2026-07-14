package hospicloud.repositoriesImpl;

import hospicloud.model.Societe;
import hospicloud.repositories.SocieteRepository;
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

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Repository
public class SocieteRepositoryImpl implements SocieteRepository {

    private static final Logger logger = LoggerFactory.getLogger(SocieteRepositoryImpl.class);

    private final JdbcTemplate jdbcTemplate;
    private final JedisPool redisPool;
    private final com.fasterxml.jackson.databind.ObjectMapper objectMapper;
    @Autowired
    public SocieteRepositoryImpl(JdbcTemplate jdbcTemplate, @Autowired(required = false) JedisPool redisPool,com.fasterxml.jackson.databind.ObjectMapper objectMapper) {
        this.jdbcTemplate = jdbcTemplate;
        this.redisPool = redisPool;
        this.objectMapper= objectMapper;
    }

    private void invalidateSocieteCache(Long id, String nom, Integer idHopital) {
        if (redisPool == null) return;
        try (Jedis jedis = redisPool.getResource()) {
            if (id != null) jedis.del("societe:id:" + id);
            if (nom != null) jedis.del("societe:nom:" + nom);
            if (idHopital != null) jedis.del("societe:hopital:" + idHopital + ":list");
            jedis.setex("societe:cache:status", 600, "dirty");
        } catch (Exception e) {
            // ne pas faire planter la logique métier pour un problème de cache
            logger.debug("Impossible d'invalider le cache Redis pour societe id={} nom={} hopital={}", id, nom, idHopital, e);
        }
    }

    private Societe mapRow(ResultSet rs) throws SQLException {

        Societe s = new Societe();

        s.setIdSociete(rs.getLong("id_societe"));
        s.setNomSociete(rs.getString("nom_societe"));
        s.setAdresseFacturation(rs.getString("adresse_facturation"));
        s.setTelephoneContact(rs.getString("telephone_contact"));
        s.setEmailContact(rs.getString("email_contact"));

        Double taux = rs.getObject("taux_couverture", Double.class);
        s.setTauxCouverture(taux);

        s.setIdHopital(rs.getObject("hospital_id", Integer.class));

        try {
            s.setNomHopital(rs.getString("nom_hopital"));
        } catch (Exception ignored) {
        }

        return s;
    } 
    private void cacheSociete(Societe societe, Integer idHopital) {
        if (redisPool == null || societe == null || societe.getIdSociete() == null) return;

        String cacheKey = "hosp:" + idHopital + ":societe:id:" + societe.getIdSociete();

        try (Jedis jedis = redisPool.getResource()) {
            jedis.setex(
                    cacheKey,
                    3600,
                    objectMapper.writeValueAsString(societe)
            );
        } catch (Exception e) {
            logger.warn("Erreur écriture cache societe id={}", societe.getIdSociete(), e);
        }
    }

    @Override
    @Transactional
    public int enregistrerSociete(Societe societe) {
        Objects.requireNonNull(societe, "societe ne peut pas être null");

        // Récupération sécurisée de l'ID hôpital depuis le contexte
        Integer idHopital = TenantContext.getRequiredHopitalId();

        // Validation minimale
        if (societe.getNomSociete() == null || societe.getNomSociete().trim().isEmpty()) {
            logger.warn("Tentative d'enregistrement d'une societe sans nom");
            return 0;
        }

        // Vérifier l'existence par nom dans le même hôpital courant
        Optional<Societe> exist = trouverParNom(societe.getNomSociete());
        if (exist.isPresent()) {
            logger.info("Societe avec le même nom existe déjà pour l'hopital {}: {}", idHopital, societe.getNomSociete());
            return 0;
        }

        final String sql = "INSERT INTO societes (hospital_id, nom_societe, adresse_facturation, telephone_contact, email_contact, taux_couverture) VALUES (?, ?, ?, ?, ?, ?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            // Utilisation de idHopital provenant du TenantContext
            ps.setObject(1, idHopital, Types.INTEGER);
            ps.setString(2, societe.getNomSociete());
            ps.setString(3, societe.getAdresseFacturation());
            ps.setString(4, societe.getTelephoneContact());
            ps.setString(5, societe.getEmailContact());
            if (societe.getTauxCouverture() != null) ps.setDouble(6, societe.getTauxCouverture()); else ps.setNull(6, Types.DOUBLE);
            return ps;
        }, keyHolder);

        Number key = keyHolder.getKey();
        if (key != null) {

            societe.setIdSociete(key.longValue());

            // important
            societe.setIdHopital(idHopital);

            invalidateSocieteCache(
                    societe.getIdSociete(),
                    societe.getNomSociete(),
                    idHopital
            );

            logger.info(
                    "Societe enregistrée avec id={} pour hopital={}",
                    societe.getIdSociete(),
                    idHopital
            );

            return 1;
        }

        logger.warn("Insertion de societe retourné sans clé générée pour nom={}", societe.getNomSociete());
        return 0;
    }
    @Override
    @Transactional
    public int modifierSociete(Societe societe) {

        Objects.requireNonNull(societe);

        if (societe.getIdSociete() == null) {
            return 0;
        }

        Integer idHopital = TenantContext.getRequiredHopitalId();

        if (!existeParId(societe.getIdSociete())) {
            return 0;
        }

        final String sql =
                "UPDATE societes " +
                "SET nom_societe=?, " +
                "adresse_facturation=?, " +
                "telephone_contact=?, " +
                "email_contact=?, " +
                "taux_couverture=? " +
                "WHERE id_societe=? " +
                "AND hospital_id=?";

        int updated = jdbcTemplate.update(
                sql,
                societe.getNomSociete(),
                societe.getAdresseFacturation(),
                societe.getTelephoneContact(),
                societe.getEmailContact(),
                societe.getTauxCouverture(),
                societe.getIdSociete(),
                idHopital
        );

        if (updated > 0) {

            societe.setIdHopital(idHopital);

            invalidateSocieteCache(
                    societe.getIdSociete(),
                    societe.getNomSociete(),
                    idHopital
            );
        }

        return updated;
    }
    
    @Override
    @Transactional
    public int supprimerSociete(Long idSociete) {
        if (idSociete == null) return 0;

        // Récupération sécurisée de l'ID de l'hôpital via le contexte
        Integer idHopital = TenantContext.getRequiredHopitalId();

        // Récupérer les informations pour invalider le cache proprement
        // On utilise la méthode qui prend en compte l'ID du contexte
        Optional<Societe> s = trouverParId(idSociete);
        String nom = s.map(Societe::getNomSociete).orElse(null);

        final String sql = "DELETE FROM societes WHERE id_societe = ? AND hospital_id = ?";
        int del = jdbcTemplate.update(sql, idSociete, idHopital);

        if (del > 0) {
            // Invalidation du cache avec l'ID du contexte
            invalidateSocieteCache(idSociete, nom, idHopital);
            logger.info("Societe id={} supprimée pour l'hôpital {}", idSociete, idHopital);
        } else {
            logger.info("Aucune societe supprimée pour id={} dans l'hôpital {}", idSociete, idHopital);
        }
        return del;
    }
    
    @Override
    public Optional<Societe> trouverParId(Long id) {

        if (id == null) {
            return Optional.empty();
        }

        Integer idHopital = TenantContext.getRequiredHopitalId();

        String cacheKey =
                "hosp:" + idHopital +
                ":societe:id:" + id;

        if (redisPool != null) {

            try (Jedis jedis = redisPool.getResource()) {

                String json = jedis.get(cacheKey);

                if (json != null) {
                    return Optional.of(
                            objectMapper.readValue(
                                    json,
                                    Societe.class
                            )
                    );
                }

            } catch (Exception ignored) {
            }
        }

        String sql =
                "SELECT s.*, h.nom AS nom_hopital " +
                "FROM societes s " +
                "LEFT JOIN hopitaux h " +
                "ON h.id_hopital = s.hospital_id " +
                "WHERE s.id_societe=? " +
                "AND s.hospital_id=?";

        List<Societe> result =
                jdbcTemplate.query(
                        sql,
                        (rs, rowNum) -> mapRow(rs),
                        id,
                        idHopital
                );

        if (result.isEmpty()) {
            return Optional.empty();
        }

        Societe societe = result.get(0);

        cacheSociete(societe, idHopital);

        return Optional.of(societe);
    }    @Override
    public List<Societe> ListerSocietes() {
        // 1. Récupération de l'ID hôpital depuis le contexte sécurisé
        Integer idHopital = TenantContext.getRequiredHopitalId();

        // 2. Ajout de la clause WHERE pour isoler les données au tenant courant
        final String sql = "SELECT y.*, h.nom AS nom_hopital " +
                           "FROM societes y " +
                           "LEFT JOIN hopitaux h ON y.hospital_id = h.id_hopital " +
                           "WHERE y.hospital_id = ? " + 
                           "ORDER BY y.id_societe DESC";

        try {
            // 3. Passage de idHopital comme paramètre pour prévenir les injections SQL
            return jdbcTemplate.query(sql, new Object[]{idHopital}, (rs, rowNum) -> {
                Societe s = mapRow(rs); 
                s.setNomHopital(rs.getString("nom_hopital"));
                return s;
            });
        } catch (Exception e) {
            logger.error("Erreur lors de la lecture de la liste des societes pour l'hopital: " + idHopital, e);
            return new ArrayList<>();
        }
    }
    
    @Override
    public Optional<Societe> trouverParNom(String nomSociete) {

        if (nomSociete == null || nomSociete.isBlank()) {
            return Optional.empty();
        }

        Integer idHopital = TenantContext.getRequiredHopitalId();

        if (idHopital == null) {
            throw new IllegalStateException("Tenant non initialisé");
        }

        String sql =
            "SELECT s.*, h.nom AS nom_hopital " +
            "FROM societes s " +
            "LEFT JOIN hopitaux h ON h.id_hopital = s.hospital_id " +
            "WHERE UPPER(TRIM(s.nom_societe)) = ? " +
            "AND s.hospital_id = ?";

        List<Societe> result = jdbcTemplate.query(
            sql,
            (rs, rowNum) -> mapRow(rs),
            nomSociete.trim().toUpperCase(),
            idHopital
        );

        return result.stream().findFirst();
    }
    
    @Override
    public boolean existeParId(Long id) {
        if (id == null) return false;

        // Récupération sécurisée de l'ID de l'hôpital courant
        Integer idHopital = TenantContext.getRequiredHopitalId();

        // Ajout de la clause WHERE pour isoler la recherche à l'hôpital courant
        final String sql = "SELECT COUNT(1) FROM societes WHERE id_societe = ? AND hospital_id = ?";
        
        try {
            Integer count = jdbcTemplate.queryForObject(sql, Integer.class, id, idHopital);
            return count != null && count > 0;
        } catch (Exception e) {
            logger.error("Erreur lors de la vérification d'existence societe id={} pour l'hopital courant", id, e);
            return false;
        }
    }

    // --- Nouvelles méthodes multi-tenant ---
    @Override
    public List<Societe> listerParHopital() {
        // Récupération sécurisée de l'ID hôpital via le contexte
        Integer idHopital = TenantContext.getRequiredHopitalId();

        final String sql = "SELECT y.*, h.nom AS nom_hopital " +
                           "FROM societes y " +
                           "LEFT JOIN hopitaux h ON y.hospital_id = h.id_hopital " +
                           "WHERE y.hospital_id = ? " +
                           "ORDER BY y.id_societe DESC";
        try {
            return jdbcTemplate.query(sql, (rs, rowNum) -> {
                Societe s = mapRow(rs); 
                s.setNomHopital(rs.getString("nom_hopital")); 
                return s;
            }, idHopital); // Utilisation de l'ID récupéré du contexte
        } catch (Exception e) {
            logger.error("Erreur lors de la lecture des societes pour l'hôpital courant id={}", idHopital, e);
            return new ArrayList<>();
        }
    }

}
