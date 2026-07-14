package hospicloud.services;

import java.util.List;

import hospicloud.model.Hopital;

public interface HospitalService {
	 void enresgitrerHopital(Hopital hopital);
	 Hopital rechercherhopitalParId(Long idHopital);
	 Hopital rechercherParNom(String nom);
	 List<Hopital> listerTous();
	 void modifier(Hopital hopital);
	 void supprimer(Integer id);

}
