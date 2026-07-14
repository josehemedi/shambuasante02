package hospicloud.repositoriesImpl;

import hospicloud.model.SignatureDocument;
import hospicloud.model.enums.StatutSignature;
import hospicloud.model.enums.TypeDocument;
import hospicloud.repositories.SignatureDocumentRepository;
import hospicloud.security.TenantContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public class SignatureDocumentRepositoryImpl implements SignatureDocumentRepository {

    private static final Logger logger = LoggerFactory.getLogger(SignatureDocumentRepositoryImpl.class);

    private final JdbcTemplate jdbcTemplate;
    private volatile boolean schemaEnsured = false;

    public SignatureDocumentRepositoryImpl(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void ensureSchema() {
        jdbcTemplate.execute("""
            CREATE TABLE IF NOT EXISTS signatures_documents (
                id BIGINT AUTO_INCREMENT PRIMARY KEY,
                id_hopital INT NOT NULL,
                document_id BIGINT NOT NULL,
                type_document VARCHAR(50) NOT NULL,
                medecin_id INT NOT NULL,
                utilisateur_id BIGINT NOT NULL,
                nom_medecin VARCHAR(255) NOT NULL,
                image_signature LONGTEXT NULL,
                hash_document VARCHAR(64) NOT NULL,
                adresse_ip VARCHAR(45) NULL,
                methode_authentification VARCHAR(50) NOT NULL,
                date_signature DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
                reference_signature VARCHAR(64) NOT NULL,
                statut ENUM('SIGNE', 'ANNULE', 'INVALIDE') NOT NULL DEFAULT 'SIGNE',
                KEY idx_sig_doc (id_hopital, type_document, document_id),
                UNIQUE KEY uk_sig_reference (reference_signature)
            ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4
            """);

        addColumnIfMissing("signatures_documents", "id_hopital", "INT NOT NULL DEFAULT 0");
        addColumnIfMissing("signatures_documents", "type_document", "VARCHAR(50) NOT NULL DEFAULT 'CONSULTATION'");
        addColumnIfMissing("signatures_documents", "medecin_id", "INT NOT NULL DEFAULT 0");
        addColumnIfMissing("signatures_documents", "nom_medecin", "VARCHAR(255) NOT NULL DEFAULT ''");
        addColumnIfMissing("signatures_documents", "reference_signature", "VARCHAR(64) NOT NULL DEFAULT ''");
        schemaEnsured = true;
    }

    private void ensureSchemaOnce() {
        if (!schemaEnsured) {
            synchronized (this) {
                if (!schemaEnsured) {
                    ensureSchema();
                }
            }
        }
    }

    private void addColumnIfMissing(String table, String column, String definition) {
        Integer count = jdbcTemplate.queryForObject(
                """
                SELECT COUNT(*) FROM information_schema.columns
                WHERE table_schema = DATABASE() AND table_name = ? AND column_name = ?
                """,
                Integer.class,
                table,
                column
        );
        if (count != null && count == 0) {
            jdbcTemplate.execute("ALTER TABLE " + table + " ADD COLUMN " + column + " " + definition);
            logger.info("Colonne {}.{} ajoutée automatiquement.", table, column);
        }
    }

    @Override
    @Transactional
    public SignatureDocument save(SignatureDocument signature) {
        ensureSchemaOnce();
        Integer hopitalId = TenantContext.getRequiredHopitalId();
        signature.setHopitalId(hopitalId);

        String sql = """
            INSERT INTO signatures_documents (
                id_hopital, document_id, type_document, medecin_id, utilisateur_id, nom_medecin,
                hash_document, adresse_ip, methode_authentification, date_signature,
                reference_signature, statut
            ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            """;

        LocalDateTime signedAt = signature.getDateSignature() != null
                ? signature.getDateSignature()
                : LocalDateTime.now();
        signature.setDateSignature(signedAt);

        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setInt(1, hopitalId);
            ps.setLong(2, signature.getDocumentId());
            ps.setString(3, signature.getTypeDocument().name());
            ps.setInt(4, signature.getMedecinId());
            ps.setLong(5, signature.getUtilisateurId());
            ps.setString(6, signature.getNomMedecin());
            ps.setString(7, signature.getHashDocument());
            ps.setString(8, signature.getAdresseIp());
            ps.setString(9, signature.getMethodeAuthentification());
            ps.setTimestamp(10, Timestamp.valueOf(signedAt));
            ps.setString(11, signature.getReferenceSignature());
            ps.setString(12, signature.getStatut().toDbValue());
            return ps;
        }, keyHolder);

        if (keyHolder.getKey() != null) {
            signature.setId(keyHolder.getKey().longValue());
        }
        return signature;
    }

    @Override
    public Optional<SignatureDocument> findActiveByDocument(TypeDocument typeDocument, Long documentId, Integer hopitalId) {
        ensureSchemaOnce();
        String sql = """
            SELECT * FROM signatures_documents
            WHERE id_hopital = ? AND type_document = ? AND document_id = ? AND statut = 'SIGNE'
            ORDER BY date_signature DESC
            LIMIT 1
            """;
        List<SignatureDocument> list = jdbcTemplate.query(
                sql,
                this::mapRow,
                hopitalId,
                typeDocument.name(),
                documentId
        );
        return list.stream().findFirst();
    }

    private SignatureDocument mapRow(ResultSet rs, int rowNum) throws SQLException {
        SignatureDocument s = new SignatureDocument();
        s.setId(rs.getLong("id"));
        s.setHopitalId(rs.getInt("id_hopital"));
        s.setDocumentId(rs.getLong("document_id"));
        s.setTypeDocument(TypeDocument.fromDb(rs.getString("type_document")));
        s.setMedecinId(rs.getInt("medecin_id"));
        s.setUtilisateurId(rs.getInt("utilisateur_id"));
        s.setNomMedecin(rs.getString("nom_medecin"));
        s.setHashDocument(rs.getString("hash_document"));
        s.setAdresseIp(rs.getString("adresse_ip"));
        s.setMethodeAuthentification(rs.getString("methode_authentification"));
        if (rs.getTimestamp("date_signature") != null) {
            s.setDateSignature(rs.getTimestamp("date_signature").toLocalDateTime());
        }
        s.setReferenceSignature(rs.getString("reference_signature"));
        s.setStatut(StatutSignature.fromDb(rs.getString("statut")));
        return s;
    }
}
