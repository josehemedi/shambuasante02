package hospicloud.repositoriesImpl.archive;

import hospicloud.model.archive.ArchiveDossierVirtuel;
import hospicloud.repositories.archive.ArchiveDossierVirtuelRepository;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.List;
import java.util.Optional;

@Repository
public class ArchiveDossierVirtuelRepositoryImpl implements ArchiveDossierVirtuelRepository {

    private final JdbcTemplate jdbcTemplate;

    private final RowMapper<ArchiveDossierVirtuel> rowMapper = (rs, rowNum) -> {
        ArchiveDossierVirtuel f = new ArchiveDossierVirtuel();
        f.setId(rs.getLong("id"));
        f.setHopitalId(rs.getInt("hopital_id"));
        long parent = rs.getLong("parent_id");
        if (!rs.wasNull()) f.setParentId(parent);
        f.setNom(rs.getString("nom"));
        int createdBy = rs.getInt("created_by");
        if (!rs.wasNull()) f.setCreatedBy(createdBy);
        Timestamp ts = rs.getTimestamp("created_at");
        if (ts != null) f.setCreatedAt(ts.toLocalDateTime());
        ts = rs.getTimestamp("updated_at");
        if (ts != null) f.setUpdatedAt(ts.toLocalDateTime());
        try {
            f.setEnfantsCount(rs.getInt("enfants_count"));
        } catch (Exception ignored) {
            f.setEnfantsCount(0);
        }
        try {
            f.setDossiersCount(rs.getInt("dossiers_count"));
        } catch (Exception ignored) {
            f.setDossiersCount(0);
        }
        return f;
    };

    public ArchiveDossierVirtuelRepositoryImpl(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public Long insert(ArchiveDossierVirtuel folder) {
        String sql = """
                INSERT INTO archives_dossiers_virtuels (hopital_id, parent_id, nom, created_by)
                VALUES (?, ?, ?, ?)
                """;
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(con -> {
            PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setInt(1, folder.getHopitalId());
            if (folder.getParentId() != null) ps.setLong(2, folder.getParentId());
            else ps.setNull(2, Types.BIGINT);
            ps.setString(3, folder.getNom());
            if (folder.getCreatedBy() != null) ps.setInt(4, folder.getCreatedBy());
            else ps.setNull(4, Types.INTEGER);
            return ps;
        }, keyHolder);
        Number key = keyHolder.getKey();
        return key != null ? key.longValue() : null;
    }

    @Override
    public boolean updateNom(Integer hopitalId, Long id, String nom) {
        return jdbcTemplate.update(
                "UPDATE archives_dossiers_virtuels SET nom = ? WHERE hopital_id = ? AND id = ?",
                nom, hopitalId, id) > 0;
    }

    @Override
    public boolean updateParent(Integer hopitalId, Long id, Long parentId) {
        return jdbcTemplate.update(
                "UPDATE archives_dossiers_virtuels SET parent_id = ? WHERE hopital_id = ? AND id = ?",
                parentId, hopitalId, id) > 0;
    }

    @Override
    public boolean deleteIfEmpty(Integer hopitalId, Long id) {
        if (countChildren(hopitalId, id) > 0 || countDossiers(hopitalId, id) > 0) {
            return false;
        }
        return jdbcTemplate.update(
                "DELETE FROM archives_dossiers_virtuels WHERE hopital_id = ? AND id = ?",
                hopitalId, id) > 0;
    }

    @Override
    public Optional<ArchiveDossierVirtuel> findById(Integer hopitalId, Long id) {
        List<ArchiveDossierVirtuel> list = jdbcTemplate.query(
                """
                SELECT f.*,
                       (SELECT COUNT(*) FROM archives_dossiers_virtuels c
                        WHERE c.hopital_id = f.hopital_id AND c.parent_id = f.id) AS enfants_count,
                       (SELECT COUNT(*) FROM archives_dossiers a
                        WHERE a.hopital_id = f.hopital_id AND a.dossier_virtuel_id = f.id) AS dossiers_count
                FROM archives_dossiers_virtuels f
                WHERE f.hopital_id = ? AND f.id = ?
                """,
                rowMapper, hopitalId, id);
        return list.stream().findFirst();
    }

    @Override
    public List<ArchiveDossierVirtuel> listChildren(Integer hopitalId, Long parentId) {
        if (parentId == null) {
            return jdbcTemplate.query(
                    """
                    SELECT f.*,
                           (SELECT COUNT(*) FROM archives_dossiers_virtuels c
                            WHERE c.hopital_id = f.hopital_id AND c.parent_id = f.id) AS enfants_count,
                           (SELECT COUNT(*) FROM archives_dossiers a
                            WHERE a.hopital_id = f.hopital_id AND a.dossier_virtuel_id = f.id) AS dossiers_count
                    FROM archives_dossiers_virtuels f
                    WHERE f.hopital_id = ? AND f.parent_id IS NULL
                    ORDER BY f.nom ASC
                    """,
                    rowMapper, hopitalId);
        }
        return jdbcTemplate.query(
                """
                SELECT f.*,
                       (SELECT COUNT(*) FROM archives_dossiers_virtuels c
                        WHERE c.hopital_id = f.hopital_id AND c.parent_id = f.id) AS enfants_count,
                       (SELECT COUNT(*) FROM archives_dossiers a
                        WHERE a.hopital_id = f.hopital_id AND a.dossier_virtuel_id = f.id) AS dossiers_count
                FROM archives_dossiers_virtuels f
                WHERE f.hopital_id = ? AND f.parent_id = ?
                ORDER BY f.nom ASC
                """,
                rowMapper, hopitalId, parentId);
    }

    @Override
    public List<ArchiveDossierVirtuel> listAll(Integer hopitalId) {
        return jdbcTemplate.query(
                """
                SELECT f.*,
                       (SELECT COUNT(*) FROM archives_dossiers_virtuels c
                        WHERE c.hopital_id = f.hopital_id AND c.parent_id = f.id) AS enfants_count,
                       (SELECT COUNT(*) FROM archives_dossiers a
                        WHERE a.hopital_id = f.hopital_id AND a.dossier_virtuel_id = f.id) AS dossiers_count
                FROM archives_dossiers_virtuels f
                WHERE f.hopital_id = ?
                ORDER BY f.nom ASC
                """,
                rowMapper, hopitalId);
    }

    @Override
    public boolean existsByNom(Integer hopitalId, Long parentId, String nom, Long excludeId) {
        Integer count;
        if (parentId == null) {
            count = jdbcTemplate.queryForObject(
                    """
                    SELECT COUNT(*) FROM archives_dossiers_virtuels
                    WHERE hopital_id = ? AND parent_id IS NULL AND LOWER(nom) = LOWER(?)
                      AND (? IS NULL OR id <> ?)
                    """,
                    Integer.class, hopitalId, nom, excludeId, excludeId);
        } else {
            count = jdbcTemplate.queryForObject(
                    """
                    SELECT COUNT(*) FROM archives_dossiers_virtuels
                    WHERE hopital_id = ? AND parent_id = ? AND LOWER(nom) = LOWER(?)
                      AND (? IS NULL OR id <> ?)
                    """,
                    Integer.class, hopitalId, parentId, nom, excludeId, excludeId);
        }
        return count != null && count > 0;
    }

    @Override
    public boolean isDescendantOf(Integer hopitalId, Long folderId, Long potentialAncestorId) {
        if (folderId == null || potentialAncestorId == null) return false;
        Long current = folderId;
        int guard = 0;
        while (current != null && guard++ < 64) {
            if (current.equals(potentialAncestorId)) return true;
            Optional<ArchiveDossierVirtuel> opt = findById(hopitalId, current);
            if (opt.isEmpty()) return false;
            current = opt.get().getParentId();
        }
        return false;
    }

    @Override
    public int countChildren(Integer hopitalId, Long folderId) {
        Integer c = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM archives_dossiers_virtuels WHERE hopital_id = ? AND parent_id = ?",
                Integer.class, hopitalId, folderId);
        return c != null ? c : 0;
    }

    @Override
    public int countDossiers(Integer hopitalId, Long folderId) {
        Integer c = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM archives_dossiers WHERE hopital_id = ? AND dossier_virtuel_id = ?",
                Integer.class, hopitalId, folderId);
        return c != null ? c : 0;
    }
}
