package hospicloud.utils;

import hospicloud.model.ConsultationMedicale;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.format.DateTimeFormatter;

public final class DocumentHashUtil {

    private static final DateTimeFormatter ISO = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    private DocumentHashUtil() {
    }

    public static String buildConsultationCanonicalPayload(ConsultationMedicale consultation) {
        String dateConsultation = consultation.getDateConsultation() != null
                ? ISO.format(consultation.getDateConsultation())
                : "";
        String diagnostic = nullToEmpty(consultation.getDiagnostic());
        String observations = nullToEmpty(consultation.getObservations());
        String analyses = nullToEmpty(consultation.getAnalysesPrescrites());
        String traitement = "";
        String ordonnance = "";

        return "{"
                + "\"consultationId\":" + consultation.getIdConsultation() + ","
                + "\"patientId\":" + consultation.getIdPatient() + ","
                + "\"medecinId\":" + consultation.getIdMedecin() + ","
                + "\"hopitalId\":" + consultation.getIdHopital() + ","
                + "\"dateConsultation\":\"" + escapeJson(dateConsultation) + "\","
                + "\"diagnostic\":\"" + escapeJson(diagnostic) + "\","
                + "\"traitement\":\"" + escapeJson(traitement) + "\","
                + "\"observations\":\"" + escapeJson(observations) + "\","
                + "\"ordonnance\":\"" + escapeJson(ordonnance) + "\","
                + "\"analyses\":\"" + escapeJson(analyses) + "\""
                + "}";
    }

    public static String sha256Hex(String payload) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(payload.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder(hash.length * 2);
            for (byte b : hash) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("Algorithme SHA-256 indisponible.", e);
        }
    }

    public static String abbreviateHash(String hash) {
        if (hash == null || hash.length() < 16) {
            return hash;
        }
        return hash.substring(0, 8) + "…" + hash.substring(hash.length() - 8);
    }

    private static String nullToEmpty(String value) {
        return value == null ? "" : value;
    }

    private static String escapeJson(String value) {
        return value
                .replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r");
    }
}
