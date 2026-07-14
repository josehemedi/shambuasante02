package hospicloud.repositoriesImpl;

import hospicloud.dtos.BillingDraftLineDTO;
import hospicloud.repositories.BillingCompositionRepository;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.time.Year;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Repository
public class BillingCompositionRepositoryImpl implements BillingCompositionRepository {

    private final JdbcTemplate jdbcTemplate;

    public BillingCompositionRepositoryImpl(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public boolean patientBelongsToHospital(Integer idPatient, Integer idHopital) {
        try {
            Integer count = jdbcTemplate.queryForObject(
                    "SELECT COUNT(*) FROM patients WHERE id_patient = ? AND id_hopital = ?",
                    Integer.class, idPatient, idHopital);
            return count != null && count > 0;
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public Optional<Integer> findOpenFactureId(Integer idPatient, Integer idHopital) {
        try {
            Integer id = jdbcTemplate.queryForObject(
                    """
                    SELECT id_facture FROM factures
                    WHERE id_patient = ? AND id_hopital = ?
                      AND statut_paiement IN ('IMPAYE', 'PARTIEL')
                    ORDER BY date_facture DESC
                    LIMIT 1
                    """,
                    Integer.class, idPatient, idHopital);
            return Optional.ofNullable(id);
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    @Override
    public Optional<Integer> findFacturePatientId(Integer idFacture, Integer idHopital) {
        try {
            Integer id = jdbcTemplate.queryForObject(
                    "SELECT id_patient FROM factures WHERE id_facture = ? AND id_hopital = ?",
                    Integer.class, idFacture, idHopital);
            return Optional.ofNullable(id);
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    @Override
    public BigDecimal findPatientInsuranceRate(Integer idPatient) {
        try {
            BigDecimal rate = jdbcTemplate.queryForObject(
                    """
                    SELECT COALESCE(taux_couverture, 0)
                    FROM patients_assurances
                    WHERE id_patient = ?
                      AND (date_expiration IS NULL OR date_expiration >= CURDATE())
                    ORDER BY id_assurance DESC
                    LIMIT 1
                    """,
                    BigDecimal.class, idPatient);
            return rate != null ? rate : BigDecimal.ZERO;
        } catch (Exception e) {
            try {
                BigDecimal rate = jdbcTemplate.queryForObject(
                        """
                        SELECT COALESCE(s.taux_couverture, 0)
                        FROM patients p
                        LEFT JOIN societes s ON p.id_societe = s.id_societe
                        WHERE p.id_patient = ?
                        """,
                        BigDecimal.class, idPatient);
                return rate != null ? rate : BigDecimal.ZERO;
            } catch (Exception ex) {
                return BigDecimal.ZERO;
            }
        }
    }

    @Override
    public BigDecimal findDefaultConsultationPrice(Integer idHopital) {
        try {
            BigDecimal price = jdbcTemplate.queryForObject(
                    """
                    SELECT prix_unitaire FROM tarifs_hopital
                    WHERE id_hopital = ? AND categorie = 'CONSULTATION' AND actif = 1
                    ORDER BY CASE WHEN code = 'CONSULT_GEN' THEN 0 ELSE 1 END, id_tarif
                    LIMIT 1
                    """,
                    BigDecimal.class, idHopital);
            return price != null ? price : BigDecimal.valueOf(20);
        } catch (Exception e) {
            return BigDecimal.valueOf(20);
        }
    }

    @Override
    public String findDefaultConsultationLabel(Integer idHopital) {
        try {
            String label = jdbcTemplate.queryForObject(
                    """
                    SELECT libelle FROM tarifs_hopital
                    WHERE id_hopital = ? AND categorie = 'CONSULTATION' AND actif = 1
                    ORDER BY CASE WHEN code = 'CONSULT_GEN' THEN 0 ELSE 1 END, id_tarif
                    LIMIT 1
                    """,
                    String.class, idHopital);
            return label != null && !label.isBlank() ? label : "Consultation médicale";
        } catch (Exception e) {
            return "Consultation médicale";
        }
    }

    @Override
    public List<BillingDraftLineDTO> collectUnbilledConsultations(Integer idPatient, Integer idHopital) {
        BigDecimal unit = findDefaultConsultationPrice(idHopital);
        String label = findDefaultConsultationLabel(idHopital);
        String sql = """
                SELECT cm.id_consultation
                FROM consultations_medicales cm
                WHERE cm.id_patient = ? AND cm.id_hopital = ?
                  AND NOT EXISTS (
                      SELECT 1 FROM facture_items fi
                      WHERE fi.source_type = 'CONSULTATION' AND fi.source_id = cm.id_consultation
                  )
                ORDER BY cm.date_consultation
                """;
        try {
            return jdbcTemplate.query(sql, (rs, i) -> {
                BillingDraftLineDTO line = new BillingDraftLineDTO();
                line.setCategorie("CONSULTATION");
                line.setDesignation(label);
                line.setQuantite(1);
                line.setPrixUnitaire(unit);
                line.setSourceType("CONSULTATION");
                line.setSourceId(rs.getLong("id_consultation"));
                return line;
            }, idPatient, idHopital);
        } catch (Exception e) {
            return List.of();
        }
    }

    @Override
    public List<BillingDraftLineDTO> collectUnbilledAnalyses(Integer idPatient, Integer idHopital) {
        String sql = """
                SELECT a.id_analyse, ta.nom_analyse, ta.prix_analyse
                FROM analyses_laboratoire a
                INNER JOIN types_analyses ta ON a.id_type_analyse = ta.id_type_analyse
                WHERE a.id_patient = ? AND a.id_hopital = ?
                  AND a.statut = 'TERMINE'
                  AND NOT EXISTS (
                      SELECT 1 FROM facture_items fi
                      WHERE fi.source_type = 'ANALYSE' AND fi.source_id = a.id_analyse
                  )
                ORDER BY a.date_demande
                """;
        try {
            return jdbcTemplate.query(sql, (rs, i) -> {
                BillingDraftLineDTO line = new BillingDraftLineDTO();
                line.setCategorie("EXAMEN");
                line.setDesignation(rs.getString("nom_analyse"));
                line.setQuantite(1);
                line.setPrixUnitaire(safe(rs.getBigDecimal("prix_analyse")));
                line.setSourceType("ANALYSE");
                line.setSourceId(rs.getLong("id_analyse"));
                return line;
            }, idPatient, idHopital);
        } catch (Exception e) {
            return List.of();
        }
    }

    @Override
    public List<BillingDraftLineDTO> collectUnbilledPharmacy(Integer idPatient, Integer idHopital) {
        List<BillingDraftLineDTO> lines = new ArrayList<>();

        String pharmaSql = """
                SELECT d.id, d.designation, d.quantite, d.prix_unitaire, d.medicament_id
                FROM pharmacie_delivrances d
                WHERE d.id_patient = ? AND d.hopital_id = ?
                  AND NOT EXISTS (
                      SELECT 1 FROM facture_items fi
                      WHERE fi.source_type = 'PHARMA_DELIV' AND fi.source_id = d.id
                  )
                ORDER BY d.date_delivrance
                """;
        try {
            lines.addAll(jdbcTemplate.query(pharmaSql, (rs, i) -> {
                BillingDraftLineDTO line = new BillingDraftLineDTO();
                line.setCategorie("MEDICAMENT");
                String designation = rs.getString("designation");
                line.setDesignation(designation != null && !designation.isBlank() ? designation : "Médicament");
                line.setQuantite(Math.max(rs.getInt("quantite"), 1));
                line.setPrixUnitaire(safe(rs.getBigDecimal("prix_unitaire")));
                line.setSourceType("PHARMA_DELIV");
                line.setSourceId(rs.getLong("id"));
                return line;
            }, idPatient, idHopital));
        } catch (Exception ignored) {
        }

        String legacySql = """
                SELECT dd.id_details, m.nom_commercial, m.prix_unitaire, dd.quantite_delivree, m.id_medicament
                FROM dispensations d
                INNER JOIN dispensation_details dd ON d.id_dispensation = dd.id_dispensation
                INNER JOIN medicaments m ON dd.id_medicament = m.id_medicament
                INNER JOIN patients p ON d.id_patient = p.id_patient
                WHERE d.id_patient = ? AND p.id_hopital = ?
                  AND NOT EXISTS (
                      SELECT 1 FROM facture_items fi
                      WHERE fi.source_type = 'DISPENSATION' AND fi.source_id = dd.id_details
                  )
                ORDER BY d.date_dispensation
                """;
        try {
            lines.addAll(jdbcTemplate.query(legacySql, (rs, i) -> {
                BillingDraftLineDTO line = new BillingDraftLineDTO();
                line.setCategorie("MEDICAMENT");
                line.setDesignation(rs.getString("nom_commercial"));
                line.setQuantite(Math.max(rs.getInt("quantite_delivree"), 1));
                line.setPrixUnitaire(safe(rs.getBigDecimal("prix_unitaire")));
                line.setSourceType("DISPENSATION");
                line.setSourceId(rs.getLong("id_details"));
                line.setIdProduitPharmacie(rs.getInt("id_medicament"));
                return line;
            }, idPatient, idHopital));
        } catch (Exception ignored) {
        }

        return lines;
    }

    @Override
    public List<BillingDraftLineDTO> collectUnbilledHospitalization(Integer idPatient, Integer idHopital) {
        String sql = """
                SELECT s.id_sejour, s.type_chambre, s.prix_journalier, s.date_entree, s.date_sortie,
                       GREATEST(1, DATEDIFF(COALESCE(s.date_sortie, NOW()), s.date_entree)) AS nb_jours
                FROM sejours_hospitalisation s
                WHERE s.id_patient = ? AND s.id_hopital = ?
                  AND NOT EXISTS (
                      SELECT 1 FROM facture_items fi
                      WHERE fi.source_type = 'SEJOUR' AND fi.source_id = s.id_sejour
                  )
                ORDER BY s.date_entree
                """;
        try {
            return jdbcTemplate.query(sql, (rs, i) -> {
                int days = Math.max(rs.getInt("nb_jours"), 1);
                String type = rs.getString("type_chambre");
                BillingDraftLineDTO line = new BillingDraftLineDTO();
                line.setCategorie("HOSPITALISATION");
                line.setDesignation("Hospitalisation"
                        + (type != null && !type.isBlank() ? " — " + type : "")
                        + " (" + days + " j)");
                line.setQuantite(days);
                line.setPrixUnitaire(safe(rs.getBigDecimal("prix_journalier")));
                line.setSourceType("SEJOUR");
                line.setSourceId(rs.getLong("id_sejour"));
                return line;
            }, idPatient, idHopital);
        } catch (Exception e) {
            return List.of();
        }
    }

    @Override
    public List<BillingDraftLineDTO> collectUnbilledActes(Integer idPatient, Integer idHopital) {
        String sql = """
                SELECT ar.id_acte_realise, ar.designation, ar.quantite, ar.prix_unitaire
                FROM actes_realises ar
                WHERE ar.id_patient = ? AND ar.id_hopital = ?
                  AND NOT EXISTS (
                      SELECT 1 FROM facture_items fi
                      WHERE fi.source_type = 'ACTE' AND fi.source_id = ar.id_acte_realise
                  )
                ORDER BY ar.date_acte
                """;
        try {
            return jdbcTemplate.query(sql, (rs, i) -> {
                BillingDraftLineDTO line = new BillingDraftLineDTO();
                line.setCategorie("ACTE_MEDICAL");
                line.setDesignation(rs.getString("designation"));
                line.setQuantite(Math.max(rs.getInt("quantite"), 1));
                line.setPrixUnitaire(safe(rs.getBigDecimal("prix_unitaire")));
                line.setSourceType("ACTE");
                line.setSourceId(rs.getLong("id_acte_realise"));
                line.setIdActeMedical(rs.getInt("id_acte_realise"));
                return line;
            }, idPatient, idHopital);
        } catch (Exception e) {
            return List.of();
        }
    }

    @Override
    public BigDecimal sumUnappliedAdvances(Integer idPatient, Integer idHopital) {
        try {
            BigDecimal sum = jdbcTemplate.queryForObject(
                    """
                    SELECT COALESCE(SUM(montant), 0) FROM avances_patient
                    WHERE id_patient = ? AND id_hopital = ? AND appliquee = 0
                    """,
                    BigDecimal.class, idPatient, idHopital);
            return sum != null ? sum : BigDecimal.ZERO;
        } catch (Exception e) {
            return BigDecimal.ZERO;
        }
    }

    @Override
    public BigDecimal sumAdvancesForFacture(Integer idFacture) {
        try {
            BigDecimal sum = jdbcTemplate.queryForObject(
                    """
                    SELECT COALESCE(SUM(montant), 0) FROM avances_patient
                    WHERE id_facture = ? AND appliquee = 1
                    """,
                    BigDecimal.class, idFacture);
            return sum != null ? sum : BigDecimal.ZERO;
        } catch (Exception e) {
            return BigDecimal.ZERO;
        }
    }

    @Override
    public Optional<BigDecimal> findFactureRemise(Integer idFacture, Integer idHopital) {
        try {
            BigDecimal remise = jdbcTemplate.queryForObject(
                    """
                    SELECT COALESCE(montant_remise, 0) FROM factures
                    WHERE id_facture = ? AND id_hopital = ?
                    """,
                    BigDecimal.class, idFacture, idHopital);
            return Optional.ofNullable(remise);
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    @Override
    public int insertFacture(Integer idPatient, Integer idHopital, String numero, BigDecimal ht,
                             BigDecimal tva, BigDecimal ttc, Integer idCaissier) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(con -> {
            PreparedStatement ps = con.prepareStatement(
                    """
                    INSERT INTO factures (
                        id_patient, id_hopital, numero_facture, montant_total_ht, tva, montant_total_ttc,
                        statut_paiement, id_caissier, sous_total_soins, montant_assurance, montant_remise,
                        montant_avances, composition_auto
                    ) VALUES (?, ?, ?, ?, ?, ?, 'IMPAYE', ?, 0, 0, 0, 0, 1)
                    """,
                    Statement.RETURN_GENERATED_KEYS);
            ps.setInt(1, idPatient);
            ps.setInt(2, idHopital);
            ps.setString(3, numero);
            ps.setBigDecimal(4, ht);
            ps.setBigDecimal(5, tva);
            ps.setBigDecimal(6, ttc);
            if (idCaissier != null) {
                ps.setInt(7, idCaissier);
            } else {
                ps.setNull(7, java.sql.Types.INTEGER);
            }
            return ps;
        }, keyHolder);
        Number key = keyHolder.getKey();
        return key != null ? key.intValue() : 0;
    }

    @Override
    public void deleteAutoLines(Integer idFacture) {
        jdbcTemplate.update(
                "DELETE FROM facture_items WHERE id_facture = ? AND source_type IS NOT NULL",
                idFacture);
    }

    @Override
    public void insertFactureItem(Integer idFacture, BillingDraftLineDTO line) {
        jdbcTemplate.update(
                """
                INSERT INTO facture_items (
                    id_facture, designation, quantite, prix_unitaire,
                    id_produit_pharmacie, id_acte_medical, categorie, source_type, source_id
                ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
                """,
                idFacture,
                line.getDesignation(),
                Math.max(line.getQuantite(), 1),
                line.getPrixUnitaire() != null ? line.getPrixUnitaire() : BigDecimal.ZERO,
                line.getIdProduitPharmacie(),
                line.getIdActeMedical(),
                line.getCategorie() != null ? line.getCategorie() : "AUTRE",
                line.getSourceType(),
                line.getSourceId());
    }

    @Override
    public BigDecimal sumFactureItems(Integer idFacture) {
        try {
            BigDecimal sum = jdbcTemplate.queryForObject(
                    """
                    SELECT COALESCE(SUM(quantite * prix_unitaire), 0)
                    FROM facture_items WHERE id_facture = ?
                    """,
                    BigDecimal.class, idFacture);
            return sum != null ? sum : BigDecimal.ZERO;
        } catch (Exception e) {
            return BigDecimal.ZERO;
        }
    }

    @Override
    public void updateFactureComposition(
            Integer idFacture,
            Integer idHopital,
            BigDecimal sousTotalSoins,
            BigDecimal tauxAssurance,
            BigDecimal montantAssurance,
            BigDecimal montantRemise,
            BigDecimal montantAvances,
            BigDecimal montantHt,
            BigDecimal montantTtc,
            String statut,
            Integer idCaissier) {
        jdbcTemplate.update(
                """
                UPDATE factures SET
                    sous_total_soins = ?,
                    taux_assurance = ?,
                    montant_assurance = ?,
                    montant_remise = ?,
                    montant_avances = ?,
                    montant_total_ht = ?,
                    montant_total_ttc = ?,
                    statut_paiement = ?,
                    id_caissier = COALESCE(?, id_caissier),
                    composition_auto = 1
                WHERE id_facture = ? AND id_hopital = ?
                """,
                sousTotalSoins,
                tauxAssurance,
                montantAssurance,
                montantRemise,
                montantAvances,
                montantHt,
                montantTtc,
                statut,
                idCaissier,
                idFacture,
                idHopital);
    }

    @Override
    public void applyAdvancesToFacture(Integer idPatient, Integer idHopital, Integer idFacture) {
        jdbcTemplate.update(
                """
                UPDATE avances_patient
                SET appliquee = 1, id_facture = ?
                WHERE id_patient = ? AND id_hopital = ? AND appliquee = 0
                """,
                idFacture, idPatient, idHopital);
    }

    @Override
    public int insertAdvance(
            Integer idHopital,
            Integer idPatient,
            BigDecimal montant,
            String method,
            String reference,
            String notes) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(con -> {
            PreparedStatement ps = con.prepareStatement(
                    """
                    INSERT INTO avances_patient (
                        id_hopital, id_patient, montant, mode_paiement, reference_transaction, notes, appliquee
                    ) VALUES (?, ?, ?, ?, ?, ?, 0)
                    """,
                    Statement.RETURN_GENERATED_KEYS);
            ps.setInt(1, idHopital);
            ps.setInt(2, idPatient);
            ps.setBigDecimal(3, montant);
            ps.setString(4, method);
            ps.setString(5, reference);
            ps.setString(6, notes);
            return ps;
        }, keyHolder);
        Number key = keyHolder.getKey();
        return key != null ? key.intValue() : 0;
    }

    @Override
    public String nextFactureNumber(Integer idHopital) {
        int year = Year.now().getValue();
        try {
            Integer count = jdbcTemplate.queryForObject(
                    """
                    SELECT COUNT(*) FROM factures
                    WHERE id_hopital = ? AND YEAR(date_facture) = ?
                    """,
                    Integer.class, idHopital, year);
            int next = (count != null ? count : 0) + 1;
            return String.format("FAC-%d-%04d", year, next);
        } catch (Exception e) {
            return "FAC-" + year + "-" + System.currentTimeMillis() % 10000;
        }
    }

    @Override
    public List<Integer> listPatientsWithOpenFactures(Integer idHopital) {
        try {
            return jdbcTemplate.queryForList(
                    """
                    SELECT DISTINCT id_patient FROM factures
                    WHERE id_hopital = ? AND statut_paiement IN ('IMPAYE', 'PARTIEL')
                    """,
                    Integer.class, idHopital);
        } catch (Exception e) {
            return List.of();
        }
    }

    @Override
    public List<hospicloud.dtos.TarifHopitalDTO> listTarifs(Integer idHopital) {
        try {
            return jdbcTemplate.query(
                    """
                    SELECT id_tarif, id_hopital, code, libelle, categorie, prix_unitaire, actif
                    FROM tarifs_hopital
                    WHERE id_hopital = ?
                    ORDER BY categorie, libelle
                    """,
                    (rs, i) -> {
                        hospicloud.dtos.TarifHopitalDTO t = new hospicloud.dtos.TarifHopitalDTO();
                        t.setIdTarif(rs.getInt("id_tarif"));
                        t.setIdHopital(rs.getInt("id_hopital"));
                        t.setCode(rs.getString("code"));
                        t.setLibelle(rs.getString("libelle"));
                        t.setCategorie(rs.getString("categorie"));
                        t.setPrixUnitaire(safe(rs.getBigDecimal("prix_unitaire")));
                        t.setActif(rs.getBoolean("actif"));
                        return t;
                    },
                    idHopital);
        } catch (Exception e) {
            return List.of();
        }
    }

    @Override
    public int upsertTarif(Integer idHopital, hospicloud.dtos.TarifHopitalDTO tarif) {
        if (tarif.getIdTarif() != null) {
            return jdbcTemplate.update(
                    """
                    UPDATE tarifs_hopital
                    SET libelle = ?, categorie = ?, prix_unitaire = ?, actif = ?, code = COALESCE(?, code)
                    WHERE id_tarif = ? AND id_hopital = ?
                    """,
                    tarif.getLibelle(),
                    tarif.getCategorie(),
                    tarif.getPrixUnitaire(),
                    tarif.isActif() ? 1 : 0,
                    tarif.getCode(),
                    tarif.getIdTarif(),
                    idHopital);
        }
        return jdbcTemplate.update(
                """
                INSERT INTO tarifs_hopital (id_hopital, code, libelle, categorie, prix_unitaire, actif)
                VALUES (?, ?, ?, ?, ?, ?)
                ON DUPLICATE KEY UPDATE
                    libelle = VALUES(libelle),
                    categorie = VALUES(categorie),
                    prix_unitaire = VALUES(prix_unitaire),
                    actif = VALUES(actif)
                """,
                idHopital,
                tarif.getCode(),
                tarif.getLibelle(),
                tarif.getCategorie(),
                tarif.getPrixUnitaire(),
                tarif.isActif() ? 1 : 0);
    }

    @Override
    public long insertPharmacieDelivrance(
            Integer hopitalId,
            Integer idPatient,
            Long medicamentId,
            int quantite,
            BigDecimal prixUnitaire,
            String designation,
            Integer delivrePar) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(con -> {
            PreparedStatement ps = con.prepareStatement(
                    """
                    INSERT INTO pharmacie_delivrances (
                        hopital_id, id_patient, medicament_id, quantite, prix_unitaire, designation, delivre_par
                    ) VALUES (?, ?, ?, ?, ?, ?, ?)
                    """,
                    Statement.RETURN_GENERATED_KEYS);
            ps.setInt(1, hopitalId);
            ps.setInt(2, idPatient);
            ps.setLong(3, medicamentId);
            ps.setInt(4, Math.max(quantite, 1));
            ps.setBigDecimal(5, prixUnitaire != null ? prixUnitaire : BigDecimal.ZERO);
            ps.setString(6, designation);
            if (delivrePar != null) {
                ps.setInt(7, delivrePar);
            } else {
                ps.setNull(7, java.sql.Types.INTEGER);
            }
            return ps;
        }, keyHolder);
        Number key = keyHolder.getKey();
        return key != null ? key.longValue() : 0L;
    }

    private BigDecimal safe(BigDecimal value) {
        return value != null ? value : BigDecimal.ZERO;
    }
}
