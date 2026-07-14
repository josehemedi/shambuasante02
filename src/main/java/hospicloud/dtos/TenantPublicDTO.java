package hospicloud.dtos;

public class TenantPublicDTO {
    private Integer idHopital;
    private String sousDomaine;
    private String name;
    private String nomCommercial;
    private String ville;
    private String pays;
    private String type;
    private String email;
    private String telephone;
    private String adresseComplete;
    private String logoUrl;
    private String planNom;
    private boolean estActif;

    public Integer getIdHopital() { return idHopital; }
    public void setIdHopital(Integer idHopital) { this.idHopital = idHopital; }

    public String getSousDomaine() { return sousDomaine; }
    public void setSousDomaine(String sousDomaine) { this.sousDomaine = sousDomaine; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getNomCommercial() { return nomCommercial; }
    public void setNomCommercial(String nomCommercial) { this.nomCommercial = nomCommercial; }

    public String getVille() { return ville; }
    public void setVille(String ville) { this.ville = ville; }

    public String getPays() { return pays; }
    public void setPays(String pays) { this.pays = pays; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getTelephone() { return telephone; }
    public void setTelephone(String telephone) { this.telephone = telephone; }

    public String getAdresseComplete() { return adresseComplete; }
    public void setAdresseComplete(String adresseComplete) { this.adresseComplete = adresseComplete; }

    public String getLogoUrl() { return logoUrl; }
    public void setLogoUrl(String logoUrl) { this.logoUrl = logoUrl; }

    public String getPlanNom() { return planNom; }
    public void setPlanNom(String planNom) { this.planNom = planNom; }

    public boolean isEstActif() { return estActif; }
    public void setEstActif(boolean estActif) { this.estActif = estActif; }
}
