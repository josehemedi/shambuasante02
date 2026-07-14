package hospicloud.model;

import java.time.LocalDate;

import hospicloud.enumeration.StatutAntecedent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class Antecedent {

    private Integer idAntecendent;

    @NotNull(message = "idPatient requis")
    private Integer idPatient;

    // hospital tenant id (may come from token, not from client)
    private Integer idHopital;

    @NotBlank(message = "typeAntecedent requis")
    private String typeAntecedent;

    @NotBlank(message = "libelle requis")
    @Size(max = 255)
    private String libelle;

    @Size(max = 2000)
    private String description;

    private boolean est_critique;

    private LocalDate dateDiagnostic;

    // 🔴 CHANGEMENT ICI : enum supprimé → String
    private StatutAntecedent statut;

    private LocalDate dateEnregistrement;

    // Full constructor
    public Antecedent(Integer idAntecendent,
                      Integer idPatient,
                      Integer idHopital,
                      String typeAntecedent,
                      String libelle,
                      String description,
                      boolean est_critique,
                      LocalDate dateDiagnostic,
                      StatutAntecedent statut,
                      LocalDate dateEnregistrement) {

        this.idAntecendent = idAntecendent;
        this.idPatient = idPatient;
        this.idHopital = idHopital;
        this.typeAntecedent = typeAntecedent;
        this.libelle = libelle;
        this.description = description;
        this.est_critique = est_critique;
        this.dateDiagnostic = dateDiagnostic;
        this.statut = statut;
        this.dateEnregistrement = dateEnregistrement;
    }

    // Overloaded constructor without idHopital
    public Antecedent(Integer idAntecendent,
                      Integer idPatient,
                      String typeAntecedent,
                      String libelle,
                      String description,
                      boolean est_critique,
                      LocalDate dateDiagnostic,
                      StatutAntecedent statut,
                      LocalDate dateEnregistrement) {

        this(idAntecendent, idPatient, null, typeAntecedent, libelle,
             description, est_critique, dateDiagnostic, statut, dateEnregistrement);
    }

    public Antecedent() {}

    public Integer getIdAntecendent() {
        return idAntecendent;
    }

    public void setIdAntecendent(Integer idAntecendent) {
        this.idAntecendent = idAntecendent;
    }

    public Integer getIdPatient() {
        return idPatient;
    }

    public void setIdPatient(Integer idPatient) {
        this.idPatient = idPatient;
    }

    public Integer getIdHopital() {
        return idHopital;
    }

    public void setIdHopital(Integer idHopital) {
        this.idHopital = idHopital;
    }

    public Integer getIdHopiatl() {
        return this.idHopital;
    }

    public void setIdHopiatl(Integer idHopiatl) {
        this.idHopital = idHopiatl;
    }

    public String getTypeAntecedent() {
        return typeAntecedent;
    }

    public void setTypeAntecedent(String typeAntecedent) {
        this.typeAntecedent = typeAntecedent;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public LocalDate getDateDiagnostic() {
        return dateDiagnostic;
    }

    public void setDateDiagnostic(LocalDate dateDiagnostic) {
        this.dateDiagnostic = dateDiagnostic;
    }

    // 🔴 CHANGEMENT ICI
    public StatutAntecedent getStatut() {
        return statut;
    }

    public void setStatut(StatutAntecedent statut) {
        this.statut = statut;
    }

    public LocalDate getDateEnregistrement() {
        return dateEnregistrement;
    }

    public void setDateEnregistrement(LocalDate dateEnregistrement) {
        this.dateEnregistrement = dateEnregistrement;
    }

    public String getLibelle() {
        return libelle;
    }

    public void setLibelle(String libelle) {
        this.libelle = libelle;
    }

    public boolean isEst_critique() {
        return est_critique;
    }

    public void setEst_critique(boolean est_critique) {
        this.est_critique = est_critique;
    }
}