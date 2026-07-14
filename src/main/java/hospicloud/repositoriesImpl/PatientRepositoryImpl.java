package hospicloud.repositoriesImpl;

import hospicloud.model.Patient;
import hospicloud.repositories.PatientRepository;
import hospicloud.security.TenantContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Collections;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

@Repository
public class PatientRepositoryImpl implements PatientRepository {

    private final JdbcTemplate jdbcTemplate;
    private final JedisPool redisPool;
    private final ObjectMapper objectMapper;
    
    private static final Logger logger = LoggerFactory.getLogger(PatientRepositoryImpl.class);

    private static final int HOPITAL_LIST_CACHE_MAX_ITEMS = 1000;
    private static final int PATIENT_CACHE_TTL_SECONDS = 1800; // 30 minutes

    @Autowired
    public PatientRepositoryImpl(JdbcTemplate jdbcTemplate, 
                                 @Autowired(required = false) JedisPool redisPool, 
                                 @Autowired(required = false) ObjectMapper objectMapper) {
        this.jdbcTemplate = jdbcTemplate;
        this.redisPool = redisPool;
        this.objectMapper = objectMapper != null ? objectMapper : new ObjectMapper();
    }

    /* -----------------------------------------------------
     * GESTION DU CACHE REDIS (ISOLATION MULTI-TENANT)
     * ----------------------------------------------------- */

    private void invalidatePatientCache(Long idPatient, String codePatient, Integer idHopital) {
        if (redisPool == null || idHopital == null) return;
        
        try (Jedis jedis = redisPool.getResource()) {
            // 1. Invalidation par ID isolée par l'Hôpital
            if (idPatient != null) {
                jedis.del("hopital:" + idHopital + ":patient:id:" + idPatient);
            }
            
            // 2. Invalidation par code isolée par l'Hôpital
            if (codePatient != null) {
                jedis.del("hopital:" + idHopital + ":patient:code:" + codePatient);
            }
            
            // 3. Invalidation de la liste complète des patients de cet hôpital
            jedis.del("hopital:" + idHopital + ":patients");
            
            // 4. Suppression des caches spécifiques des médecins pour cet hôpital via pattern
            // (Évite les stale data si un patient change de nom ou d'état actif)
            java.util.Set<String> keys = jedis.keys("hopital:" + idHopital + ":medecin:*:patients");
            if (keys != null && !keys.isEmpty()) {
                jedis.del(keys.toArray(new String[0]));
            }
        } catch (Exception e) {
            logger.error("Erreur Redis lors de l'invalidation pour l'hôpital " + idHopital, e);
        }
    }

    private List<Patient> getHospitalListFromCache(Integer idHopital) {
        if (redisPool == null || idHopital == null) return null;
        String cacheKey = "hopital:" + idHopital + ":patients";
        
        try (Jedis jedis = redisPool.getResource()) {
            String cached = jedis.get(cacheKey);
            if (cached == null) return null;
            return objectMapper.readValue(cached, new TypeReference<List<Patient>>() {});
        } catch (Exception e) {
            logger.error("Erreur lecture cache liste patient hôpital " + idHopital, e);
            return null;
        }
    }

    private void saveHospitalListToCache(Integer idHopital, List<Patient> patients) {
        if (redisPool == null || idHopital == null || patients == null) return;
        String cacheKey = "hopital:" + idHopital + ":patients";
        
        try (Jedis jedis = redisPool.getResource()) {
            String json = objectMapper.writeValueAsString(patients);
            jedis.setex(cacheKey, PATIENT_CACHE_TTL_SECONDS, json);
        } catch (Exception e) {
            logger.error("Erreur écriture cache liste patient hôpital " + idHopital, e);
        }
    }

    private void savePatientToCache(Patient p) {
        if (redisPool == null || p == null || p.getIdHopital() == null) return;
        try (Jedis jedis = redisPool.getResource()) {
            String json = objectMapper.writeValueAsString(p);
            if (p.getIdPatient() != null) {
                jedis.setex("hopital:" + p.getIdHopital() + ":patient:id:" + p.getIdPatient(), PATIENT_CACHE_TTL_SECONDS, json);
            }
            if (p.getCodePatient() != null) {
                jedis.setex("hopital:" + p.getIdHopital() + ":patient:code:" + p.getCodePatient(), PATIENT_CACHE_TTL_SECONDS, json);
            }
        } catch (Exception e) {
            logger.error("Erreur mise en cache du patient individuel", e);
        }
    }

    /* -----------------------------------------------------
     * LOGIQUE DE SÉQUENCE (ID LOCK COMPATIBLE TOUS SGBD)
     * ----------------------------------------------------- */

    @Transactional(propagation = Propagation.REQUIRED)
    protected long getNextSequenceValue(String seqName) {
        return jdbcTemplate.execute((Connection con) -> {
            try (PreparedStatement ps = con.prepareStatement("SELECT next_val FROM sequences WHERE seq_name = ? FOR UPDATE")) {
                ps.setString(1, seqName);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        long current = rs.getLong(1);
                        long next = current + 1;
                        try (PreparedStatement ups = con.prepareStatement("UPDATE sequences SET next_val = ? WHERE seq_name = ?")) {
                            ups.setLong(1, next);
                            ups.setString(2, seqName);
                            ups.executeUpdate();
                        }
                        return current;
                    } else {
                        try (PreparedStatement ins = con.prepareStatement("INSERT INTO sequences (seq_name, next_val) VALUES (?, ?)")) {
                            ins.setString(1, seqName);
                            ins.setLong(2, 2);
                            ins.executeUpdate();
                        }
                        return 1L;
                    }
                }
            }
        });
    }

    /* -----------------------------------------------------
     * MÉTHODES CRUD CLASSIQUES (PORTÉE HÔPITAL)
     * ----------------------------------------------------- */

    @Override
    @Transactional
    public void enregistrerPatient(Patient patient) {
        if (patient == null) return;
        
        Integer hopitalId = TenantContext.getRequiredHopitalId();
        patient.setIdHopital(hopitalId); 

        final int MAX_ATTEMPTS = 5;

        for (int attempt = 0; attempt < MAX_ATTEMPTS; attempt++) {
            if (patient.getCodePatient() == null || patient.getCodePatient().trim().isEmpty() || attempt > 0) {
                patient.setCodePatient(generatePatientCode());
            }

            final String sql = "INSERT INTO patients (id_hopital, code_patient, nom, prenom, sexe, date_naissance, groupe_sanguin, adresse, telephone, email, profession, est_actif, date_enregistrement, contact_urgence, id_societe, matricule_employe, cree_par) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
            KeyHolder keyHolder = new GeneratedKeyHolder();

            try {
                jdbcTemplate.update((Connection connection) -> {
                    PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
                    ps.setObject(1, hopitalId, Types.INTEGER);
                    ps.setString(2, patient.getCodePatient());
                    ps.setString(3, patient.getNom());
                    ps.setString(4, patient.getPrenom());
                    ps.setString(5, patient.getSexe());
                    ps.setDate(6, patient.getDateNaissance() != null ? Date.valueOf(patient.getDateNaissance()) : null);
                    ps.setString(7, patient.getGroupeSanguin());
                    ps.setString(8, patient.getAdresse());
                    ps.setString(9, patient.getTelephone());
                    ps.setString(10, patient.getEmail());
                    ps.setString(11, patient.getProfession());
                    ps.setBoolean(12, patient.isEstActif());
                    ps.setTimestamp(13, Timestamp.valueOf(patient.getDateEnregistrement() != null ? patient.getDateEnregistrement() : LocalDateTime.now()));
                    ps.setString(14, patient.getContactUrgence());
                    if (patient.getIdSociete() != null) {
                        ps.setInt(15, patient.getIdSociete());
                    } else {
                        ps.setNull(15, Types.INTEGER);
                    }
                    ps.setString(16, patient.getNumeroMatricule());
                    if (patient.getCreePar() != null) {
                        ps.setInt(17, patient.getCreePar());
                    } else {
                        ps.setNull(17, Types.INTEGER);
                    }
                    return ps;
                }, keyHolder);

                if (keyHolder.getKey() != null) {
                    patient.setIdPatient(keyHolder.getKey().longValue());
                }

                invalidatePatientCache(patient.getIdPatient(), patient.getCodePatient(), hopitalId);
                savePatientToCache(patient);
                return;

            } catch (org.springframework.dao.DuplicateKeyException dkex) {
                if (attempt == MAX_ATTEMPTS - 1) throw dkex;
            }
        }
    }

    @Override
    public Optional<Patient> trouverPatientParId(Long idPatient) {
        if (idPatient == null) return Optional.empty();
        
        Integer hopitalId = TenantContext.getRequiredHopitalId();
        String cacheKey = "hopital:" + hopitalId + ":patient:id:" + idPatient;

        if (redisPool != null) {
            try (Jedis jedis = redisPool.getResource()) {
                String cached = jedis.get(cacheKey);
                if (cached != null) {
                    return Optional.of(objectMapper.readValue(cached, Patient.class));
                }
            } catch (Exception e) {
                logger.error("Erreur parsing JSON depuis le cache Redis", e);
            }
        }

        final String sql = "SELECT * FROM patients WHERE id_patient = ? AND id_hopital = ?";
        try {
            Patient p = jdbcTemplate.queryForObject(sql, (rs, rowNum) -> mapRowToPatient(rs), idPatient, hopitalId);
            if (p != null) savePatientToCache(p);
            return Optional.ofNullable(p);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    @Override
    @Transactional
    public void modifierPatient(Patient patient) {
        if (patient == null || patient.getIdPatient() == null) return;

        Integer hopitalId = TenantContext.getRequiredHopitalId();

        final String sql = "UPDATE patients SET nom = ?, prenom = ?, sexe = ?, adresse = ?, telephone = ?, email = ?, est_actif = ?, modifie_par = ? " +
                           "WHERE id_patient = ? AND id_hopital = ?";
        
        int rowsUpdated = jdbcTemplate.update(sql,
                patient.getNom(), patient.getPrenom(), patient.getSexe(),
                patient.getAdresse(), patient.getTelephone(), patient.getEmail(),
                patient.isEstActif(), patient.getModifiePar(),
                patient.getIdPatient(), hopitalId);

        if (rowsUpdated > 0) {
            invalidatePatientCache(patient.getIdPatient(), patient.getCodePatient(), hopitalId);
        }
    }

    @Override
    @Transactional
    public void supprimerPatient(Long idPatient) {
        Optional<Patient> pOpt = trouverPatientParId(idPatient);
        if (pOpt.isPresent()) {
            Patient p = pOpt.get();
            Integer hopitalId = TenantContext.getRequiredHopitalId();

            final String sql = "DELETE FROM patients WHERE id_patient = ? AND id_hopital = ?";
            int rowsDeleted = jdbcTemplate.update(sql, idPatient, hopitalId);

            if (rowsDeleted > 0) {
                invalidatePatientCache(idPatient, p.getCodePatient(), hopitalId);
            }
        }
    }

    @Override
    public List<Patient> trouverTousLesPatients() {
        return trouverTousLesPatients(null);
    }

    @Override
    public List<Patient> trouverTousLesPatients(Integer creePar) {
        Integer hopitalId = TenantContext.getRequiredHopitalId();

        if (creePar == null && redisPool != null) {
            List<Patient> cached = getHospitalListFromCache(hopitalId);
            if (cached != null) return cached;
        }

        final String sql = creePar != null
                ? "SELECT * FROM patients WHERE id_hopital = ? AND cree_par = ? ORDER BY nom ASC"
                : "SELECT * FROM patients WHERE id_hopital = ? ORDER BY nom ASC";

        List<Patient> list = creePar != null
                ? jdbcTemplate.query(sql, (rs, rowNum) -> mapRowToPatient(rs), hopitalId, creePar)
                : jdbcTemplate.query(sql, (rs, rowNum) -> mapRowToPatient(rs), hopitalId);

        if (creePar == null && redisPool != null && list.size() <= HOPITAL_LIST_CACHE_MAX_ITEMS) {
            saveHospitalListToCache(hopitalId, list);
        }

        return list;
    }

    @Override
    public Optional<Patient> trouverPatientParNumero(String numero) {
        if (numero == null || numero.trim().isEmpty()) return Optional.empty();

        Integer hopitalId = TenantContext.getRequiredHopitalId();
        String cacheKey = "hopital:" + hopitalId + ":patient:code:" + numero;

        if (redisPool != null) {
            try (Jedis jedis = redisPool.getResource()) {
                String cachedJson = jedis.get(cacheKey);
                if (cachedJson != null) {
                    return Optional.of(objectMapper.readValue(cachedJson, Patient.class));
                }
            } catch (Exception e) {
                logger.error("Erreur cache pour code_patient", e);
            }
        }

        final String sql = "SELECT * FROM patients WHERE code_patient = ? AND id_hopital = ?";
        try {
            Patient p = jdbcTemplate.queryForObject(sql, (rs, rowNum) -> mapRowToPatient(rs), numero, hopitalId);
            if (p != null) savePatientToCache(p);
            return Optional.ofNullable(p);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }
    
    @Override
    public List<Patient> rechercherParNomEtPrenom(String nom, String prenom) {
        Integer hopitalId = TenantContext.getRequiredHopitalId();
        
        final String sql = "SELECT * FROM patients WHERE nom LIKE ? AND prenom LIKE ? AND id_hopital = ? ORDER BY nom ASC"; 

        String pnom = (nom == null || nom.trim().isEmpty()) ? "%" : "%" + nom + "%";
        String pprenom = (prenom == null || prenom.trim().isEmpty()) ? "%" : "%" + prenom + "%";

        return jdbcTemplate.query(sql, (rs, rowNum) -> mapRowToPatient(rs), pnom, pprenom, hopitalId);
    }

    /* -----------------------------------------------------
     * FONCTIONNALITÉS EXCLUSIVES MÉDECIN (VOS AJUSTEMENTS)
     * ----------------------------------------------------- */
    
    @Override
    @Transactional
    public void lierPatientAMedecin(Integer idMedecin, Long idPatient) {
        if (idMedecin == null || idPatient == null) return;
        Integer hopitalId = TenantContext.getRequiredHopitalId();

        // Sécurité : On vérifie d'abord que le patient ciblé appartient bien à l'hôpital du locataire connecté
        final String sqlCheck = "SELECT COUNT(1) FROM patients WHERE id_patient = ? AND id_hopital = ?";
        Integer count = jdbcTemplate.queryForObject(sqlCheck, Integer.class, idPatient, hopitalId);

        if (count == null || count == 0) {
            throw new SecurityException("Violation de périmètre SaaS : Le patient n'appartient pas à votre établissement.");
        }

        final String sqlInsert = "INSERT IGNORE INTO medecin_patient (id_medecin, id_patient) VALUES (?, ?)";
        jdbcTemplate.update(sqlInsert, idMedecin, idPatient);

        // Invalidation des caches de listes du médecin (non bloquant si Redis est indisponible)
        if (redisPool != null) {
            try (Jedis jedis = redisPool.getResource()) {
                jedis.del("hopital:" + hopitalId + ":medecin:" + idMedecin + ":patients");
                jedis.del("hopital:" + hopitalId + ":medecin:" + idMedecin + ":patients:assigned");
            } catch (Exception e) {
                logger.warn("Redis indisponible — invalidation cache médecin {} ignorée: {}",
                        idMedecin, e.getMessage());
            }
        }
    }

    @Override
    public List<Patient> listerPatientsParMedecin(Integer idMedecin) {
        return listerPatientsAssignesAuMedecin(idMedecin);
    }

    @Override
    public List<Patient> listerPatientsAssignesAuMedecin(Integer idMedecin) {
        if (idMedecin == null) return Collections.emptyList();
        Integer hopitalId = TenantContext.getRequiredHopitalId();
        String cacheKey = "hopital:" + hopitalId + ":medecin:" + idMedecin + ":patients:assigned";

        if (redisPool != null) {
            try (Jedis jedis = redisPool.getResource()) {
                String cached = jedis.get(cacheKey);
                if (cached != null) {
                    return objectMapper.readValue(cached, new TypeReference<List<Patient>>() {});
                }
            } catch (Exception e) {
                logger.error("Erreur cache médecin assigné " + idMedecin, e);
            }
        }

        final String sql = "SELECT p.* FROM patients p " +
                           "INNER JOIN medecin_patient mp ON p.id_patient = mp.id_patient " +
                           "WHERE mp.id_medecin = ? AND p.id_hopital = ? ORDER BY p.nom ASC";

        List<Patient> list = jdbcTemplate.query(sql, (rs, rowNum) -> mapRowToPatient(rs), idMedecin, hopitalId);

        if (redisPool != null && !list.isEmpty() && list.size() <= HOPITAL_LIST_CACHE_MAX_ITEMS) {
            try (Jedis jedis = redisPool.getResource()) {
                jedis.setex(cacheKey, PATIENT_CACHE_TTL_SECONDS, objectMapper.writeValueAsString(list));
            } catch (Exception e) {
                logger.error("Erreur sauvegarde cache médecin assigné", e);
            }
        }

        return list;
    }

    @Override
    public List<Patient> rechercherPatientsDuMedecin(Integer idMedecin, String nom, String prenom) {
        if (idMedecin == null) return Collections.emptyList();
        return rechercherParNomEtPrenom(nom, prenom);
    }

    @Override
    public Patient consulterDossierPatientParMedecin(Integer idMedecin, Long idPatient) {
        if (idMedecin == null || idPatient == null) {
            throw new IllegalArgumentException("Les identifiants médecin et patient sont obligatoires.");
        }
        Integer hopitalId = TenantContext.getRequiredHopitalId();

        final String sql = "SELECT p.* FROM patients p WHERE p.id_patient = ? AND p.id_hopital = ?";
        try {
            return jdbcTemplate.queryForObject(sql, (rs, rowNum) -> mapRowToPatient(rs), idPatient, hopitalId);
        } catch (EmptyResultDataAccessException e) {
            throw new SecurityException("Accès interdit : ce patient n'appartient pas à votre établissement.");
        }
    }

    /* -----------------------------------------------------
     * HELPERS INTERNES
     * ----------------------------------------------------- */

    private String generatePatientCode() {
        Integer hopitalId = TenantContext.getRequiredHopitalId();
        String seqName = "patient_seq_hopital_" + hopitalId;
        long seq = getNextSequenceValue(seqName);
        int year = LocalDate.now().getYear();
        return String.format("PAT-%d-%04d", year, seq);
    }

    private Patient mapRowToPatient(ResultSet rs) throws SQLException {
        Patient p = new Patient();
        p.setIdPatient(rs.getLong("id_patient"));
        p.setIdHopital(rs.getObject("id_hopital", Integer.class));
        p.setCodePatient(rs.getString("code_patient"));
        p.setNom(rs.getString("nom"));
        p.setPrenom(rs.getString("prenom"));
        p.setSexe(rs.getString("sexe"));
        Date d = rs.getDate("date_naissance");
        if (d != null) p.setDateNaissance(d.toLocalDate());
        p.setGroupeSanguin(rs.getString("groupe_sanguin"));
        p.setAdresse(rs.getString("adresse"));
        p.setTelephone(rs.getString("telephone"));
        p.setEmail(rs.getString("email"));
        p.setProfession(rs.getString("profession"));
        p.setEstActif(rs.getBoolean("est_actif"));
        Timestamp ts = rs.getTimestamp("date_enregistrement");
        if (ts != null) p.setDateEnregistrement(ts.toLocalDateTime());
        p.setContactUrgence(rs.getString("contact_urgence"));
        p.setIdSociete(rs.getObject("id_societe", Integer.class));
        p.setNumeroMatricule(rs.getString("matricule_employe"));
        try {
            p.setStatutClinique(rs.getString("statut_clinique"));
        } catch (SQLException ignored) {
            p.setStatutClinique("AMBULATOIRE");
        }
        try {
            p.setCreePar(rs.getObject("cree_par", Integer.class));
            p.setModifiePar(rs.getObject("modifie_par", Integer.class));
        } catch (SQLException ignored) {
            // colonnes optionnelles avant migration
        }
        return p;
    }

    @Override
    public void mettreAJourStatutClinique(Long idPatient, String statut) {
        Integer hopitalId = TenantContext.getRequiredHopitalId();
        jdbcTemplate.update(
                "UPDATE patients SET statut_clinique = ? WHERE id_hopital = ? AND id_patient = ?",
                statut, hopitalId, idPatient);
    }

    @Override
    @Transactional
    public void syncDemoPatients() {
        final int hopitalId = 1;
        final int medecinId = 1;
        final String email = "amara.diallo@gmail.com";

        Long patientId = null;
        try {
            patientId = jdbcTemplate.queryForObject(
                    "SELECT id_patient FROM patients WHERE LOWER(email) = LOWER(?) AND id_hopital = ?",
                    Long.class,
                    email,
                    hopitalId
            );
        } catch (EmptyResultDataAccessException ignored) {
            // dossier absent — création ci-dessous
        }

        if (patientId == null) {
            Long idFromUser = null;
            try {
                idFromUser = jdbcTemplate.queryForObject(
                        "SELECT id_patient FROM utilisateurs WHERE LOWER(email) = LOWER(?)",
                        Long.class,
                        email
                );
            } catch (EmptyResultDataAccessException ignored) {
                // compte démo absent
            }
            long targetId = idFromUser != null ? idFromUser : 1L;

            Integer existingAtId = jdbcTemplate.queryForObject(
                    "SELECT COUNT(*) FROM patients WHERE id_patient = ?",
                    Integer.class,
                    targetId
            );

            if (existingAtId != null && existingAtId == 0) {
                jdbcTemplate.update(
                        """
                        INSERT INTO patients
                        (id_patient, id_hopital, code_patient, nom, prenom, sexe, date_naissance, email, telephone, est_actif, date_enregistrement)
                        VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, TRUE, NOW())
                        """,
                        targetId,
                        hopitalId,
                        "PAT-DEMO-0001",
                        "Diallo",
                        "Amara",
                        "F",
                        Date.valueOf(LocalDate.of(1995, 3, 12)),
                        email,
                        "+243810000001"
                );
                patientId = targetId;
            } else {
                KeyHolder keyHolder = new GeneratedKeyHolder();
                jdbcTemplate.update(connection -> {
                    PreparedStatement ps = connection.prepareStatement(
                            """
                            INSERT INTO patients
                            (id_hopital, code_patient, nom, prenom, sexe, date_naissance, email, telephone, est_actif, date_enregistrement)
                            VALUES (?, ?, ?, ?, ?, ?, ?, ?, TRUE, NOW())
                            """,
                            Statement.RETURN_GENERATED_KEYS
                    );
                    ps.setInt(1, hopitalId);
                    ps.setString(2, "PAT-DEMO-0001");
                    ps.setString(3, "Diallo");
                    ps.setString(4, "Amara");
                    ps.setString(5, "F");
                    ps.setDate(6, Date.valueOf(LocalDate.of(1995, 3, 12)));
                    ps.setString(7, email);
                    ps.setString(8, "+243810000001");
                    return ps;
                }, keyHolder);
                if (keyHolder.getKey() != null) {
                    patientId = keyHolder.getKey().longValue();
                }
            }
        }

        if (patientId != null) {
            jdbcTemplate.update(
                    """
                    UPDATE patients
                    SET nom = ?, prenom = ?, sexe = ?, email = ?, est_actif = TRUE
                    WHERE id_patient = ? AND id_hopital = ?
                    """,
                    "Diallo",
                    "Amara",
                    "F",
                    email,
                    patientId,
                    hopitalId
            );
            jdbcTemplate.update(
                    "UPDATE utilisateurs SET id_patient = ?, est_actif = TRUE WHERE LOWER(email) = LOWER(?)",
                    patientId,
                    email
            );
            jdbcTemplate.update(
                    "INSERT IGNORE INTO medecin_patient (id_medecin, id_patient) VALUES (?, ?)",
                    medecinId,
                    patientId
            );
        }

        jdbcTemplate.update(
                "UPDATE utilisateurs SET est_actif = TRUE WHERE LOWER(email) = LOWER(?)",
                "ngozi.achebe@shambua.health"
        );

        ensureDemoTeleconsultation(hopitalId, medecinId, patientId != null ? patientId : 1L);
    }

    private void ensureDemoTeleconsultation(int hopitalId, int medecinId, long patientId) {
        Integer existingId = null;
        try {
            existingId = jdbcTemplate.queryForObject(
                    """
                    SELECT id_rdv FROM rendez_vous01
                    WHERE id_hopital = ? AND id_patient = ? AND id_medecin = ?
                      AND UPPER(canal) = 'TELECONSULTATION'
                      AND statut_rdv IN ('PROGRAMME', 'CONFIRME', 'EN_COURS')
                      AND motif_visite LIKE '%démo%'
                    ORDER BY date_heure_rdv DESC
                    LIMIT 1
                    """,
                    Integer.class,
                    hopitalId,
                    patientId,
                    medecinId
            );
        } catch (EmptyResultDataAccessException ignored) {
            // aucune téléconsultation démo — création ci-dessous
        }

        if (existingId != null) {
            jdbcTemplate.update(
                    """
                    UPDATE rendez_vous01
                    SET date_heure_rdv = DATE_ADD(NOW(), INTERVAL 30 MINUTE),
                        statut_rdv = 'CONFIRME'
                    WHERE id_rdv = ?
                    """,
                    existingId
            );
            return;
        }

        jdbcTemplate.update(
                """
                INSERT INTO rendez_vous01
                (id_hopital, id_patient, id_medecin, date_heure_rdv, duree_estimee, motif_visite,
                 canal, statut_rdv, date_creation)
                VALUES (?, ?, ?, DATE_ADD(NOW(), INTERVAL 30 MINUTE), 30, 'Téléconsultation de suivi — démo',
                        'TELECONSULTATION', 'CONFIRME', NOW())
                """,
                hopitalId,
                patientId,
                medecinId
        );
    }
}