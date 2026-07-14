package hospicloud.repositoriesImpl;

import hospicloud.dtos.TenantCashierFeeLineDTO;
import hospicloud.dtos.TenantCashierHistoryItemDTO;
import hospicloud.dtos.CashierInvoiceDetailDTO;
import hospicloud.dtos.TenantCashierKpisDTO;
import hospicloud.dtos.TenantCashierPaymentContextDTO;
import hospicloud.dtos.TenantCashierQueueItemDTO;
import hospicloud.dtos.reporting.CashierInvoiceLineRowDTO;
import hospicloud.repositories.TenantCashierRepository;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

@Repository
public class TenantCashierRepositoryImpl implements TenantCashierRepository {

    private final JdbcTemplate jdbcTemplate;

    public TenantCashierRepositoryImpl(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public String findHospitalName(Integer idHopital) {
        try {
            return jdbcTemplate.queryForObject(
                    "SELECT nom FROM hopitaux WHERE id_hopital = ?", String.class, idHopital);
        } catch (Exception e) {
            return "Hospital";
        }
    }

    @Override
    public TenantCashierKpisDTO getKpis(Integer idHopital) {
        TenantCashierKpisDTO kpis = new TenantCashierKpisDTO();
        try {
            Long waiting = jdbcTemplate.queryForObject(
                    """
                    SELECT COUNT(1) FROM factures
                    WHERE id_hopital = ? AND statut_paiement IN ('IMPAYE', 'PARTIEL')
                    """,
                    Long.class, idHopital);
            kpis.setWaitingPayment(waiting != null ? waiting : 0L);
        } catch (Exception ignored) {
        }

        try {
            Long partial = jdbcTemplate.queryForObject(
                    "SELECT COUNT(1) FROM factures WHERE id_hopital = ? AND statut_paiement = 'PARTIEL'",
                    Long.class, idHopital);
            kpis.setPartialPayments(partial != null ? partial : 0L);
        } catch (Exception ignored) {
        }

        try {
            BigDecimal collected = jdbcTemplate.queryForObject(
                    """
                    SELECT COALESCE(SUM(pa.montant_paye), 0)
                    FROM paiements pa
                    INNER JOIN factures f ON pa.id_facture = f.id_facture
                    WHERE f.id_hopital = ? AND DATE(pa.date_paiement) = CURRENT_DATE
                    """,
                    BigDecimal.class, idHopital);
            kpis.setCollectedToday(collected != null ? collected.longValue() : 0L);
        } catch (Exception ignored) {
        }

        try {
            Long discharge = jdbcTemplate.queryForObject(
                    """
                    SELECT COUNT(1)
                    FROM bons_sortie bs
                    WHERE bs.id_hopital = ?
                      AND bs.statut_workflow = 'AUTORISE'
                      AND COALESCE(bs.statut_paiement_final, 0) = 0
                    """,
                    Long.class, idHopital);
            kpis.setAdminDischargePending(discharge != null ? discharge : 0L);
        } catch (Exception ignored) {
        }

        return kpis;
    }

    @Override
    public List<TenantCashierQueueItemDTO> listQueue(Integer idHopital, int limit) {
        int safeLimit = Math.max(1, Math.min(limit, 200));
        String sql = """
                SELECT f.id_facture, f.numero_facture, f.date_facture, f.montant_total_ttc, f.statut_paiement,
                       COALESCE(f.sous_total_soins, f.montant_total_ht, f.montant_total_ttc) AS sous_total_soins,
                       COALESCE(f.montant_assurance, 0) AS montant_assurance,
                       COALESCE(f.montant_remise, 0) AS montant_remise,
                       COALESCE(f.montant_avances, 0) AS montant_avances,
                       COALESCE(f.taux_assurance, 0) AS taux_assurance,
                       p.id_patient, p.code_patient, p.prenom, p.nom, p.sexe, p.date_naissance,
                       COALESCE(paid.total_paid, 0) AS paid_amount
                FROM factures f
                INNER JOIN patients p ON f.id_patient = p.id_patient AND f.id_hopital = p.id_hopital
                LEFT JOIN (
                    SELECT pa.id_facture, SUM(pa.montant_paye) AS total_paid
                    FROM paiements pa
                    INNER JOIN factures fx ON pa.id_facture = fx.id_facture
                    WHERE fx.id_hopital = ?
                    GROUP BY pa.id_facture
                ) paid ON paid.id_facture = f.id_facture
                WHERE f.id_hopital = ?
                  AND f.statut_paiement IN ('IMPAYE', 'PARTIEL')
                ORDER BY f.date_facture DESC
                LIMIT ?
                """;

        try {
            List<TenantCashierQueueItemDTO> queue = jdbcTemplate.query(sql, (rs, rowNum) -> {
                TenantCashierQueueItemDTO item = new TenantCashierQueueItemDTO();
                int idFacture = rs.getInt("id_facture");
                item.setIdFacture(idFacture);
                item.setId("bill-" + idFacture);
                item.setInvoiceNumber(rs.getString("numero_facture"));
                item.setPatientId(rs.getString("code_patient") != null
                        ? rs.getString("code_patient")
                        : "PT-" + rs.getInt("id_patient"));
                item.setIdPatientDb(rs.getInt("id_patient"));
                item.setPatientName(trimName(rs.getString("prenom"), rs.getString("nom")));
                item.setSex(rs.getString("sexe"));
                item.setAge(calcAge(rs.getDate("date_naissance")));
                Timestamp visitTs = rs.getTimestamp("date_facture");
                item.setVisitDate(visitTs != null ? visitTs.toLocalDateTime() : null);

                BigDecimal total = rs.getBigDecimal("montant_total_ttc");
                BigDecimal paid = rs.getBigDecimal("paid_amount");
                if (total == null) total = BigDecimal.ZERO;
                if (paid == null) paid = BigDecimal.ZERO;
                BigDecimal balance = total.subtract(paid).max(BigDecimal.ZERO);

                item.setTotalAmount(total);
                item.setPaidAmount(paid);
                item.setBalanceDue(balance);
                item.setSousTotalSoins(safeDecimal(rs.getBigDecimal("sous_total_soins")));
                item.setMontantAssurance(safeDecimal(rs.getBigDecimal("montant_assurance")));
                item.setMontantRemise(safeDecimal(rs.getBigDecimal("montant_remise")));
                item.setMontantAvances(safeDecimal(rs.getBigDecimal("montant_avances")));
                item.setTauxAssurance(safeDecimal(rs.getBigDecimal("taux_assurance")));
                item.setStatus(mapQueueStatus(rs.getString("statut_paiement"), paid, total));
                attachFeeLines(item, idFacture, idHopital);
                return item;
            }, idHopital, idHopital, safeLimit);
            return queue;
        } catch (Exception e) {
            return List.of();
        }
    }

    @Override
    public List<TenantCashierHistoryItemDTO> listHistory(Integer idHopital, int limit) {
        int safeLimit = Math.max(1, Math.min(limit, 100));
        String sql = """
                SELECT pa.id_paiement, pa.montant_paye, pa.date_paiement, pa.reference_transaction,
                       f.id_facture, f.numero_facture, f.montant_total_ttc,
                       p.code_patient, p.prenom, p.nom,
                       mp.nom_mode,
                       COALESCE(paid_after.total_paid, 0) AS total_paid_after
                FROM paiements pa
                INNER JOIN factures f ON pa.id_facture = f.id_facture
                INNER JOIN patients p ON f.id_patient = p.id_patient AND f.id_hopital = p.id_hopital
                LEFT JOIN modes_paiement mp ON pa.id_mode_paiement = mp.id_mode_paiement AND mp.id_hopital = f.id_hopital
                LEFT JOIN (
                    SELECT pa2.id_facture, SUM(pa2.montant_paye) AS total_paid
                    FROM paiements pa2
                    INNER JOIN factures fx ON pa2.id_facture = fx.id_facture
                    WHERE fx.id_hopital = ?
                    GROUP BY pa2.id_facture
                ) paid_after ON paid_after.id_facture = f.id_facture
                WHERE f.id_hopital = ?
                ORDER BY pa.date_paiement DESC
                LIMIT ?
                """;

        try {
            return jdbcTemplate.query(sql, (rs, rowNum) -> {
                TenantCashierHistoryItemDTO item = new TenantCashierHistoryItemDTO();
                int idPaiement = rs.getInt("id_paiement");
                item.setId("pay-" + idPaiement);
                item.setReceiptNumber("REC-" + idPaiement);
                item.setInvoiceNumber(rs.getString("numero_facture"));
                item.setPatientName(trimName(rs.getString("prenom"), rs.getString("nom")));
                item.setPatientId(rs.getString("code_patient"));
                Timestamp paidTs = rs.getTimestamp("date_paiement");
                item.setPaidAt(paidTs != null ? paidTs.toLocalDateTime() : null);
                item.setAmount(rs.getBigDecimal("montant_paye"));
                item.setMethod(mapMethodLabel(rs.getString("nom_mode")));
                item.setPaymentType("total");

                BigDecimal totalTtc = rs.getBigDecimal("montant_total_ttc");
                BigDecimal paidAfter = rs.getBigDecimal("total_paid_after");
                if (totalTtc != null && paidAfter != null) {
                    item.setBalanceAfter(totalTtc.subtract(paidAfter).max(BigDecimal.ZERO));
                    if (item.getBalanceAfter().compareTo(BigDecimal.ZERO) > 0) {
                        item.setPaymentType("partial");
                    }
                }
                return item;
            }, idHopital, idHopital, safeLimit);
        } catch (Exception e) {
            return List.of();
        }
    }

    @Override
    public Optional<BigDecimal> findFactureTotalTtc(Integer idFacture, Integer idHopital) {
        try {
            BigDecimal total = jdbcTemplate.queryForObject(
                    "SELECT montant_total_ttc FROM factures WHERE id_facture = ? AND id_hopital = ?",
                    BigDecimal.class, idFacture, idHopital);
            return Optional.ofNullable(total);
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    @Override
    public BigDecimal sumPaidForFacture(Integer idFacture, Integer idHopital) {
        try {
            BigDecimal sum = jdbcTemplate.queryForObject(
                    """
                    SELECT COALESCE(SUM(pa.montant_paye), 0)
                    FROM paiements pa
                    INNER JOIN factures f ON pa.id_facture = f.id_facture
                    WHERE pa.id_facture = ? AND f.id_hopital = ?
                    """,
                    BigDecimal.class, idFacture, idHopital);
            return sum != null ? sum : BigDecimal.ZERO;
        } catch (Exception e) {
            return BigDecimal.ZERO;
        }
    }

    @Override
    public Integer resolveModePaiementId(Integer idHopital, String method) {
        String normalized = method == null ? "" : method.trim().toLowerCase();
        String preferred = switch (normalized) {
            case "mobile_money", "mobile", "m-pesa", "mpesa" -> "M-Pesa";
            case "cash", "especes", "espèces" -> "Espèces";
            case "card", "carte", "transfer", "virement" -> "Espèces";
            default -> "Espèces";
        };
        try {
            return jdbcTemplate.queryForObject(
                    """
                    SELECT id_mode_paiement FROM modes_paiement
                    WHERE id_hopital = ? AND nom_mode = ?
                    LIMIT 1
                    """,
                    Integer.class, idHopital, preferred);
        } catch (Exception e) {
            try {
                return jdbcTemplate.queryForObject(
                        "SELECT id_mode_paiement FROM modes_paiement WHERE id_hopital = ? ORDER BY id_mode_paiement LIMIT 1",
                        Integer.class, idHopital);
            } catch (Exception ex) {
                return 1;
            }
        }
    }

    @Override
    public int insertPaiement(Integer idFacture, Integer idModePaiement, BigDecimal amount, String reference) {
        return jdbcTemplate.update(
                """
                INSERT INTO paiements (id_facture, id_mode_paiement, montant_paye, reference_transaction)
                VALUES (?, ?, ?, ?)
                """,
                idFacture, idModePaiement, amount, reference);
    }

    @Override
    public boolean updateFactureStatut(Integer idFacture, Integer idHopital, String statut, Integer idCaissier) {
        int rows = jdbcTemplate.update(
                """
                UPDATE factures
                SET statut_paiement = ?, id_caissier = ?
                WHERE id_facture = ? AND id_hopital = ?
                """,
                statut, idCaissier, idFacture, idHopital);
        return rows > 0;
    }

    private void attachFeeLines(TenantCashierQueueItemDTO item, int idFacture, int idHopital) {
        String sql = """
                SELECT fi.id_item, fi.designation, fi.quantite, fi.prix_unitaire, fi.sous_total,
                       fi.id_produit_pharmacie, fi.id_acte_medical, fi.categorie
                FROM facture_items fi
                INNER JOIN factures f ON fi.id_facture = f.id_facture
                WHERE fi.id_facture = ? AND f.id_hopital = ?
                ORDER BY fi.id_item
                """;
        try {
            List<TenantCashierFeeLineDTO> consultation = new ArrayList<>();
            List<TenantCashierFeeLineDTO> laboratory = new ArrayList<>();
            List<TenantCashierFeeLineDTO> pharmacy = new ArrayList<>();
            List<TenantCashierFeeLineDTO> hospitalization = new ArrayList<>();
            List<TenantCashierFeeLineDTO> medicalActs = new ArrayList<>();
            List<TenantCashierFeeLineDTO> other = new ArrayList<>();

            jdbcTemplate.query(sql, rs -> {
                TenantCashierFeeLineDTO line = new TenantCashierFeeLineDTO();
                line.setId("item-" + rs.getInt("id_item"));
                line.setLabel(rs.getString("designation"));
                line.setQty(rs.getInt("quantite"));
                line.setUnitPrice(rs.getBigDecimal("prix_unitaire"));
                BigDecimal subtotal = rs.getBigDecimal("sous_total");
                if (subtotal == null && line.getUnitPrice() != null) {
                    subtotal = line.getUnitPrice().multiply(BigDecimal.valueOf(line.getQty()));
                }
                line.setTotal(subtotal != null ? subtotal : BigDecimal.ZERO);

                String categorie = null;
                try {
                    categorie = rs.getString("categorie");
                } catch (Exception ignored) {
                }

                if (categorie != null && !categorie.isBlank()) {
                    switch (categorie) {
                        case "CONSULTATION" -> consultation.add(line);
                        case "EXAMEN" -> laboratory.add(line);
                        case "MEDICAMENT" -> pharmacy.add(line);
                        case "HOSPITALISATION" -> hospitalization.add(line);
                        case "ACTE_MEDICAL" -> medicalActs.add(line);
                        default -> other.add(line);
                    }
                } else if (rs.getObject("id_produit_pharmacie") != null) {
                    pharmacy.add(line);
                } else if (rs.getObject("id_acte_medical") != null) {
                    medicalActs.add(line);
                } else if (isLaboratoryLine(line.getLabel())) {
                    laboratory.add(line);
                } else if (isHospitalizationLine(line.getLabel())) {
                    hospitalization.add(line);
                } else {
                    consultation.add(line);
                }
            }, idFacture, idHopital);

            if (consultation.isEmpty() && laboratory.isEmpty() && pharmacy.isEmpty()
                    && hospitalization.isEmpty() && medicalActs.isEmpty() && other.isEmpty()) {
                TenantCashierFeeLineDTO fallback = new TenantCashierFeeLineDTO();
                fallback.setId("total");
                fallback.setLabel("Facture");
                fallback.setQty(1);
                fallback.setTotal(item.getTotalAmount());
                fallback.setUnitPrice(item.getTotalAmount());
                consultation.add(fallback);
            }

            item.setConsultationFees(consultation);
            item.setLaboratoryFees(laboratory);
            item.setPharmacyItems(pharmacy);
            item.setHospitalizationFees(hospitalization);
            item.setMedicalActFees(medicalActs);
            item.setOtherFees(other);
        } catch (Exception ignored) {
            TenantCashierFeeLineDTO fallback = new TenantCashierFeeLineDTO();
            fallback.setId("total");
            fallback.setLabel("Facture");
            fallback.setQty(1);
            fallback.setTotal(item.getTotalAmount());
            fallback.setUnitPrice(item.getTotalAmount());
            item.setConsultationFees(List.of(fallback));
        }
    }

    private String mapQueueStatus(String statut, BigDecimal paid, BigDecimal total) {
        if ("PARTIEL".equalsIgnoreCase(statut) || (paid != null && paid.compareTo(BigDecimal.ZERO) > 0 && total != null && paid.compareTo(total) < 0)) {
            return "partial";
        }
        return "pending";
    }

    private String mapMethodLabel(String nomMode) {
        if (nomMode == null) return "cash";
        String lower = nomMode.toLowerCase();
        if (lower.contains("m-pesa") || lower.contains("mobile")) return "mobile_money";
        if (lower.contains("esp")) return "cash";
        return "cash";
    }

    private boolean isLaboratoryLine(String label) {
        if (label == null) return false;
        String lower = label.toLowerCase();
        return lower.contains("labo") || lower.contains("analyse") || lower.contains("nfs")
                || lower.contains("radio") || lower.contains("glyc");
    }

    private boolean isHospitalizationLine(String label) {
        if (label == null) return false;
        String lower = label.toLowerCase();
        return lower.contains("hospital") || lower.contains("chambre") || lower.contains("nuit");
    }

    private String trimName(String prenom, String nom) {
        String full = ((prenom != null ? prenom : "") + " " + (nom != null ? nom : "")).trim();
        return full.isEmpty() ? "—" : full;
    }

    private Integer calcAge(java.sql.Date birthDate) {
        if (birthDate == null) return null;
        return Period.between(birthDate.toLocalDate(), LocalDate.now()).getYears();
    }

    @Override
    public Optional<TenantCashierPaymentContextDTO> findFacturePaymentContext(Integer idFacture, Integer idHopital) {
        if (idFacture == null || idHopital == null) {
            return Optional.empty();
        }
        try {
            return jdbcTemplate.query(
                    """
                    SELECT f.numero_facture, p.prenom, p.nom
                    FROM factures f
                    INNER JOIN patients p ON f.id_patient = p.id_patient AND f.id_hopital = p.id_hopital
                    WHERE f.id_facture = ? AND f.id_hopital = ?
                    LIMIT 1
                    """,
                    rs -> {
                        if (!rs.next()) {
                            return Optional.<TenantCashierPaymentContextDTO>empty();
                        }
                        String invoiceNumber = rs.getString("numero_facture");
                        if (invoiceNumber == null || invoiceNumber.isBlank()) {
                            invoiceNumber = String.valueOf(idFacture);
                        }
                        return Optional.of(new TenantCashierPaymentContextDTO(
                                invoiceNumber,
                                trimName(rs.getString("prenom"), rs.getString("nom"))));
                    },
                    idFacture,
                    idHopital);
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    @Override
    public Optional<CashierInvoiceDetailDTO> findInvoiceDetail(Integer idFacture, Integer idHopital) {
        if (idFacture == null || idHopital == null) {
            return Optional.empty();
        }
        try {
            return jdbcTemplate.query(
                    """
                    SELECT f.id_facture, f.numero_facture, f.date_facture,
                           f.montant_total_ht, f.tva, f.montant_total_ttc, f.statut_paiement,
                           p.code_patient, p.prenom, p.nom, p.sexe, p.date_naissance, p.telephone
                    FROM factures f
                    INNER JOIN patients p ON f.id_patient = p.id_patient AND f.id_hopital = p.id_hopital
                    WHERE f.id_facture = ? AND f.id_hopital = ?
                    LIMIT 1
                    """,
                    rs -> {
                        if (!rs.next()) {
                            return Optional.<CashierInvoiceDetailDTO>empty();
                        }
                        CashierInvoiceDetailDTO detail = new CashierInvoiceDetailDTO();
                        detail.setIdFacture(rs.getInt("id_facture"));
                        String numero = rs.getString("numero_facture");
                        detail.setInvoiceNumber(numero != null && !numero.isBlank()
                                ? numero : String.valueOf(idFacture));
                        Timestamp invoiceTs = rs.getTimestamp("date_facture");
                        detail.setInvoiceDate(invoiceTs != null ? invoiceTs.toLocalDateTime() : LocalDateTime.now());
                        detail.setPatientName(trimName(rs.getString("prenom"), rs.getString("nom")));
                        detail.setPatientCode(rs.getString("code_patient"));
                        detail.setPatientPhone(rs.getString("telephone"));
                        detail.setPatientSex(rs.getString("sexe"));
                        detail.setPatientAge(calcAge(rs.getDate("date_naissance")));
                        detail.setTotalHt(safeDecimal(rs.getBigDecimal("montant_total_ht")));
                        detail.setTva(safeDecimal(rs.getBigDecimal("tva")));
                        detail.setTotalTtc(safeDecimal(rs.getBigDecimal("montant_total_ttc")));
                        detail.setPaymentStatus(rs.getString("statut_paiement"));
                        BigDecimal paid = sumPaidForFacture(idFacture, idHopital);
                        detail.setPaidAmount(paid);
                        detail.setBalanceDue(detail.getTotalTtc().subtract(paid).max(BigDecimal.ZERO));
                        return Optional.of(detail);
                    },
                    idFacture,
                    idHopital);
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    @Override
    public List<CashierInvoiceLineRowDTO> findInvoiceLines(Integer idFacture, Integer idHopital) {
        if (idFacture == null || idHopital == null) {
            return List.of();
        }
        String sql = """
                SELECT fi.designation, fi.quantite, fi.prix_unitaire, fi.sous_total,
                       fi.id_produit_pharmacie, fi.id_acte_medical
                FROM facture_items fi
                INNER JOIN factures f ON fi.id_facture = f.id_facture
                WHERE fi.id_facture = ? AND f.id_hopital = ?
                ORDER BY fi.id_item
                """;
        try {
            AtomicInteger counter = new AtomicInteger(1);
            List<CashierInvoiceLineRowDTO> lines = jdbcTemplate.query(sql, (rs, rowNum) -> {
                TenantCashierFeeLineDTO line = new TenantCashierFeeLineDTO();
                line.setLabel(rs.getString("designation"));
                line.setQty(rs.getInt("quantite"));
                line.setUnitPrice(rs.getBigDecimal("prix_unitaire"));
                BigDecimal subtotal = rs.getBigDecimal("sous_total");
                line.setTotal(subtotal != null ? subtotal : BigDecimal.ZERO);
                String category = categorizeLine(line.getLabel(), rs.getObject("id_produit_pharmacie") != null);
                return new CashierInvoiceLineRowDTO(
                        String.valueOf(counter.getAndIncrement()),
                        nullToDash(line.getLabel()),
                        String.valueOf(line.getQty()),
                        formatMoney(line.getUnitPrice()),
                        formatMoney(line.getTotal()),
                        category);
            }, idFacture, idHopital);

            if (lines.isEmpty()) {
                Optional<BigDecimal> total = findFactureTotalTtc(idFacture, idHopital);
                lines = List.of(new CashierInvoiceLineRowDTO(
                        "1",
                        "Prestations médicales",
                        "1",
                        formatMoney(total.orElse(BigDecimal.ZERO)),
                        formatMoney(total.orElse(BigDecimal.ZERO)),
                        "Consultation"));
            }
            return lines;
        } catch (Exception e) {
            return List.of();
        }
    }

    private static BigDecimal safeDecimal(BigDecimal value) {
        return value != null ? value : BigDecimal.ZERO;
    }

    private static String formatMoney(BigDecimal value) {
        if (value == null) {
            return "0 GNF";
        }
        return value.setScale(0, java.math.RoundingMode.HALF_UP).toPlainString() + " GNF";
    }

    private static String nullToDash(String value) {
        return value == null || value.isBlank() ? "—" : value.trim();
    }

    private static String categorizeLine(String label, boolean pharmacy) {
        if (pharmacy) {
            return "Pharmacie";
        }
        if (isLaboratoryLineStatic(label)) {
            return "Laboratoire";
        }
        if (isHospitalizationLineStatic(label)) {
            return "Hospitalisation";
        }
        return "Consultation";
    }

    private static boolean isLaboratoryLineStatic(String label) {
        if (label == null) return false;
        String lower = label.toLowerCase();
        return lower.contains("labo") || lower.contains("analyse") || lower.contains("nfs")
                || lower.contains("radio") || lower.contains("glyc");
    }

    private static boolean isHospitalizationLineStatic(String label) {
        if (label == null) return false;
        String lower = label.toLowerCase();
        return lower.contains("hospital") || lower.contains("chambre") || lower.contains("nuit");
    }
}
