package hospicloud.repositoriesImpl;

import hospicloud.dtos.sortie.PretSortieDTO;
import hospicloud.model.BonSortie;
import hospicloud.repositories.BonSortieRepository;
import hospicloud.security.TenantContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
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
public class BonSortieRepositoryImpl implements BonSortieRepository {

    private final JdbcTemplate jdbcTemplate;

    @Autowired
    public BonSortieRepositoryImpl(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public BonSortie save(BonSortie dischargeNote) {
        String sql = """
            INSERT INTO bons_sortie (id_hopital, id_patient, id_consultation, id_admission, id_ordonnance,
            numero_bon, diagnostic_final, etat_sortie, recommandations_post_hospitalisation,
            statut_paiement_final, autorise_par, statut_workflow)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            """;

        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setInt(1, dischargeNote.getIdHopital());
            ps.setInt(2, dischargeNote.getIdPatient());
            if (dischargeNote.getIdConsultation() != null) {
                ps.setInt(3, dischargeNote.getIdConsultation());
            } else {
                ps.setNull(3, java.sql.Types.INTEGER);
            }
            if (dischargeNote.getIdAdmission() != null) {
                ps.setInt(4, dischargeNote.getIdAdmission());
            } else {
                ps.setNull(4, java.sql.Types.INTEGER);
            }
            if (dischargeNote.getIdOrdonnance() != null) {
                ps.setLong(5, dischargeNote.getIdOrdonnance());
            } else {
                ps.setNull(5, java.sql.Types.BIGINT);
            }
            ps.setString(6, dischargeNote.getNumeroBon());
            ps.setString(7, dischargeNote.getDiagnosticFinal());
            ps.setString(8, dischargeNote.getEtatSortie());
            ps.setString(9, dischargeNote.getRecommandationsPostHospitalisation());
            ps.setInt(10, Boolean.TRUE.equals(dischargeNote.getStatutPaiementFinal()) ? 1 : 0);
            ps.setString(11, dischargeNote.getAutorisePar());
            ps.setString(12, dischargeNote.getStatutWorkflow() != null ? dischargeNote.getStatutWorkflow() : "AUTORISE_MEDICALEMENT");
            return ps;
        }, keyHolder);

        if (keyHolder.getKey() != null) {
            dischargeNote.setIdBonSortie(keyHolder.getKey().intValue());
        }
        return dischargeNote;
    }

    @Override
    public Optional<BonSortie> findById(Integer id) {
        String sql = "SELECT * FROM bons_sortie WHERE id_bon_sortie = ? AND id_hopital = ?";
        try {
            BonSortie note = jdbcTemplate.queryForObject(sql, this::mapRow, id, TenantContext.getRequiredHopitalId());
            return Optional.ofNullable(note);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    @Override
    public List<BonSortie> findByPatientId(Integer patientId) {
        String sql = "SELECT * FROM bons_sortie WHERE id_patient = ? AND id_hopital = ? ORDER BY date_sortie DESC";
        return jdbcTemplate.query(sql, this::mapRow, patientId, TenantContext.getRequiredHopitalId());
    }

    @Override
    public int countDischargeNotesByYear(int year) {
        String sql = "SELECT COUNT(*) FROM bons_sortie WHERE YEAR(date_sortie) = ? AND id_hopital = ?";
        return jdbcTemplate.queryForObject(sql, Integer.class, year, TenantContext.getRequiredHopitalId());
    }

    @Override
    public boolean update(BonSortie dischargeNote) {
        String sql = """
            UPDATE bons_sortie SET diagnostic_final = ?, etat_sortie = ?, statut_paiement_final = ?,
            recommandations_post_hospitalisation = ?, autorise_par = ?, statut_workflow = ?
            WHERE id_bon_sortie = ? AND id_hopital = ?
            """;
        int rows = jdbcTemplate.update(sql,
                dischargeNote.getDiagnosticFinal(),
                dischargeNote.getEtatSortie(),
                Boolean.TRUE.equals(dischargeNote.getStatutPaiementFinal()) ? 1 : 0,
                dischargeNote.getRecommandationsPostHospitalisation(),
                dischargeNote.getAutorisePar(),
                dischargeNote.getStatutWorkflow(),
                dischargeNote.getIdBonSortie(),
                TenantContext.getRequiredHopitalId());
        return rows > 0;
    }

    @Override
    public boolean deleteById(Integer id) {
        String sql = "DELETE FROM bons_sortie WHERE id_bon_sortie = ? AND id_hopital = ?";
        return jdbcTemplate.update(sql, id, TenantContext.getRequiredHopitalId()) > 0;
    }

    @Override
    public Optional<BonSortie> findByReferenceNumber(String referenceNumber) {
        String sql = "SELECT * FROM bons_sortie WHERE numero_bon = ? AND id_hopital = ?";
        try {
            BonSortie note = jdbcTemplate.queryForObject(sql, this::mapRow, referenceNumber, TenantContext.getRequiredHopitalId());
            return Optional.ofNullable(note);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    @Override
    public List<BonSortie> findByAuthorizedBy(String physicianName) {
        String sql = "SELECT * FROM bons_sortie WHERE autorise_par = ? AND id_hopital = ?";
        return jdbcTemplate.query(sql, this::mapRow, physicianName, TenantContext.getRequiredHopitalId());
    }

    @Override
    public List<BonSortie> findByDischargeDateBetween(LocalDateTime startDate, LocalDateTime endDate) {
        String sql = "SELECT * FROM bons_sortie WHERE date_sortie BETWEEN ? AND ? AND id_hopital = ?";
        return jdbcTemplate.query(sql, this::mapRow, startDate, endDate, TenantContext.getRequiredHopitalId());
    }

    @Override
    public List<BonSortie> findByDischargeStatus(String status) {
        String sql = "SELECT * FROM bons_sortie WHERE etat_sortie = ? AND id_hopital = ?";
        return jdbcTemplate.query(sql, this::mapRow, status, TenantContext.getRequiredHopitalId());
    }

    @Override
    public boolean isPaymentSettled(Integer dischargeNoteId) {
        String sql = "SELECT statut_paiement_final FROM bons_sortie WHERE id_bon_sortie = ? AND id_hopital = ?";
        try {
            Boolean isSettled = jdbcTemplate.queryForObject(sql, Boolean.class, dischargeNoteId, TenantContext.getRequiredHopitalId());
            return Boolean.TRUE.equals(isSettled);
        } catch (EmptyResultDataAccessException e) {
            return false;
        }
    }

    @Override
    public boolean existsAutorisationEnCours(Integer patientId) {
        String sql = """
            SELECT COUNT(*) FROM bons_sortie
            WHERE id_patient = ? AND id_hopital = ?
              AND statut_workflow IN ('AUTORISE_MEDICALEMENT', 'EN_ATTENTE_PAIEMENT')
            """;
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, patientId, TenantContext.getRequiredHopitalId());
        return count != null && count > 0;
    }

    @Override
    public List<PretSortieDTO> listPretesPourDelivrance(Integer hopitalId) {
        String sql = """
            SELECT b.id_bon_sortie, b.numero_bon, b.id_patient, b.diagnostic_final, b.etat_sortie,
                   b.autorise_par, b.statut_paiement_final, b.statut_workflow, b.date_sortie,
                   b.recommandations_post_hospitalisation AS recommandations,
                   TRIM(CONCAT(COALESCE(p.prenom, ''), ' ', COALESCE(p.nom, ''))) AS nom_patient
            FROM bons_sortie b
            JOIN patients p ON b.id_patient = p.id_patient AND b.id_hopital = p.id_hopital
            WHERE b.id_hopital = ? AND b.statut_workflow = 'AUTORISE_MEDICALEMENT'
            ORDER BY b.date_sortie ASC
            """;
        return jdbcTemplate.query(sql, (rs, rowNum) -> {
            PretSortieDTO dto = new PretSortieDTO();
            dto.setIdBonSortie(rs.getInt("id_bon_sortie"));
            dto.setNumeroBon(rs.getString("numero_bon"));
            dto.setIdPatient(rs.getInt("id_patient"));
            dto.setNomPatient(rs.getString("nom_patient"));
            dto.setDiagnosticFinal(rs.getString("diagnostic_final"));
            dto.setEtatSortie(rs.getString("etat_sortie"));
            dto.setAutorisePar(rs.getString("autorise_par"));
            dto.setStatutPaiementFinal(rs.getBoolean("statut_paiement_final"));
            dto.setStatutWorkflow(rs.getString("statut_workflow"));
            dto.setRecommandations(rs.getString("recommandations"));
            Timestamp ts = rs.getTimestamp("date_sortie");
            if (ts != null) {
                dto.setDateSortie(ts.toLocalDateTime());
            }
            return dto;
        }, hopitalId);
    }

    @Override
    public boolean finaliserDelivrance(Integer idBonSortie, Integer hopitalId, boolean paiementConfirme, Integer delivrePar) {
        String sql = """
            UPDATE bons_sortie
            SET statut_paiement_final = ?, statut_workflow = 'DELIVRE', delivre_par = ?
            WHERE id_bon_sortie = ? AND id_hopital = ? AND statut_workflow = 'AUTORISE_MEDICALEMENT'
            """;
        return jdbcTemplate.update(sql, paiementConfirme ? 1 : 0, delivrePar, idBonSortie, hopitalId) > 0;
    }

    private BonSortie mapRow(java.sql.ResultSet rs, int rowNum) throws java.sql.SQLException {
        BonSortie note = new BonSortie();
        note.setIdBonSortie(rs.getInt("id_bon_sortie"));
        note.setIdHopital(rs.getInt("id_hopital"));
        note.setIdPatient(rs.getInt("id_patient"));
        int idConsultation = rs.getInt("id_consultation");
        note.setIdConsultation(rs.wasNull() ? null : idConsultation);
        int idAdmission = rs.getInt("id_admission");
        if (!rs.wasNull()) {
            note.setIdAdmission(idAdmission);
        }
        long idOrdonnance = rs.getLong("id_ordonnance");
        if (!rs.wasNull()) {
            note.setIdOrdonnance(idOrdonnance);
        }
        note.setNumeroBon(rs.getString("numero_bon"));
        Timestamp ts = rs.getTimestamp("date_sortie");
        if (ts != null) {
            note.setDateSortie(ts.toLocalDateTime());
        }
        note.setDiagnosticFinal(rs.getString("diagnostic_final"));
        note.setEtatSortie(rs.getString("etat_sortie"));
        note.setRecommandationsPostHospitalisation(rs.getString("recommandations_post_hospitalisation"));
        note.setStatutPaiementFinal(rs.getBoolean("statut_paiement_final"));
        note.setAutorisePar(rs.getString("autorise_par"));
        try {
            note.setStatutWorkflow(rs.getString("statut_workflow"));
        } catch (java.sql.SQLException ignored) {
            note.setStatutWorkflow("AUTORISE_MEDICALEMENT");
        }
        return note;
    }
}
