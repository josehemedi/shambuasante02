package hospicloud.dtos;

public class StatistiqueMedecinDTO {
	public StatistiqueMedecinDTO(long consultationsAujourdhui, long hospitalisationsEnCours, long patientsTotal,
			long rendezVousAujourdhui, long examensEnAttente, long notificationsNonLues) {
		super();
		this.consultationsAujourdhui = consultationsAujourdhui;
		this.hospitalisationsEnCours = hospitalisationsEnCours;
		this.patientsTotal = patientsTotal;
		this.rendezVousAujourdhui = rendezVousAujourdhui;
		this.examensEnAttente = examensEnAttente;
		this.notificationsNonLues = notificationsNonLues;
	}
	private long consultationsAujourdhui;
    private long hospitalisationsEnCours;
    private long patientsTotal;
    private long rendezVousAujourdhui;
    private long examensEnAttente;
    private long notificationsNonLues;
	public long getConsultationsAujourdhui() {
		return consultationsAujourdhui;
	}
	public void setConsultationsAujourdhui(long consultationsAujourdhui) {
		this.consultationsAujourdhui = consultationsAujourdhui;
	}
	public long getHospitalisationsEnCours() {
		return hospitalisationsEnCours;
	}
	public void setHospitalisationsEnCours(long hospitalisationsEnCours) {
		this.hospitalisationsEnCours = hospitalisationsEnCours;
	}
	public long getPatientsTotal() {
		return patientsTotal;
	}
	public void setPatientsTotal(long patientsTotal) {
		this.patientsTotal = patientsTotal;
	}
	public long getRendezVousAujourdhui() {
		return rendezVousAujourdhui;
	}
	public void setRendezVousAujourdhui(long rendezVousAujourdhui) {
		this.rendezVousAujourdhui = rendezVousAujourdhui;
	}
	public long getExamensEnAttente() {
		return examensEnAttente;
	}
	public void setExamensEnAttente(long examensEnAttente) {
		this.examensEnAttente = examensEnAttente;
	}
	public long getNotificationsNonLues() {
		return notificationsNonLues;
	}
	public void setNotificationsNonLues(long notificationsNonLues) {
		this.notificationsNonLues = notificationsNonLues;
	}
	
	
	@Override
	public String toString() {
		return "StatistiqueMedecinDTO [consultationsAujourdhui=" + consultationsAujourdhui
				+ ", hospitalisationsEnCours=" + hospitalisationsEnCours + ", patientsTotal=" + patientsTotal
				+ ", rendezVousAujourdhui=" + rendezVousAujourdhui + ", examensEnAttente=" + examensEnAttente
				+ ", notificationsNonLues=" + notificationsNonLues + "]";
	}
	public StatistiqueMedecinDTO() {
		super();
	}
    
    

}
