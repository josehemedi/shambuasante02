package hospicloud.repositoriesImpl;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.sql.Types;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import hospicloud.model.Hopital;
import hospicloud.repositories.HopitalRepository;

@Repository
public class HopitalRepositoryImpl implements HopitalRepository {
    
    private static final Logger logger = LoggerFactory.getLogger(HopitalRepositoryImpl.class);

    private final JdbcTemplate jdbcTemplate;

    @Autowired
    public HopitalRepositoryImpl(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private Hopital mapRow(ResultSet rs) throws SQLException {
        Hopital h = new Hopital();
        try {
            int id = rs.getInt("id_hopital");
            if (!rs.wasNull()) h.setIdHopital(id);
        } catch (SQLException ignored) { }
        try { h.setNom(rs.getString("nom")); } catch (SQLException ignored) { }
        try { h.setAdresse(rs.getString("adresse")); } catch (SQLException ignored) { }
        try { h.setTelephone(rs.getString("telephone")); } catch (SQLException ignored) { }
        try { h.setEmail(rs.getString("email")); } catch (SQLException ignored) { }
        try { h.setLogoUrl(rs.getString("logo_url")); } catch (SQLException ignored) { }
        try { h.setVille(rs.getString("ville")); } catch (SQLException ignored) { }
        try { h.setPays(rs.getString("pays")); } catch (SQLException ignored) { }
        try { h.setType(rs.getString("type")); } catch (SQLException ignored) { }
        try {
            Timestamp t = rs.getTimestamp("date_creation");
            if (t != null) h.setDateCreation(t.toLocalDateTime());
        } catch (SQLException ignored) { }
        try {
            Timestamp t = rs.getTimestamp("date_modification");
            if (t != null) h.setDateModification(t.toLocalDateTime());
        } catch (SQLException ignored) { }
        try { h.setEstActif(rs.getBoolean("est_actif")); } catch (SQLException ignored) { }
        try { h.setNomCommercial(rs.getString("nom_commercial")); } catch (SQLException ignored) { }
        try { h.setSousDomaine(rs.getString("sous_domaine")); } catch (SQLException ignored) { }
        try { h.setAdresseComplete(rs.getString("adresse_complete")); } catch (SQLException ignored) { }
        return h;
    }

    @Override
    @Transactional
    public void enresgitrerHopital(Hopital hopital) {
        if (hopital == null) return;

        // Validation minimale
        if (hopital.getNom() == null || hopital.getNom().trim().isEmpty()) {
            logger.warn("Tentative d'enregistrement d'un hopital sans nom");
            return;
        }

        // Normaliser le nom pour la vérification (trim + lower)
        final String normalizedNom = hopital.getNom().trim().toLowerCase();

        // Vérifier doublon par nom (insensible à la casse et aux espaces)
        final String checkSql = "SELECT COUNT(1) FROM hopitaux WHERE LOWER(TRIM(nom)) = ?";
        try {
            Integer count = jdbcTemplate.queryForObject(checkSql, Integer.class, normalizedNom);
            if (count != null && count > 0) {
                logger.info("Hopital avec le même nom existe déjà (insensible casse): {}", hopital.getNom());
                return; // éviter l'insertion en cas de doublon
            }
        } catch (Exception e) {
            // si la vérification échoue, on continue et laisse la tentative d'insertion
            logger.debug("Impossible de vérifier l'existence de l'hopital par nom, tentative d'insertion continue", e);
        }

        final String sql = "INSERT INTO hopitaux (nom, adresse, telephone, email, logo_url, ville, pays, type, date_creation, date_modification, est_actif, sous_domaine, nom_commercial, adresse_complete) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, hopital.getNom());
            ps.setString(2, hopital.getAdresse());
            ps.setString(3, hopital.getTelephone());
            ps.setString(4, hopital.getEmail());
            ps.setString(5, hopital.getLogoUrl());
            ps.setString(6, hopital.getVille());
            ps.setString(7, hopital.getPays());
            ps.setString(8, hopital.getType());

            LocalDateTime now = LocalDateTime.now();
            Timestamp tsCreation = hopital.getDateCreation() != null ? Timestamp.valueOf(hopital.getDateCreation()) : Timestamp.valueOf(now);
            Timestamp tsModif = hopital.getDateModification() != null ? Timestamp.valueOf(hopital.getDateModification()) : null;

            ps.setTimestamp(9, tsCreation);
            if (tsModif != null) ps.setTimestamp(10, tsModif); else ps.setNull(10, Types.TIMESTAMP);
            ps.setBoolean(11, hopital.isEstActif());
            ps.setString(12, hopital.getSousDomaine() != null ? hopital.getSousDomaine() : "");
            ps.setString(13, hopital.getNomCommercial() != null ? hopital.getNomCommercial() : hopital.getNom());
            ps.setString(14, hopital.getAdresseComplete() != null ? hopital.getAdresseComplete() : hopital.getAdresse());
            return ps;
        }, keyHolder);

        Number key = keyHolder.getKey();
        if (key != null) {
            hopital.setIdHopital(key.intValue());
            logger.info("Hopital enregistré avec id={}", hopital.getIdHopital());
        }
    }

    @Override
    public Hopital rechercherhopitalParId(Long idHopital) {
        if (idHopital == null) return null;
        final String sql = "SELECT * FROM hopitaux WHERE id_hopital = ?";
        try {
            List<Hopital> list = jdbcTemplate.query(sql, new Object[]{idHopital}, (rs, rowNum) -> mapRow(rs));
            return list.isEmpty() ? null : list.get(0);
        } catch (Exception e) {
            // log if logger available; keep simple
            return null;
        }
    }

    @Override
    public Hopital rechercherParNom(String nom) {
        if (nom == null || nom.trim().isEmpty()) return null;
        final String sql = "SELECT * FROM hopitaux WHERE nom = ?";
        try {
            List<Hopital> list = jdbcTemplate.query(sql, new Object[]{nom.trim()}, (rs, rowNum) -> mapRow(rs));
            return list.isEmpty() ? null : list.get(0);
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    public List<Hopital> listerTous() {
        final String sql = "SELECT * FROM hopitaux ORDER BY id_hopital DESC";
        try {
            return jdbcTemplate.query(sql, (rs, rowNum) -> mapRow(rs));
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }

    @Override
    public Long countActifsByHopital(Integer hopitalId) {
        String sql = "SELECT COUNT(1) FROM hopitaux WHERE id_hopital = ? AND est_actif = true";
        return jdbcTemplate.queryForObject(sql, Long.class, hopitalId);
    }

    @Override
    public Long countActifsByHopitalInPeriod(Integer hopitalId, LocalDate startDate, LocalDate endDate) {
        String sql = "SELECT COUNT(1) FROM hopitaux WHERE id_hopital = ? AND est_actif = true AND date_creation >= ? AND date_creation < ?";
        return jdbcTemplate.queryForObject(sql, Long.class, hopitalId, startDate.atStartOfDay(), endDate.plusDays(1).atStartOfDay());
    }

    @Override
    public Long countAllActifs() {
        return jdbcTemplate.queryForObject(
                "SELECT COUNT(1) FROM hopitaux WHERE est_actif = true", Long.class);
    }

    @Override
    public Long countAllActifsExistingBefore(LocalDate date) {
        return jdbcTemplate.queryForObject(
                "SELECT COUNT(1) FROM hopitaux WHERE est_actif = true AND date_creation < ?",
                Long.class, date.atStartOfDay());
    }

    @Override
    public boolean existsByEmail(String email) {
        if (email == null || email.isBlank()) {
            return false;
        }
        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(1) FROM hopitaux WHERE LOWER(email) = LOWER(?)",
                Integer.class, email.trim());
        return count != null && count > 0;
    }

    @Override
    public boolean existsBySousDomaine(String sousDomaine) {
        if (sousDomaine == null || sousDomaine.isBlank()) {
            return false;
        }
        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(1) FROM hopitaux WHERE sous_domaine = ?",
                Integer.class, sousDomaine.trim());
        return count != null && count > 0;
    }

    @Override
    public java.util.Optional<Hopital> findBySousDomaine(String sousDomaine) {
        if (sousDomaine == null || sousDomaine.isBlank()) {
            return java.util.Optional.empty();
        }
        final String sql = "SELECT * FROM hopitaux WHERE LOWER(TRIM(sous_domaine)) = LOWER(?) LIMIT 1";
        try {
            List<Hopital> list = jdbcTemplate.query(sql, (rs, rowNum) -> mapRow(rs), sousDomaine.trim());
            return list.isEmpty() ? java.util.Optional.empty() : java.util.Optional.of(list.get(0));
        } catch (Exception e) {
            logger.warn("findBySousDomaine failed for {}", sousDomaine, e);
            return java.util.Optional.empty();
        }
    }

    @Override
    public boolean existsBySousDomaineExcludingId(String sousDomaine, Integer idHopital) {
        if (sousDomaine == null || sousDomaine.isBlank() || idHopital == null) {
            return false;
        }
        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(1) FROM hopitaux WHERE sous_domaine = ? AND id_hopital <> ?",
                Integer.class, sousDomaine.trim(), idHopital);
        return count != null && count > 0;
    }

    @Override
    public boolean existsByEmailExcludingId(String email, Integer idHopital) {
        if (email == null || email.isBlank() || idHopital == null) {
            return false;
        }
        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(1) FROM hopitaux WHERE LOWER(email) = LOWER(?) AND id_hopital <> ?",
                Integer.class, email.trim(), idHopital);
        return count != null && count > 0;
    }

    @Override
    @Transactional
    public void modifier(Hopital hopital) {
        if (hopital == null || hopital.getIdHopital() == null) return;
        final String sql = """
                UPDATE hopitaux
                SET nom = ?, adresse = ?, telephone = ?, email = ?, logo_url = ?, ville = ?, pays = ?, type = ?,
                    date_modification = ?, est_actif = ?, sous_domaine = ?, nom_commercial = ?, adresse_complete = ?
                WHERE id_hopital = ?
                """;

        LocalDateTime now = LocalDateTime.now();
        Timestamp tsModif = hopital.getDateModification() != null ? Timestamp.valueOf(hopital.getDateModification()) : Timestamp.valueOf(now);

        jdbcTemplate.update(sql,
                hopital.getNom(),
                hopital.getAdresse(),
                hopital.getTelephone(),
                hopital.getEmail(),
                hopital.getLogoUrl(),
                hopital.getVille(),
                hopital.getPays(),
                hopital.getType(),
                tsModif,
                hopital.isEstActif(),
                hopital.getSousDomaine(),
                hopital.getNomCommercial(),
                hopital.getAdresseComplete(),
                hopital.getIdHopital());
    }

    @Override
    @Transactional
    public void supprimer(Integer id) {
        if (id == null) return;

        try {
            // 1) Supprimer les sociétés (table 'societes' utilise 'hospital_id')
            int deletedSocietes = jdbcTemplate.update("DELETE FROM societes WHERE hospital_id = ?", id);
            logger.info("Supprimées {} sociétés liées à l'hôpital {}", deletedSocietes, id);

            // 2) Supprimer les rendez-vous
            int deletedRdv = jdbcTemplate.update("DELETE FROM rendez_vous WHERE id_hopital = ?", id);
            logger.info("Supprimés {} rendez-vous liés à l'hôpital {}", deletedRdv, id);

            // 3) Supprimer les antécédents
            int deletedAntecedents = jdbcTemplate.update("DELETE FROM antecedents WHERE id_hopital = ?", id);
            logger.info("Supprimés {} antécédents liés à l'hôpital {}", deletedAntecedents, id);

            // 4) Supprimer les patients
            int deletedPatients = jdbcTemplate.update("DELETE FROM patients WHERE id_hopital = ?", id);
            logger.info("Supprimés {} patients liés à l'hôpital {}", deletedPatients, id);

            // 5) Autres tables dépendantes éventuelles (ajouter si besoin)
            // Exemple: supprimer dossiers, medicaments, etc. si présents

            // 6) Enfin supprimer l'hôpital
            int deletedHopital = jdbcTemplate.update("DELETE FROM hopitaux WHERE id_hopital = ?", id);
            if (deletedHopital == 0) {
                logger.warn("Aucun hôpital supprimé pour id={}", id);
            } else {
                logger.info("Hôpital {} supprimé physiquement ({} ligne(s) affectée(s))", id, deletedHopital);
            }
        } catch (Exception e) {
            logger.error("Erreur lors de la suppression physique de l'hôpital {}", id, e);
            throw new RuntimeException("Erreur lors de la suppression de l'hôpital", e);
        }
    }

}