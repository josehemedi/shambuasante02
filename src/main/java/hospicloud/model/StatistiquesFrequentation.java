package hospicloud.model;

import java.time.LocalDate;
import lombok.Data;

@Data
public class StatistiquesFrequentation {
    private Integer id;
    private Integer idHopital;
    private Integer idMedecin;
    private LocalDate dateStat;
    private Integer nombreConsultations;
    private Integer nombreHospitalisations;
    private Integer nombreNouveauxPatients;
    private Double chiffreAffaireJournalier;
	public StatistiquesFrequentation(Integer id, Integer idHopital, Integer idMedecin, LocalDate dateStat,
			Integer nombreConsultations, Integer nombreHospitalisations, Integer nombreNouveauxPatients,
			Double chiffreAffaireJournalier) {
		super();
		this.id = id;
		this.idHopital = idHopital;
		this.idMedecin = idMedecin;
		this.dateStat = dateStat;
		this.nombreConsultations = nombreConsultations;
		this.nombreHospitalisations = nombreHospitalisations;
		this.nombreNouveauxPatients = nombreNouveauxPatients;
		this.chiffreAffaireJournalier = chiffreAffaireJournalier;
	}
	public Integer getId() {
		return id;
	}
	public void setId(Integer id) {
		this.id = id;
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
	public LocalDate getDateStat() {
		return dateStat;
	}
	public void setDateStat(LocalDate dateStat) {
		this.dateStat = dateStat;
	}
	public Integer getNombreConsultations() {
		return nombreConsultations;
	}
	public void setNombreConsultations(Integer nombreConsultations) {
		this.nombreConsultations = nombreConsultations;
	}
	public Integer getNombreHospitalisations() {
		return nombreHospitalisations;
	}
	public void setNombreHospitalisations(Integer nombreHospitalisations) {
		this.nombreHospitalisations = nombreHospitalisations;
	}
	public Integer getNombreNouveauxPatients() {
		return nombreNouveauxPatients;
	}
	public void setNombreNouveauxPatients(Integer nombreNouveauxPatients) {
		this.nombreNouveauxPatients = nombreNouveauxPatients;
	}
	public Double getChiffreAffaireJournalier() {
		return chiffreAffaireJournalier;
	}
	public void setChiffreAffaireJournalier(Double chiffreAffaireJournalier) {
		this.chiffreAffaireJournalier = chiffreAffaireJournalier;
	}
	public StatistiquesFrequentation() {
		super();
		// TODO Auto-generated constructor stub
	}
    
    
}