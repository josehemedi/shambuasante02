package hospicloud.repositoriesImpl;

import hospicloud.model.Facture;
import hospicloud.repositories.FactureRepository;
import hospicloud.security.TenantContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public class FactureRepositoryImpl implements FactureRepository {

    private final JdbcTemplate jdbcTemplate;
    private final JedisPool redisPool;
    private static final Logger logger = LoggerFactory.getLogger(FactureRepositoryImpl.class);

    public FactureRepositoryImpl(JdbcTemplate jdbcTemplate,
                                 @Autowired(required = false) JedisPool redisPool) {
        this.jdbcTemplate = jdbcTemplate;
        this.redisPool = redisPool;
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public Facture save(Facture facture) {
        Integer hopitalId = TenantContext.getRequiredHopitalId();
        facture.setIdHopital(hopitalId);

        String sql = "INSERT INTO factures (id_hopital, id_patient, numero_facture, date_facture, " +
                "montant_total_ht, tva, montant_total_ttc, statut_paiement, id_caissier) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update((Connection con) -> {
            PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setInt(1, hopitalId);
            ps.setInt(2, facture.getIdPatient());
            ps.setString(3, facture.getNumeroFacture());
            ps.setTimestamp(4, Timestamp.valueOf(facture.getDateFacture() != null ? facture.getDateFacture() : LocalDateTime.now()));
            ps.setBigDecimal(5, facture.getMontantTotalHt());
            ps.setBigDecimal(6, facture.getTva());
            ps.setBigDecimal(7, facture.getMontantTotalTtc());
            ps.setString(8, facture.getStatutPaiement());
            ps.setInt(9, facture.getIdCaissier() != null ? facture.getIdCaissier() : null);
            return ps;
        }, keyHolder);

        if (keyHolder.getKey() != null) {
            facture.setIdFacture(keyHolder.getKey().intValue());
        }

        invalidateFactureCache(hopitalId, facture.getIdFacture());
        return facture;
    }

    @Override
    public Optional<Facture> findById(Integer id) {
        Integer hopitalId = TenantContext.getRequiredHopitalId();
        String sql = "SELECT * FROM factures WHERE id_facture = ? AND id_hopital = ?";
        try {
            Facture facture = jdbcTemplate.queryForObject(sql, this::mapRowToFacture, id, hopitalId);
            return Optional.ofNullable(facture);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    @Override
    public Optional<Facture> findByNumeroFacture(String numeroFacture) {
        Integer hopitalId = TenantContext.getRequiredHopitalId();
        String sql = "SELECT * FROM factures WHERE numero_facture = ? AND id_hopital = ?";
        try {
            Facture facture = jdbcTemplate.queryForObject(sql, this::mapRowToFacture, numeroFacture, hopitalId);
            return Optional.ofNullable(facture);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    @Override
    public List<Facture> findByIdHopital(Integer idHopital) {
        Integer tenantId = TenantContext.getRequiredHopitalId();
        String sql = "SELECT * FROM factures WHERE id_hopital = ? ORDER BY date_facture DESC";
        return jdbcTemplate.query(sql, this::mapRowToFacture, tenantId);
    }

    @Override
    public List<Facture> findByIdPatient(Integer idPatient) {
        Integer hopitalId = TenantContext.getRequiredHopitalId();
        String sql = "SELECT * FROM factures WHERE id_patient = ? AND id_hopital = ? ORDER BY date_facture DESC";
        return jdbcTemplate.query(sql, this::mapRowToFacture, idPatient, hopitalId);
    }

    @Override
    public List<Facture> findByStatutPaiement(String statutPaiement) {
        Integer hopitalId = TenantContext.getRequiredHopitalId();
        String sql = "SELECT * FROM factures WHERE statut_paiement = ? AND id_hopital = ? ORDER BY date_facture DESC";
        return jdbcTemplate.query(sql, this::mapRowToFacture, statutPaiement, hopitalId);
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public boolean updateStatutPaiement(Integer factureId, String nouveauStatut) {
        Integer hopitalId = TenantContext.getRequiredHopitalId();
        String sql = "UPDATE factures SET statut_paiement = ? WHERE id_facture = ? AND id_hopital = ?";
        int rows = jdbcTemplate.update(sql, nouveauStatut, factureId, hopitalId);
        if (rows > 0) {
            invalidateFactureCache(hopitalId, factureId);
        }
        return rows > 0;
    }

    @Override
    public long getNextSequenceValue(String seqName) {
        return jdbcTemplate.execute((Connection con) -> {
            try (PreparedStatement ps = con.prepareStatement("SELECT next_val FROM sequences WHERE seq_name = ? FOR UPDATE")) {
                ps.setString(1, seqName);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        long current = rs.getLong(1);
                        long next = current + 1;
                        try (PreparedStatement ups = con.prepareStatement("UPDATE sequences SET next_val = ? WHERE seq_name = ?")) {
                            ups.setLong(1, next);
                            ups.setString(2, seqName);
                            ups.executeUpdate();
                        }
                        return current;
                    } else {
                        try (PreparedStatement ins = con.prepareStatement("INSERT INTO sequences (seq_name, next_val) VALUES (?, ?)")) {
                            ins.setString(1, seqName);
                            ins.setLong(2, 2);
                            ins.executeUpdate();
                        }
                        return 1L;
                    }
                }
            }
        });
    }

    private void invalidateFactureCache(Integer idHopital, Integer idFacture) {
        if (redisPool == null || idHopital == null || idFacture == null) return;
        try (Jedis jedis = redisPool.getResource()) {
            jedis.del("hopital:" + idHopital + ":facture:id:" + idFacture);
            jedis.del("hopital:" + idHopital + ":factures");
        } catch (Exception e) {
            logger.error("Erreur invalidation cache facture", e);
        }
    }

    private Facture mapRowToFacture(ResultSet rs, int rowNum) throws SQLException {
        Facture f = new Facture();
        f.setIdFacture(rs.getInt("id_facture"));
        f.setIdHopital(rs.getObject("id_hopital", Integer.class));
        f.setIdPatient(rs.getInt("id_patient"));
        f.setNumeroFacture(rs.getString("numero_facture"));
        f.setDateFacture(rs.getTimestamp("date_facture") != null ?
                rs.getTimestamp("date_facture").toLocalDateTime() : null);
        f.setMontantTotalHt(rs.getBigDecimal("montant_total_ht"));
        f.setTva(rs.getBigDecimal("tva"));
        f.setMontantTotalTtc(rs.getBigDecimal("montant_total_ttc"));
        f.setStatutPaiement(rs.getString("statut_paiement"));
        f.setIdCaissier(rs.getObject("id_caissier", Integer.class));
        return f;
    }
}