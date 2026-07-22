package hospicloud.repositoriesImpl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import hospicloud.dtos.TeleconsultationReminderCandidate;
import hospicloud.dtos.events.RendezVousModifieEvent;
import hospicloud.events.EventProducer;
import hospicloud.exceptions.RendezVousModificationNotAllowedException;
import hospicloud.exceptions.rendezvous.RendezVousConflictException;
import hospicloud.exceptions.rendezvous.RendezVousNotFoundException;
import hospicloud.model.RendezVous;
import hospicloud.repositories.RendezVousRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import java.sql.*;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

@Repository
public class RendezVousRepositoryImpl implements RendezVousRepository {

    private final JdbcTemplate jdbcTemplate;
    private final JedisPool redisPool;
    private final ObjectMapper objectMapper;

    private final EventProducer eventProducer;

    @Autowired
    public RendezVousRepositoryImpl(JdbcTemplate jdbcTemplate,
                                    @Autowired(required = false) JedisPool redisPool,EventProducer eventProducer) {
        this.jdbcTemplate = jdbcTemplate;
        this.redisPool = redisPool;
        this.objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());
        this.eventProducer = eventProducer;
    }

    //=====================================================
    // SAFE DATE CONVERTER
    //=====================================================
    private LocalDateTime toLocalDateTime(Object obj) {
        if (obj == null) return null;

        if (obj instanceof Timestamp) {
            return ((Timestamp) obj).toLocalDateTime();
        }
        if (obj instanceof LocalDateTime) {
            return (LocalDateTime) obj;
        }
        if (obj instanceof java.util.Date) {
            return new Timestamp(((java.util.Date) obj).getTime()).toLocalDateTime();
        }
        if (obj instanceof String) {
            return LocalDateTime.parse((String) obj);
        }
        throw new IllegalArgumentException("Type date inconnu: " + obj.getClass());
    }

    // =====================================================
    // ROW MAPPER
    // =====================================================
    private final class RendezVousRowMapper implements RowMapper<RendezVous> {
        @Override
        public RendezVous mapRow(ResultSet rs, int rowNum) throws SQLException {
            RendezVous r = new RendezVous();
            r.setIdRdv(rs.getObject("id_rdv", Integer.class));
            r.setIdHopital(rs.getObject("id_hopital", Integer.class));
            r.setIdPatient(rs.getObject("id_patient", Integer.class));
            r.setIdMedecin(rs.getObject("id_medecin", Integer.class));
            r.setDateHeureRdv(toLocalDateTime(rs.getObject("date_heure_rdv")));
            r.setDureeEstimee(rs.getObject("duree_estimee", Integer.class));
            r.setMotifVisite(rs.getString("motif_visite"));
            r.setCanal(rs.getString("canal"));
            r.setStatutRdv(rs.getString("statut_rdv"));
            r.setDateCreation(toLocalDateTime(rs.getObject("date_creation")));
            r.setCreePar(rs.getObject("cree_par", Integer.class));
            try {
                r.setUrlVisio(rs.getString("url_visio"));
            } catch (SQLException ignored) {
                // Colonne absente avant migration.
            }
            try {
                r.setNomPatient(rs.getString("nom_patient"));
            } catch (SQLException ignored) {
                // Colonne absente sur les requêtes sans jointure patient.
            }
            try {
                r.setNomMedecin(rs.getString("nom_medecin"));
            } catch (SQLException ignored) {
                // Colonne absente sur les requêtes sans jointure médecin.
            }
            return r;
        }
    }

    // =====================================================
    // CONSULTATION ET LISTES
    // =====================================================
    @Override
    public List<RendezVous> listerParMedecin(Integer idMedecin) {
        Integer hopitalId = hospicloud.security.TenantContext.getRequiredHopitalId();
        if (idMedecin == null) return new ArrayList<>();

        final String sql =
                "SELECT r.*, TRIM(CONCAT(COALESCE(p.prenom, ''), ' ', COALESCE(p.nom, ''))) AS nom_patient " +
                "FROM rendez_vous01 r " +
                "LEFT JOIN patients p ON r.id_patient = p.id_patient AND r.id_hopital = p.id_hopital " +
                "WHERE r.id_medecin = ? AND r.id_hopital = ? ORDER BY r.date_heure_rdv DESC";
        return jdbcTemplate.query(sql, new RendezVousRowMapper(), idMedecin, hopitalId);
    }

    @Override
    public List<RendezVous> listerParMedecinEtDate(Integer idMedecin, LocalDate date) {
        Integer hopitalId = hospicloud.security.TenantContext.getRequiredHopitalId();
        if (idMedecin == null || date == null) return new ArrayList<>();

        LocalDateTime start = date.atStartOfDay();
        LocalDateTime end = date.plusDays(1).atStartOfDay();

        final String sql =
                "SELECT r.*, TRIM(CONCAT(COALESCE(p.prenom, ''), ' ', COALESCE(p.nom, ''))) AS nom_patient " +
                "FROM rendez_vous01 r " +
                "LEFT JOIN patients p ON r.id_patient = p.id_patient AND r.id_hopital = p.id_hopital " +
                "WHERE r.id_medecin = ? AND r.id_hopital = ? " +
                "AND r.date_heure_rdv >= ? AND r.date_heure_rdv < ? ORDER BY r.date_heure_rdv ASC";

        return jdbcTemplate.query(sql,
                new RendezVousRowMapper(),
                idMedecin,
                hopitalId,
                Timestamp.valueOf(start),
                Timestamp.valueOf(end));
    }

    @Override
    public List<RendezVous> listerRendezVousDuJourParMedecin(Integer idMedecin) {
        Integer hopitalId = hospicloud.security.TenantContext.getRequiredHopitalId();
        if (idMedecin == null) return new ArrayList<>();

        // Clé de cache segmentée par hôpital ET par médecin
        String cacheKey = "rdv_jour_hosp:" + hopitalId + ":" + idMedecin;

        if (redisPool != null) {
            try (Jedis jedis = redisPool.getResource()) {
                String cached = jedis.get(cacheKey);
                if (cached != null) {
                    return objectMapper.readValue(
                            cached,
                            objectMapper.getTypeFactory().constructCollectionType(List.class, RendezVous.class)
                    );
                }
            } catch (Exception ignored) {}
        }

        LocalDateTime start = LocalDate.now().atStartOfDay();
        LocalDateTime end = LocalDate.now().plusDays(1).atStartOfDay();

        final String sql =
                "SELECT r.*, TRIM(CONCAT(COALESCE(p.prenom, ''), ' ', COALESCE(p.nom, ''))) AS nom_patient " +
                "FROM rendez_vous01 r " +
                "LEFT JOIN patients p ON r.id_patient = p.id_patient AND r.id_hopital = p.id_hopital " +
                "WHERE r.id_hopital = ? AND r.id_medecin = ? " +
                "AND r.date_heure_rdv >= ? AND r.date_heure_rdv < ? ORDER BY r.date_heure_rdv ASC";

        List<RendezVous> result = jdbcTemplate.query(sql,
                new RendezVousRowMapper(),
                hopitalId,
                idMedecin,
                Timestamp.valueOf(start),
                Timestamp.valueOf(end));

        if (redisPool != null) {
            try (Jedis jedis = redisPool.getResource()) {
                jedis.setex(cacheKey, 300, objectMapper.writeValueAsString(result));
            } catch (Exception ignored) {}
        }

        return result;
    }

    // =====================================================
    // DÉTAIL
    // =====================================================
    @Override
    public RendezVous trouverParId(Integer idRdv) {
        Integer hopitalId = hospicloud.security.TenantContext.getRequiredHopitalId();
        if (idRdv == null) return null;

        final String sql =
                "SELECT r.*, TRIM(CONCAT(COALESCE(p.prenom, ''), ' ', COALESCE(p.nom, ''))) AS nom_patient " +
                "FROM rendez_vous01 r " +
                "LEFT JOIN patients p ON r.id_patient = p.id_patient AND r.id_hopital = p.id_hopital " +
                "WHERE r.id_rdv = ? AND r.id_hopital = ?";
        try {
            return jdbcTemplate.queryForObject(sql, new RendezVousRowMapper(), idRdv, hopitalId);
        } catch (EmptyResultDataAccessException e) {
            return null; // ou throw new RendezVousNotFoundException(idRdv); selon votre préférence métier
        }
    }

    // =====================================================
    // VÉRIFICATION DISPONIBILITÉ
    // =====================================================
    private int getDureeFromRow(Map<String, Object> row) {
        Object raw = getColumnValue(row, "duree_estimee");
        return raw != null ? ((Number) raw).intValue() : 30;
    }

    private LocalDateTime getDateHeureFromRow(Map<String, Object> row) {
        return toLocalDateTime(getColumnValue(row, "date_heure_rdv"));
    }

    private Object getColumnValue(Map<String, Object> row, String column) {
        if (row.containsKey(column)) {
            return row.get(column);
        }
        for (Map.Entry<String, Object> entry : row.entrySet()) {
            if (entry.getKey().equalsIgnoreCase(column)) {
                return entry.getValue();
            }
        }
        return null;
    }

    private List<Map<String, Object>> chargerCreneauxOccupes(String sql, Object... params) {
        return jdbcTemplate.query(sql, (rs, rowNum) -> {
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("date_heure_rdv", rs.getTimestamp("date_heure_rdv"));
            row.put("duree_estimee", rs.getObject("duree_estimee"));
            return row;
        }, params);
    }

    private int normaliserDuree(Integer dureeMinutes) {
        return dureeMinutes != null && dureeMinutes > 0 ? dureeMinutes : 30;
    }

    private boolean creneauxSeChevauchent(
            LocalDateTime debut1,
            int duree1,
            LocalDateTime debut2,
            int duree2) {
        if (debut1 == null || debut2 == null) return false;
        LocalDateTime fin1 = debut1.plusMinutes(normaliserDuree(duree1));
        LocalDateTime fin2 = debut2.plusMinutes(normaliserDuree(duree2));
        return debut1.isBefore(fin2) && debut2.isBefore(fin1);
    }

    private boolean existeConflitCreneau(
            Integer idMedecin,
            LocalDateTime debut,
            Integer dureeMinutes,
            Integer idRdvExclu) {
        Integer hopitalId = hospicloud.security.TenantContext.getRequiredHopitalId();
        int duree = normaliserDuree(dureeMinutes);
        LocalDateTime fin = debut.plusMinutes(duree);

        final String sqlBase =
                "SELECT COUNT(*) FROM rendez_vous01 " +
                "WHERE id_medecin = ? AND id_hopital = ? AND statut_rdv <> 'ANNULE' " +
                "AND ? < DATE_ADD(date_heure_rdv, INTERVAL COALESCE(NULLIF(duree_estimee, 0), 30) MINUTE) " +
                "AND date_heure_rdv < ? ";

        Long count;
        if (idRdvExclu == null) {
            count = jdbcTemplate.queryForObject(
                    sqlBase,
                    Long.class,
                    idMedecin,
                    hopitalId,
                    Timestamp.valueOf(debut),
                    Timestamp.valueOf(fin));
        } else {
            count = jdbcTemplate.queryForObject(
                    sqlBase + "AND id_rdv <> ?",
                    Long.class,
                    idMedecin,
                    hopitalId,
                    Timestamp.valueOf(debut),
                    Timestamp.valueOf(fin),
                    idRdvExclu);
        }

        return count != null && count > 0;
    }

    @Override
    public boolean estCreneauLibre(Integer idMedecin, LocalDateTime dateHeure) {
        return estCreneauLibre(idMedecin, dateHeure, 30);
    }

    @Override
    public boolean estCreneauLibre(Integer idMedecin, LocalDateTime dateHeure, Integer dureeMinutes) {
        if (dateHeure == null) throw new IllegalArgumentException("dateHeure null");
        if (idMedecin == null) throw new IllegalArgumentException("idMedecin null");
        return !existeConflitCreneau(idMedecin, dateHeure, dureeMinutes, null);
    }

    private boolean estCreneauLibreExcluantRdv(
            Integer idMedecin,
            LocalDateTime dateHeure,
            Integer dureeMinutes,
            Integer idRdvAExclure) {
        return !existeConflitCreneau(idMedecin, dateHeure, dureeMinutes, idRdvAExclure);
    }

    // =====================================================
    // CRÉATION ET MODIFICATION
    // =====================================================
    @Override
    @Transactional
    public RendezVous creer(RendezVous rdv) {
        Integer hopitalId = hospicloud.security.TenantContext.getRequiredHopitalId();
        if (rdv == null) throw new IllegalArgumentException("RDV null");

        if (!estCreneauLibre(rdv.getIdMedecin(), rdv.getDateHeureRdv(), rdv.getDureeEstimee())) {
            throw new RendezVousConflictException();
        }

        final String sql =
                "INSERT INTO rendez_vous01 (id_hopital, id_patient, id_medecin, date_heure_rdv, " +
                "duree_estimee, motif_visite, canal, statut_rdv, url_visio, date_creation, cree_par) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(conn -> {
            PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setObject(1, hopitalId);
            ps.setObject(2, rdv.getIdPatient());
            ps.setObject(3, rdv.getIdMedecin());
            ps.setTimestamp(4, Timestamp.valueOf(rdv.getDateHeureRdv()));
            ps.setObject(5, rdv.getDureeEstimee());
            ps.setString(6, rdv.getMotifVisite());
            ps.setString(7, rdv.getCanal());
            ps.setString(8, rdv.getStatutRdv() != null ? rdv.getStatutRdv() : "PROGRAMME");
            ps.setString(9, rdv.getUrlVisio());
            ps.setTimestamp(10, Timestamp.valueOf(LocalDateTime.now()));
            ps.setObject(11, rdv.getCreePar());
            rdv.setDateCreation(LocalDateTime.now());
            return ps;
        }, keyHolder);

        if (keyHolder.getKey() != null) {
            rdv.setIdRdv(keyHolder.getKey().intValue());
        }

        nettoyerCache(hopitalId, rdv.getIdMedecin());
        return rdv;
    }

    @Override
    @Transactional
    public void modifierRendezVous(RendezVous rdv) {
        Integer hopitalId = hospicloud.security.TenantContext.getRequiredHopitalId();
        if (rdv == null || rdv.getIdRdv() == null) throw new IllegalArgumentException("RDV invalide pour modification");
        
        RendezVous rdvExistant = trouverParId(rdv.getIdRdv());

        if (rdvExistant == null) {
            throw new RendezVousNotFoundException(rdv.getIdRdv());
        }

        long heuresRestantes = Duration.between(
                LocalDateTime.now(),
                rdvExistant.getDateHeureRdv()
        ).toHours();

        if (heuresRestantes <= 48) {
            throw new RendezVousModificationNotAllowedException();
        }

        // Sécurité : On vérifie que le créneau est libre (en excluant ce RDV lui-même de la vérification)
        if (!estCreneauLibreExcluantRdv(rdv.getIdMedecin(), rdv.getDateHeureRdv(), rdv.getDureeEstimee(), rdv.getIdRdv())) {
            throw new RendezVousConflictException();
        }

        int rows = jdbcTemplate.update(
                "UPDATE rendez_vous01 SET date_heure_rdv=?, duree_estimee=?, motif_visite=?, canal=?, statut_rdv=?, rappel_30min_envoye_at=NULL " +
                "WHERE id_rdv=? AND id_hopital=?",
                Timestamp.valueOf(rdv.getDateHeureRdv()),
                rdv.getDureeEstimee(),
                rdv.getMotifVisite(),
                rdv.getCanal(),
                rdv.getStatutRdv(),
                rdv.getIdRdv(),
                hopitalId
        );

        if (rows == 0) throw new RendezVousNotFoundException(rdv.getIdRdv());

        nettoyerCache(hopitalId, rdv.getIdMedecin());
    }

    @Override
    @Transactional
    public void reporterRendezVous(Integer idRdv, LocalDateTime nouvelleDate) {

        Integer hopitalId = hospicloud.security.TenantContext.getRequiredHopitalId();

        if (idRdv == null || nouvelleDate == null) {
            throw new IllegalArgumentException("Paramètres de report invalides");
        }

        RendezVous rdvActuel = trouverParId(idRdv);

        if (rdvActuel == null) {
            throw new RendezVousNotFoundException(idRdv);
        }

        // ============================
        // ⛔ RÈGLE MÉTIER : 48H
        // ============================
        long heuresRestantes = Duration.between(
                LocalDateTime.now(),
                rdvActuel.getDateHeureRdv()
        ).toHours();

        if (heuresRestantes <= 48) {
            throw new RendezVousModificationNotAllowedException(
                    "Le rendez-vous ne peut plus être reporté moins de 48 heures avant la consultation."
            );
        }

        // ============================
        // ⛔ CONFLIT DE CRÉNEAU
        // ============================
        if (!estCreneauLibreExcluantRdv(
                rdvActuel.getIdMedecin(),
                nouvelleDate,
                rdvActuel.getDureeEstimee(),
                idRdv)) {

            throw new RendezVousConflictException();
        }

        // ============================
        // 💾 UPDATE BDD
        // ============================
        int rows = jdbcTemplate.update(
                "UPDATE rendez_vous01 SET date_heure_rdv=?, statut_rdv='PROGRAMME', rappel_30min_envoye_at=NULL " +
                        "WHERE id_rdv=? AND id_hopital=?",
                Timestamp.valueOf(nouvelleDate),
                idRdv,
                hopitalId
        );

        if (rows == 0) {
            throw new RendezVousNotFoundException(idRdv);
        }

        // ============================
        // 🧹 CACHE
        // ============================
        nettoyerCache(hopitalId, rdvActuel.getIdMedecin());

        // ============================
        // 📦 EVENT SAAS (RABBITMQ)
        // ============================
        RendezVousModifieEvent event = new RendezVousModifieEvent(
                rdvActuel.getIdRdv(),
                rdvActuel.getIdMedecin(),
                rdvActuel.getIdPatient(),
                hopitalId,
                rdvActuel.getDateHeureRdv(),
                nouvelleDate
        );
        System.out.println("===== VERIFICATION RDV =====");
        System.out.println("RDV ID = " + rdvActuel.getIdRdv());
        System.out.println("MEDECIN ID (DB) = " + rdvActuel.getIdMedecin());
        System.out.println("PATIENT ID = " + rdvActuel.getIdPatient());

        eventProducer.publishRendezVousModifie(event);
    
    }    // =====================================================
    // CHANGEMENTS DE STATUT (CYCLE DE VIE)
    // =====================================================
    @Override
    @Transactional
    public void confirmerPresence(Integer idRdv) {
        changerStatutRdv(idRdv, "CONFIRME");
    }

    @Override
    @Transactional
    public void annulerRendezVous(Integer idRdv) {
        changerStatutRdv(idRdv, "ANNULE");
    }

    @Override
    @Transactional
    public void marquerCommeAbsent(Integer idRdv) {
        changerStatutRdv(idRdv, "ABSENT");
    }

    @Override
    @Transactional
    public void marquerCommeTermine(Integer idRdv) {
        changerStatutRdv(idRdv, "VALIDE");
    }

    @Override
    @Transactional
    public void mettreAJourStatut(Integer idRdv, String statut) {
        if (statut == null || statut.isBlank()) {
            throw new IllegalArgumentException("Statut RDV requis");
        }
        changerStatutRdv(idRdv, statut.trim().toUpperCase());
    }

    // Facto interne pour éviter la duplication des updates de statut
    private void changerStatutRdv(Integer idRdv, String nouveauStatut) {
        Integer hopitalId = hospicloud.security.TenantContext.getRequiredHopitalId();
        if (idRdv == null) throw new IllegalArgumentException("ID RDV null");

        RendezVous rdv = trouverParId(idRdv);
        if (rdv == null) throw new RendezVousNotFoundException(idRdv);

        int updated = jdbcTemplate.update(
                "UPDATE rendez_vous01 SET statut_rdv=? WHERE id_rdv=? AND id_hopital=?",
                nouveauStatut, idRdv, hopitalId);

        if (updated == 0) throw new RendezVousNotFoundException(idRdv);

        nettoyerCache(hopitalId, rdv.getIdMedecin());
    }

    @Override
    public List<RendezVous> listerParHopital() {
        return listerParHopital(null);
    }

    @Override
    public List<RendezVous> listerParHopital(Integer creePar) {
        Integer hopitalId = hospicloud.security.TenantContext.getRequiredHopitalId();
        final String sql = creePar != null
                ? "SELECT r.*, TRIM(CONCAT(COALESCE(p.prenom, ''), ' ', COALESCE(p.nom, ''))) AS nom_patient "
                + "FROM rendez_vous01 r "
                + "LEFT JOIN patients p ON r.id_patient = p.id_patient AND r.id_hopital = p.id_hopital "
                + "WHERE r.id_hopital = ? AND r.cree_par = ? ORDER BY r.date_heure_rdv DESC"
                : "SELECT r.*, TRIM(CONCAT(COALESCE(p.prenom, ''), ' ', COALESCE(p.nom, ''))) AS nom_patient "
                + "FROM rendez_vous01 r "
                + "LEFT JOIN patients p ON r.id_patient = p.id_patient AND r.id_hopital = p.id_hopital "
                + "WHERE r.id_hopital = ? ORDER BY r.date_heure_rdv DESC";
        return creePar != null
                ? jdbcTemplate.query(sql, new RendezVousRowMapper(), hopitalId, creePar)
                : jdbcTemplate.query(sql, new RendezVousRowMapper(), hopitalId);
    }

    @Override
    public List<RendezVous> listerParPatient(Integer idPatient) {
        Integer hopitalId = hospicloud.security.TenantContext.getRequiredHopitalId();
        if (idPatient == null) return new ArrayList<>();

        final String sql =
                "SELECT r.*, TRIM(CONCAT(COALESCE(m.prenom, ''), ' ', COALESCE(m.nom, ''))) AS nom_medecin " +
                "FROM rendez_vous01 r " +
                "LEFT JOIN medecin m ON r.id_medecin = m.id_medecin AND r.id_hopital = m.id_hopital " +
                "WHERE r.id_patient = ? AND r.id_hopital = ? ORDER BY r.date_heure_rdv DESC";

        return jdbcTemplate.query(sql, new RendezVousRowMapper(), idPatient, hopitalId);
    }

    @Override
    @Transactional
    public void mettreAJourUrlVisio(Integer idRdv, String urlVisio) {
        Integer hopitalId = hospicloud.security.TenantContext.getRequiredHopitalId();
        jdbcTemplate.update(
                "UPDATE rendez_vous01 SET url_visio = ? WHERE id_rdv = ? AND id_hopital = ?",
                urlVisio, idRdv, hopitalId);
    }

    @Override
    public List<TeleconsultationReminderCandidate> listerTeleconsultationsPourRappel(
            LocalDateTime fenetreDebut,
            LocalDateTime fenetreFin) {
        if (fenetreDebut == null || fenetreFin == null) {
            return List.of();
        }

        final String sql =
                "SELECT r.id_rdv, r.id_hopital, r.date_heure_rdv, r.url_visio, r.motif_visite, "
                        + "p.email AS email_patient, "
                        + "TRIM(CONCAT(COALESCE(p.prenom, ''), ' ', COALESCE(p.nom, ''))) AS nom_patient, "
                        + "m.email AS email_medecin, "
                        + "m.telephone_pro AS telephone_medecin, "
                        + "p.telephone AS telephone_patient, "
                        + "TRIM(CONCAT(COALESCE(m.prenom, ''), ' ', COALESCE(m.nom, ''))) AS nom_medecin, "
                        + "COALESCE(NULLIF(TRIM(h.nom_commercial), ''), h.nom) AS nom_hopital "
                        + "FROM rendez_vous01 r "
                        + "INNER JOIN hopitaux h ON r.id_hopital = h.id_hopital AND h.est_actif = TRUE "
                        + "INNER JOIN patients p ON r.id_patient = p.id_patient AND r.id_hopital = p.id_hopital "
                        + "INNER JOIN medecin m ON r.id_medecin = m.id_medecin AND r.id_hopital = m.id_hopital "
                        + "WHERE UPPER(r.canal) = 'TELECONSULTATION' "
                        + "AND UPPER(r.statut_rdv) NOT IN ('ANNULE', 'ABSENT') "
                        + "AND r.date_heure_rdv >= ? AND r.date_heure_rdv <= ? "
                        + "AND r.rappel_30min_envoye_at IS NULL "
                        + "ORDER BY r.date_heure_rdv ASC";

        return jdbcTemplate.query(
                sql,
                (rs, rowNum) -> {
                    TeleconsultationReminderCandidate c = new TeleconsultationReminderCandidate();
                    c.setIdRdv(rs.getObject("id_rdv", Integer.class));
                    c.setIdHopital(rs.getObject("id_hopital", Integer.class));
                    c.setDateHeureRdv(toLocalDateTime(rs.getObject("date_heure_rdv")));
                    c.setUrlVisio(rs.getString("url_visio"));
                    c.setMotifVisite(rs.getString("motif_visite"));
                    c.setEmailPatient(rs.getString("email_patient"));
                    c.setNomPatient(rs.getString("nom_patient"));
                    c.setEmailMedecin(rs.getString("email_medecin"));
                    c.setTelephonePatient(rs.getString("telephone_patient"));
                    c.setTelephoneMedecin(rs.getString("telephone_medecin"));
                    c.setNomMedecin(rs.getString("nom_medecin"));
                    c.setNomHopital(rs.getString("nom_hopital"));
                    return c;
                },
                Timestamp.valueOf(fenetreDebut),
                Timestamp.valueOf(fenetreFin));
    }

    @Override
    @Transactional
    public boolean reclamerRappel30Min(Integer idRdv, Integer idHopital) {
        if (idRdv == null || idHopital == null) {
            return false;
        }
        int updated = jdbcTemplate.update(
                "UPDATE rendez_vous01 SET rappel_30min_envoye_at = ? "
                        + "WHERE id_rdv = ? AND id_hopital = ? AND rappel_30min_envoye_at IS NULL",
                Timestamp.valueOf(LocalDateTime.now()),
                idRdv,
                idHopital);
        return updated > 0;
    }

    @Override
    @Transactional
    public void reinitialiserRappel30Min(Integer idRdv, Integer idHopital) {
        if (idRdv == null || idHopital == null) {
            return;
        }
        jdbcTemplate.update(
                "UPDATE rendez_vous01 SET rappel_30min_envoye_at = NULL WHERE id_rdv = ? AND id_hopital = ?",
                idRdv,
                idHopital);
    }

    // =====================================================
    // NETTOYAGE DU CACHE (REDIS)
    // =====================================================
    private void nettoyerCache(Integer hopitalId, Integer idMedecin) {
        if (redisPool != null && hopitalId != null && idMedecin != null) {
            try (Jedis jedis = redisPool.getResource()) {
                String cacheKey = "rdv_jour_hosp:" + hopitalId + ":" + idMedecin;
                jedis.del(cacheKey);
            } catch (Exception e) {
                System.err.println("Redis log error (non-blocking): " + e.getMessage());
            }
        }
    }
}