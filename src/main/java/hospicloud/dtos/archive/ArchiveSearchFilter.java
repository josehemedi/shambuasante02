package hospicloud.dtos.archive;

import hospicloud.model.archive.StatutArchive;
import hospicloud.model.archive.TypeEpisode;

import java.time.LocalDateTime;

public class ArchiveSearchFilter {

    private StatutArchive statut;
    private TypeEpisode typeEpisode;
    private Long patientId;
    private Integer idMedecin;
    private Integer idService;
    private String search;
    private LocalDateTime dateFrom;
    private LocalDateTime dateTo;
    private int page = 0;
    private int size = 20;
    private String sort = "date_fin_episode";
    private String direction = "DESC";

    public StatutArchive getStatut() { return statut; }
    public void setStatut(StatutArchive statut) { this.statut = statut; }

    public TypeEpisode getTypeEpisode() { return typeEpisode; }
    public void setTypeEpisode(TypeEpisode typeEpisode) { this.typeEpisode = typeEpisode; }

    public Long getPatientId() { return patientId; }
    public void setPatientId(Long patientId) { this.patientId = patientId; }

    public Integer getIdMedecin() { return idMedecin; }
    public void setIdMedecin(Integer idMedecin) { this.idMedecin = idMedecin; }

    public Integer getIdService() { return idService; }
    public void setIdService(Integer idService) { this.idService = idService; }

    public String getSearch() { return search; }
    public void setSearch(String search) { this.search = search; }

    public LocalDateTime getDateFrom() { return dateFrom; }
    public void setDateFrom(LocalDateTime dateFrom) { this.dateFrom = dateFrom; }

    public LocalDateTime getDateTo() { return dateTo; }
    public void setDateTo(LocalDateTime dateTo) { this.dateTo = dateTo; }

    public int getPage() { return page; }
    public void setPage(int page) { this.page = page; }

    public int getSize() { return size; }
    public void setSize(int size) { this.size = size; }

    public String getSort() { return sort; }
    public void setSort(String sort) { this.sort = sort; }

    public String getDirection() { return direction; }
    public void setDirection(String direction) { this.direction = direction; }
}
