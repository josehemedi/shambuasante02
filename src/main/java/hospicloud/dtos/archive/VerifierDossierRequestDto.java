package hospicloud.dtos.archive;

import hospicloud.model.archive.TypeEpisode;

public class VerifierDossierRequestDto {

    private TypeEpisode typeEpisode;
    private Long episodeId;
    private Long patientId;

    public TypeEpisode getTypeEpisode() { return typeEpisode; }
    public void setTypeEpisode(TypeEpisode typeEpisode) { this.typeEpisode = typeEpisode; }

    public Long getEpisodeId() { return episodeId; }
    public void setEpisodeId(Long episodeId) { this.episodeId = episodeId; }

    public Long getPatientId() { return patientId; }
    public void setPatientId(Long patientId) { this.patientId = patientId; }
}
