package hospicloud.dtos;

public class LabResultatSubmitDTO {
    private String resultatTexte;
    private String interpretation;
    private String valeursReference;
    private String statut; // EN_COURS | TERMINE

    public String getResultatTexte() { return resultatTexte; }
    public void setResultatTexte(String resultatTexte) { this.resultatTexte = resultatTexte; }

    public String getInterpretation() { return interpretation; }
    public void setInterpretation(String interpretation) { this.interpretation = interpretation; }

    public String getValeursReference() { return valeursReference; }
    public void setValeursReference(String valeursReference) { this.valeursReference = valeursReference; }

    public String getStatut() { return statut; }
    public void setStatut(String statut) { this.statut = statut; }
}
