package hospicloud.repositoriesImpl;

import hospicloud.dtos.CreatePharmacieMedicamentRequest;
import hospicloud.dtos.PharmacieMedicamentDTO;
import hospicloud.dtos.PharmacieStockAlertDTO;
import hospicloud.repositories.PharmacieMedicamentRepository;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public class PharmacieMedicamentRepositoryImpl implements PharmacieMedicamentRepository {

    private final JdbcTemplate jdbcTemplate;

    public PharmacieMedicamentRepositoryImpl(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void ensureSchema() {
        jdbcTemplate.execute("""
            CREATE TABLE IF NOT EXISTS pharmacie_medicaments (
                id BIGINT AUTO_INCREMENT PRIMARY KEY,
                hopital_id INT NOT NULL,
                nom_medicament VARCHAR(150) NOT NULL,
                nom_generique VARCHAR(150) NULL,
                categorie VARCHAR(100) NULL,
                dosage VARCHAR(100) NULL,
                forme VARCHAR(50) NULL,
                unite VARCHAR(50) NULL,
                quantite_stock INT NOT NULL DEFAULT 0,
                stock_minimum INT NOT NULL DEFAULT 0,
                prix_achat DECIMAL(10,2) NULL,
                prix_vente DECIMAL(10,2) NULL,
                numero_lot VARCHAR(100) NULL,
                date_expiration DATE NULL,
                fournisseur VARCHAR(150) NULL,
                statut ENUM(
                    'DISPONIBLE',
                    'STOCK_FAIBLE',
                    'RUPTURE_STOCK',
                    'EXPIRE',
                    'DESACTIVE'
                ) NOT NULL DEFAULT 'DISPONIBLE',
                cree_par_utilisateur_id INT NULL,
                modifie_par_utilisateur_id INT NULL,
                date_creation TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                date_modification TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                CONSTRAINT fk_pharmacie_medicaments_hopital
                    FOREIGN KEY (hopital_id) REFERENCES hopitaux(id_hopital),
                CONSTRAINT fk_pharmacie_medicaments_cree_par
                    FOREIGN KEY (cree_par_utilisateur_id) REFERENCES utilisateurs(id_utilisateur),
                CONSTRAINT fk_pharmacie_medicaments_modifie_par
                    FOREIGN KEY (modifie_par_utilisateur_id) REFERENCES utilisateurs(id_utilisateur)
            )
            """);

        jdbcTemplate.execute("""
            CREATE TABLE IF NOT EXISTS pharmacie_alertes (
                id BIGINT AUTO_INCREMENT PRIMARY KEY,
                hopital_id INT NOT NULL,
                medicament_id BIGINT NOT NULL,
                type_alerte ENUM('STOCK_FAIBLE','RUPTURE_STOCK','EXPIRE') NOT NULL,
                quantite_stock INT NOT NULL,
                stock_minimum INT NOT NULL,
                nom_medicament VARCHAR(150) NOT NULL,
                statut_alerte ENUM('ACTIVE','RESOLUE') NOT NULL DEFAULT 'ACTIVE',
                date_creation TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                date_resolution TIMESTAMP NULL,
                CONSTRAINT fk_pharmacie_alertes_hopital
                    FOREIGN KEY (hopital_id) REFERENCES hopitaux(id_hopital),
                CONSTRAINT fk_pharmacie_alertes_medicament
                    FOREIGN KEY (medicament_id) REFERENCES pharmacie_medicaments(id)
            )
            """);
    }

    @Override
    public PharmacieMedicamentDTO create(Integer hopitalId, Integer creeParUtilisateurId, CreatePharmacieMedicamentRequest request) {
        String statut = resolveStatut(request.getQuantiteStock(), request.getStockMinimum(), request.getDateExpiration());
        String sql = """
            INSERT INTO pharmacie_medicaments (
                hopital_id, nom_medicament, nom_generique, categorie, dosage, forme, unite,
                quantite_stock, stock_minimum, prix_achat, prix_vente, numero_lot,
                date_expiration, fournisseur, statut, cree_par_utilisateur_id
            ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            """;

        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setInt(1, hopitalId);
            ps.setString(2, request.getNomMedicament().trim());
            ps.setString(3, blankToNull(request.getNomGenerique()));
            ps.setString(4, blankToNull(request.getCategorie()));
            ps.setString(5, blankToNull(request.getDosage()));
            ps.setString(6, blankToNull(request.getForme()));
            ps.setString(7, blankToNull(request.getUnite()));
            ps.setInt(8, request.getQuantiteStock());
            ps.setInt(9, request.getStockMinimum());
            setBigDecimal(ps, 10, request.getPrixAchat());
            setBigDecimal(ps, 11, request.getPrixVente());
            ps.setString(12, blankToNull(request.getNumeroLot()));
            if (request.getDateExpiration() != null) {
                ps.setDate(13, Date.valueOf(request.getDateExpiration()));
            } else {
                ps.setNull(13, java.sql.Types.DATE);
            }
            ps.setString(14, blankToNull(request.getFournisseur()));
            ps.setString(15, statut);
            if (creeParUtilisateurId != null) {
                ps.setInt(16, creeParUtilisateurId);
            } else {
                ps.setNull(16, java.sql.Types.INTEGER);
            }
            return ps;
        }, keyHolder);

        Number key = keyHolder.getKey();
        Long id = key != null ? key.longValue() : null;
        if (id == null) {
            throw new IllegalStateException("Impossible de récupérer l'identifiant du médicament créé");
        }
        return findByIdAndHopital(id, hopitalId)
                .orElseThrow(() -> new IllegalStateException("Médicament créé mais introuvable"));
    }

    @Override
    public void syncStatuts(Integer hopitalId) {
        try {
            jdbcTemplate.update("""
                UPDATE pharmacie_medicaments
                SET statut = CASE
                    WHEN statut = 'DESACTIVE' THEN 'DESACTIVE'
                    WHEN date_expiration IS NOT NULL AND date_expiration < CURDATE() THEN 'EXPIRE'
                    WHEN quantite_stock <= 0 THEN 'RUPTURE_STOCK'
                    WHEN quantite_stock <= stock_minimum THEN 'STOCK_FAIBLE'
                    ELSE 'DISPONIBLE'
                END
                WHERE hopital_id = ?
                """, hopitalId);
        } catch (Exception ignored) {
            // Table absente ou schéma incompatible
        }
    }

    @Override
    public void processStockAlerts(Integer hopitalId) {
        syncStatuts(hopitalId);
        try {
            resolveRecoveredAlerts(hopitalId);
            createMissingAlerts(hopitalId);
        } catch (Exception ignored) {
            // Alertes non critiques
        }
    }

    @Override
    public List<PharmacieStockAlertDTO> listActiveAlerts(Integer hopitalId) {
        String sql = """
            SELECT id, medicament_id, nom_medicament, type_alerte, quantite_stock, stock_minimum, date_creation
            FROM pharmacie_alertes
            WHERE hopital_id = ? AND statut_alerte = 'ACTIVE'
            ORDER BY
              CASE type_alerte
                WHEN 'RUPTURE_STOCK' THEN 1
                WHEN 'EXPIRE' THEN 2
                WHEN 'STOCK_FAIBLE' THEN 3
                ELSE 4
              END,
              date_creation DESC
            """;
        try {
            return jdbcTemplate.query(sql, (rs, rowNum) -> mapAlertRow(rs), hopitalId);
        } catch (Exception e) {
            return List.of();
        }
    }

    private void resolveRecoveredAlerts(Integer hopitalId) {
        jdbcTemplate.update("""
            UPDATE pharmacie_alertes a
            INNER JOIN pharmacie_medicaments m ON a.medicament_id = m.id AND a.hopital_id = m.hopital_id
            SET a.statut_alerte = 'RESOLUE', a.date_resolution = CURRENT_TIMESTAMP
            WHERE a.hopital_id = ? AND a.statut_alerte = 'ACTIVE'
              AND (
                (a.type_alerte = 'STOCK_FAIBLE' AND m.statut != 'STOCK_FAIBLE')
                OR (a.type_alerte = 'RUPTURE_STOCK' AND m.statut != 'RUPTURE_STOCK')
                OR (a.type_alerte = 'EXPIRE' AND m.statut != 'EXPIRE')
              )
            """, hopitalId);
    }

    private void createMissingAlerts(Integer hopitalId) {
        String sql = """
            SELECT m.id, m.nom_medicament, m.statut, m.quantite_stock, m.stock_minimum
            FROM pharmacie_medicaments m
            WHERE m.hopital_id = ?
              AND m.statut IN ('STOCK_FAIBLE', 'RUPTURE_STOCK', 'EXPIRE')
            """;
        jdbcTemplate.query(sql, rs -> {
            long medicamentId = rs.getLong("id");
            String nom = rs.getString("nom_medicament");
            String statut = rs.getString("statut");
            int qty = rs.getInt("quantite_stock");
            int minimum = rs.getInt("stock_minimum");
            String typeAlerte = statut;
            Integer existing = jdbcTemplate.queryForObject("""
                SELECT COUNT(1) FROM pharmacie_alertes
                WHERE hopital_id = ? AND medicament_id = ? AND type_alerte = ? AND statut_alerte = 'ACTIVE'
                """, Integer.class, hopitalId, medicamentId, typeAlerte);
            if (existing == null || existing == 0) {
                jdbcTemplate.update("""
                    INSERT INTO pharmacie_alertes (
                        hopital_id, medicament_id, type_alerte, quantite_stock, stock_minimum, nom_medicament
                    ) VALUES (?, ?, ?, ?, ?, ?)
                    """, hopitalId, medicamentId, typeAlerte, qty, minimum, nom);
            } else {
                jdbcTemplate.update("""
                    UPDATE pharmacie_alertes
                    SET quantite_stock = ?, stock_minimum = ?, nom_medicament = ?
                    WHERE hopital_id = ? AND medicament_id = ? AND type_alerte = ? AND statut_alerte = 'ACTIVE'
                    """, qty, minimum, nom, hopitalId, medicamentId, typeAlerte);
            }
        }, hopitalId);
    }

    private PharmacieStockAlertDTO mapAlertRow(java.sql.ResultSet rs) throws java.sql.SQLException {
        PharmacieStockAlertDTO dto = new PharmacieStockAlertDTO();
        dto.setId(rs.getLong("id"));
        dto.setMedicamentId(rs.getLong("medicament_id"));
        dto.setNomMedicament(rs.getString("nom_medicament"));
        String type = rs.getString("type_alerte");
        dto.setTypeAlerte(type);
        dto.setQuantiteStock(rs.getInt("quantite_stock"));
        dto.setStockMinimum(rs.getInt("stock_minimum"));
        Timestamp created = rs.getTimestamp("date_creation");
        dto.setDateCreation(created != null ? created.toLocalDateTime() : null);
        dto.setLevel(resolveAlertLevel(type));
        dto.setMessage(buildAlertMessage(type, dto.getNomMedicament(), dto.getQuantiteStock(), dto.getStockMinimum(), "en"));
        dto.setMessageFr(buildAlertMessage(type, dto.getNomMedicament(), dto.getQuantiteStock(), dto.getStockMinimum(), "fr"));
        return dto;
    }

    private String resolveAlertLevel(String typeAlerte) {
        return switch (typeAlerte) {
            case "RUPTURE_STOCK", "EXPIRE" -> "critical";
            case "STOCK_FAIBLE" -> "warning";
            default -> "info";
        };
    }

    private String buildAlertMessage(String type, String nom, int qty, int minimum, String lang) {
        if ("fr".equals(lang)) {
            return switch (type) {
                case "RUPTURE_STOCK" -> nom + " : rupture de stock (0 unité restante).";
                case "EXPIRE" -> nom + " : médicament expiré — action requise.";
                case "STOCK_FAIBLE" -> nom + " : stock faible (" + qty + " / seuil " + minimum + ").";
                default -> nom + " : alerte stock.";
            };
        }
        return switch (type) {
            case "RUPTURE_STOCK" -> nom + ": out of stock (0 units remaining).";
            case "EXPIRE" -> nom + ": expired medicine — action required.";
            case "STOCK_FAIBLE" -> nom + ": low stock (" + qty + " / threshold " + minimum + ").";
            default -> nom + ": stock alert.";
        };
    }

    @Override
    public List<PharmacieMedicamentDTO> listByHopital(Integer hopitalId) {
        String sql = """
            SELECT id, hopital_id, nom_medicament, nom_generique, categorie, dosage, forme, unite,
                   quantite_stock, stock_minimum, prix_achat, prix_vente, numero_lot,
                   date_expiration, fournisseur, statut, cree_par_utilisateur_id, date_creation
            FROM pharmacie_medicaments
            WHERE hopital_id = ?
            ORDER BY date_creation DESC
            """;
        try {
            return jdbcTemplate.query(sql, (rs, rowNum) -> mapRow(rs), hopitalId);
        } catch (Exception e) {
            return List.of();
        }
    }

    @Override
    public Optional<PharmacieMedicamentDTO> findByIdAndHopital(Long id, Integer hopitalId) {
        String sql = """
            SELECT id, hopital_id, nom_medicament, nom_generique, categorie, dosage, forme, unite,
                   quantite_stock, stock_minimum, prix_achat, prix_vente, numero_lot,
                   date_expiration, fournisseur, statut, cree_par_utilisateur_id, date_creation
            FROM pharmacie_medicaments
            WHERE id = ? AND hopital_id = ?
            """;
        try {
            List<PharmacieMedicamentDTO> rows = jdbcTemplate.query(sql, (rs, rowNum) -> mapRow(rs), id, hopitalId);
            return rows.stream().findFirst();
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    @Override
    public void decrementStock(Long id, Integer hopitalId, int quantity) {
        int qty = Math.max(quantity, 1);
        int updated = jdbcTemplate.update(
                """
                UPDATE pharmacie_medicaments
                SET quantite_stock = GREATEST(0, quantite_stock - ?)
                WHERE id = ? AND hopital_id = ? AND quantite_stock >= ?
                """,
                qty, id, hopitalId, qty);
        if (updated == 0) {
            throw new IllegalArgumentException("Stock insuffisant pour ce médicament");
        }
    }

    static String resolveStatut(int quantiteStock, int stockMinimum, LocalDate dateExpiration) {
        if (dateExpiration != null && dateExpiration.isBefore(LocalDate.now())) {
            return "EXPIRE";
        }
        if (quantiteStock <= 0) {
            return "RUPTURE_STOCK";
        }
        if (quantiteStock <= stockMinimum) {
            return "STOCK_FAIBLE";
        }
        return "DISPONIBLE";
    }

    private PharmacieMedicamentDTO mapRow(java.sql.ResultSet rs) throws java.sql.SQLException {
        PharmacieMedicamentDTO dto = new PharmacieMedicamentDTO();
        dto.setId(rs.getLong("id"));
        dto.setHopitalId(rs.getInt("hopital_id"));
        dto.setNomMedicament(rs.getString("nom_medicament"));
        dto.setNomGenerique(rs.getString("nom_generique"));
        dto.setCategorie(rs.getString("categorie"));
        dto.setDosage(rs.getString("dosage"));
        dto.setForme(rs.getString("forme"));
        dto.setUnite(rs.getString("unite"));
        dto.setQuantiteStock(rs.getInt("quantite_stock"));
        dto.setStockMinimum(rs.getInt("stock_minimum"));
        dto.setPrixAchat(rs.getBigDecimal("prix_achat"));
        dto.setPrixVente(rs.getBigDecimal("prix_vente"));
        dto.setNumeroLot(rs.getString("numero_lot"));
        Date exp = rs.getDate("date_expiration");
        dto.setDateExpiration(exp != null ? exp.toLocalDate() : null);
        dto.setFournisseur(rs.getString("fournisseur"));
        dto.setStatut(rs.getString("statut"));
        int creePar = rs.getInt("cree_par_utilisateur_id");
        dto.setCreeParUtilisateurId(rs.wasNull() ? null : creePar);
        Timestamp created = rs.getTimestamp("date_creation");
        dto.setDateCreation(created != null ? created.toLocalDateTime() : null);
        return dto;
    }

    private void setBigDecimal(PreparedStatement ps, int index, BigDecimal value) throws java.sql.SQLException {
        if (value != null) {
            ps.setBigDecimal(index, value);
        } else {
            ps.setNull(index, java.sql.Types.DECIMAL);
        }
    }

    private String blankToNull(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }
}
