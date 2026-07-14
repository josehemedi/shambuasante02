package hospicloud.servicesImpl;

import hospicloud.dtos.TenantPublicDTO;
import hospicloud.exceptions.ResourceNotFoundException;
import hospicloud.model.Hopital;
import hospicloud.repositories.AbonnementRepository;
import hospicloud.repositories.HopitalRepository;
import hospicloud.saas.SaasPlanRegistry;
import hospicloud.services.TenantPublicService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@Transactional(readOnly = true)
public class TenantPublicServiceImpl implements TenantPublicService {

    private final HopitalRepository hopitalRepository;
    private final AbonnementRepository abonnementRepository;

    public TenantPublicServiceImpl(HopitalRepository hopitalRepository,
                                   AbonnementRepository abonnementRepository) {
        this.hopitalRepository = hopitalRepository;
        this.abonnementRepository = abonnementRepository;
    }

    @Override
    public TenantPublicDTO getBySubdomain(String subdomain) {
        if (subdomain == null || subdomain.isBlank()) {
            throw new ResourceNotFoundException("Sous-domaine requis");
        }
        Hopital hopital = hopitalRepository.findBySousDomaine(subdomain.trim().toLowerCase())
                .orElseThrow(() -> new ResourceNotFoundException("Établissement introuvable pour ce sous-domaine"));

        TenantPublicDTO dto = new TenantPublicDTO();
        dto.setIdHopital(hopital.getIdHopital());
        dto.setSousDomaine(hopital.getSousDomaine());
        dto.setName(hopital.getNom());
        dto.setNomCommercial(hopital.getNomCommercial());
        dto.setVille(hopital.getVille());
        dto.setPays(hopital.getPays());
        dto.setType(hopital.getType());
        dto.setEmail(hopital.getEmail());
        dto.setTelephone(hopital.getTelephone());
        dto.setAdresseComplete(hopital.getAdresseComplete());
        dto.setLogoUrl(hopital.getLogoUrl());
        dto.setEstActif(hopital.isEstActif());

        Optional.ofNullable(hopital.getIdHopital())
                .flatMap(abonnementRepository::findActiveSubscription)
                .ifPresent(sub -> dto.setPlanNom(SaasPlanRegistry.normalize(sub.getPlanNom())));

        return dto;
    }
}
