package hospicloud.repositoriesImpl.archive;

import hospicloud.dtos.archive.ArchiveSearchFilter;
import hospicloud.dtos.archive.ArchiveStatistiquesDto;
import hospicloud.model.archive.ArchiveDossier;
import hospicloud.model.archive.ReglesArchivageHopital;
import hospicloud.model.archive.StatutArchive;
import hospicloud.model.archive.TypeEpisode;
import hospicloud.repositories.archive.ArchiveDossierRepository;
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
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Repository
public class ArchiveDossierRepositoryImpl implements ArchiveDossierRepository {

    private static final Set<String> SORT_COLUMNS = Set.of(
            "date_fin_episode", "date_archivage", "created_at", "statut_archive", "patient_id");

    private final JdbcTemplate jdbcTemplate;

    private static final String BASE_SELECT = """
            SELECT a.*,
                   TRIM(CONCAT(COALESCE(p.prenom, ''), ' ', COALESCE(p.nom, ''))) AS nom_patient,
                   CONCAT('PT-', p.id_patient) AS numero_dossier,
                   TRIM(CONCAT(COALESCE(m.prenom, ''), ' ', COALESCE(m.nom, ''))) AS nom_medecin,
                   ua.email AS nom_archiviste,
                   uv.email AS nom_verificateur
            FROM archives_dossiers a
            INNER JOIN patients p ON p.id_patient = a.patient_id AND p.id_hopital = a.hopital_id
            LEFT JOIN medecin m ON m.id_medecin = a.id_medecin
            LEFT JOIN utilisateurs ua ON ua.id_utilisateur = a.archive_par
            LEFT JOIN utilisateurs uv ON uv.id_utilisateur = a.verifie_par
            """;

    private final RowMapper<ArchiveDossier> rowMapper = (rs, rowNum) -> {
        ArchiveDossier a = new ArchiveDossier();
        a.setId(rs.getLong("id"));
        a.setHopitalId(rs.getInt("hopital_id"));
        a.setPatientId(rs.getLong("patient_id"));
        a.setTypeEpisode(TypeEpisode.valueOf(rs.getString("type_episode")));
        a.setEpisodeId(rs.getLong("episode_id"));
        a.setStatutArchive(StatutArchive.valueOf(rs.getString("statut_archive")));
        Timestamp ts = rs.getTimestamp("date_fin_episode");
        if (ts != null) a.setDateFinEpisode(ts.toLocalDateTime());
        ts = rs.getTimestamp("date_demande_archivage");
        if (ts != null) a.setDateDemandeArchivage(ts.toLocalDateTime());
        ts = rs.getTimestamp("date_archivage");
        if (ts != null) a.setDateArchivage(ts.toLocalDateTime());
        int archivePar = rs.getInt("archive_par");
        if (!rs.wasNull()) a.setArchivePar(archivePar);
        int verifiePar = rs.getInt("verifie_par");
        if (!rs.wasNull()) a.setVerifiePar(verifiePar);
        a.setMotifArchivage(rs.getString("motif_archivage"));
        a.setObservation(rs.getString("observation"));
        a.setDossierComplet(rs.getBoolean("dossier_complet"));
        a.setEmplacementPhysique(rs.getString("emplacement_physique"));
        a.setNumeroBoiteArchive(rs.getString("numero_boite_archive"));
        a.setNumeroRayon(rs.getString("numero_rayon"));
        ts = rs.getTimestamp("date_restauration");
        if (ts != null) a.setDateRestauration(ts.toLocalDateTime());
        int restaurePar = rs.getInt("restaure_par");
        if (!rs.wasNull()) a.setRestaurePar(restaurePar);
        a.setMotifRestauration(rs.getString("motif_restauration"));
        a.setVersion(rs.getInt("version"));
        int idMedecin = rs.getInt("id_medecin");
        if (!rs.wasNull()) a.setIdMedecin(idMedecin);
        int idService = rs.getInt("id_service");
        if (!rs.wasNull()) a.setIdService(idService);
        ts = rs.getTimestamp("created_at");
        if (ts != null) a.setCreatedAt(ts.toLocalDateTime());
        ts = rs.getTimestamp("updated_at");
        if (ts != null) a.setUpdatedAt(ts.toLocalDateTime());
        a.setNomPatient(rs.getString("nom_patient"));
        a.setNumeroDossier(rs.getString("numero_dossier"));
        a.setNomMedecin(rs.getString("nom_medecin"));
        a.setNomArchiviste(rs.getString("nom_archiviste"));
        a.setNomVerificateur(rs.getString("nom_verificateur"));
        return a;
    };

    public ArchiveDossierRepositoryImpl(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public Long insert(ArchiveDossier archive) {
        String sql = """
                INSERT INTO archives_dossiers (
                    hopital_id, patient_id, type_episode, episode_id, statut_archive,
                    date_fin_episode, date_demande_archivage, dossier_complet,
                    id_medecin, id_service, observation, version
                ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                """;
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(con -> {
            PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setInt(1, archive.getHopitalId());
            ps.setLong(2, archive.getPatientId());
            ps.setString(3, archive.getTypeEpisode().name());
            ps.setLong(4, archive.getEpisodeId());
            ps.setString(5, archive.getStatutArchive().name());
            ps.setTimestamp(6, archive.getDateFinEpisode() != null
                    ? Timestamp.valueOf(archive.getDateFinEpisode()) : null);
            ps.setTimestamp(7, archive.getDateDemandeArchivage() != null
                    ? Timestamp.valueOf(archive.getDateDemandeArchivage()) : null);
            ps.setBoolean(8, archive.isDossierComplet());
            if (archive.getIdMedecin() != null) ps.setInt(9, archive.getIdMedecin());
            else ps.setNull(9, java.sql.Types.INTEGER);
            if (archive.getIdService() != null) ps.setInt(10, archive.getIdService());
            else ps.setNull(10, java.sql.Types.INTEGER);
            ps.setString(11, archive.getObservation());
            ps.setInt(12, archive.getVersion());
            return ps;
        }, keyHolder);
        Number key = keyHolder.getKey();
        return key != null ? key.longValue() : null;
    }

    @Override
    public boolean updateStatut(ArchiveDossier archive) {
        String sql = """
                UPDATE archives_dossiers SET
                    statut_archive = ?,
                    date_demande_archivage = ?,
                    date_archivage = ?,
                    archive_par = ?,
                    verifie_par = ?,
                    motif_archivage = ?,
                    observation = ?,
                    dossier_complet = ?,
                    emplacement_physique = ?,
                    numero_boite_archive = ?,
                    numero_rayon = ?,
                    date_restauration = ?,
                    restaure_par = ?,
                    motif_restauration = ?,
                    version = version + 1
                WHERE id = ? AND hopital_id = ? AND version = ?
                """;
        int rows = jdbcTemplate.update(sql,
                archive.getStatutArchive().name(),
                archive.getDateDemandeArchivage() != null
                        ? Timestamp.valueOf(archive.getDateDemandeArchivage()) : null,
                archive.getDateArchivage() != null
                        ? Timestamp.valueOf(archive.getDateArchivage()) : null,
                archive.getArchivePar(),
                archive.getVerifiePar(),
                archive.getMotifArchivage(),
                archive.getObservation(),
                archive.isDossierComplet(),
                archive.getEmplacementPhysique(),
                archive.getNumeroBoiteArchive(),
                archive.getNumeroRayon(),
                archive.getDateRestauration() != null
                        ? Timestamp.valueOf(archive.getDateRestauration()) : null,
                archive.getRestaurePar(),
                archive.getMotifRestauration(),
                archive.getId(),
                archive.getHopitalId(),
                archive.getVersion());
        return rows > 0;
    }

    @Override
    public Optional<ArchiveDossier> findById(Integer hopitalId, Long id) {
        String sql = BASE_SELECT + " WHERE a.hopital_id = ? AND a.id = ?";
        List<ArchiveDossier> list = jdbcTemplate.query(sql, rowMapper, hopitalId, id);
        return list.isEmpty() ? Optional.empty() : Optional.of(list.get(0));
    }

    @Override
    public Optional<ArchiveDossier> findByEpisode(Integer hopitalId, TypeEpisode typeEpisode, Long episodeId) {
        String sql = BASE_SELECT + " WHERE a.hopital_id = ? AND a.type_episode = ? AND a.episode_id = ?";
        List<ArchiveDossier> list = jdbcTemplate.query(sql, rowMapper,
                hopitalId, typeEpisode.name(), episodeId);
        return list.isEmpty() ? Optional.empty() : Optional.of(list.get(0));
    }

    @Override
    public List<ArchiveDossier> search(Integer hopitalId, ArchiveSearchFilter filter) {
        StringBuilder sql = new StringBuilder(BASE_SELECT).append(" WHERE a.hopital_id = ?");
        List<Object> params = new ArrayList<>();
        params.add(hopitalId);
        appendFilters(sql, params, filter);
        sql.append(" ORDER BY a.").append(resolveSortColumn(filter.getSort()));
        sql.append(" ").append("ASC".equalsIgnoreCase(filter.getDirection()) ? "ASC" : "DESC");
        sql.append(" LIMIT ? OFFSET ?");
        int size = Math.min(Math.max(filter.getSize(), 1), 100);
        int page = Math.max(filter.getPage(), 0);
        params.add(size);
        params.add(page * size);
        return jdbcTemplate.query(sql.toString(), rowMapper, params.toArray());
    }

    @Override
    public long count(Integer hopitalId, ArchiveSearchFilter filter) {
        StringBuilder sql = new StringBuilder("""
                SELECT COUNT(1) FROM archives_dossiers a
                INNER JOIN patients p ON p.id_patient = a.patient_id AND p.id_hopital = a.hopital_id
                WHERE a.hopital_id = ?
                """);
        List<Object> params = new ArrayList<>();
        params.add(hopitalId);
        appendFilters(sql, params, filter);
        Long total = jdbcTemplate.queryForObject(sql.toString(), Long.class, params.toArray());
        return total != null ? total : 0L;
    }

    @Override
    public ArchiveStatistiquesDto computeStatistiques(Integer hopitalId) {
        ArchiveStatistiquesDto stats = new ArchiveStatistiquesDto();
        stats.setAVerifier(countByStatut(hopitalId, StatutArchive.A_VERIFIER));
        stats.setIncomplets(countByStatut(hopitalId, StatutArchive.INCOMPLET));
        stats.setPretAArchiver(countByStatut(hopitalId, StatutArchive.PRET_A_ARCHIVER));
        stats.setArchives(countByStatut(hopitalId, StatutArchive.ARCHIVE));

        Long today = jdbcTemplate.queryForObject("""
                SELECT COUNT(1) FROM archives_dossiers
                WHERE hopital_id = ? AND statut_archive = 'ARCHIVE'
                  AND DATE(date_archivage) = CURDATE()
                """, Long.class, hopitalId);
        stats.setArchivesAujourdhui(today != null ? today : 0L);

        Long month = jdbcTemplate.queryForObject("""
                SELECT COUNT(1) FROM archives_dossiers
                WHERE hopital_id = ? AND statut_archive = 'ARCHIVE'
                  AND YEAR(date_archivage) = YEAR(CURDATE())
                  AND MONTH(date_archivage) = MONTH(CURDATE())
                """, Long.class, hopitalId);
        stats.setArchivesCeMois(month != null ? month : 0L);

        Double avgDays = jdbcTemplate.queryForObject("""
                SELECT AVG(TIMESTAMPDIFF(DAY, date_fin_episode, date_archivage))
                FROM archives_dossiers
                WHERE hopital_id = ? AND statut_archive = 'ARCHIVE'
                  AND date_fin_episode IS NOT NULL AND date_archivage IS NOT NULL
                """, Double.class, hopitalId);
        stats.setTempsMoyenAvantArchivageJours(avgDays);

        return stats;
    }

    @Override
    public ReglesArchivageHopital findOrCreateRegles(Integer hopitalId) {
        List<ReglesArchivageHopital> existing = jdbcTemplate.query("""
                SELECT id, hopital_id, exiger_cloture_medicale,
                       exiger_cloture_administrative, exiger_cloture_financiere
                FROM regles_archivage_hopital WHERE hopital_id = ?
                """, (rs, rowNum) -> {
            ReglesArchivageHopital r = new ReglesArchivageHopital();
            r.setId(rs.getInt("id"));
            r.setHopitalId(rs.getInt("hopital_id"));
            r.setExigerClotureMedicale(rs.getBoolean("exiger_cloture_medicale"));
            r.setExigerClotureAdministrative(rs.getBoolean("exiger_cloture_administrative"));
            r.setExigerClotureFinanciere(rs.getBoolean("exiger_cloture_financiere"));
            return r;
        }, hopitalId);

        if (!existing.isEmpty()) {
            return existing.get(0);
        }

        jdbcTemplate.update("""
                INSERT INTO regles_archivage_hopital (hopital_id) VALUES (?)
                """, hopitalId);
        return findOrCreateRegles(hopitalId);
    }

    @Override
    public boolean updateRegles(ReglesArchivageHopital regles) {
        int rows = jdbcTemplate.update("""
                UPDATE regles_archivage_hopital SET
                    exiger_cloture_medicale = ?,
                    exiger_cloture_administrative = ?,
                    exiger_cloture_financiere = ?
                WHERE hopital_id = ?
                """,
                regles.isExigerClotureMedicale(),
                regles.isExigerClotureAdministrative(),
                regles.isExigerClotureFinanciere(),
                regles.getHopitalId());
        return rows > 0;
    }

    private long countByStatut(Integer hopitalId, StatutArchive statut) {
        Long count = jdbcTemplate.queryForObject(
                "SELECT COUNT(1) FROM archives_dossiers WHERE hopital_id = ? AND statut_archive = ?",
                Long.class, hopitalId, statut.name());
        return count != null ? count : 0L;
    }

    private void appendFilters(StringBuilder sql, List<Object> params, ArchiveSearchFilter filter) {
        if (filter.getStatut() != null) {
            sql.append(" AND a.statut_archive = ?");
            params.add(filter.getStatut().name());
        }
        if (filter.getTypeEpisode() != null) {
            sql.append(" AND a.type_episode = ?");
            params.add(filter.getTypeEpisode().name());
        }
        if (filter.getPatientId() != null) {
            sql.append(" AND a.patient_id = ?");
            params.add(filter.getPatientId());
        }
        if (filter.getIdMedecin() != null) {
            sql.append(" AND a.id_medecin = ?");
            params.add(filter.getIdMedecin());
        }
        if (filter.getIdService() != null) {
            sql.append(" AND a.id_service = ?");
            params.add(filter.getIdService());
        }
        if (filter.getDateFrom() != null) {
            sql.append(" AND a.date_fin_episode >= ?");
            params.add(Timestamp.valueOf(filter.getDateFrom()));
        }
        if (filter.getDateTo() != null) {
            sql.append(" AND a.date_fin_episode <= ?");
            params.add(Timestamp.valueOf(filter.getDateTo()));
        }
        if (filter.getSearch() != null && !filter.getSearch().isBlank()) {
            sql.append(" AND (p.nom LIKE ? OR p.prenom LIKE ? OR CAST(p.id_patient AS CHAR) LIKE ?)");
            String pattern = "%" + filter.getSearch().trim() + "%";
            params.add(pattern);
            params.add(pattern);
            params.add(pattern);
        }
    }

    private String resolveSortColumn(String sort) {
        if (sort != null && SORT_COLUMNS.contains(sort)) {
            return sort;
        }
        return "date_fin_episode";
    }
}
