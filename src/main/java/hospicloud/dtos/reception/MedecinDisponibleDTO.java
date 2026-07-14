package hospicloud.dtos.reception;

public class MedecinDisponibleDTO {

    private Integer idMedecin;
    private String nom;
    private String prenom;
    private String nomComplet;
    private String specialite;
    private String service;
    private Boolean disponible;
    private boolean enHoraire;
    private long patientsEnFile;
    private long patientsAssignes;
    private String email;
    private String telephonePro;

    public Integer getIdMedecin() { return idMedecin; }
    public void setIdMedecin(Integer idMedecin) { this.idMedecin = idMedecin; }

    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }

    public String getPrenom() { return prenom; }
    public void setPrenom(String prenom) { this.prenom = prenom; }

    public String getNomComplet() { return nomComplet; }
    public void setNomComplet(String nomComplet) { this.nomComplet = nomComplet; }

    public String getSpecialite() { return specialite; }
    public void setSpecialite(String specialite) { this.specialite = specialite; }

    public String getService() { return service; }
    public void setService(String service) { this.service = service; }

    public Boolean getDisponible() { return disponible; }
    public void setDisponible(Boolean disponible) { this.disponible = disponible; }

    public boolean isEnHoraire() { return enHoraire; }
    public void setEnHoraire(boolean enHoraire) { this.enHoraire = enHoraire; }

    public long getPatientsEnFile() { return patientsEnFile; }
    public void setPatientsEnFile(long patientsEnFile) { this.patientsEnFile = patientsEnFile; }

    public long getPatientsAssignes() { return patientsAssignes; }
    public void setPatientsAssignes(long patientsAssignes) { this.patientsAssignes = patientsAssignes; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getTelephonePro() { return telephonePro; }
    public void setTelephonePro(String telephonePro) { this.telephonePro = telephonePro; }
}
