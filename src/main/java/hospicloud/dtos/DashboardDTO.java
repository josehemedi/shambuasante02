package hospicloud.dtos;

import java.util.List;

public class DashboardDTO {
    private StatistiqueMedecinDTO statistiques;
    private List<RendezVousJourDTO> rendezVousDuJour;
    private List<AlerteDTO> alertes;
    private List<MedecinFileItemDTO> filePatients;
    private List<DoctorConsultationActiveDTO> consultationsActives;
    private List<DoctorPendingNoteDTO> notesEnAttente;

    public DashboardDTO() {}

    public DashboardDTO(StatistiqueMedecinDTO statistiques,
                        List<RendezVousJourDTO> rendezVousDuJour,
                        List<AlerteDTO> alertes,
                        List<MedecinFileItemDTO> filePatients,
                        List<DoctorConsultationActiveDTO> consultationsActives,
                        List<DoctorPendingNoteDTO> notesEnAttente) {
        this.statistiques = statistiques;
        this.rendezVousDuJour = rendezVousDuJour;
        this.alertes = alertes;
        this.filePatients = filePatients;
        this.consultationsActives = consultationsActives;
        this.notesEnAttente = notesEnAttente;
    }

    public StatistiqueMedecinDTO getStatistiques() { return statistiques; }
    public void setStatistiques(StatistiqueMedecinDTO statistiques) { this.statistiques = statistiques; }

    public List<RendezVousJourDTO> getRendezVousDuJour() { return rendezVousDuJour; }
    public void setRendezVousDuJour(List<RendezVousJourDTO> rendezVousDuJour) { this.rendezVousDuJour = rendezVousDuJour; }

    public List<AlerteDTO> getAlertes() { return alertes; }
    public void setAlertes(List<AlerteDTO> alertes) { this.alertes = alertes; }

    public List<MedecinFileItemDTO> getFilePatients() { return filePatients; }
    public void setFilePatients(List<MedecinFileItemDTO> filePatients) { this.filePatients = filePatients; }

    public List<DoctorConsultationActiveDTO> getConsultationsActives() { return consultationsActives; }
    public void setConsultationsActives(List<DoctorConsultationActiveDTO> consultationsActives) {
        this.consultationsActives = consultationsActives;
    }

    public List<DoctorPendingNoteDTO> getNotesEnAttente() { return notesEnAttente; }
    public void setNotesEnAttente(List<DoctorPendingNoteDTO> notesEnAttente) { this.notesEnAttente = notesEnAttente; }
}