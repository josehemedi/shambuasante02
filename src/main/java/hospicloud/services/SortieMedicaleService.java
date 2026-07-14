package hospicloud.services;

import hospicloud.dtos.sortie.AutoriserSortieRequestDTO;
import hospicloud.dtos.sortie.AutoriserSortieResponseDTO;
import hospicloud.dtos.sortie.ContexteSortieDTO;
import hospicloud.dtos.sortie.PretSortieDTO;

import java.util.List;

public interface SortieMedicaleService {

    ContexteSortieDTO getContexteSortie(Integer idPatient);

    AutoriserSortieResponseDTO autoriserSortieMedicale(AutoriserSortieRequestDTO request);

    List<PretSortieDTO> listerPretesPourDelivrance();

    PretSortieDTO delivrerBonSortie(Integer idBonSortie, boolean paiementConfirme);
}
