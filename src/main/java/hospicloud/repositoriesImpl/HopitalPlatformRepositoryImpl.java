package hospicloud.repositoriesImpl;

import hospicloud.dtos.HospitalActivityDTO;
import hospicloud.dtos.HospitalDetailDTO;
import hospicloud.dtos.HospitalOverviewDTO;
import hospicloud.repositories.HopitalPlatformRepository;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public class HopitalPlatformRepositoryImpl implements HopitalPlatformRepository {

    private static final String ACTIVE_SUBSCRIPTION_JOIN = """
            LEFT JOIN (
                SELECT a.id_hopital, a.plan_nom, a.montant_mensuel, a.statut
                FROM abonnements a
                INNER JOIN (
                    SELECT id_hopital, MAX(id_abonnement) AS max_id
                    FROM abonnements
                    WHERE statut = 'actif'
                    GROUP BY id_hopital
                ) latest ON latest.max_id = a.id_abonnement
            ) abo ON abo.id_hopital = h.id_hopital
            """;

    private final JdbcTemplate jdbcTemplate;

    public HopitalPlatformRepositoryImpl(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public List<HospitalOverviewDTO> listOverview() {
        String sql = """
                SELECT h.id_hopital, h.nom, h.pays, h.ville, h.type, h.est_actif,
                       h.date_creation, h.date_modification, h.email, h.telephone,
                       abo.plan_nom, abo.montant_mensuel, abo.statut AS abo_statut,
                       (SELECT COUNT(1) FROM utilisateurs u
                        WHERE u.id_hopital = h.id_hopital AND u.est_actif = TRUE) AS users,
                       (SELECT MAX(COALESCE(u.date_modification, u.date_creation))
                        FROM utilisateurs u WHERE u.id_hopital = h.id_hopital) AS last_user_activity,
                       (SELECT TRIM(CONCAT(COALESCE(u.prenom, ''), ' ', COALESCE(u.nom, '')))
                        FROM utilisateurs u
                        WHERE u.id_hopital = h.id_hopital AND u.role = 'TENANT_ADMIN' AND u.est_actif = TRUE
                        ORDER BY u.id_utilisateur ASC LIMIT 1) AS contact_name
                FROM hopitaux h
                """ + ACTIVE_SUBSCRIPTION_JOIN + """
                ORDER BY h.nom ASC
                """;
        return jdbcTemplate.query(sql, (rs, rowNum) -> {
            HospitalOverviewDTO dto = new HospitalOverviewDTO();
            int id = rs.getInt("id_hopital");
            dto.setIdHopital(id);
            dto.setId("T-" + String.format("%03d", id));
            dto.setName(rs.getString("nom"));
            dto.setCountry(blankToDash(rs.getString("pays")));
            dto.setCity(blankToDash(rs.getString("ville")));
            dto.setEmail(rs.getString("email") != null ? rs.getString("email") : "");
            dto.setPhone(rs.getString("telephone") != null ? rs.getString("telephone") : "");
            dto.setEstActif(rs.getBoolean("est_actif"));

            String plan = rs.getString("plan_nom");
            if (plan == null || plan.isBlank()) {
                plan = "Starter";
            }
            dto.setPlan(plan);

            BigDecimal mrr = rs.getBigDecimal("montant_mensuel");
            dto.setMrr(mrr != null ? mrr : BigDecimal.ZERO);
            dto.setUsers(rs.getLong("users"));
            dto.setSpecialty(mapTypeToSpecialty(rs.getString("type")));

            String contact = rs.getString("contact_name");
            dto.setContact(contact != null && !contact.isBlank() ? contact.trim() : "—");

            boolean hopitalActif = rs.getBoolean("est_actif");
            String aboStatut = rs.getString("abo_statut");
            dto.setStatus(resolveStatus(hopitalActif, aboStatut, dto.getMrr(), rs.getTimestamp("date_creation")));

            Timestamp joined = rs.getTimestamp("date_creation");
            if (joined != null) {
                dto.setJoined(joined.toLocalDateTime());
            }

            Timestamp lastUser = rs.getTimestamp("last_user_activity");
            Timestamp modified = rs.getTimestamp("date_modification");
            LocalDateTime lastActive = null;
            if (lastUser != null) {
                lastActive = lastUser.toLocalDateTime();
            } else if (modified != null) {
                lastActive = modified.toLocalDateTime();
            } else if (joined != null) {
                lastActive = joined.toLocalDateTime();
            }
            dto.setLastActive(lastActive);
            return dto;
        });
    }

    @Override
    public Optional<HospitalDetailDTO> findDetailById(Integer idHopital) {
        if (idHopital == null) {
            return Optional.empty();
        }
        String sql = """
                SELECT h.id_hopital, h.nom, h.nom_commercial, h.sous_domaine, h.pays, h.ville, h.adresse,
                       h.adresse_complete, h.type, h.est_actif, h.date_creation, h.date_modification,
                       h.email, h.telephone, h.logo_url,
                       abo.plan_nom, abo.montant_mensuel, abo.statut AS abo_statut,
                       (SELECT COUNT(1) FROM utilisateurs u
                        WHERE u.id_hopital = h.id_hopital AND u.est_actif = TRUE) AS users,
                       (SELECT MAX(COALESCE(u.date_modification, u.date_creation))
                        FROM utilisateurs u WHERE u.id_hopital = h.id_hopital) AS last_user_activity,
                       (SELECT TRIM(CONCAT(COALESCE(u.prenom, ''), ' ', COALESCE(u.nom, '')))
                        FROM utilisateurs u
                        WHERE u.id_hopital = h.id_hopital AND u.role = 'TENANT_ADMIN' AND u.est_actif = TRUE
                        ORDER BY u.id_utilisateur ASC LIMIT 1) AS contact_name
                FROM hopitaux h
                """ + ACTIVE_SUBSCRIPTION_JOIN + """
                WHERE h.id_hopital = ?
                """;
        List<HospitalDetailDTO> rows = jdbcTemplate.query(sql, (rs, rowNum) -> mapDetail(rs), idHopital);
        return rows.isEmpty() ? Optional.empty() : Optional.of(rows.get(0));
    }

    private HospitalDetailDTO mapDetail(java.sql.ResultSet rs) throws java.sql.SQLException {
        HospitalDetailDTO dto = new HospitalDetailDTO();
        int id = rs.getInt("id_hopital");
        dto.setIdHopital(id);
        dto.setId("T-" + String.format("%03d", id));
        dto.setName(rs.getString("nom"));
        dto.setNomCommercial(blankToEmpty(rs.getString("nom_commercial")));
        dto.setSousDomaine(blankToEmpty(rs.getString("sous_domaine")));
        dto.setCountry(blankToDash(rs.getString("pays")));
        dto.setCity(blankToDash(rs.getString("ville")));
        dto.setAdresse(blankToEmpty(rs.getString("adresse")));
        dto.setAdresseComplete(blankToEmpty(rs.getString("adresse_complete")));
        dto.setType(rs.getString("type") != null ? rs.getString("type") : "CLINIQUE");
        dto.setEmail(rs.getString("email") != null ? rs.getString("email") : "");
        dto.setPhone(rs.getString("telephone") != null ? rs.getString("telephone") : "");
        dto.setLogoUrl(blankToEmpty(rs.getString("logo_url")));
        dto.setEstActif(rs.getBoolean("est_actif"));

        String plan = rs.getString("plan_nom");
        if (plan == null || plan.isBlank()) {
            plan = "Starter";
        }
        dto.setPlan(plan);

        BigDecimal mrr = rs.getBigDecimal("montant_mensuel");
        dto.setMrr(mrr != null ? mrr : BigDecimal.ZERO);
        dto.setUsers(rs.getLong("users"));
        dto.setSpecialty(mapTypeToSpecialty(rs.getString("type")));

        String contact = rs.getString("contact_name");
        dto.setContact(contact != null && !contact.isBlank() ? contact.trim() : "—");

        String aboStatut = rs.getString("abo_statut");
        dto.setStatus(resolveStatus(rs.getBoolean("est_actif"), aboStatut, dto.getMrr(), rs.getTimestamp("date_creation")));

        Timestamp joined = rs.getTimestamp("date_creation");
        if (joined != null) {
            dto.setJoined(joined.toLocalDateTime());
        }

        Timestamp lastUser = rs.getTimestamp("last_user_activity");
        Timestamp modified = rs.getTimestamp("date_modification");
        LocalDateTime lastActive = null;
        if (lastUser != null) {
            lastActive = lastUser.toLocalDateTime();
        } else if (modified != null) {
            lastActive = modified.toLocalDateTime();
        } else if (joined != null) {
            lastActive = joined.toLocalDateTime();
        }
        dto.setLastActive(lastActive);
        return dto;
    }

    private String blankToEmpty(String value) {
        return value == null ? "" : value;
    }

    @Override
    public long countTotal() {
        Long count = jdbcTemplate.queryForObject("SELECT COUNT(1) FROM hopitaux", Long.class);
        return count != null ? count : 0L;
    }

    @Override
    public long countByStatus(String status) {
        return listOverview().stream().filter(h -> status.equals(h.getStatus())).count();
    }

    @Override
    public List<HospitalActivityDTO> listRecentActivity(int limit) {
        int safeLimit = Math.min(Math.max(limit, 1), 50);
        String logsSql = """
                SELECT la.id_log, la.id_hopital, la.action, la.date_activite,
                       TRIM(CONCAT(COALESCE(u.prenom, ''), ' ', COALESCE(u.nom, ''))) AS user_name,
                       h.nom AS hospital_name
                FROM logs_activite la
                JOIN utilisateurs u ON u.id_utilisateur = la.id_utilisateur
                JOIN hopitaux h ON h.id_hopital = la.id_hopital
                ORDER BY la.date_activite DESC
                LIMIT ?
                """;
        List<HospitalActivityDTO> fromLogs = jdbcTemplate.query(logsSql, (rs, rowNum) -> {
            HospitalActivityDTO dto = new HospitalActivityDTO();
            dto.setId("LOG-" + rs.getInt("id_log"));
            dto.setHospitalId("T-" + String.format("%03d", rs.getInt("id_hopital")));
            dto.setAction(rs.getString("action") != null ? rs.getString("action") : "activity");
            String userName = rs.getString("user_name");
            dto.setUser(userName != null && !userName.isBlank() ? userName.trim() : "System");
            Timestamp ts = rs.getTimestamp("date_activite");
            if (ts != null) {
                dto.setTimestamp(ts.toLocalDateTime());
            }
            dto.setDetails(rs.getString("action") + " — " + rs.getString("hospital_name"));
            return dto;
        }, safeLimit);

        if (!fromLogs.isEmpty()) {
            return fromLogs;
        }

        String usersSql = """
                SELECT u.id_utilisateur, u.id_hopital, u.email, u.date_creation,
                       TRIM(CONCAT(COALESCE(u.prenom, ''), ' ', COALESCE(u.nom, ''))) AS user_name,
                       h.nom AS hospital_name
                FROM utilisateurs u
                JOIN hopitaux h ON h.id_hopital = u.id_hopital
                ORDER BY u.date_creation DESC
                LIMIT ?
                """;
        List<HospitalActivityDTO> fromUsers = jdbcTemplate.query(usersSql, (rs, rowNum) -> {
            HospitalActivityDTO dto = new HospitalActivityDTO();
            dto.setId("USR-" + rs.getInt("id_utilisateur"));
            dto.setHospitalId("T-" + String.format("%03d", rs.getInt("id_hopital")));
            dto.setAction("user_created");
            String userName = rs.getString("user_name");
            dto.setUser(userName != null && !userName.isBlank() ? userName.trim() : "System");
            Timestamp ts = rs.getTimestamp("date_creation");
            if (ts != null) {
                dto.setTimestamp(ts.toLocalDateTime());
            }
            dto.setDetails("Compte créé : " + rs.getString("email") + " — " + rs.getString("hospital_name"));
            return dto;
        }, safeLimit);

        if (!fromUsers.isEmpty()) {
            return fromUsers;
        }

        String hospitalsSql = """
                SELECT id_hopital, nom, date_creation
                FROM hopitaux
                ORDER BY date_creation DESC
                LIMIT ?
                """;
        return jdbcTemplate.query(hospitalsSql, (rs, rowNum) -> {
            HospitalActivityDTO dto = new HospitalActivityDTO();
            dto.setId("HOS-" + rs.getInt("id_hopital"));
            dto.setHospitalId("T-" + String.format("%03d", rs.getInt("id_hopital")));
            dto.setAction("hospital_created");
            dto.setUser("System");
            Timestamp ts = rs.getTimestamp("date_creation");
            if (ts != null) {
                dto.setTimestamp(ts.toLocalDateTime());
            }
            dto.setDetails("Hôpital enregistré : " + rs.getString("nom"));
            return dto;
        }, safeLimit);
    }

    private String resolveStatus(boolean hopitalActif, String aboStatut, BigDecimal mrr, Timestamp dateCreation) {
        if (!hopitalActif) {
            return "suspended";
        }
        if ("suspendu".equalsIgnoreCase(aboStatut) || "annule".equalsIgnoreCase(aboStatut)) {
            return "suspended";
        }
        if (aboStatut == null && mrr.compareTo(BigDecimal.ZERO) == 0) {
            if (dateCreation != null) {
                LocalDate created = dateCreation.toLocalDateTime().toLocalDate();
                if (created.isAfter(LocalDate.now().minusDays(30))) {
                    return "trial";
                }
            }
            return "trial";
        }
        return "active";
    }

    private String mapTypeToSpecialty(String type) {
        if (type == null || type.isBlank()) {
            return "General";
        }
        return switch (type.toUpperCase()) {
            case "CLINIQUE" -> "Clinic";
            case "HOPITAL_GENERAL" -> "General Hospital";
            case "CENTRE_MEDICAL" -> "Medical Center";
            case "MATERNITE" -> "Maternity";
            case "LABORATOIRE" -> "Laboratory";
            default -> type;
        };
    }

    private String blankToDash(String value) {
        return value == null || value.isBlank() ? "—" : value;
    }
}
