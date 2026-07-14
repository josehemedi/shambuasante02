package hospicloud.repositoriesImpl;

import hospicloud.dtos.patient.PatientMessageConversationDTO;
import hospicloud.repositories.PatientMessageRepository;
import hospicloud.repositories.TeleconsultationChatRepository;
import hospicloud.security.ChatMessageCryptoService;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public class PatientMessageRepositoryImpl implements PatientMessageRepository {

    private final JdbcTemplate jdbcTemplate;
    private final ChatMessageCryptoService cryptoService;
    private final TeleconsultationChatRepository teleconsultationChatRepository;

    public PatientMessageRepositoryImpl(JdbcTemplate jdbcTemplate,
                                        ChatMessageCryptoService cryptoService,
                                        TeleconsultationChatRepository teleconsultationChatRepository) {
        this.jdbcTemplate = jdbcTemplate;
        this.cryptoService = cryptoService;
        this.teleconsultationChatRepository = teleconsultationChatRepository;
    }

    @Override
    public List<PatientMessageConversationDTO> listConversations(Integer idPatient, Integer idHopital) {
        teleconsultationChatRepository.ensureSchema();

        String sql = """
                SELECT r.id_rdv, r.id_hopital, r.date_heure_rdv, r.motif_visite, r.statut_rdv,
                       TRIM(CONCAT(COALESCE(m.prenom, ''), ' ', COALESCE(m.nom, ''))) AS nom_medecin,
                       COALESCE((
                         SELECT COUNT(*)
                         FROM teleconsultation_chat_messages cm
                         WHERE cm.id_hopital = r.id_hopital AND cm.id_rdv = r.id_rdv
                           AND cm.sender_role = 'doctor' AND cm.read_by_patient_at IS NULL
                       ), 0) AS unread_count,
                       (
                         SELECT cm.contenu
                         FROM teleconsultation_chat_messages cm
                         WHERE cm.id_hopital = r.id_hopital AND cm.id_rdv = r.id_rdv
                         ORDER BY cm.created_at DESC, cm.id DESC
                         LIMIT 1
                       ) AS last_message_encrypted,
                       (
                         SELECT cm.created_at
                         FROM teleconsultation_chat_messages cm
                         WHERE cm.id_hopital = r.id_hopital AND cm.id_rdv = r.id_rdv
                         ORDER BY cm.created_at DESC, cm.id DESC
                         LIMIT 1
                       ) AS last_message_at,
                       (
                         SELECT cm.sender_role
                         FROM teleconsultation_chat_messages cm
                         WHERE cm.id_hopital = r.id_hopital AND cm.id_rdv = r.id_rdv
                         ORDER BY cm.created_at DESC, cm.id DESC
                         LIMIT 1
                       ) AS last_sender_role
                FROM rendez_vous01 r
                LEFT JOIN medecin m ON r.id_medecin = m.id_medecin AND r.id_hopital = m.id_hopital
                WHERE r.id_patient = ? AND r.id_hopital = ?
                  AND UPPER(r.canal) = 'TELECONSULTATION'
                  AND r.statut_rdv NOT IN ('ANNULE', 'ABSENT')
                ORDER BY COALESCE(
                  (
                    SELECT cm.created_at
                    FROM teleconsultation_chat_messages cm
                    WHERE cm.id_hopital = r.id_hopital AND cm.id_rdv = r.id_rdv
                    ORDER BY cm.created_at DESC, cm.id DESC
                    LIMIT 1
                  ),
                  r.date_heure_rdv
                ) DESC
                LIMIT 50
                """;

        try {
            return jdbcTemplate.query(sql, (rs, rowNum) -> {
                PatientMessageConversationDTO dto = new PatientMessageConversationDTO();
                dto.setIdRdv(rs.getInt("id_rdv"));
                dto.setIdHopital(rs.getInt("id_hopital"));
                dto.setDoctorName(blankToDash(rs.getString("nom_medecin")));
                dto.setMotifVisite(rs.getString("motif_visite"));
                dto.setStatutRdv(rs.getString("statut_rdv"));

                Timestamp rdvTs = rs.getTimestamp("date_heure_rdv");
                if (rdvTs != null) {
                    dto.setDateHeureRdv(rdvTs.toLocalDateTime());
                }

                dto.setUnreadCount(rs.getLong("unread_count"));

                String encrypted = rs.getString("last_message_encrypted");
                if (encrypted != null && !encrypted.isBlank()) {
                    try {
                        dto.setLastMessagePreview(truncatePreview(cryptoService.decrypt(encrypted)));
                    } catch (Exception ignored) {
                        dto.setLastMessagePreview("…");
                    }
                }

                Timestamp lastTs = rs.getTimestamp("last_message_at");
                if (lastTs != null) {
                    dto.setLastMessageAt(lastTs.toLocalDateTime());
                } else if (rdvTs != null) {
                    dto.setLastMessageAt(rdvTs.toLocalDateTime());
                }

                dto.setLastSenderRole(rs.getString("last_sender_role"));
                return dto;
            }, idPatient, idHopital);
        } catch (Exception ex) {
            return List.of();
        }
    }

    private String blankToDash(String value) {
        return value == null || value.isBlank() ? "—" : value.trim();
    }

    private String truncatePreview(String content) {
        if (content == null) return "";
        String trimmed = content.trim();
        return trimmed.length() > 120 ? trimmed.substring(0, 117) + "…" : trimmed;
    }
}
