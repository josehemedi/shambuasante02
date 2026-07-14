package hospicloud.servicesImpl;

import hospicloud.dtos.TenantSubscriptionDTO;
import hospicloud.model.Hopital;
import hospicloud.repositories.AbonnementRepository;
import hospicloud.repositories.HopitalRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TenantSubscriptionAccessServiceImplTest {

    @Mock
    private AbonnementRepository abonnementRepository;

    @Mock
    private HopitalRepository hopitalRepository;

    @InjectMocks
    private TenantSubscriptionAccessServiceImpl service;

    @Test
    void shouldRestrictWhenSubscriptionExpired() {
        TenantSubscriptionDTO dto = new TenantSubscriptionDTO();
        dto.setUiStatus("expired");
        when(abonnementRepository.findActiveSubscription(1)).thenReturn(Optional.of(dto));

        assertTrue(service.isPlatformAccessRestricted(1));
    }

    @Test
    void shouldRestrictWhenHospitalInactiveWithoutActiveSubscription() {
        when(abonnementRepository.findActiveSubscription(1)).thenReturn(Optional.empty());
        Hopital hopital = new Hopital();
        hopital.setIdHopital(1);
        hopital.setEstActif(false);
        when(hopitalRepository.rechercherhopitalParId(1L)).thenReturn(hopital);

        assertTrue(service.isPlatformAccessRestricted(1));
    }

    @Test
    void shouldNotRestrictDuringTrial() {
        when(abonnementRepository.findActiveSubscription(1)).thenReturn(Optional.empty());
        Hopital hopital = new Hopital();
        hopital.setIdHopital(1);
        hopital.setEstActif(true);
        when(hopitalRepository.rechercherhopitalParId(1L)).thenReturn(hopital);

        assertFalse(service.isPlatformAccessRestricted(1));
    }

    @Test
    void shouldNotRestrictWhenSubscriptionActive() {
        TenantSubscriptionDTO dto = new TenantSubscriptionDTO();
        dto.setUiStatus("active");
        when(abonnementRepository.findActiveSubscription(1)).thenReturn(Optional.of(dto));

        assertFalse(service.isPlatformAccessRestricted(1));
    }
}
