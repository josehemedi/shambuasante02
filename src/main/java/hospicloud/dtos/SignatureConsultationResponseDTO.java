package hospicloud.dtos;

public class SignatureConsultationResponseDTO {
    private Long idConsultation;
    private String statut;
    private String nomMedecin;
    private String numeroOrdre;
    private String dateSignature;
    private String referenceSignature;
    private String hashAbrege;

    public Long getIdConsultation() { return idConsultation; }
    public void setIdConsultation(Long idConsultation) { this.idConsultation = idConsultation; }

    public String getStatut() { return statut; }
    public void setStatut(String statut) { this.statut = statut; }

    public String getNomMedecin() { return nomMedecin; }
    public void setNomMedecin(String nomMedecin) { this.nomMedecin = nomMedecin; }

    public String getNumeroOrdre() { return numeroOrdre; }
    public void setNumeroOrdre(String numeroOrdre) { this.numeroOrdre = numeroOrdre; }

    public String getDateSignature() { return dateSignature; }
    public void setDateSignature(String dateSignature) { this.dateSignature = dateSignature; }

    public String getReferenceSignature() { return referenceSignature; }
    public void setReferenceSignature(String referenceSignature) { this.referenceSignature = referenceSignature; }

    public String getHashAbrege() { return hashAbrege; }
    public void setHashAbrege(String hashAbrege) { this.hashAbrege = hashAbrege; }
}
