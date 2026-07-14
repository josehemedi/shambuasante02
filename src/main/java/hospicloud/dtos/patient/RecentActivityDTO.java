package hospicloud.dtos.patient;

import java.time.LocalDateTime;

public class RecentActivityDTO {
    private String typeActivite; // Rendez-vous, Ordonnance, Facture...
    private String description;
    private LocalDateTime dateHeure;
    private String statut;

    public RecentActivityDTO() {}

    public RecentActivityDTO(String typeActivite, String description, LocalDateTime dateHeure, String statut) {
        this.typeActivite = typeActivite;
        this.description = description;
        this.dateHeure = dateHeure;
        this.statut = statut;
    }

    // Getters and Setters
    public String getTypeActivite() { return typeActivite; }
    public void setTypeActivite(String typeActivite) { this.typeActivite = typeActivite; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public LocalDateTime getDateHeure() { return dateHeure; }
    public void setDateHeure(LocalDateTime dateHeure) { this.dateHeure = dateHeure; }

    public String getStatut() { return statut; }
    public void setStatut(String statut) { this.statut = statut; }
}
