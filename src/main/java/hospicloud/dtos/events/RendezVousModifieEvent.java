package hospicloud.dtos.events;

import java.time.LocalDateTime;

public class RendezVousModifieEvent {

    private Integer idRdv;
    private Integer idHopital;
    private Integer idMedecin;
    private Integer idPatient;

    private LocalDateTime ancienneDate;
    private LocalDateTime nouvelleDate;

    public RendezVousModifieEvent() {
    }

	public Integer getIdRdv() {
		return idRdv;
	}

	public void setIdRdv(Integer idRdv) {
		this.idRdv = idRdv;
	}

	public Integer getIdHopital() {
		return idHopital;
	}

	public void setIdHopital(Integer idHopital) {
		this.idHopital = idHopital;
	}

	public Integer getIdMedecin() {
		return idMedecin;
	}

	public void setIdMedecin(Integer idMedecin) {
		this.idMedecin = idMedecin;
	}

	public Integer getIdPatient() {
		return idPatient;
	}

	public void setIdPatient(Integer idPatient) {
		this.idPatient = idPatient;
	}

	public LocalDateTime getAncienneDate() {
		return ancienneDate;
	}

	public void setAncienneDate(LocalDateTime ancienneDate) {
		this.ancienneDate = ancienneDate;
	}

	public LocalDateTime getNouvelleDate() {
		return nouvelleDate;
	}

	public void setNouvelleDate(LocalDateTime nouvelleDate) {
		this.nouvelleDate = nouvelleDate;
	}

	public RendezVousModifieEvent(Integer idRdv, Integer idHopital, Integer idMedecin, Integer idPatient,
			LocalDateTime ancienneDate, LocalDateTime nouvelleDate) {
		super();
		this.idRdv = idRdv;
		this.idHopital = idHopital;
		this.idMedecin = idMedecin;
		this.idPatient = idPatient;
		this.ancienneDate = ancienneDate;
		this.nouvelleDate = nouvelleDate;
	}
    
}