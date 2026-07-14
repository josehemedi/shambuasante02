package hospicloud.model;

import lombok.Builder;

@Builder
public class Societe {
	private Long idSociete;
	private String nomSociete;
	private String adresseFacturation;
	private String telephoneContact;
	private String emailContact;
	private Double tauxCouverture;
	private Integer idHopital; // champ ajouté pour multi-tenant
	private String nomHopital;
	
	public Societe(Long idSociete, String nomSociete, String adresseFacturation, String telephoneContact,
				String emailContact, Double tauxCouverture,Integer idHopital,String nomHopital) {
		super();
		this.idSociete = idSociete;
		this.nomSociete = nomSociete;
		this.adresseFacturation = adresseFacturation;
		this.telephoneContact = telephoneContact;
		this.emailContact = emailContact;
		this.tauxCouverture = tauxCouverture;
		this.idHopital = idHopital;
		this.nomHopital = nomHopital;
	}
	
	public Societe() {
		super();
		// TODO Auto-generated constructor stub
	}

	public Long getIdSociete() {
		return idSociete;
	}
	public void setIdSociete(Long idSociete) {
		this.idSociete = idSociete;
	}
	public String getNomSociete() {
		return nomSociete;
	}
	public void setNomSociete(String nomSociete) {
		this.nomSociete = nomSociete;
	}
	public String getAdresseFacturation() {
		return adresseFacturation;
	}
	public void setAdresseFacturation(String adresseFacturation) {
		this.adresseFacturation = adresseFacturation;
	}
	public String getTelephoneContact() {
		return telephoneContact;
	}
	public void setTelephoneContact(String telephoneContact) {
		this.telephoneContact = telephoneContact;
	}
	public String getEmailContact() {
		return emailContact;
	}
	public void setEmailContact(String emailContact) {
		this.emailContact = emailContact;
	}
	public Double getTauxCouverture() {
		return tauxCouverture;
	}
	public void setTauxCouverture(Double tauxCouverture) {
		this.tauxCouverture = tauxCouverture;
	}
	
	public Integer getIdHopital() {
		return idHopital;
	}

	public void setIdHopital(Integer idHopital) {
		this.idHopital = idHopital;
	}
	// on doit ajouter les accesseurs et mutateurs 

	public String getNomHopital() {
		return nomHopital;
	}

	public void setNomHopital(String nomHopital) {
		this.nomHopital = nomHopital;
	}
	
}