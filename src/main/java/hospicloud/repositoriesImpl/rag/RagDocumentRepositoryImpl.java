package hospicloud.repositoriesImpl.rag;

import hospicloud.model.rag.RagDocument;
import hospicloud.repositories.rag.RagDocumentRepository;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
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
public class RagDocumentRepositoryImpl implements RagDocumentRepository {

    private final JdbcTemplate jdbcTemplate;

    private static final RowMapper<RagDocument> MAPPER = (rs, rowNum) -> {
        RagDocument d = new RagDocument();
        d.setId(rs.getLong("id"));
        int hop = rs.getInt("hopital_id");
        d.setHopitalId(rs.wasNull() ? null : hop);
        d.setCategorie(rs.getString("categorie"));
        d.setTitre(rs.getString("titre"));
        d.setContenu(rs.getString("contenu"));
        d.setVersionLabel(rs.getString("version_label"));
        d.setStatut(rs.getString("statut"));
        d.setAudience(rs.getString("audience"));
        d.setTags(rs.getString("tags"));
        Timestamp exp = rs.getTimestamp("expire_at");
        d.setExpireAt(exp != null ? exp.toLocalDateTime() : null);
        d.setCreatedBy(rs.getObject("created_by") != null ? rs.getInt("created_by") : null);
        d.setUpdatedBy(rs.getObject("updated_by") != null ? rs.getInt("updated_by") : null);
        Timestamp c = rs.getTimestamp("created_at");
        Timestamp u = rs.getTimestamp("updated_at");
        d.setCreatedAt(c != null ? c.toLocalDateTime() : null);
        d.setUpdatedAt(u != null ? u.toLocalDateTime() : null);
        return d;
    };

    public RagDocumentRepositoryImpl(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public List<RagDocument> listForAudience(Integer hopitalId, String audience, boolean includeExpired) {
        String sql = """
                SELECT * FROM rag_documents
                WHERE statut = 'ACTIF'
                  AND (audience = ? OR audience = 'ALL')
                  AND (hopital_id IS NULL OR hopital_id = ?)
                ORDER BY hopital_id IS NULL, updated_at DESC
                LIMIT 40
                """;
        List<RagDocument> docs = jdbcTemplate.query(sql, MAPPER, audience, hopitalId);
        if (includeExpired) {
            return docs;
        }
        LocalDateTime now = LocalDateTime.now();
        return docs.stream()
                .filter(d -> d.getExpireAt() == null || d.getExpireAt().isAfter(now))
                .toList();
    }

    @Override
    public List<RagDocument> listByHopital(Integer hopitalId) {
        if (hopitalId == null) {
            return jdbcTemplate.query(
                    "SELECT * FROM rag_documents WHERE hopital_id IS NULL ORDER BY updated_at DESC LIMIT 200",
                    MAPPER);
        }
        return jdbcTemplate.query(
                """
                SELECT * FROM rag_documents
                WHERE hopital_id = ? OR hopital_id IS NULL
                ORDER BY hopital_id IS NULL, updated_at DESC
                LIMIT 200
                """,
                MAPPER,
                hopitalId);
    }

    @Override
    public Optional<RagDocument> findById(Long id) {
        List<RagDocument> list = jdbcTemplate.query("SELECT * FROM rag_documents WHERE id = ?", MAPPER, id);
        return list.stream().findFirst();
    }

    @Override
    public Long insert(RagDocument doc) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(con -> {
            PreparedStatement ps = con.prepareStatement("""
                    INSERT INTO rag_documents
                    (hopital_id, categorie, titre, contenu, version_label, statut, audience, tags, expire_at, created_by, updated_by)
                    VALUES (?,?,?,?,?,?,?,?,?,?,?)
                    """, Statement.RETURN_GENERATED_KEYS);
            if (doc.getHopitalId() == null) ps.setObject(1, null);
            else ps.setInt(1, doc.getHopitalId());
            ps.setString(2, doc.getCategorie());
            ps.setString(3, doc.getTitre());
            ps.setString(4, doc.getContenu());
            ps.setString(5, doc.getVersionLabel() != null ? doc.getVersionLabel() : "1.0");
            ps.setString(6, doc.getStatut() != null ? doc.getStatut() : "ACTIF");
            ps.setString(7, doc.getAudience() != null ? doc.getAudience() : "MEDECIN");
            ps.setString(8, doc.getTags());
            if (doc.getExpireAt() == null) ps.setObject(9, null);
            else ps.setTimestamp(9, Timestamp.valueOf(doc.getExpireAt()));
            if (doc.getCreatedBy() == null) ps.setObject(10, null);
            else ps.setInt(10, doc.getCreatedBy());
            if (doc.getUpdatedBy() == null) ps.setObject(11, null);
            else ps.setInt(11, doc.getUpdatedBy());
            return ps;
        }, keyHolder);
        Number key = keyHolder.getKey();
        return key != null ? key.longValue() : null;
    }

    @Override
    public void update(RagDocument doc) {
        jdbcTemplate.update("""
                UPDATE rag_documents SET
                  categorie=?, titre=?, contenu=?, version_label=?, statut=?, audience=?, tags=?, expire_at=?, updated_by=?
                WHERE id=? AND (hopital_id = ? OR (? IS NULL AND hopital_id IS NULL))
                """,
                doc.getCategorie(),
                doc.getTitre(),
                doc.getContenu(),
                doc.getVersionLabel(),
                doc.getStatut(),
                doc.getAudience(),
                doc.getTags(),
                doc.getExpireAt() != null ? Timestamp.valueOf(doc.getExpireAt()) : null,
                doc.getUpdatedBy(),
                doc.getId(),
                doc.getHopitalId(),
                doc.getHopitalId());
    }

    @Override
    public void delete(Long id, Integer hopitalId) {
        if (hopitalId == null) {
            jdbcTemplate.update("DELETE FROM rag_documents WHERE id = ? AND hopital_id IS NULL", id);
        } else {
            jdbcTemplate.update("DELETE FROM rag_documents WHERE id = ? AND hopital_id = ?", id, hopitalId);
        }
    }
}
