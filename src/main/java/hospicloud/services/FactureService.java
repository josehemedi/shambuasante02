package hospicloud.services;

import hospicloud.dtos.FactureRequestDto;
import hospicloud.dtos.FactureResponseDto;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public interface FactureService {

    FactureResponseDto creerFacture(FactureRequestDto requestDto);

    FactureResponseDto obtenirParId(Integer idFacture);

    FactureResponseDto obtenirParNumero(String numeroFacture);

    List<FactureResponseDto> listerFacturesDuPatient(Integer idPatient);

    List<FactureResponseDto> listerFacturesDeLHopital();

    List<FactureResponseDto> listerParStatut(String statut);

    FactureResponseDto mettreAJourStatut(Integer idFacture, String nouveauStatut);

    BigDecimal calculerTotalTtc(BigDecimal montantHt, BigDecimal tauxTva);

    Map<String, Object> getFactureParams(Integer idFacture);
}