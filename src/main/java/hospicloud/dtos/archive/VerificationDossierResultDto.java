package hospicloud.dtos.archive;

import hospicloud.model.archive.TypeEpisode;

import java.util.ArrayList;
import java.util.List;

public class VerificationDossierResultDto {

    private boolean complet;
    private boolean peutArchiver;
    private List<String> manquants = new ArrayList<>();
    private List<String> avertissements = new ArrayList<>();

    public boolean isComplet() { return complet; }
    public void setComplet(boolean complet) { this.complet = complet; }

    public boolean isPeutArchiver() { return peutArchiver; }
    public void setPeutArchiver(boolean peutArchiver) { this.peutArchiver = peutArchiver; }

    public List<String> getManquants() { return manquants; }
    public void setManquants(List<String> manquants) { this.manquants = manquants; }

    public List<String> getAvertissements() { return avertissements; }
    public void setAvertissements(List<String> avertissements) { this.avertissements = avertissements; }

    public void addManquant(String item) { this.manquants.add(item); }
    public void addAvertissement(String item) { this.avertissements.add(item); }
}
