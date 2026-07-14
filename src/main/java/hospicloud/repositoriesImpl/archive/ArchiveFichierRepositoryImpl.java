package hospicloud.repositoriesImpl.archive;

import hospicloud.model.archive.ArchiveFichier;
import hospicloud.repositories.archive.ArchiveFichierRepository;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Repository
public class ArchiveFichierRepositoryImpl implements ArchiveFichierRepository {

    private final JdbcTemplate jdbcTemplate;

    private static final RowMapper<ArchiveFichier> MAPPER = (rs, i) -> {
        ArchiveFichier f = new ArchiveFichier();
        f.setId(rs.getLong("id"));
        f.setHopitalId(rs.getInt("hopital_id"));
        f.setArchiveId(rs.getLong("archive_id"));
        f.setTypeFichier(rs.getString("type_fichier"));
        f.setNomFichier(rs.getString("nom_fichier"));
        f.setCheminStockage(rs.getString("chemin_stockage"));
        f.setMimeType(rs.getString("mime_type"));
        long taille = rs.getLong("taille_octets");
        f.setTailleOctets(rs.wasNull() ? null : taille);
        Timestamp genereAt = rs.getTimestamp("genere_at");
        f.setGenereAt(genereAt != null ? genereAt.toLocalDateTime() : null);
        int generePar = rs.getInt("genere_par");
        f.setGenerePar(rs.wasNull() ? null : generePar);
        Timestamp created = rs.getTimestamp("created_at");
        f.setCreatedAt(created != null ? created.toLocalDateTime() : null);
        Timestamp updated = rs.getTimestamp("updated_at");
        f.setUpdatedAt(updated != null ? updated.toLocalDateTime() : null);
        return f;
    };

    public ArchiveFichierRepositoryImpl(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public Long upsert(ArchiveFichier fichier) {
        Optional<ArchiveFichier> existing = findByArchiveAndType(
                fichier.getHopitalId(), fichier.getArchiveId(), fichier.getTypeFichier());
        if (existing.isPresent()) {
            Long id = existing.get().getId();
            jdbcTemplate.update("""
                    UPDATE archives_fichiers
                       SET nom_fichier = ?,
                           chemin_stockage = ?,
                           mime_type = ?,
                           taille_octets = ?,
                           genere_at = ?,
                           genere_par = ?,
                           updated_at = CURRENT_TIMESTAMP
                     WHERE hopital_id = ? AND id = ?
                    """,
                    fichier.getNomFichier(),
                    fichier.getCheminStockage(),
                    fichier.getMimeType(),
                    fichier.getTailleOctets(),
                    toTs(fichier.getGenereAt()),
                    fichier.getGenerePar(),
                    fichier.getHopitalId(),
                    id);
            return id;
        }

        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(con -> {
            PreparedStatement ps = con.prepareStatement("""
                    INSERT INTO archives_fichiers
                        (hopital_id, archive_id, type_fichier, nom_fichier, chemin_stockage,
                         mime_type, taille_octets, genere_at, genere_par)
                    VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
                    """, Statement.RETURN_GENERATED_KEYS);
            ps.setInt(1, fichier.getHopitalId());
            ps.setLong(2, fichier.getArchiveId());
            ps.setString(3, fichier.getTypeFichier());
            ps.setString(4, fichier.getNomFichier());
            ps.setString(5, fichier.getCheminStockage());
            ps.setString(6, fichier.getMimeType());
            if (fichier.getTailleOctets() != null) {
                ps.setLong(7, fichier.getTailleOctets());
            } else {
                ps.setObject(7, null);
            }
            ps.setTimestamp(8, toTs(fichier.getGenereAt()));
            if (fichier.getGenerePar() != null) {
                ps.setInt(9, fichier.getGenerePar());
            } else {
                ps.setObject(9, null);
            }
            return ps;
        }, keyHolder);
        Number key = keyHolder.getKey();
        return key != null ? key.longValue() : null;
    }

    @Override
    public Optional<ArchiveFichier> findById(Integer hopitalId, Long id) {
        List<ArchiveFichier> rows = jdbcTemplate.query(
                "SELECT * FROM archives_fichiers WHERE hopital_id = ? AND id = ?",
                MAPPER, hopitalId, id);
        return rows.stream().findFirst();
    }

    @Override
    public Optional<ArchiveFichier> findByArchiveAndType(Integer hopitalId, Long archiveId, String typeFichier) {
        List<ArchiveFichier> rows = jdbcTemplate.query(
                """
                SELECT * FROM archives_fichiers
                 WHERE hopital_id = ? AND archive_id = ? AND type_fichier = ?
                """,
                MAPPER, hopitalId, archiveId, typeFichier);
        return rows.stream().findFirst();
    }

    @Override
    public List<ArchiveFichier> findByArchiveId(Integer hopitalId, Long archiveId) {
        return jdbcTemplate.query(
                """
                SELECT * FROM archives_fichiers
                 WHERE hopital_id = ? AND archive_id = ?
                 ORDER BY genere_at DESC, id DESC
                """,
                MAPPER, hopitalId, archiveId);
    }

    @Override
    public List<ArchiveFichier> findByArchiveIds(Integer hopitalId, Collection<Long> archiveIds) {
        if (archiveIds == null || archiveIds.isEmpty()) {
            return Collections.emptyList();
        }
        List<Long> ids = new ArrayList<>(archiveIds);
        String placeholders = String.join(",", Collections.nCopies(ids.size(), "?"));
        List<Object> args = new ArrayList<>();
        args.add(hopitalId);
        args.addAll(ids);
        return jdbcTemplate.query(
                "SELECT * FROM archives_fichiers WHERE hopital_id = ? AND archive_id IN (" + placeholders + ")",
                MAPPER,
                args.toArray());
    }

    @Override
    public boolean deleteById(Integer hopitalId, Long id) {
        int rows = jdbcTemplate.update(
                "DELETE FROM archives_fichiers WHERE hopital_id = ? AND id = ?",
                hopitalId, id);
        return rows > 0;
    }

    private static Timestamp toTs(LocalDateTime value) {
        return value != null ? Timestamp.valueOf(value) : null;
    }
}
