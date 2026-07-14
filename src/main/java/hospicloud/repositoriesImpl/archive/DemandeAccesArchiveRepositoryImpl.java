package hospicloud.repositoriesImpl.archive;

import hospicloud.model.archive.DemandeAccesArchive;
import hospicloud.model.archive.StatutDemandeAccesArchive;
import hospicloud.repositories.archive.DemandeAccesArchiveRepository;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public class DemandeAccesArchiveRepositoryImpl implements DemandeAccesArchiveRepository {

    private final JdbcTemplate jdbcTemplate;

    public DemandeAccesArchiveRepositoryImpl(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public Long insert(DemandeAccesArchive demande) {
        String sql = """
                INSERT INTO demandes_acces_archive (hopital_id, archive_id, demandeur_id, motif, statut)
                VALUES (?, ?, ?, ?, ?)
                """;
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(con -> {
            PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setInt(1, demande.getHopitalId());
            ps.setLong(2, demande.getArchiveId());
            ps.setInt(3, demande.getDemandeurId());
            ps.setString(4, demande.getMotif());
            ps.setString(5, demande.getStatut().name());
            return ps;
        }, keyHolder);
        Number key = keyHolder.getKey();
        return key != null ? key.longValue() : null;
    }

    @Override
    public Optional<DemandeAccesArchive> findById(Integer hopitalId, Long id) {
        List<DemandeAccesArchive> list = queryList(
                " WHERE d.hopital_id = ? AND d.id = ?", hopitalId, id);
        return list.isEmpty() ? Optional.empty() : Optional.of(list.get(0));
    }

    @Override
    public List<DemandeAccesArchive> findByArchiveId(Integer hopitalId, Long archiveId) {
        return queryList(" WHERE d.hopital_id = ? AND d.archive_id = ? ORDER BY d.date_demande DESC",
                hopitalId, archiveId);
    }

    @Override
    public List<DemandeAccesArchive> findEnAttente(Integer hopitalId) {
        return queryList(" WHERE d.hopital_id = ? AND d.statut = 'EN_ATTENTE' ORDER BY d.date_demande ASC",
                hopitalId);
    }

    @Override
    public boolean updateStatut(Integer hopitalId, Long id, StatutDemandeAccesArchive statut,
                                Integer traitePar, String observation) {
        int rows = jdbcTemplate.update("""
                UPDATE demandes_acces_archive SET
                    statut = ?, traite_par = ?, date_traitement = ?, observation = ?
                WHERE id = ? AND hopital_id = ?
                """,
                statut.name(), traitePar, Timestamp.valueOf(LocalDateTime.now()),
                observation, id, hopitalId);
        return rows > 0;
    }

    private List<DemandeAccesArchive> queryList(String whereClause, Object... params) {
        String sql = """
                SELECT d.*, ud.email AS nom_demandeur, ut.email AS nom_traite_par
                FROM demandes_acces_archive d
                LEFT JOIN utilisateurs ud ON ud.id_utilisateur = d.demandeur_id
                LEFT JOIN utilisateurs ut ON ut.id_utilisateur = d.traite_par
                """ + whereClause;
        return jdbcTemplate.query(sql, (rs, rowNum) -> {
            DemandeAccesArchive d = new DemandeAccesArchive();
            d.setId(rs.getLong("id"));
            d.setHopitalId(rs.getInt("hopital_id"));
            d.setArchiveId(rs.getLong("archive_id"));
            d.setDemandeurId(rs.getInt("demandeur_id"));
            d.setMotif(rs.getString("motif"));
            d.setStatut(StatutDemandeAccesArchive.valueOf(rs.getString("statut")));
            Timestamp ts = rs.getTimestamp("date_demande");
            if (ts != null) d.setDateDemande(ts.toLocalDateTime());
            int traitePar = rs.getInt("traite_par");
            if (!rs.wasNull()) d.setTraitePar(traitePar);
            ts = rs.getTimestamp("date_traitement");
            if (ts != null) d.setDateTraitement(ts.toLocalDateTime());
            d.setObservation(rs.getString("observation"));
            d.setNomDemandeur(rs.getString("nom_demandeur"));
            d.setNomTraitePar(rs.getString("nom_traite_par"));
            return d;
        }, params);
    }
}
