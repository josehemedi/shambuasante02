package hospicloud.dtos;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class RapportRequestDto {

    public enum TypeRapport {
        FACTURE, BULLETIN_SORTIE, ORDONNANCE
    }

    private TypeRapport type;
    private Integer idRapport;
    private Integer idHopital;
    private String emailDestinataire;
    private String nomPatient;
    private String numeroFacture;
    private LocalDateTime dateFacture;
    private BigDecimal montantTotalHt;
    private BigDecimal tva;
    private BigDecimal montantTotalTtc;
    private String statutPaiement;

    public TypeRapport getType() { return type; }
    public void setType(TypeRapport type) { this.type = type; }

    public Integer getIdRapport() { return idRapport; }
    public void setIdRapport(Integer idRapport) { this.idRapport = idRapport; }

    public Integer getIdHopital() { return idHopital; }
    public void setIdHopital(Integer idHopital) { this.idHopital = idHopital; }

    public String getEmailDestinataire() { return emailDestinataire; }
    public void setEmailDestinataire(String emailDestinataire) { this.emailDestinataire = emailDestinataire; }

    public String getNomPatient() { return nomPatient; }
    public void setNomPatient(String nomPatient) { this.nomPatient = nomPatient; }

    public String getNumeroFacture() { return numeroFacture; }
    public void setNumeroFacture(String numeroFacture) { this.numeroFacture = numeroFacture; }

    public LocalDateTime getDateFacture() { return dateFacture; }
    public void setDateFacture(LocalDateTime dateFacture) { this.dateFacture = dateFacture; }

    public BigDecimal getMontantTotalHt() { return montantTotalHt; }
    public void setMontantTotalHt(BigDecimal montantTotalHt) { this.montantTotalHt = montantTotalHt; }

    public BigDecimal getTva() { return tva; }
    public void setTva(BigDecimal tva) { this.tva = tva; }

    public BigDecimal getMontantTotalTtc() { return montantTotalTtc; }
    public void setMontantTotalTtc(BigDecimal montantTotalTtc) { this.montantTotalTtc = montantTotalTtc; }

    public String getStatutPaiement() { return statutPaiement; }
    public void setStatutPaiement(String statutPaiement) { this.statutPaiement = statutPaiement; }
}