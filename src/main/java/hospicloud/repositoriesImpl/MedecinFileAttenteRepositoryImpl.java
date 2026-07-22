package hospicloud.repositoriesImpl;

import hospicloud.dtos.MedecinFileItemDTO;
import hospicloud.model.reception.Admission;
import hospicloud.repositories.MedecinFileAttenteRepository;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Repository
public class MedecinFileAttenteRepositoryImpl implements MedecinFileAttenteRepository {

    private final JdbcTemplate jdbcTemplate;

    public MedecinFileAttenteRepositoryImpl(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public List<MedecinFileItemDTO> listerFileDuMedecin(Integer idMedecin, Integer idHopital) {
        Map<String, MedecinFileItemDTO> byKey = new LinkedHashMap<>();

        for (MedecinFileItemDTO item : listerAdmissions(idMedecin, idHopital)) {
            byKey.put("A-" + item.getIdAdmission(), item);
        }
        for (MedecinFileItemDTO item : listerRdvSansAdmission(idMedecin, idHopital)) {
            byKey.putIfAbsent("R-" + item.getIdRendezVous(), item);
        }
        return new ArrayList<>(byKey.values());
    }

    private List<MedecinFileItemDTO> listerAdmissions(Integer idMedecin, Integer idHopital) {
        String sql = """
            SELECT a.id_admission,
                   a.id_rendez_vous,
                   a.id_patient,
                   a.statut,
                   a.niveau_priorite,
                   a.temps_arrivee,
                   a.numero_passage,
                   COALESCE(
                       NULLIF(TRIM(a.salle), ''),
                       CASE WHEN UPPER(COALESCE(r.canal, '')) = 'TELECONSULTATION'
                            THEN 'Téléconsultation' ELSE 'Consultation' END
                   ) AS salle,
                   TRIM(CONCAT(COALESCE(p.prenom, ''), ' ', COALESCE(p.nom, ''))) AS patient_name
            FROM admission a
            INNER JOIN patients p ON a.id_patient = p.id_patient AND a.id_hopital = p.id_hopital
            LEFT JOIN rendez_vous01 r ON a.id_rendez_vous = r.id_rdv AND a.id_hopital = r.id_hopital
            WHERE a.id_hopital = ?
              AND a.id_medecin = ?
              AND DATE(a.temps_arrivee) = CURRENT_DATE
              AND a.statut IN ('ATTENTE_TRIAGE', 'EN_ATTENTE', 'ORIENTE', 'ENREGISTRE', 'APPELE')
            ORDER BY CASE a.statut
                       WHEN 'APPELE' THEN 0
                       WHEN 'ENREGISTRE' THEN 1
                       WHEN 'ORIENTE' THEN 2
                       WHEN 'ATTENTE_TRIAGE' THEN 3
                       ELSE 4 END,
                     a.niveau_priorite ASC,
                     a.temps_arrivee ASC
            """;
        try {
            return jdbcTemplate.query(sql, (rs, rowNum) -> {
                MedecinFileItemDTO dto = new MedecinFileItemDTO();
                dto.setIdAdmission(rs.getInt("id_admission"));
                Object idRdv = rs.getObject("id_rendez_vous");
                dto.setIdRendezVous(idRdv != null ? rs.getInt("id_rendez_vous") : null);
                dto.setIdPatient(rs.getInt("id_patient"));
                dto.setPatientName(trim(rs.getString("patient_name")));
                LocalDateTime arrivee = toLdt(rs.getTimestamp("temps_arrivee"));
                dto.setTempsArrivee(arrivee);
                dto.setWaited(formatWaited(arrivee));
                dto.setStatut(rs.getString("statut"));
                dto.setNumeroPassage(rs.getObject("numero_passage") != null ? rs.getInt("numero_passage") : null);
                dto.setSalle(rs.getString("salle"));
                dto.setPriority(mapPriority(rs.getInt("niveau_priorite"), dto.getStatut()));
                applyActions(dto);
                return dto;
            }, idHopital, idMedecin);
        } catch (Exception ex) {
            // Fallback sans colonnes optionnelles (migration partielle)
            try {
                String fallbackSql = """
                    SELECT a.id_admission,
                           a.id_rendez_vous,
                           a.id_patient,
                           a.statut,
                           a.niveau_priorite,
                           a.temps_arrivee,
                           TRIM(CONCAT(COALESCE(p.prenom, ''), ' ', COALESCE(p.nom, ''))) AS patient_name
                    FROM admission a
                    INNER JOIN patients p ON a.id_patient = p.id_patient AND a.id_hopital = p.id_hopital
                    WHERE a.id_hopital = ?
                      AND a.id_medecin = ?
                      AND DATE(a.temps_arrivee) = CURRENT_DATE
                      AND a.statut IN ('ATTENTE_TRIAGE', 'EN_ATTENTE', 'ORIENTE', 'ENREGISTRE', 'APPELE')
                    ORDER BY a.niveau_priorite ASC, a.temps_arrivee ASC
                    """;
                return jdbcTemplate.query(fallbackSql, (rs, rowNum) -> {
                    MedecinFileItemDTO dto = new MedecinFileItemDTO();
                    dto.setIdAdmission(rs.getInt("id_admission"));
                    Object idRdv = rs.getObject("id_rendez_vous");
                    dto.setIdRendezVous(idRdv != null ? rs.getInt("id_rendez_vous") : null);
                    dto.setIdPatient(rs.getInt("id_patient"));
                    dto.setPatientName(trim(rs.getString("patient_name")));
                    LocalDateTime arrivee = toLdt(rs.getTimestamp("temps_arrivee"));
                    dto.setTempsArrivee(arrivee);
                    dto.setWaited(formatWaited(arrivee));
                    dto.setStatut(rs.getString("statut"));
                    dto.setSalle("Consultation");
                    dto.setPriority(mapPriority(rs.getInt("niveau_priorite"), dto.getStatut()));
                    applyActions(dto);
                    return dto;
                }, idHopital, idMedecin);
            } catch (Exception fallbackEx) {
                return List.of();
            }
        }
    }

    private List<MedecinFileItemDTO> listerRdvSansAdmission(Integer idMedecin, Integer idHopital) {
        String sql = """
            SELECT r.id_rdv,
                   r.id_patient,
                   r.date_heure_rdv,
                   r.statut_rdv,
                   CASE WHEN UPPER(COALESCE(r.canal, '')) = 'TELECONSULTATION'
                        THEN 'Téléconsultation' ELSE 'Consultation' END AS salle,
                   TRIM(CONCAT(COALESCE(p.prenom, ''), ' ', COALESCE(p.nom, ''))) AS patient_name
            FROM rendez_vous01 r
            INNER JOIN patients p ON r.id_patient = p.id_patient AND r.id_hopital = p.id_hopital
            WHERE r.id_hopital = ?
              AND r.id_medecin = ?
              AND DATE(r.date_heure_rdv) = CURRENT_DATE
              AND r.statut_rdv IN ('CONFIRME', 'PROGRAMME', 'EN_ATTENTE', 'ENREGISTRE')
              AND NOT EXISTS (
                  SELECT 1 FROM admission a
                  WHERE a.id_hopital = r.id_hopital
                    AND a.id_rendez_vous = r.id_rdv
                    AND a.statut IN ('EN_ATTENTE', 'ENREGISTRE', 'APPELE', 'EN_CONSULTATION')
              )
            ORDER BY r.date_heure_rdv ASC
            """;
        try {
            return jdbcTemplate.query(sql, (rs, rowNum) -> {
                MedecinFileItemDTO dto = new MedecinFileItemDTO();
                dto.setIdRendezVous(rs.getInt("id_rdv"));
                dto.setIdPatient(rs.getInt("id_patient"));
                dto.setPatientName(trim(rs.getString("patient_name")));
                LocalDateTime arrivee = toLdt(rs.getTimestamp("date_heure_rdv"));
                dto.setTempsArrivee(arrivee);
                dto.setWaited(formatWaited(arrivee));
                dto.setStatut("EN_ATTENTE");
                dto.setSalle(rs.getString("salle"));
                dto.setPriority(mapPriority(3, dto.getStatut()));
                dto.setCanCall(true);
                dto.setCanStart(false);
                return dto;
            }, idHopital, idMedecin);
        } catch (Exception ex) {
            return List.of();
        }
    }

    private void applyActions(MedecinFileItemDTO dto) {
        String statut = dto.getStatut() == null ? "" : dto.getStatut().toUpperCase();
        dto.setCanCall(
                "ATTENTE_TRIAGE".equals(statut)
                        || "EN_ATTENTE".equals(statut)
                        || "ORIENTE".equals(statut)
                        || "ENREGISTRE".equals(statut)
                        || "APPELE".equals(statut));
        dto.setCanStart("APPELE".equals(statut));
    }

    private String mapPriority(int niveau, String statut) {
        if ("APPELE".equalsIgnoreCase(statut)) {
            return "high";
        }
        if (niveau <= 1) {
            return "high";
        }
        if (niveau == 2) {
            return "normal";
        }
        return "low";
    }

    @Override
    public Optional<Admission> trouverAdmission(Integer idAdmission, Integer idHopital) {
        String sql = "SELECT * FROM admission WHERE id_admission = ? AND id_hopital = ?";
        try {
            return Optional.ofNullable(jdbcTemplate.queryForObject(sql, (rs, rowNum) -> mapAdmission(rs), idAdmission, idHopital));
        } catch (EmptyResultDataAccessException ex) {
            return Optional.empty();
        }
    }

    @Override
    public Optional<Admission> trouverAdmissionOuverteParRdv(Integer idRdv, Integer idHopital) {
        String sql = """
            SELECT * FROM admission
            WHERE id_hopital = ? AND id_rendez_vous = ?
              AND statut IN ('ATTENTE_TRIAGE', 'EN_ATTENTE', 'ORIENTE', 'ENREGISTRE', 'APPELE')
            ORDER BY id_admission DESC
            LIMIT 1
            """;
        try {
            return Optional.ofNullable(jdbcTemplate.queryForObject(sql, (rs, rowNum) -> mapAdmission(rs), idHopital, idRdv));
        } catch (EmptyResultDataAccessException ex) {
            return Optional.empty();
        }
    }

    private Admission mapAdmission(java.sql.ResultSet rs) throws java.sql.SQLException {
        Admission a = new Admission();
        a.setIdAdmission(rs.getInt("id_admission"));
        a.setIdHopital(rs.getInt("id_hopital"));
        a.setIdPatient(rs.getInt("id_patient"));
        a.setIdMedecin(rs.getObject("id_medecin") != null ? rs.getInt("id_medecin") : null);
        a.setIdRendezVous(rs.getObject("id_rendez_vous") != null ? rs.getInt("id_rendez_vous") : null);
        a.setNiveauPriorite(rs.getInt("niveau_priorite"));
        a.setTempsArrivee(toLdt(rs.getTimestamp("temps_arrivee")));
        a.setStatut(rs.getString("statut"));
        try {
            a.setNumeroPassage(rs.getObject("numero_passage") != null ? rs.getInt("numero_passage") : null);
            a.setSalle(rs.getString("salle"));
        } catch (Exception ignored) {
            // colonnes optionnelles avant migration
        }
        return a;
    }

    @Override
    public int prochainNumeroPassage(Integer idHopital) {
        try {
            Integer max = jdbcTemplate.queryForObject(
                    """
                    SELECT COALESCE(MAX(numero_passage), 0)
                    FROM admission
                    WHERE id_hopital = ? AND DATE(temps_arrivee) = CURRENT_DATE
                    """,
                    Integer.class,
                    idHopital
            );
            return (max == null ? 0 : max) + 1;
        } catch (Exception ex) {
            Integer count = jdbcTemplate.queryForObject(
                    """
                    SELECT COUNT(*) FROM admission
                    WHERE id_hopital = ? AND DATE(temps_arrivee) = CURRENT_DATE
                    """,
                    Integer.class,
                    idHopital
            );
            return (count == null ? 0 : count) + 1;
        }
    }

    @Override
    public void marquerAppele(Integer idAdmission, Integer idHopital, Integer numeroPassage, String salle) {
        try {
            jdbcTemplate.update(
                    """
                    UPDATE admission
                    SET statut = 'APPELE',
                        numero_passage = COALESCE(numero_passage, ?),
                        salle = COALESCE(NULLIF(TRIM(salle), ''), ?),
                        appele_a = CURRENT_TIMESTAMP
                    WHERE id_admission = ? AND id_hopital = ?
                    """,
                    numeroPassage,
                    salle,
                    idAdmission,
                    idHopital
            );
        } catch (Exception ex) {
            try {
                jdbcTemplate.update(
                        """
                        UPDATE admission
                        SET statut = 'APPELE', appele_a = CURRENT_TIMESTAMP
                        WHERE id_admission = ? AND id_hopital = ?
                        """,
                        idAdmission,
                        idHopital
                );
            } catch (Exception ignored) {
                jdbcTemplate.update(
                        "UPDATE admission SET statut = 'APPELE' WHERE id_admission = ? AND id_hopital = ?",
                        idAdmission, idHopital
                );
            }
        }
    }

    @Override
    public void mettreAJourStatut(Integer idAdmission, Integer idHopital, String statut) {
        jdbcTemplate.update(
                "UPDATE admission SET statut = ? WHERE id_admission = ? AND id_hopital = ?",
                statut, idAdmission, idHopital
        );
    }

    @Override
    public Integer creerAdmissionPourAppel(Admission admission) {
        String sql = """
            INSERT INTO admission
              (id_hopital, id_patient, id_medecin, id_rendez_vous, niveau_priorite, temps_arrivee, statut, cree_par, check_in_par, numero_passage, salle)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            """;
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setInt(1, admission.getIdHopital());
            ps.setInt(2, admission.getIdPatient());
            if (admission.getIdMedecin() != null) {
                ps.setInt(3, admission.getIdMedecin());
            } else {
                ps.setNull(3, java.sql.Types.INTEGER);
            }
            if (admission.getIdRendezVous() != null) {
                ps.setInt(4, admission.getIdRendezVous());
            } else {
                ps.setNull(4, java.sql.Types.INTEGER);
            }
            ps.setInt(5, admission.getNiveauPriorite() != null ? admission.getNiveauPriorite() : 3);
            ps.setTimestamp(6, Timestamp.valueOf(
                    admission.getTempsArrivee() != null ? admission.getTempsArrivee() : LocalDateTime.now()));
            ps.setString(7, admission.getStatut() != null ? admission.getStatut() : "EN_ATTENTE");
            if (admission.getCreePar() != null) {
                ps.setInt(8, admission.getCreePar());
            } else {
                ps.setNull(8, java.sql.Types.INTEGER);
            }
            if (admission.getCheckInPar() != null) {
                ps.setInt(9, admission.getCheckInPar());
            } else {
                ps.setNull(9, java.sql.Types.INTEGER);
            }
            if (admission.getNumeroPassage() != null) {
                ps.setInt(10, admission.getNumeroPassage());
            } else {
                ps.setNull(10, java.sql.Types.INTEGER);
            }
            ps.setString(11, admission.getSalle());
            return ps;
        }, keyHolder);
        Number key = keyHolder.getKey();
        return key != null ? key.intValue() : null;
    }

    @Override
    public String trouverNomPatient(Integer idPatient, Integer idHopital) {
        try {
            return trim(jdbcTemplate.queryForObject(
                    """
                    SELECT TRIM(CONCAT(COALESCE(prenom, ''), ' ', COALESCE(nom, '')))
                    FROM patients WHERE id_patient = ? AND id_hopital = ?
                    """,
                    String.class, idPatient, idHopital));
        } catch (EmptyResultDataAccessException ex) {
            return "Patient";
        }
    }

    @Override
    public String trouverNomMedecin(Integer idMedecin, Integer idHopital) {
        try {
            return trim(jdbcTemplate.queryForObject(
                    """
                    SELECT TRIM(CONCAT(COALESCE(prenom, ''), ' ', COALESCE(nom, '')))
                    FROM medecin WHERE id_medecin = ? AND id_hopital = ?
                    """,
                    String.class, idMedecin, idHopital));
        } catch (EmptyResultDataAccessException ex) {
            return "Médecin";
        }
    }

    @Override
    public String trouverSalleRdv(Integer idRdv, Integer idHopital) {
        if (idRdv == null) {
            return "Consultation";
        }
        try {
            String canal = jdbcTemplate.queryForObject(
                    """
                    SELECT canal FROM rendez_vous01
                    WHERE id_rdv = ? AND id_hopital = ?
                    """,
                    String.class, idRdv, idHopital);
            if (canal != null && "TELECONSULTATION".equalsIgnoreCase(canal.trim())) {
                return "Téléconsultation";
            }
            return "Consultation";
        } catch (Exception ex) {
            return "Consultation";
        }
    }

    private String formatWaited(LocalDateTime appointmentTime) {
        if (appointmentTime == null) {
            return "—";
        }
        long minutes = Math.max(0, Duration.between(appointmentTime, LocalDateTime.now()).toMinutes());
        if (minutes < 60) {
            return minutes + " min";
        }
        return (minutes / 60) + " h " + (minutes % 60) + " min";
    }

    private LocalDateTime toLdt(Timestamp timestamp) {
        return timestamp != null ? timestamp.toLocalDateTime() : null;
    }

    private String trim(String value) {
        return value == null ? "" : value.trim();
    }
}
