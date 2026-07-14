package hospicloud.servicesImpl;

import hospicloud.model.HoraireTravail;
import hospicloud.repositories.HoraireTravailRepository;
import hospicloud.security.TenantContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class HoraireTravailServiceTest {

    @Mock
    private HoraireTravailRepository horaireRepository;

    @InjectMocks
    private HoraireTravailServiceImpl service;

    private HoraireTravail horaire;

    @BeforeEach
    void setUp() {
        TenantContext.setHopitalId(1);
        horaire = new HoraireTravail();
        horaire.setId(1L);
        horaire.setHopitalId(1);
        horaire.setMedecinId(10);
        horaire.setJourSemaine("Lundi");
        horaire.setHeureDebut(LocalTime.of(8, 0));
        horaire.setHeureFin(LocalTime.of(12, 0));
    }

    // =========================
    // TEST CREATION OK
    // =========================
    @Test
    void testCreerHoraire_Success() {
        when(horaireRepository.enregistrer(horaire)).thenReturn(horaire);

        HoraireTravail result = service.creerHoraire(horaire);

        assertNotNull(result);
        assertEquals(1, result.getHopitalId());
        verify(horaireRepository, times(1)).enregistrer(horaire);
    }

    // =========================
    // TEST VALIDATION NULL
    // =========================
    @Test
    void testCreerHoraire_Null() {
        Exception ex = assertThrows(IllegalArgumentException.class,
                () -> service.creerHoraire(null));

        assertEquals("Horaire obligatoire", ex.getMessage());
    }

    // =========================
    // TEST HEURES INVALIDES
    // =========================
    @Test
    void testCreerHoraire_HeuresInvalides() {
        horaire.setHeureDebut(LocalTime.of(15, 0));
        horaire.setHeureFin(LocalTime.of(10, 0));

        Exception ex = assertThrows(IllegalArgumentException.class,
                () -> service.creerHoraire(horaire));

        assertTrue(ex.getMessage().contains("Heure début"));
    }

    // =========================
    // TEST MODIFICATION OK
    // =========================
    @Test
    void testModifierHoraire_Success() {
        when(horaireRepository.trouverParId(1L)).thenReturn(Optional.of(horaire));
        when(horaireRepository.modifier(horaire)).thenReturn(1);

        HoraireTravail result = service.modifierHoraire(horaire);

        assertNotNull(result);
        verify(horaireRepository, times(1)).modifier(horaire);
    }

    // =========================
    // TEST MODIFICATION NON TROUVÉ
    // =========================
    @Test
    void testModifierHoraire_NotFound() {
        when(horaireRepository.trouverParId(1L)).thenReturn(Optional.empty());

        Exception ex = assertThrows(IllegalArgumentException.class,
                () -> service.modifierHoraire(horaire));

        assertTrue(ex.getMessage().contains("Introuvable"));
    }

    // =========================
    // TEST SUPPRESSION
    // =========================
    @Test
    void testSupprimerHoraire_Success() {
        when(horaireRepository.supprimerParId(1L)).thenReturn(1);

        boolean result = service.supprimerHoraire(1L);

        assertTrue(result);
        verify(horaireRepository).supprimerParId(1L);
    }

    // =========================
    // TEST SUPPRESSION NULL
    // =========================
    @Test
    void testSupprimerHoraire_Null() {
        boolean result = service.supprimerHoraire(null);

        assertFalse(result);
    }
}