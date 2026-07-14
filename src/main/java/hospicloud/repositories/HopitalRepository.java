package hospicloud.repositories;

import java.time.LocalDate;
import java.util.List;

import hospicloud.model.Hopital;

public interface HopitalRepository {
	// j'ai definit toutes les methodes necessaires et importantes
	 void enresgitrerHopital(Hopital hopital);
	 Hopital rechercherhopitalParId(Long idHopital);
	 Hopital rechercherParNom(String nom);
	 List<Hopital> listerTous();
	 void modifier(Hopital hopital);
	 void supprimer(Integer id);
	 Long countActifsByHopital(Integer hopitalId);
	 Long countActifsByHopitalInPeriod(Integer hopitalId, LocalDate startDate, LocalDate endDate);
	 /** Agrégation plateforme Super Admin */
	 Long countAllActifs();
	 Long countAllActifsExistingBefore(LocalDate date);
	 boolean existsByEmail(String email);
	 boolean existsBySousDomaine(String sousDomaine);
	 boolean existsBySousDomaineExcludingId(String sousDomaine, Integer idHopital);
	 boolean existsByEmailExcludingId(String email, Integer idHopital);
	 java.util.Optional<Hopital> findBySousDomaine(String sousDomaine);
}
