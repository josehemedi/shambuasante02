package hospicloud.repositoriesImpl;

import hospicloud.dtos.lab.LabDashboardStatsDTO;
import hospicloud.dtos.lab.ResultatAnalyseCritiqueDTO;
import hospicloud.model.lab.CommandeAnalyse;
import hospicloud.repositories.LabDashboardRepository;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;

@Repository
public class LabDashboardRepositoryImpl implements LabDashboardRepository {

    private final JdbcTemplate jdbcTemplate;

    public LabDashboardRepositoryImpl(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public LabDashboardStatsDTO getStatsByLocataire(String idLocataire) {
        String sql = 
            "SELECT " +
            "  SUM(CASE WHEN statut = 'EN_ATTENTE' THEN 1 ELSE 0 END) AS en_attente, " +
            "  SUM(CASE WHEN statut = 'EN_COURS' THEN 1 ELSE 0 END) AS en_cours, " +
            "  SUM(CASE WHEN statut = 'TERMINE' THEN 1 ELSE 0 END) AS terminees, " +
            "  (SELECT COUNT(*) FROM resultats_analyses ra WHERE ra.id_locataire = ? AND ra.est_critique = TRUE AND ra.est_acquitte = FALSE) AS critiques " +
            "FROM commandes_analyses ca " +
            "WHERE ca.id_locataire = ?";

        return jdbcTemplate.queryForObject(sql, (rs, rowNum) -> {
            return new LabDashboardStatsDTO(
                rs.getLong("en_attente"),
                rs.getLong("en_cours"),
                rs.getLong("terminees"),
                rs.getLong("critiques")
            );
        }, idLocataire, idLocataire);
    }

    @Override
    public List<ResultatAnalyseCritiqueDTO> getResultatsCritiquesNonAcquittes(String idLocataire, int limit, int offset) {
        String sql = 
            "SELECT id, id_commande_analyse, nom_parametre, valeur_mesuree, unite, seuil_min, seuil_max, est_acquitte " +
            "FROM resultats_analyses " +
            "WHERE id_locataire = ? AND est_critique = TRUE AND est_acquitte = FALSE " +
            "ORDER BY id_commande_analyse DESC " +
            "LIMIT ? OFFSET ?";

        return jdbcTemplate.query(sql, (rs, rowNum) -> {
            ResultatAnalyseCritiqueDTO dto = new ResultatAnalyseCritiqueDTO();
            dto.setId(rs.getString("id"));
            dto.setIdCommandeAnalyse(rs.getString("id_commande_analyse"));
            dto.setNomParametre(rs.getString("nom_parametre"));
            dto.setValeurMesuree(rs.getBigDecimal("valeur_mesuree"));
            dto.setUnite(rs.getString("unite"));
            dto.setSeuilMin(rs.getBigDecimal("seuil_min"));
            dto.setSeuilMax(rs.getBigDecimal("seuil_max"));
            dto.setEstAcquitte(rs.getBoolean("est_acquitte"));
            return dto;
        }, idLocataire, limit, offset);
    }

    @Override
    public void acquitterResultatCritique(String idResultat, String idLocataire) {
        String sql = "UPDATE resultats_analyses SET est_acquitte = TRUE WHERE id = ? AND id_locataire = ?";
        jdbcTemplate.update(sql, idResultat, idLocataire);
    }

    @Override
    public List<CommandeAnalyse> getCommandesAnalyses(String idLocataire, String statut, int limit, int offset) {
        StringBuilder sql = new StringBuilder("SELECT id, id_locataire, statut, urgence FROM commandes_analyses WHERE id_locataire = ?");
        List<Object> params = new ArrayList<>();
        params.add(idLocataire);

        if (statut != null && !statut.isEmpty()) {
            sql.append(" AND statut = ?");
            params.add(statut);
        }

        sql.append(" ORDER BY urgence DESC LIMIT ? OFFSET ?"); // Basic order logic
        params.add(limit);
        params.add(offset);

        return jdbcTemplate.query(sql.toString(), (rs, rowNum) -> {
            CommandeAnalyse c = new CommandeAnalyse();
            c.setId(rs.getString("id"));
            c.setIdLocataire(rs.getString("id_locataire"));
            c.setStatut(rs.getString("statut"));
            c.setUrgence(rs.getString("urgence"));
            // Add dateCommande mapping based on actual column mapping if added
            return c;
        }, params.toArray());
    }
}
