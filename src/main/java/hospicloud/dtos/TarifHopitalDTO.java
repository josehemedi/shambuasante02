package hospicloud.dtos;

import java.math.BigDecimal;

public class TarifHopitalDTO {
    private Integer idTarif;
    private Integer idHopital;
    private String code;
    private String libelle;
    private String categorie;
    private BigDecimal prixUnitaire;
    private boolean actif = true;

    public Integer getIdTarif() { return idTarif; }
    public void setIdTarif(Integer idTarif) { this.idTarif = idTarif; }

    public Integer getIdHopital() { return idHopital; }
    public void setIdHopital(Integer idHopital) { this.idHopital = idHopital; }

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }

    public String getLibelle() { return libelle; }
    public void setLibelle(String libelle) { this.libelle = libelle; }

    public String getCategorie() { return categorie; }
    public void setCategorie(String categorie) { this.categorie = categorie; }

    public BigDecimal getPrixUnitaire() { return prixUnitaire; }
    public void setPrixUnitaire(BigDecimal prixUnitaire) { this.prixUnitaire = prixUnitaire; }

    public boolean isActif() { return actif; }
    public void setActif(boolean actif) { this.actif = actif; }
}
