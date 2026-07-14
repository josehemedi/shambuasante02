package hospicloud.security;

public final class TenantContext {

    private static final ThreadLocal<Integer> CURRENT_TENANT =
            new ThreadLocal<>();

    private TenantContext() {}

    public static void setHopitalId(Integer hopitalId) {
        CURRENT_TENANT.set(hopitalId);
    }

    public static Integer getHopitalId() {
        return CURRENT_TENANT.get();
    }

    public static Integer getRequiredHopitalId() {
        Integer id = CURRENT_TENANT.get();
        if (id == null) {
            throw new IllegalStateException("Tenant context not initialized. No hospital ID found in request context.");
        }
        return id;
    }

    public static void clear() {
        CURRENT_TENANT.remove();
    }
}