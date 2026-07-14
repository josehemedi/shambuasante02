package hospicloud.servicesImpl;

import hospicloud.exceptions.ResourceNotFoundException;
import hospicloud.model.Societe;
import hospicloud.repositories.SocieteRepository;
import hospicloud.security.TenantContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class SocieteServiceImplTest {

    @Mock
    private SocieteRepository societeRepository;

    @InjectMocks
    private SocieteServiceImpl societeService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    // =========================
    // CREATE
    // =========================

    @Test
    void creerSociete_shouldThrow_whenInvalid() {

        assertThrows(IllegalArgumentException.class,
                () -> societeService.creerSociete(null));

        Societe s = new Societe();
        s.setNomSociete(" ");

        assertThrows(IllegalArgumentException.class,
                () -> societeService.creerSociete(s));
    }

    @Test
    void creerSociete_shouldThrow_whenDuplicate() {

        Societe s = new Societe();
        s.setNomSociete("AssurX");

        try (MockedStatic<TenantContext> mocked = Mockito.mockStatic(TenantContext.class)) {

            mocked.when(TenantContext::getRequiredHopitalId).thenReturn(1);

            when(societeRepository.trouverParNom("ASSURX"))
                    .thenReturn(Optional.of(new Societe()));

            assertThrows(IllegalStateException.class,
                    () -> societeService.creerSociete(s));

            verify(societeRepository, never()).enregistrerSociete(any());
        }
    }

    @Test
    void creerSociete_success() {

        Societe s = new Societe();
        s.setNomSociete("AssurY");
        s.setTauxCouverture(null);

        try (MockedStatic<TenantContext> mocked = Mockito.mockStatic(TenantContext.class)) {

            mocked.when(TenantContext::getRequiredHopitalId).thenReturn(2);

            when(societeRepository.trouverParNom("ASSURY"))
                    .thenReturn(Optional.empty());

            when(societeRepository.enregistrerSociete(any(Societe.class)))
                    .thenReturn(1);

            assertDoesNotThrow(() -> societeService.creerSociete(s));

            assertEquals(0.0, s.getTauxCouverture());
            assertEquals("ASSURY", s.getNomSociete());

            verify(societeRepository).enregistrerSociete(s);
        }
    }

    // =========================
    // UPDATE
    // =========================

    @Test
    void mettreAJourSociete_shouldThrow_whenNullOrMissingId() {

        assertThrows(IllegalArgumentException.class,
                () -> societeService.mettreAJourSociete(null));

        Societe s = new Societe();
        assertThrows(IllegalArgumentException.class,
                () -> societeService.mettreAJourSociete(s));
    }

    @Test
    void mettreAJourSociete_shouldThrow_whenNotExists() {

        Societe s = new Societe();
        s.setIdSociete(10L);

        try (MockedStatic<TenantContext> mocked = Mockito.mockStatic(TenantContext.class)) {

            mocked.when(TenantContext::getRequiredHopitalId).thenReturn(5);

            when(societeRepository.existeParId(10L)).thenReturn(false);

            assertThrows(ResourceNotFoundException.class,
                    () -> societeService.mettreAJourSociete(s));
        }
    }

    @Test
    void mettreAJourSociete_success() {

        Societe s = new Societe();
        s.setIdSociete(10L);

        try (MockedStatic<TenantContext> mocked = Mockito.mockStatic(TenantContext.class)) {

            mocked.when(TenantContext::getRequiredHopitalId).thenReturn(5);

            when(societeRepository.existeParId(10L)).thenReturn(true);
            when(societeRepository.modifierSociete(s)).thenReturn(1);

            assertDoesNotThrow(() -> societeService.mettreAJourSociete(s));

            verify(societeRepository).modifierSociete(s);
        }
    }

    // =========================
    // DELETE
    // =========================

    @Test
    void supprimerSociete_shouldThrow_whenNotExists() {

        try (MockedStatic<TenantContext> mocked = Mockito.mockStatic(TenantContext.class)) {

            mocked.when(TenantContext::getRequiredHopitalId).thenReturn(6);

            when(societeRepository.existeParId(11L)).thenReturn(false);

            assertThrows(ResourceNotFoundException.class,
                    () -> societeService.supprimerSociete(11L));
        }
    }

    @Test
    void supprimerSociete_success() {

        try (MockedStatic<TenantContext> mocked = Mockito.mockStatic(TenantContext.class)) {

            mocked.when(TenantContext::getRequiredHopitalId).thenReturn(6);

            when(societeRepository.existeParId(11L)).thenReturn(true);

            assertDoesNotThrow(() -> societeService.supprimerSociete(11L));

            verify(societeRepository).supprimerSociete(11L);
        }
    }

    // =========================
    // READ
    // =========================

    @Test
    void listerParHopital_shouldReturnList() {

        when(societeRepository.listerParHopital())
                .thenReturn(List.of(new Societe(), new Societe()));

        List<Societe> result = societeService.listerParHopital();

        assertEquals(2, result.size());
    }

    @Test
    void recupererParId_shouldReturnOptional() {

        Societe s = new Societe();

        when(societeRepository.trouverParId(1L))
                .thenReturn(Optional.of(s));

        Optional<Societe> result = societeService.recupererParId(1L);

        assertTrue(result.isPresent());
    }

    @Test
    void verifierAppartenance_shouldReturnBoolean() {

        Societe s = new Societe();
        s.setIdHopital(1);
        when(societeRepository.trouverParId(1L)).thenReturn(Optional.of(s));

        try (MockedStatic<TenantContext> mocked = Mockito.mockStatic(TenantContext.class)) {
            mocked.when(TenantContext::getRequiredHopitalId).thenReturn(1);
            assertTrue(societeService.verifierAppartenance(1L));
        }
    }

    // =========================
    // SEARCH
    // =========================

    @Test
    void trouverParNom_shouldReturnEmpty_whenBlank() {

        try (MockedStatic<TenantContext> mocked =
                     Mockito.mockStatic(TenantContext.class)) {

            mocked.when(TenantContext::getRequiredHopitalId).thenReturn(1);

            Optional<Societe> result = societeService.trouverParNom("  ");

            assertTrue(result.isEmpty());
        }
    }

    @Test
    void trouverParNom_shouldReturnResult() {

        Societe s = new Societe();

        when(societeRepository.trouverParNom("ASSURX"))
                .thenReturn(Optional.of(s));

        try (MockedStatic<TenantContext> mocked = Mockito.mockStatic(TenantContext.class)) {

            mocked.when(TenantContext::getRequiredHopitalId).thenReturn(1);

            Optional<Societe> result = societeService.trouverParNom("assurx");

            assertTrue(result.isPresent());
        }
    }

    // =========================
    // ADMIN
    // =========================

    @Test
    void listerTout_shouldReturnList() {

        when(societeRepository.ListerSocietes())
                .thenReturn(List.of(new Societe()));

        List<Societe> result = societeService.listerTout();

        assertEquals(1, result.size());
    }
}