package hospicloud.model;

import hospicloud.model.enums.StatutSignature;
import hospicloud.model.enums.TypeDocument;

import java.time.LocalDateTime;

public class SignatureDocument {
    private Long id;
    private Integer hopitalId;
    private Long documentId;
    private TypeDocument typeDocument;
    private Integer medecinId;
    private Integer utilisateurId;
    private String nomMedecin;
    private String hashDocument;
    private String adresseIp;
    private String methodeAuthentification;
    private LocalDateTime dateSignature;
    private StatutSignature statut;
    private String referenceSignature;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Integer getHopitalId() { return hopitalId; }
    public void setHopitalId(Integer hopitalId) { this.hopitalId = hopitalId; }

    public Long getDocumentId() { return documentId; }
    public void setDocumentId(Long documentId) { this.documentId = documentId; }

    public TypeDocument getTypeDocument() { return typeDocument; }
    public void setTypeDocument(TypeDocument typeDocument) { this.typeDocument = typeDocument; }

    public Integer getMedecinId() { return medecinId; }
    public void setMedecinId(Integer medecinId) { this.medecinId = medecinId; }

    public Integer getUtilisateurId() { return utilisateurId; }
    public void setUtilisateurId(Integer utilisateurId) { this.utilisateurId = utilisateurId; }

    public String getNomMedecin() { return nomMedecin; }
    public void setNomMedecin(String nomMedecin) { this.nomMedecin = nomMedecin; }

    public String getHashDocument() { return hashDocument; }
    public void setHashDocument(String hashDocument) { this.hashDocument = hashDocument; }

    public String getAdresseIp() { return adresseIp; }
    public void setAdresseIp(String adresseIp) { this.adresseIp = adresseIp; }

    public String getMethodeAuthentification() { return methodeAuthentification; }
    public void setMethodeAuthentification(String methodeAuthentification) {
        this.methodeAuthentification = methodeAuthentification;
    }

    public LocalDateTime getDateSignature() { return dateSignature; }
    public void setDateSignature(LocalDateTime dateSignature) { this.dateSignature = dateSignature; }

    public StatutSignature getStatut() { return statut; }
    public void setStatut(StatutSignature statut) { this.statut = statut; }

    public String getReferenceSignature() { return referenceSignature; }
    public void setReferenceSignature(String referenceSignature) { this.referenceSignature = referenceSignature; }
}
