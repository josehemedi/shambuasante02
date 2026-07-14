package hospicloud.controlleurs;

import hospicloud.dtos.CreateTenantUserRequest;
import hospicloud.dtos.TenantUserResponse;
import hospicloud.dtos.UpdateTenantUserRequest;
import hospicloud.services.TenantUserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tenant-admin/users")
public class TenantAdminUserController {

    private final TenantUserService tenantUserService;

    public TenantAdminUserController(TenantUserService tenantUserService) {
        this.tenantUserService = tenantUserService;
    }

    @PostMapping
    public ResponseEntity<TenantUserResponse> createUser(@Valid @RequestBody CreateTenantUserRequest request) {
        TenantUserResponse created = tenantUserService.createUser(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping
    public ResponseEntity<List<TenantUserResponse>> listUsers() {
        return ResponseEntity.ok(tenantUserService.listUsers());
    }

    @GetMapping("/{id}")
    public ResponseEntity<TenantUserResponse> getUser(@PathVariable Integer id) {
        return ResponseEntity.ok(tenantUserService.getUser(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<TenantUserResponse> updateUser(@PathVariable Integer id,
                                                         @Valid @RequestBody UpdateTenantUserRequest request) {
        return ResponseEntity.ok(tenantUserService.updateUser(id, request));
    }

    @PatchMapping("/{id}/disable")
    public ResponseEntity<TenantUserResponse> disableUser(@PathVariable Integer id) {
        return ResponseEntity.ok(tenantUserService.disableUser(id));
    }

    @PatchMapping("/{id}/enable")
    public ResponseEntity<TenantUserResponse> enableUser(@PathVariable Integer id) {
        return ResponseEntity.ok(tenantUserService.enableUser(id));
    }
}
