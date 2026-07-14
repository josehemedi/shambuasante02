package hospicloud.dtos;

import java.util.List;

public class DoctorWorkspaceDTO {

    private long rendezVousAujourdhui;
    private long resultatsLaboEnAttente;
    private long messagesNonLus;
    private List<RendezVousJourDTO> agendaDuJour;
    private List<DoctorWorkspaceActivityDTO> activitesRecentes;

    public DoctorWorkspaceDTO() {
    }

    public DoctorWorkspaceDTO(long rendezVousAujourdhui,
                              long resultatsLaboEnAttente,
                              long messagesNonLus,
                              List<RendezVousJourDTO> agendaDuJour,
                              List<DoctorWorkspaceActivityDTO> activitesRecentes) {
        this.rendezVousAujourdhui = rendezVousAujourdhui;
        this.resultatsLaboEnAttente = resultatsLaboEnAttente;
        this.messagesNonLus = messagesNonLus;
        this.agendaDuJour = agendaDuJour;
        this.activitesRecentes = activitesRecentes;
    }

    public long getRendezVousAujourdhui() {
        return rendezVousAujourdhui;
    }

    public void setRendezVousAujourdhui(long rendezVousAujourdhui) {
        this.rendezVousAujourdhui = rendezVousAujourdhui;
    }

    public long getResultatsLaboEnAttente() {
        return resultatsLaboEnAttente;
    }

    public void setResultatsLaboEnAttente(long resultatsLaboEnAttente) {
        this.resultatsLaboEnAttente = resultatsLaboEnAttente;
    }

    public long getMessagesNonLus() {
        return messagesNonLus;
    }

    public void setMessagesNonLus(long messagesNonLus) {
        this.messagesNonLus = messagesNonLus;
    }

    public List<RendezVousJourDTO> getAgendaDuJour() {
        return agendaDuJour;
    }

    public void setAgendaDuJour(List<RendezVousJourDTO> agendaDuJour) {
        this.agendaDuJour = agendaDuJour;
    }

    public List<DoctorWorkspaceActivityDTO> getActivitesRecentes() {
        return activitesRecentes;
    }

    public void setActivitesRecentes(List<DoctorWorkspaceActivityDTO> activitesRecentes) {
        this.activitesRecentes = activitesRecentes;
    }
}
