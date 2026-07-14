package hospicloud.repositoriesImpl.archive;

import hospicloud.model.archive.HistoriqueArchivage;
import hospicloud.model.archive.StatutArchive;
import hospicloud.repositories.archive.HistoriqueArchivageRepository;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.List;

@Repository
public class HistoriqueArchivageRepositoryImpl implements HistoriqueArchivageRepository {

    private final JdbcTemplate jdbcTemplate;

    public HistoriqueArchivageRepositoryImpl(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public Long insert(HistoriqueArchivage historique) {
        String sql = """
                INSERT INTO historique_archivage (
                    hopital_id, archive_id, ancien_statut, nouveau_statut, action,
                    motif, observation, effectue_par, adresse_ip, user_agent
                ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                """;
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(con -> {
            PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setInt(1, historique.getHopitalId());
            ps.setLong(2, historique.getArchiveId());
            ps.setString(3, historique.getAncienStatut() != null
                    ? historique.getAncienStatut().name() : null);
            ps.setString(4, historique.getNouveauStatut().name());
            ps.setString(5, historique.getAction());
            ps.setString(6, historique.getMotif());
            ps.setString(7, historique.getObservation());
            if (historique.getEffectuePar() != null) {
                ps.setInt(8, historique.getEffectuePar());
            } else {
                ps.setNull(8, java.sql.Types.INTEGER);
            }
            ps.setString(9, historique.getAdresseIp());
            ps.setString(10, historique.getUserAgent());
            return ps;
        }, keyHolder);
        Number key = keyHolder.getKey();
        return key != null ? key.longValue() : null;
    }

    @Override
    public List<HistoriqueArchivage> findByArchiveId(Integer hopitalId, Long archiveId) {
        String sql = """
                SELECT h.*, u.email AS nom_effectue_par
                FROM historique_archivage h
                LEFT JOIN utilisateurs u ON u.id_utilisateur = h.effectue_par
                WHERE h.hopital_id = ? AND h.archive_id = ?
                ORDER BY h.date_action DESC
                """;
        return jdbcTemplate.query(sql, (rs, rowNum) -> {
            HistoriqueArchivage h = new HistoriqueArchivage();
            h.setId(rs.getLong("id"));
            h.setHopitalId(rs.getInt("hopital_id"));
            h.setArchiveId(rs.getLong("archive_id"));
            String ancien = rs.getString("ancien_statut");
            if (ancien != null) h.setAncienStatut(StatutArchive.valueOf(ancien));
            h.setNouveauStatut(StatutArchive.valueOf(rs.getString("nouveau_statut")));
            h.setAction(rs.getString("action"));
            h.setMotif(rs.getString("motif"));
            h.setObservation(rs.getString("observation"));
            int effectuePar = rs.getInt("effectue_par");
            if (!rs.wasNull()) h.setEffectuePar(effectuePar);
            Timestamp ts = rs.getTimestamp("date_action");
            if (ts != null) h.setDateAction(ts.toLocalDateTime());
            h.setAdresseIp(rs.getString("adresse_ip"));
            h.setUserAgent(rs.getString("user_agent"));
            h.setNomEffectuePar(rs.getString("nom_effectue_par"));
            return h;
        }, hopitalId, archiveId);
    }
}
