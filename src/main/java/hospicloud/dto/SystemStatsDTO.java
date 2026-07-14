package hospicloud.dto;

import java.time.Instant;

/**
 * DTO (Data Transfer Object) pour transporter les statistiques du système.
 * Utilise un Java Record pour l'immutabilité et la concision.
 *
 * @param cpuUsage    L'utilisation actuelle du CPU en pourcentage.
 * @param memoryUsage L'utilisation de la mémoire en pourcentage.
 * @param timestamp   L'horodatage de la collecte des données.
 */
public record SystemStatsDTO(
        double cpuUsage,
        double memoryUsage,
        long timestamp) {
    /**
     * Constructeur compact pour la validation (optionnel).
     */
    public SystemStatsDTO {
        if (cpuUsage < 0 || cpuUsage > 1) {
            throw new IllegalArgumentException("L'utilisation du CPU doit être entre 0 et 1.");
        }
        if (memoryUsage < 0 || memoryUsage > 100) {
            throw new IllegalArgumentException("L'utilisation de la mémoire doit être entre 0 et 100.");
        }
    }
}
