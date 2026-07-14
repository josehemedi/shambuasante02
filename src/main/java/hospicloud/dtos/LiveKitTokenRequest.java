package hospicloud.dtos;

public class LiveKitTokenRequest {
    private Integer idRendezVous;

    public LiveKitTokenRequest() {}

    public LiveKitTokenRequest(Integer idRendezVous) {
        this.idRendezVous = idRendezVous;
    }

    public Integer getIdRendezVous() { return idRendezVous; }
    public void setIdRendezVous(Integer idRendezVous) { this.idRendezVous = idRendezVous; }
}