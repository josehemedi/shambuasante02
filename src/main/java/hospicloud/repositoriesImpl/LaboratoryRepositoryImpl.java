package hospicloud.repositoriesImpl;

import hospicloud.dtos.LaboratoryKpisDTO;
import hospicloud.dtos.LaboratoryTestItemDTO;
import hospicloud.dtos.MedecinDemandeAnalyseResponseDTO;
import hospicloud.model.AnalyseLaboratoire;
import hospicloud.repositories.LaboratoryRepository;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Repository
public class LaboratoryRepositoryImpl implements LaboratoryRepository {

    private static final Map<String, String> TEST_CODE_LABELS = Map.of(
            "CBC", "Hémogramme (NFS)",
            "LIPID", "Bilan lipidique",
            "TSH", "TSH (Hormone thyréostimulante)",
            "GLU", "Glycémie plasmatique",
            "UA", "Analyse d'urine",
            "BMP", "Bilan métabolique de base"
    );

    private final JdbcTemplate jdbcTemplate;

    public LaboratoryRepositoryImpl(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public LaboratoryKpisDTO getKpis(Integer idHopital) {
        String sql = """
                SELECT
                  COUNT(1) AS total,
                  SUM(CASE WHEN statut = 'EN_ATTENTE' THEN 1 ELSE 0 END) AS pending,
                  SUM(CASE WHEN statut IN ('PRELEVE', 'EN_COURS') THEN 1 ELSE 0 END) AS in_progress,
                  SUM(CASE WHEN statut = 'TERMINE' THEN 1 ELSE 0 END) AS completed
                FROM analyses_laboratoire
                WHERE id_hopital = ?
                """;
        try {
            return jdbcTemplate.queryForObject(sql, (rs, rowNum) -> {
                LaboratoryKpisDTO kpis = new LaboratoryKpisDTO();
                kpis.setTotal(rs.getLong("total"));
                kpis.setPending(rs.getLong("pending"));
                kpis.setInProgress(rs.getLong("in_progress"));
                kpis.setCompleted(rs.getLong("completed"));
                return kpis;
            }, idHopital);
        } catch (Exception e) {
            LaboratoryKpisDTO empty = new LaboratoryKpisDTO();
            return empty;
        }
    }

    @Override
    public List<LaboratoryTestItemDTO> listTests(Integer idHopital, int limit) {
        int safeLimit = Math.max(1, Math.min(limit, 500));
        String sql = """
                SELECT a.id_analyse, a.statut, a.date_demande, a.date_prelevement,
                       TRIM(CONCAT(COALESCE(p.prenom, ''), ' ', COALESCE(p.nom, ''))) AS patient_name,
                       COALESCE(ta.nom_analyse, 'Analyse') AS test_name,
                       TRIM(CONCAT(COALESCE(um.prenom, ''), ' ', COALESCE(um.nom, ''))) AS medecin_name,
                       TRIM(CONCAT(COALESCE(ul.prenom, ''), ' ', COALESCE(ul.nom, ''))) AS laborantin_name
                FROM analyses_laboratoire a
                INNER JOIN patients p ON a.id_patient = p.id_patient AND a.id_hopital = p.id_hopital
                LEFT JOIN types_analyses ta ON a.id_type_analyse = ta.id_type_analyse AND ta.id_hopital = a.id_hopital
                LEFT JOIN utilisateurs um ON a.id_medecin = um.id_utilisateur
                LEFT JOIN utilisateurs ul ON a.id_laborantin = ul.id_utilisateur
                WHERE a.id_hopital = ?
                ORDER BY COALESCE(a.date_prelevement, a.date_demande) DESC
                LIMIT ?
                """;
        try {
            return jdbcTemplate.query(sql, (rs, rowNum) -> {
                LaboratoryTestItemDTO item = new LaboratoryTestItemDTO();
                int idAnalyse = rs.getInt("id_analyse");
                item.setIdAnalyse(idAnalyse);
                item.setId("LAB-" + String.format("%04d", idAnalyse));
                item.setPatient(blankToDash(rs.getString("patient_name")));
                item.setTestName(rs.getString("test_name"));
                item.setDate(resolveDate(rs.getTimestamp("date_prelevement"), rs.getTimestamp("date_demande")));
                item.setStatus(mapStatus(rs.getString("statut")));
                item.setCollectedBy(blankToDash(rs.getString("medecin_name")));
                item.setProcessedBy(blankToDash(rs.getString("laborantin_name")));
                return item;
            }, idHopital, safeLimit);
        } catch (Exception e) {
            return List.of();
        }
    }

    private LocalDateTime resolveDate(Timestamp prelevement, Timestamp demande) {
        if (prelevement != null) {
            return prelevement.toLocalDateTime();
        }
        if (demande != null) {
            return demande.toLocalDateTime();
        }
        return null;
    }

    private String mapStatus(String statut) {
        if (statut == null) {
            return "pending";
        }
        return switch (statut.toUpperCase()) {
            case "EN_ATTENTE" -> "pending";
            case "PRELEVE", "EN_COURS" -> "in_progress";
            case "TERMINE" -> "completed";
            case "ANNULE" -> "cancelled";
            default -> "pending";
        };
    }

    private String blankToDash(String value) {
        return value == null || value.isBlank() ? "—" : value.trim();
    }

    @Override
    public Integer resolveOrCreateTypeAnalyse(Integer idHopital, String testCode, String testName) {
        String label = (testName != null && !testName.isBlank())
                ? testName.trim()
                : TEST_CODE_LABELS.getOrDefault(
                        testCode != null ? testCode.toUpperCase() : "",
                        testCode != null ? testCode : "Analyse");

        String findSql = """
                SELECT id_type_analyse FROM types_analyses
                WHERE id_hopital = ? AND UPPER(nom_analyse) = UPPER(?)
                LIMIT 1
                """;
        List<Integer> found = jdbcTemplate.query(findSql, (rs, rowNum) -> rs.getInt("id_type_analyse"), idHopital, label);
        if (!found.isEmpty()) {
            return found.get(0);
        }

        String insertSql = """
                INSERT INTO types_analyses (id_hopital, nom_analyse, description, prix_analyse)
                VALUES (?, ?, ?, ?)
                """;
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(insertSql, Statement.RETURN_GENERATED_KEYS);
            ps.setInt(1, idHopital);
            ps.setString(2, label);
            ps.setString(3, testCode);
            ps.setBigDecimal(4, java.math.BigDecimal.valueOf(15.00));
            return ps;
        }, keyHolder);
        Number key = keyHolder.getKey();
        return key != null ? key.intValue() : null;
    }

    @Override
    public Integer insertAnalyse(AnalyseLaboratoire analyse, Integer idHopital) {
        try {
            return insertAnalyseInternal(analyse, idHopital, true);
        } catch (Exception ex) {
            // Si FK consultation incompatible : réessayer sans lien consultation
            if (analyse.getIdConsultation() != null
                    && ex.getMessage() != null
                    && ex.getMessage().toLowerCase().contains("foreign key")) {
                analyse.setIdConsultation(null);
                return insertAnalyseInternal(analyse, idHopital, false);
            }
            throw ex;
        }
    }

    private Integer insertAnalyseInternal(AnalyseLaboratoire analyse, Integer idHopital, boolean withConsultation) {
        String sql = """
                INSERT INTO analyses_laboratoire
                (id_patient, id_medecin, id_type_analyse, id_consultation, date_demande,
                 statut, urgence, observations_medecin, id_hopital)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
                """;
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setInt(1, analyse.getIdPatient());
            ps.setInt(2, analyse.getIdMedecin());
            ps.setInt(3, analyse.getIdTypeAnalyse());
            if (withConsultation && analyse.getIdConsultation() != null) {
                ps.setLong(4, analyse.getIdConsultation().longValue());
            } else {
                ps.setNull(4, java.sql.Types.BIGINT);
            }
            ps.setTimestamp(5, Timestamp.valueOf(
                    analyse.getDateDemande() != null ? analyse.getDateDemande() : LocalDateTime.now()));
            ps.setString(6, analyse.getStatut());
            ps.setString(7, analyse.getUrgence());
            ps.setString(8, analyse.getObservationsMedecin());
            ps.setInt(9, idHopital);
            return ps;
        }, keyHolder);
        Number key = keyHolder.getKey();
        return key != null ? key.intValue() : null;
    }

    @Override
    public List<MedecinDemandeAnalyseResponseDTO> listDemandesMedecin(Integer idHopital, Integer idUtilisateurMedecin) {
        String sql = """
                SELECT a.id_analyse, a.statut, a.urgence, a.date_demande, a.observations_medecin,
                       a.id_consultation, a.resultat_texte, a.interpretation, a.valeurs_reference,
                       p.id_patient,
                       TRIM(CONCAT(COALESCE(p.prenom, ''), ' ', COALESCE(p.nom, ''))) AS patient_name,
                       COALESCE(ta.nom_analyse, 'Analyse') AS test_name,
                       TRIM(CONCAT(COALESCE(um.prenom, ''), ' ', COALESCE(um.nom, ''))) AS medecin_name
                FROM analyses_laboratoire a
                INNER JOIN patients p ON a.id_patient = p.id_patient AND a.id_hopital = p.id_hopital
                LEFT JOIN types_analyses ta ON a.id_type_analyse = ta.id_type_analyse AND ta.id_hopital = a.id_hopital
                LEFT JOIN utilisateurs um ON a.id_medecin = um.id_utilisateur
                WHERE a.id_hopital = ? AND a.id_medecin = ?
                ORDER BY a.date_demande DESC
                LIMIT 200
                """;
        try {
            return jdbcTemplate.query(sql, (rs, rowNum) -> mapDemande(rs), idHopital, idUtilisateurMedecin);
        } catch (Exception ex) {
            // Fallback sans colonnes résultat (avant migration)
            String fallback = """
                    SELECT a.id_analyse, a.statut, a.urgence, a.date_demande, a.observations_medecin,
                           a.id_consultation, NULL AS resultat_texte, NULL AS interpretation, NULL AS valeurs_reference,
                           p.id_patient,
                           TRIM(CONCAT(COALESCE(p.prenom, ''), ' ', COALESCE(p.nom, ''))) AS patient_name,
                           COALESCE(ta.nom_analyse, 'Analyse') AS test_name,
                           TRIM(CONCAT(COALESCE(um.prenom, ''), ' ', COALESCE(um.nom, ''))) AS medecin_name
                    FROM analyses_laboratoire a
                    INNER JOIN patients p ON a.id_patient = p.id_patient AND a.id_hopital = p.id_hopital
                    LEFT JOIN types_analyses ta ON a.id_type_analyse = ta.id_type_analyse AND ta.id_hopital = a.id_hopital
                    LEFT JOIN utilisateurs um ON a.id_medecin = um.id_utilisateur
                    WHERE a.id_hopital = ? AND a.id_medecin = ?
                    ORDER BY a.date_demande DESC
                    LIMIT 200
                    """;
            return jdbcTemplate.query(fallback, (rs, rowNum) -> mapDemande(rs), idHopital, idUtilisateurMedecin);
        }
    }

    @Override
    public List<MedecinDemandeAnalyseResponseDTO> listDemandesHopital(Integer idHopital) {
        String sql = """
                SELECT a.id_analyse, a.statut, a.urgence, a.date_demande, a.observations_medecin,
                       a.id_consultation, a.resultat_texte, a.interpretation, a.valeurs_reference,
                       p.id_patient,
                       TRIM(CONCAT(COALESCE(p.prenom, ''), ' ', COALESCE(p.nom, ''))) AS patient_name,
                       COALESCE(ta.nom_analyse, 'Analyse') AS test_name,
                       TRIM(CONCAT(COALESCE(um.prenom, ''), ' ', COALESCE(um.nom, ''))) AS medecin_name
                FROM analyses_laboratoire a
                INNER JOIN patients p ON a.id_patient = p.id_patient AND a.id_hopital = p.id_hopital
                LEFT JOIN types_analyses ta ON a.id_type_analyse = ta.id_type_analyse AND ta.id_hopital = a.id_hopital
                LEFT JOIN utilisateurs um ON a.id_medecin = um.id_utilisateur
                WHERE a.id_hopital = ?
                ORDER BY CASE a.statut
                           WHEN 'EN_ATTENTE' THEN 0
                           WHEN 'PRELEVE' THEN 1
                           WHEN 'EN_COURS' THEN 2
                           ELSE 3 END,
                         a.date_demande DESC
                LIMIT 300
                """;
        try {
            return jdbcTemplate.query(sql, (rs, rowNum) -> mapDemande(rs), idHopital);
        } catch (Exception ex) {
            return List.of();
        }
    }

    @Override
    public MedecinDemandeAnalyseResponseDTO trouverDemande(Integer idAnalyse, Integer idHopital) {
        String sql = """
                SELECT a.id_analyse, a.statut, a.urgence, a.date_demande, a.observations_medecin,
                       a.id_consultation, a.resultat_texte, a.interpretation, a.valeurs_reference,
                       p.id_patient,
                       TRIM(CONCAT(COALESCE(p.prenom, ''), ' ', COALESCE(p.nom, ''))) AS patient_name,
                       COALESCE(ta.nom_analyse, 'Analyse') AS test_name,
                       TRIM(CONCAT(COALESCE(um.prenom, ''), ' ', COALESCE(um.nom, ''))) AS medecin_name
                FROM analyses_laboratoire a
                INNER JOIN patients p ON a.id_patient = p.id_patient AND a.id_hopital = p.id_hopital
                LEFT JOIN types_analyses ta ON a.id_type_analyse = ta.id_type_analyse AND ta.id_hopital = a.id_hopital
                LEFT JOIN utilisateurs um ON a.id_medecin = um.id_utilisateur
                WHERE a.id_hopital = ? AND a.id_analyse = ?
                LIMIT 1
                """;
        List<MedecinDemandeAnalyseResponseDTO> rows =
                jdbcTemplate.query(sql, (rs, rowNum) -> mapDemande(rs), idHopital, idAnalyse);
        return rows.isEmpty() ? null : rows.get(0);
    }

    @Override
    public void soumettreResultat(Integer idAnalyse, Integer idHopital, Integer idLaborantin,
                                  String resultatTexte, String interpretation, String valeursReference, String statut) {
        try {
            jdbcTemplate.update(
                    """
                    UPDATE analyses_laboratoire
                    SET resultat_texte = ?,
                        interpretation = ?,
                        valeurs_reference = ?,
                        statut = ?,
                        id_laborantin = ?,
                        date_resultat = CASE WHEN ? = 'TERMINE' THEN CURRENT_TIMESTAMP ELSE date_resultat END,
                        date_prelevement = COALESCE(date_prelevement, CURRENT_TIMESTAMP)
                    WHERE id_analyse = ? AND id_hopital = ?
                    """,
                    resultatTexte,
                    interpretation,
                    valeursReference,
                    statut,
                    idLaborantin,
                    statut,
                    idAnalyse,
                    idHopital
            );
        } catch (Exception ex) {
            jdbcTemplate.update(
                    """
                    UPDATE analyses_laboratoire
                    SET resultat_texte = ?,
                        interpretation = ?,
                        valeurs_reference = ?,
                        statut = ?,
                        id_laborantin = ?
                    WHERE id_analyse = ? AND id_hopital = ?
                    """,
                    resultatTexte,
                    interpretation,
                    valeursReference,
                    statut,
                    idLaborantin,
                    idAnalyse,
                    idHopital
            );
        }
    }

    private MedecinDemandeAnalyseResponseDTO mapDemande(java.sql.ResultSet rs) throws java.sql.SQLException {
        MedecinDemandeAnalyseResponseDTO dto = new MedecinDemandeAnalyseResponseDTO();
        int idAnalyse = rs.getInt("id_analyse");
        dto.setIdAnalyse(idAnalyse);
        dto.setId("LAB-" + String.format("%04d", idAnalyse));
        dto.setPatientName(blankToDash(rs.getString("patient_name")));
        int idPatient = rs.getInt("id_patient");
        dto.setIdPatient(idPatient);
        dto.setPatientId("PT-" + idPatient);
        dto.setTestName(rs.getString("test_name"));
        dto.setRequestedBy(blankToDash(rs.getString("medecin_name")));
        Timestamp demande = rs.getTimestamp("date_demande");
        dto.setDate(demande != null ? demande.toLocalDateTime() : null);
        dto.setStatus(mapMedecinStatus(rs.getString("statut")));
        dto.setPriority(mapMedecinPriority(rs.getString("urgence")));
        dto.setNotes(rs.getString("observations_medecin"));
        dto.setObservationsMedecin(rs.getString("observations_medecin"));
        Object idConsult = rs.getObject("id_consultation");
        dto.setIdConsultation(idConsult != null ? rs.getInt("id_consultation") : null);
        try {
            dto.setResultatTexte(rs.getString("resultat_texte"));
            dto.setInterpretation(rs.getString("interpretation"));
            dto.setValeursReference(rs.getString("valeurs_reference"));
        } catch (Exception ignored) {
            // colonnes absentes
        }
        return dto;
    }

    private String mapMedecinStatus(String statut) {
        if (statut == null) {
            return "Pending";
        }
        return switch (statut.toUpperCase()) {
            case "EN_ATTENTE" -> "Pending";
            case "PRELEVE", "EN_COURS" -> "In Progress";
            case "TERMINE" -> "Completed";
            case "ANNULE" -> "Cancelled";
            default -> "Pending";
        };
    }

    private String mapMedecinPriority(String urgence) {
        if (urgence == null) {
            return "Routine";
        }
        return switch (urgence.toUpperCase()) {
            case "HAUTE" -> "Urgent";
            case "VITALE" -> "STAT";
            default -> "Routine";
        };
    }
}
