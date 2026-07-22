package hospicloud.repositoriesImpl;

import hospicloud.dtos.reception.AdmissionDTO;
import hospicloud.dtos.reception.MedecinDisponibleDTO;
import hospicloud.dtos.reception.ReceptionDashboardStatsDTO;
import hospicloud.dtos.reception.ReceptionRegistrationPointDTO;
import hospicloud.model.reception.Admission;
import hospicloud.repositories.ReceptionDashboardRepository;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.sql.Timestamp;
import java.sql.Types;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import hospicloud.model.RendezVous;
import java.util.ArrayList;
import java.util.List;

@Repository
public class ReceptionDashboardRepositoryImpl implements ReceptionDashboardRepository {

    private final JdbcTemplate jdbcTemplate;

    public ReceptionDashboardRepositoryImpl(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private LocalDateTime toLocalDateTime(Timestamp ts) {
        return ts != null ? ts.toLocalDateTime() : null;
    }

    private long safeCount(String sql, Integer idHopital) {
        try {
            Long value = jdbcTemplate.queryForObject(sql, Long.class, idHopital);
            return value != null ? value : 0L;
        } catch (Exception e) {
            return 0L;
        }
    }

    @Override
    public ReceptionDashboardStatsDTO getDashboardStats(Integer idHopital) {
        long rendezVousJour = safeCount(
                "SELECT COUNT(id_rdv) FROM rendez_vous01 WHERE id_hopital = ? AND DATE(date_heure_rdv) = CURRENT_DATE",
                idHopital);

        long rendezVousHier = safeCount(
                "SELECT COUNT(id_rdv) FROM rendez_vous01 WHERE id_hopital = ? AND DATE(date_heure_rdv) = DATE_SUB(CURRENT_DATE, INTERVAL 1 DAY)",
                idHopital);

        long enAttente = safeCount(
                "SELECT COUNT(id_admission) FROM admission WHERE id_hopital = ? AND DATE(temps_arrivee) = CURRENT_DATE "
                        + "AND statut IN ('EN_ATTENTE', 'ATTENTE_TRIAGE')",
                idHopital);
        if (enAttente == 0) {
            enAttente = safeCount(
                    "SELECT COUNT(id_rdv) FROM rendez_vous01 WHERE id_hopital = ? AND DATE(date_heure_rdv) = CURRENT_DATE AND statut_rdv = 'PROGRAMME'",
                    idHopital);
        }

        long enAttenteHier = safeCount(
                "SELECT COUNT(id_admission) FROM admission WHERE id_hopital = ? "
                        + "AND statut IN ('EN_ATTENTE', 'ATTENTE_TRIAGE') "
                        + "AND DATE(temps_arrivee) = DATE_SUB(CURRENT_DATE, INTERVAL 1 DAY)",
                idHopital);

        long enregistres = safeCount(
                "SELECT COUNT(id_admission) FROM admission WHERE id_hopital = ? AND statut IN ('ENREGISTRE', 'EN_CONSULTATION') AND DATE(temps_arrivee) = CURRENT_DATE",
                idHopital);
        if (enregistres == 0) {
            enregistres = safeCount(
                    "SELECT COUNT(id_rdv) FROM rendez_vous01 WHERE id_hopital = ? AND DATE(date_heure_rdv) = CURRENT_DATE AND statut_rdv IN ('CONFIRME', 'TERMINE')",
                    idHopital);
        }

        long enregistresHier = safeCount(
                "SELECT COUNT(id_admission) FROM admission WHERE id_hopital = ? AND statut IN ('ENREGISTRE', 'EN_CONSULTATION') AND DATE(temps_arrivee) = DATE_SUB(CURRENT_DATE, INTERVAL 1 DAY)",
                idHopital);

        long nouvellesInscr = safeCount(
                "SELECT COUNT(id_patient) FROM patients WHERE id_hopital = ? AND DATE(date_enregistrement) = CURRENT_DATE",
                idHopital);

        long nouvellesInscrHier = safeCount(
                "SELECT COUNT(id_patient) FROM patients WHERE id_hopital = ? AND DATE(date_enregistrement) = DATE_SUB(CURRENT_DATE, INTERVAL 1 DAY)",
                idHopital);

        return new ReceptionDashboardStatsDTO(
                rendezVousJour,
                enAttente,
                enregistres,
                nouvellesInscr,
                rendezVousJour - rendezVousHier,
                enAttente - enAttenteHier,
                enregistres - enregistresHier,
                nouvellesInscr - nouvellesInscrHier
        );
    }

    @Override
    public List<AdmissionDTO> getAdmissionsEnAttente(Integer idHopital) {
        List<AdmissionDTO> admissions = listerAdmissionsEnAttente(idHopital);
        if (!admissions.isEmpty()) {
            return admissions;
        }
        return listerRendezVousDuJourCommeFile(idHopital);
    }

    private List<AdmissionDTO> listerAdmissionsEnAttente(Integer idHopital) {
        try {
            String sql =
                    "SELECT a.id_admission, a.id_patient, a.id_medecin, a.id_rendez_vous, a.niveau_priorite, " +
                    "a.temps_arrivee, a.statut, a.numero_passage, r.motif_visite, " +
                    "TRIM(CONCAT(COALESCE(p.prenom, ''), ' ', COALESCE(p.nom, ''))) AS nom_patient, " +
                    "TRIM(CONCAT(COALESCE(m.prenom, ''), ' ', COALESCE(m.nom, ''))) AS nom_medecin " +
                    "FROM admission a " +
                    "JOIN patients p ON a.id_patient = p.id_patient AND a.id_hopital = p.id_hopital " +
                    "LEFT JOIN medecin m ON a.id_medecin = m.id_medecin AND a.id_hopital = m.id_hopital " +
                    "LEFT JOIN rendez_vous01 r ON a.id_rendez_vous = r.id_rdv AND a.id_hopital = r.id_hopital " +
                    "WHERE a.id_hopital = ? " +
                    "AND DATE(a.temps_arrivee) = CURRENT_DATE " +
                    "AND a.statut IN ('ATTENTE_TRIAGE', 'EN_ATTENTE', 'ORIENTE', 'ENREGISTRE', 'APPELE', 'ABSENT') " +
                    "ORDER BY " +
                    "  CASE a.statut " +
                    "    WHEN 'ATTENTE_TRIAGE' THEN 0 WHEN 'EN_ATTENTE' THEN 1 WHEN 'ORIENTE' THEN 2 " +
                    "    WHEN 'ENREGISTRE' THEN 3 WHEN 'APPELE' THEN 4 WHEN 'ABSENT' THEN 5 ELSE 6 END, " +
                    "  a.niveau_priorite ASC, a.temps_arrivee ASC";

            return jdbcTemplate.query(sql, (rs, rowNum) -> mapAdmissionRow(rs), idHopital);
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }

    private AdmissionDTO mapAdmissionRow(java.sql.ResultSet rs) throws java.sql.SQLException {
        AdmissionDTO dto = new AdmissionDTO();
        dto.setIdAdmission(rs.getInt("id_admission"));
        dto.setIdPatient(rs.getInt("id_patient"));
        Object idMed = rs.getObject("id_medecin");
        dto.setIdMedecin(idMed != null ? rs.getInt("id_medecin") : null);
        Object idRdv = rs.getObject("id_rendez_vous");
        dto.setIdRendezVous(idRdv != null ? rs.getInt("id_rendez_vous") : null);
        dto.setNomCompletPatient(rs.getString("nom_patient"));
        dto.setNomMedecin(rs.getString("nom_medecin"));
        dto.setNiveauPriorite(rs.getInt("niveau_priorite"));
        dto.setTempsArrivee(toLocalDateTime(rs.getTimestamp("temps_arrivee")));
        String statut = rs.getString("statut");
        dto.setStatut(statut);
        dto.setStatutAdministratif(mapStatutAdministratif(statut));
        Object num = rs.getObject("numero_passage");
        dto.setNumeroPassage(num != null ? rs.getInt("numero_passage") : null);
        try {
            dto.setMotifVisite(rs.getString("motif_visite"));
        } catch (Exception ignored) {
            dto.setMotifVisite(null);
        }
        if (dto.getTempsArrivee() != null) {
            dto.setTempsAttenteMinutes(java.time.Duration.between(dto.getTempsArrivee(), LocalDateTime.now()).toMinutes());
        }
        return dto;
    }

    private static String mapStatutAdministratif(String statut) {
        if (statut == null) return "waiting";
        return switch (statut.toUpperCase()) {
            case "ATTENTE_TRIAGE" -> "waiting_triage";
            case "ORIENTE" -> "oriented";
            case "ENREGISTRE", "APPELE", "EN_CONSULTATION" -> "received";
            case "ABSENT" -> "absent";
            default -> "waiting";
        };
    }

    private List<AdmissionDTO> listerRendezVousDuJourCommeFile(Integer idHopital) {
        String sql =
                "SELECT r.id_rdv, r.id_patient, r.id_medecin, r.date_heure_rdv, r.statut_rdv, r.motif_visite, " +
                "TRIM(CONCAT(COALESCE(p.prenom, ''), ' ', COALESCE(p.nom, ''))) AS nom_patient, " +
                "TRIM(CONCAT(COALESCE(m.prenom, ''), ' ', COALESCE(m.nom, ''))) AS nom_medecin " +
                "FROM rendez_vous01 r " +
                "JOIN patients p ON r.id_patient = p.id_patient AND r.id_hopital = p.id_hopital " +
                "LEFT JOIN medecin m ON r.id_medecin = m.id_medecin AND r.id_hopital = m.id_hopital " +
                "WHERE r.id_hopital = ? AND DATE(r.date_heure_rdv) = CURRENT_DATE " +
                "AND r.statut_rdv NOT IN ('ANNULE') " +
                "ORDER BY r.date_heure_rdv ASC";

        return jdbcTemplate.query(sql, (rs, rowNum) -> {
            AdmissionDTO dto = new AdmissionDTO();
            dto.setIdAdmission(rs.getInt("id_rdv"));
            dto.setIdPatient(rs.getInt("id_patient"));
            Object idMed = rs.getObject("id_medecin");
            dto.setIdMedecin(idMed != null ? rs.getInt("id_medecin") : null);
            dto.setIdRendezVous(rs.getInt("id_rdv"));
            dto.setNomCompletPatient(rs.getString("nom_patient"));
            dto.setNomMedecin(rs.getString("nom_medecin"));
            dto.setNiveauPriorite(3);
            dto.setTempsArrivee(toLocalDateTime(rs.getTimestamp("date_heure_rdv")));
            String statutRdv = rs.getString("statut_rdv");
            dto.setStatut(statutRdv);
            dto.setStatutAdministratif("ABSENT".equalsIgnoreCase(statutRdv) ? "absent" : "scheduled");
            dto.setMotifVisite(rs.getString("motif_visite"));
            return dto;
        }, idHopital);
    }

    @Override
    public List<ReceptionRegistrationPointDTO> getInscriptionsParHeure(Integer idHopital) {
        String sql =
                "SELECT HOUR(date_enregistrement) AS heure, COUNT(id_patient) AS total " +
                "FROM patients " +
                "WHERE id_hopital = ? AND DATE(date_enregistrement) = CURRENT_DATE " +
                "GROUP BY HOUR(date_enregistrement) " +
                "ORDER BY heure ASC";

        return jdbcTemplate.query(sql, (rs, rowNum) ->
                new ReceptionRegistrationPointDTO(rs.getInt("heure"), rs.getLong("total")), idHopital);
    }

    @Override
    public Admission trouverAdmissionParId(Integer idAdmission, Integer idHopital) {
        String sql = "SELECT * FROM admission WHERE id_admission = ? AND id_hopital = ?";
        try {
            return jdbcTemplate.queryForObject(sql, (rs, rowNum) -> {
                Admission a = new Admission();
                a.setIdAdmission(rs.getInt("id_admission"));
                a.setIdHopital(rs.getInt("id_hopital"));
                a.setIdPatient(rs.getInt("id_patient"));
                a.setIdMedecin(rs.getObject("id_medecin") != null ? rs.getInt("id_medecin") : null);
                a.setIdRendezVous(rs.getObject("id_rendez_vous") != null ? rs.getInt("id_rendez_vous") : null);
                a.setNiveauPriorite(rs.getInt("niveau_priorite"));
                a.setTempsArrivee(toLocalDateTime(rs.getTimestamp("temps_arrivee")));
                a.setStatut(rs.getString("statut"));
                Object num = rs.getObject("numero_passage");
                a.setNumeroPassage(num != null ? rs.getInt("numero_passage") : null);
                return a;
            }, idAdmission, idHopital);
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }

    @Override
    public void mettreAJourStatutAdmission(Integer idAdmission, Integer idHopital, String nouveauStatut) {
        String sql = "UPDATE admission SET statut = ? WHERE id_admission = ? AND id_hopital = ?";
        jdbcTemplate.update(sql, nouveauStatut, idAdmission, idHopital);
    }

    @Override
    public void creerAdmission(Admission a) {
        String sql = "INSERT INTO admission (id_hopital, id_patient, id_medecin, id_rendez_vous, niveau_priorite, temps_arrivee, statut, cree_par, check_in_par) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        jdbcTemplate.update(sql,
            a.getIdHopital(),
            a.getIdPatient(),
            a.getIdMedecin(),
            a.getIdRendezVous(),
            a.getNiveauPriorite(),
            Timestamp.valueOf(a.getTempsArrivee() == null ? LocalDateTime.now() : a.getTempsArrivee()),
            a.getStatut(),
            a.getCreePar(),
            a.getCheckInPar());
    }

    @Override
    public boolean aRendezVousAujourdhui(Integer idPatient, Integer idHopital) {
        String sql = "SELECT COUNT(*) FROM rendez_vous01 WHERE id_patient = ? AND id_hopital = ? AND DATE(date_heure_rdv) = CURRENT_DATE";
        Long count = jdbcTemplate.queryForObject(sql, Long.class, idPatient, idHopital);
        return count != null && count > 0;
    }

    @Override
    public Admission trouverAdmissionActiveParPatient(Integer idPatient, Integer idHopital) {
        String sql = """
            SELECT * FROM admission
            WHERE id_patient = ? AND id_hopital = ?
              AND statut IN ('EN_CONSULTATION', 'HOSPITALISE', 'ENREGISTRE')
            ORDER BY temps_arrivee DESC
            LIMIT 1
            """;
        try {
            return jdbcTemplate.queryForObject(sql, (rs, rowNum) -> {
                Admission a = new Admission();
                a.setIdAdmission(rs.getInt("id_admission"));
                a.setIdHopital(rs.getInt("id_hopital"));
                a.setIdPatient(rs.getInt("id_patient"));
                a.setIdMedecin(rs.getObject("id_medecin") != null ? rs.getInt("id_medecin") : null);
                a.setIdRendezVous(rs.getObject("id_rendez_vous") != null ? rs.getInt("id_rendez_vous") : null);
                a.setNiveauPriorite(rs.getInt("niveau_priorite"));
                a.setTempsArrivee(toLocalDateTime(rs.getTimestamp("temps_arrivee")));
                a.setStatut(rs.getString("statut"));
                return a;
            }, idPatient, idHopital);
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }

    @Override
    public List<RendezVous> listerRendezVousDuJour(Integer idHopital) {
        String sql =
                "SELECT r.id_rdv, r.id_hopital, r.id_patient, r.id_medecin, r.date_heure_rdv, " +
                "r.duree_estimee, r.motif_visite, r.canal, r.statut_rdv, r.url_visio, r.date_creation, r.cree_par, " +
                "TRIM(CONCAT(COALESCE(p.prenom, ''), ' ', COALESCE(p.nom, ''))) AS nom_patient, " +
                "TRIM(CONCAT(COALESCE(m.prenom, ''), ' ', COALESCE(m.nom, ''))) AS nom_medecin " +
                "FROM rendez_vous01 r " +
                "JOIN patients p ON r.id_patient = p.id_patient AND r.id_hopital = p.id_hopital " +
                "LEFT JOIN medecin m ON r.id_medecin = m.id_medecin AND r.id_hopital = m.id_hopital " +
                "WHERE r.id_hopital = ? AND DATE(r.date_heure_rdv) = CURRENT_DATE " +
                "AND r.statut_rdv NOT IN ('ANNULE', 'ABSENT') " +
                "ORDER BY r.date_heure_rdv ASC";
        try {
            return jdbcTemplate.query(sql, (rs, rowNum) -> {
                RendezVous rdv = new RendezVous();
                rdv.setIdRdv(rs.getInt("id_rdv"));
                rdv.setIdHopital(rs.getInt("id_hopital"));
                rdv.setIdPatient(rs.getInt("id_patient"));
                rdv.setIdMedecin(rs.getInt("id_medecin"));
                Timestamp ts = rs.getTimestamp("date_heure_rdv");
                if (ts != null) rdv.setDateHeureRdv(ts.toLocalDateTime());
                rdv.setDureeEstimee(rs.getObject("duree_estimee", Integer.class));
                rdv.setMotifVisite(rs.getString("motif_visite"));
                rdv.setCanal(rs.getString("canal"));
                rdv.setStatutRdv(rs.getString("statut_rdv"));
                rdv.setUrlVisio(rs.getString("url_visio"));
                Timestamp dc = rs.getTimestamp("date_creation");
                if (dc != null) rdv.setDateCreation(dc.toLocalDateTime());
                rdv.setCreePar(rs.getObject("cree_par", Integer.class));
                rdv.setNomPatient(rs.getString("nom_patient"));
                rdv.setNomMedecin(rs.getString("nom_medecin"));
                return rdv;
            }, idHopital);
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }

    @Override
    public List<MedecinDisponibleDTO> listerMedecinsDisponibles(
            Integer idHopital, String specialiteOuService, boolean uniquementEnHoraire) {
        String jourFr = frenchDayLabel(LocalDate.now().getDayOfWeek());
        String jourEn = LocalDate.now().getDayOfWeek().name();
        String filter = StringUtils.hasText(specialiteOuService) ? specialiteOuService.trim() : null;

        String sql = """
                SELECT m.id_medecin, m.nom, m.prenom, m.specialite, m.email, m.telephone_pro,
                       m.disponibilite_status,
                       COALESCE((
                           SELECT COUNT(*) FROM admission a
                           WHERE a.id_medecin = m.id_medecin AND a.id_hopital = m.id_hopital
                             AND DATE(a.temps_arrivee) = CURRENT_DATE
                             AND a.statut IN ('EN_ATTENTE', 'ENREGISTRE', 'EN_CONSULTATION')
                       ), 0) AS patients_en_file,
                       COALESCE((
                           SELECT COUNT(*) FROM medecin_patient mp
                           WHERE mp.id_medecin = m.id_medecin
                       ), 0) AS patients_assignes,
                       EXISTS (
                           SELECT 1 FROM horaire_travaille h
                           WHERE h.medecin_id = m.id_medecin AND h.hopital_id = m.id_hopital
                             AND UPPER(h.jour_semaine) IN (UPPER(?), UPPER(?))
                             AND CURTIME() BETWEEN h.heure_debut AND h.heure_fin
                       ) AS en_horaire
                FROM medecin m
                WHERE m.id_hopital = ?
                  AND COALESCE(m.disponibilite_status, 1) = 1
                  AND (? IS NULL OR UPPER(COALESCE(m.specialite, '')) LIKE UPPER(CONCAT('%', ?, '%')))
                ORDER BY en_horaire DESC, patients_en_file ASC, patients_assignes ASC, m.nom ASC, m.prenom ASC
                """;

        List<MedecinDisponibleDTO> list = jdbcTemplate.query(sql, (rs, rowNum) -> {
            MedecinDisponibleDTO dto = new MedecinDisponibleDTO();
            dto.setIdMedecin(rs.getInt("id_medecin"));
            dto.setNom(rs.getString("nom"));
            dto.setPrenom(rs.getString("prenom"));
            String full = ((rs.getString("prenom") != null ? rs.getString("prenom") : "") + " "
                    + (rs.getString("nom") != null ? rs.getString("nom") : "")).trim();
            dto.setNomComplet(full.isBlank() ? "—" : full);
            dto.setSpecialite(rs.getString("specialite"));
            dto.setService(rs.getString("specialite"));
            dto.setDisponible(rs.getBoolean("disponibilite_status"));
            dto.setEnHoraire(rs.getBoolean("en_horaire"));
            dto.setPatientsEnFile(rs.getLong("patients_en_file"));
            dto.setPatientsAssignes(rs.getLong("patients_assignes"));
            dto.setEmail(rs.getString("email"));
            dto.setTelephonePro(rs.getString("telephone_pro"));
            return dto;
        }, jourFr, jourEn, idHopital, filter, filter);

        if (uniquementEnHoraire) {
            return list.stream().filter(MedecinDisponibleDTO::isEnHoraire).toList();
        }
        return list;
    }

    @Override
    public List<String> listerSpecialites(Integer idHopital) {
        String sql = """
                SELECT DISTINCT specialite FROM medecin
                WHERE id_hopital = ? AND specialite IS NOT NULL AND TRIM(specialite) <> ''
                ORDER BY specialite
                """;
        try {
            return jdbcTemplate.query(sql, (rs, rowNum) -> rs.getString("specialite"), idHopital);
        } catch (Exception e) {
            return List.of();
        }
    }

    @Override
    public Integer creerAdmissionRetourId(Admission a) {
        String sql = "INSERT INTO admission (id_hopital, id_patient, id_medecin, id_rendez_vous, niveau_priorite, "
                + "temps_arrivee, statut, cree_par, check_in_par, type_visite, motif_general, service_demande, "
                + "observations_admin, mode_paiement) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();
        try {
            jdbcTemplate.update(connection -> {
                PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
                bindAdmissionInsert(ps, a, true);
                return ps;
            }, keyHolder);
        } catch (Exception ex) {
            // Schéma sans colonnes visite : fallback minimal
            String fallback = "INSERT INTO admission (id_hopital, id_patient, id_medecin, id_rendez_vous, "
                    + "niveau_priorite, temps_arrivee, statut, cree_par, check_in_par) "
                    + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
            jdbcTemplate.update(connection -> {
                PreparedStatement ps = connection.prepareStatement(fallback, Statement.RETURN_GENERATED_KEYS);
                bindAdmissionInsert(ps, a, false);
                return ps;
            }, keyHolder);
        }
        Number key = keyHolder.getKey();
        return key != null ? key.intValue() : null;
    }

    private void bindAdmissionInsert(PreparedStatement ps, Admission a, boolean withVisitFields)
            throws java.sql.SQLException {
        ps.setInt(1, a.getIdHopital());
        ps.setInt(2, a.getIdPatient());
        if (a.getIdMedecin() != null) {
            ps.setInt(3, a.getIdMedecin());
        } else {
            ps.setNull(3, Types.INTEGER);
        }
        if (a.getIdRendezVous() != null) {
            ps.setInt(4, a.getIdRendezVous());
        } else {
            ps.setNull(4, Types.INTEGER);
        }
        ps.setInt(5, a.getNiveauPriorite() != null ? a.getNiveauPriorite() : 3);
        ps.setTimestamp(6, Timestamp.valueOf(
                a.getTempsArrivee() == null ? LocalDateTime.now() : a.getTempsArrivee()));
        ps.setString(7, a.getStatut() != null ? a.getStatut() : "ATTENTE_TRIAGE");
        if (a.getCreePar() != null) {
            ps.setInt(8, a.getCreePar());
        } else {
            ps.setNull(8, Types.INTEGER);
        }
        if (a.getCheckInPar() != null) {
            ps.setInt(9, a.getCheckInPar());
        } else {
            ps.setNull(9, Types.INTEGER);
        }
        if (!withVisitFields) {
            return;
        }
        ps.setString(10, a.getTypeVisite());
        ps.setString(11, a.getMotifGeneral());
        ps.setString(12, a.getServiceDemande());
        ps.setString(13, a.getObservationsAdmin());
        ps.setString(14, a.getModePaiement());
    }

    @Override
    public Integer allouerNumeroPassage(Integer idAdmission, Integer idHopital) {
        Integer max = jdbcTemplate.queryForObject(
                """
                SELECT COALESCE(MAX(numero_passage), 0)
                FROM admission
                WHERE id_hopital = ? AND DATE(temps_arrivee) = CURRENT_DATE
                """,
                Integer.class,
                idHopital
        );
        int prochain = (max == null ? 0 : max) + 1;
        try {
            jdbcTemplate.update(
                    """
                    UPDATE admission
                    SET numero_passage = COALESCE(numero_passage, ?)
                    WHERE id_admission = ? AND id_hopital = ?
                    """,
                    prochain, idAdmission, idHopital
            );
        } catch (Exception ex) {
            // Colonnes absentes : ignorer le numéro de passage
            return null;
        }
        return prochain;
    }

    private String frenchDayLabel(DayOfWeek day) {
        return switch (day) {
            case MONDAY -> "Lundi";
            case TUESDAY -> "Mardi";
            case WEDNESDAY -> "Mercredi";
            case THURSDAY -> "Jeudi";
            case FRIDAY -> "Vendredi";
            case SATURDAY -> "Samedi";
            case SUNDAY -> "Dimanche";
        };
    }
}
