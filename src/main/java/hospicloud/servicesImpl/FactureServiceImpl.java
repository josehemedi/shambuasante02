package hospicloud.servicesImpl;

import hospicloud.dtos.FactureRequestDto;
import hospicloud.dtos.FactureResponseDto;
import hospicloud.model.Facture;
import hospicloud.model.Patient;
import hospicloud.repositories.FactureRepository;
import hospicloud.repositories.PatientRepository;
import hospicloud.security.CurrentUserContext;
import hospicloud.security.TenantAuthorization;
import hospicloud.security.TenantContext;
import hospicloud.services.FactureService;
import hospicloud.model.Role;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Transactional
public class FactureServiceImpl implements FactureService {

    private final FactureRepository factureRepository;
    private final PatientRepository patientRepository;

    @Autowired
    public FactureServiceImpl(FactureRepository factureRepository,
                              PatientRepository patientRepository) {
        this.factureRepository = factureRepository;
        this.patientRepository = patientRepository;
    }

    @Override
    public FactureResponseDto creerFacture(FactureRequestDto requestDto) {
        Facture facture = new Facture();

        facture.setIdHopital(TenantContext.getRequiredHopitalId());
        facture.setIdPatient(requestDto.getIdPatient());
        facture.setNumeroFacture(requestDto.getNumeroFacture() != null ?
                requestDto.getNumeroFacture() : genererNumeroFacture());
        facture.setDateFacture(LocalDateTime.now());
        
        BigDecimal montantHt = requestDto.getMontantTotalHt() != null ? requestDto.getMontantTotalHt() : BigDecimal.ZERO;
        BigDecimal tauxTva = requestDto.getTauxTva() != null ? requestDto.getTauxTva() : BigDecimal.valueOf(16.00);
        BigDecimal montantTtc = calculerTotalTtc(montantHt, tauxTva);
        
        facture.setMontantTotalHt(montantHt);
        facture.setTva(tauxTva);
        facture.setMontantTotalTtc(montantTtc);
        facture.setStatutPaiement("IMPAYE");
        facture.setIdCaissier(requestDto.getIdCaissier());

        Facture saved = factureRepository.save(facture);
        return mapToResponseDto(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public FactureResponseDto obtenirParId(Integer idFacture) {
        Facture facture = factureRepository.findById(idFacture)
                .orElseThrow(() -> new RuntimeException("Facture non trouvée avec l'ID: " + idFacture));
        return mapToResponseDto(facture);
    }

    @Override
    @Transactional(readOnly = true)
    public FactureResponseDto obtenirParNumero(String numeroFacture) {
        Facture facture = factureRepository.findByNumeroFacture(numeroFacture)
                .orElseThrow(() -> new RuntimeException("Facture non trouvée avec le numéro: " + numeroFacture));
        return mapToResponseDto(facture);
    }

    @Override
    @Transactional(readOnly = true)
    public List<FactureResponseDto> listerFacturesDuPatient(Integer idPatient) {
        return factureRepository.findByIdPatient(idPatient).stream()
                .map(this::mapToResponseDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<FactureResponseDto> listerFacturesDeLHopital() {
        Integer hopitalId = TenantContext.getRequiredHopitalId();
        return factureRepository.findByIdHopital(hopitalId).stream()
                .map(this::mapToResponseDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<FactureResponseDto> listerParStatut(String statut) {
        return factureRepository.findByStatutPaiement(statut).stream()
                .map(this::mapToResponseDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public FactureResponseDto mettreAJourStatut(Integer idFacture, String nouveauStatut) {
        Facture facture = factureRepository.findById(idFacture)
                .orElseThrow(() -> new RuntimeException("Facture non trouvée avec l'ID: " + idFacture));

        factureRepository.updateStatutPaiement(idFacture, nouveauStatut);
        return mapToResponseDto(facture);
    }

    @Override
    public BigDecimal calculerTotalTtc(BigDecimal montantHt, BigDecimal tauxTva) {
        return montantHt.multiply(BigDecimal.valueOf(1).add(tauxTva.divide(BigDecimal.valueOf(100))));
    }

    @Override
    @Transactional(readOnly = true)
    public Map<String, Object> getFactureParams(Integer idFacture) {
        Facture facture = factureRepository.findById(idFacture)
                .orElseThrow(() -> new RuntimeException("Facture non trouvée avec l'ID: " + idFacture));

        Patient patient = patientRepository.trouverPatientParId(facture.getIdPatient().longValue())
                .orElseThrow(() -> new RuntimeException("Patient non trouvé avec l'ID: " + facture.getIdPatient()));

        Map<String, Object> params = new java.util.HashMap<>();
        params.put("idPatient", facture.getIdPatient());
        params.put("numeroFacture", facture.getNumeroFacture());
        params.put("dateFacture", facture.getDateFacture() != null ? facture.getDateFacture().toString() : "");
        params.put("montantTotalHt", facture.getMontantTotalHt() != null ? facture.getMontantTotalHt().doubleValue() : 0.0);
        params.put("tva", facture.getTva() != null ? facture.getTva().doubleValue() : 16.0);
        params.put("montantTotalTtc", facture.getMontantTotalTtc() != null ? facture.getMontantTotalTtc().doubleValue() : 0.0);
        params.put("statutPaiement", facture.getStatutPaiement());
        params.put("idCaissier", facture.getIdCaissier());
        params.put("NOM_PATIENT", patient.getPrenom() + " " + patient.getNom());
        params.put("NOM_CAISSE", facture.getIdCaissier() != null ? "Caissier #" + facture.getIdCaissier() : "Non assigné");

        return params;
    }

    private String genererNumeroFacture() {
        Integer hopitalId = TenantContext.getRequiredHopitalId();
        int year = LocalDate.now().getYear();
        String seqName = "facture_seq_hopital_" + hopitalId + "_" + year;

        long seq = factureRepository.getNextSequenceValue(seqName);
        return String.format("FAC-%d-%04d", year, seq);
    }

    private FactureResponseDto mapToResponseDto(Facture f) {
        FactureResponseDto dto = new FactureResponseDto();
        dto.setIdFacture(f.getIdFacture());
        dto.setNumeroFacture(f.getNumeroFacture());
        dto.setDateFacture(f.getDateFacture());
        dto.setMontantTotalHt(f.getMontantTotalHt());
        dto.setTva(f.getTva());
        dto.setMontantTotalTtc(f.getMontantTotalTtc());
        dto.setStatutPaiement(f.getStatutPaiement());
        dto.setIdPatient(f.getIdPatient());
        dto.setIdHopital(f.getIdHopital());
        dto.setIdCaissier(f.getIdCaissier());

        if (f.getIdPatient() != null) {
            try {
                patientRepository.trouverPatientParId(f.getIdPatient().longValue())
                        .ifPresent(p -> dto.setNomPatient(p.getPrenom() + " " + p.getNom()));
            } catch (Exception ignored) {}
        }

        return dto;
    }
}