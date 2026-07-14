package hospicloud.model.archive;

public class ReglesArchivageHopital {

    private Integer id;
    private Integer hopitalId;
    private boolean exigerClotureMedicale = true;
    private boolean exigerClotureAdministrative;
    private boolean exigerClotureFinanciere;

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public Integer getHopitalId() { return hopitalId; }
    public void setHopitalId(Integer hopitalId) { this.hopitalId = hopitalId; }

    public boolean isExigerClotureMedicale() { return exigerClotureMedicale; }
    public void setExigerClotureMedicale(boolean exigerClotureMedicale) { this.exigerClotureMedicale = exigerClotureMedicale; }

    public boolean isExigerClotureAdministrative() { return exigerClotureAdministrative; }
    public void setExigerClotureAdministrative(boolean exigerClotureAdministrative) { this.exigerClotureAdministrative = exigerClotureAdministrative; }

    public boolean isExigerClotureFinanciere() { return exigerClotureFinanciere; }
    public void setExigerClotureFinanciere(boolean exigerClotureFinanciere) { this.exigerClotureFinanciere = exigerClotureFinanciere; }
}
