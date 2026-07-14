package hospicloud.services;

import hospicloud.dtos.TenantPublicDTO;

public interface TenantPublicService {
    TenantPublicDTO getBySubdomain(String subdomain);
}
