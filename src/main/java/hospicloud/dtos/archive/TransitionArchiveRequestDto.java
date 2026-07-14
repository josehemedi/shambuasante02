package hospicloud.dtos.archive;

public class TransitionArchiveRequestDto {

    private String motif;
    private String observation;
    private String emplacementPhysique;
    private String numeroBoiteArchive;
    private String numeroRayon;

    public String getMotif() { return motif; }
    public void setMotif(String motif) { this.motif = motif; }

    public String getObservation() { return observation; }
    public void setObservation(String observation) { this.observation = observation; }

    public String getEmplacementPhysique() { return emplacementPhysique; }
    public void setEmplacementPhysique(String emplacementPhysique) { this.emplacementPhysique = emplacementPhysique; }

    public String getNumeroBoiteArchive() { return numeroBoiteArchive; }
    public void setNumeroBoiteArchive(String numeroBoiteArchive) { this.numeroBoiteArchive = numeroBoiteArchive; }

    public String getNumeroRayon() { return numeroRayon; }
    public void setNumeroRayon(String numeroRayon) { this.numeroRayon = numeroRayon; }
}
