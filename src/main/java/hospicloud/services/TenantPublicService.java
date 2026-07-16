package hospicloud.services;

import hospicloud.dtos.TenantPublicDTO;

public interface TenantPublicService {
    TenantPublicDTO getBySubdomain(String subdomain);

    /** Établissement du compte connecté (id_hopital JWT / TenantContext). */
    TenantPublicDTO getByHopitalId(Integer hopitalId);
}
