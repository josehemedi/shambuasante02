package hospicloud.dtos.patient;

import java.time.LocalDateTime;

public class UpcomingAppointmentDTO {
    private Integer idRdv;
    private Integer idHopital;
    private LocalDateTime dateHeureRdv;
    private String motifVisite;
    private String nomMedecin;
    private String canal;
    private String statutRdv;
    /** Référence professionnelle affichable, ex. TC-01-0009 */
    private String numeroTeleconsultation;

    public UpcomingAppointmentDTO() {}

    public UpcomingAppointmentDTO(Integer idRdv, Integer idHopital, LocalDateTime dateHeureRdv,
                                  String motifVisite, String nomMedecin, String canal, String statutRdv) {
        this.idRdv = idRdv;
        this.idHopital = idHopital;
        this.dateHeureRdv = dateHeureRdv;
        this.motifVisite = motifVisite;
        this.nomMedecin = nomMedecin;
        this.canal = canal;
        this.statutRdv = statutRdv;
        this.numeroTeleconsultation = buildNumero(idHopital, idRdv);
    }

    private static String buildNumero(Integer idHopital, Integer idRdv) {
        if (idRdv == null || idRdv <= 0) {
            return "TC-0000";
        }
        String seq = String.format("%04d", idRdv);
        if (idHopital != null && idHopital > 0) {
            return String.format("TC-%02d-%s", idHopital, seq);
        }
        return "TC-" + seq;
    }

    public Integer getIdRdv() { return idRdv; }
    public void setIdRdv(Integer idRdv) { this.idRdv = idRdv; }

    public Integer getIdHopital() { return idHopital; }
    public void setIdHopital(Integer idHopital) { this.idHopital = idHopital; }

    public LocalDateTime getDateHeureRdv() { return dateHeureRdv; }
    public void setDateHeureRdv(LocalDateTime dateHeureRdv) { this.dateHeureRdv = dateHeureRdv; }

    public String getMotifVisite() { return motifVisite; }
    public void setMotifVisite(String motifVisite) { this.motifVisite = motifVisite; }

    public String getNomMedecin() { return nomMedecin; }
    public void setNomMedecin(String nomMedecin) { this.nomMedecin = nomMedecin; }

    public String getCanal() { return canal; }
    public void setCanal(String canal) { this.canal = canal; }

    public String getStatutRdv() { return statutRdv; }
    public void setStatutRdv(String statutRdv) { this.statutRdv = statutRdv; }

    public String getNumeroTeleconsultation() { return numeroTeleconsultation; }
    public void setNumeroTeleconsultation(String numeroTeleconsultation) {
        this.numeroTeleconsultation = numeroTeleconsultation;
    }
}
