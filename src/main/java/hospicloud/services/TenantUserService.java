package hospicloud.services;

import hospicloud.dtos.CreateTenantUserRequest;
import hospicloud.dtos.TenantUserResponse;
import hospicloud.dtos.UpdateTenantUserRequest;

import java.util.List;

public interface TenantUserService {

    TenantUserResponse createUser(CreateTenantUserRequest request);

    List<TenantUserResponse> listUsers();

    TenantUserResponse getUser(Integer id);

    TenantUserResponse updateUser(Integer id, UpdateTenantUserRequest request);

    TenantUserResponse disableUser(Integer id);

    TenantUserResponse enableUser(Integer id);
}
