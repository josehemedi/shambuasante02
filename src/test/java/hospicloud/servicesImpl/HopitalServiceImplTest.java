package hospicloud.servicesImpl;

import hospicloud.exceptions.ResourceNotFoundException;
import hospicloud.model.Hopital;
import hospicloud.repositories.HopitalRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class HopitalServiceImplTest {

    @Mock
    private HopitalRepository hopitalRepository;

    @InjectMocks
    private HopitalServiceImpl hopitalService;

    @Captor
    private ArgumentCaptor<Hopital> hopitalCaptor;

    private Hopital sample;

    @BeforeEach
    void setUp() {
        sample = new Hopital();
        sample.setNom("Hôpital Test");
        sample.setAdresse("Rue 1");
        sample.setEmail("contact@test.com");
        sample.setTelephone("+243 812 345 678");
        sample.setVille("Kinshasa");
        sample.setPays("RDC");
        sample.setType("PRIVE");
        sample.setEstActif(true);
    }

    // =========================
    // CREATE SUCCESS
    // =========================
    @Test
    void enresgitrerHopital_success_callsRepository() {

        when(hopitalRepository.rechercherParNom("Hôpital Test")).thenReturn(null);

        hopitalService.enresgitrerHopital(sample);

        verify(hopitalRepository).enresgitrerHopital(hopitalCaptor.capture());

        Hopital saved = hopitalCaptor.getValue();

        assertEquals("Hôpital Test", saved.getNom());
        assertNotNull(saved.getDateCreation());
    }

    // =========================
    // DUPLICATE NAME
    // =========================
    @Test
    void enresgitrerHopital_duplicateName_throws() {

        Hopital existing = new Hopital();
        existing.setIdHopital(1);
        existing.setNom("Hôpital Test");

        when(hopitalRepository.rechercherParNom("Hôpital Test"))
                .thenReturn(existing);

        assertThrows(IllegalStateException.class,
                () -> hopitalService.enresgitrerHopital(sample));

        verify(hopitalRepository, never()).enresgitrerHopital(any());
    }

    // =========================
    // INVALID EMAIL
    // =========================
    @Test
    void enresgitrerHopital_invalidEmail_throws() {

        sample.setEmail("bad-email");

        assertThrows(IllegalArgumentException.class,
                () -> hopitalService.enresgitrerHopital(sample));

        verify(hopitalRepository, never()).enresgitrerHopital(any());
    }

    // =========================
    // MODIFY - NOT FOUND
    // =========================
    @Test
    void modifier_notFound_throws() {

        sample.setIdHopital(10);

        when(hopitalRepository.rechercherhopitalParId(10L))
                .thenReturn(null);

        assertThrows(ResourceNotFoundException.class,
                () -> hopitalService.modifier(sample));
    }

    // =========================
    // MODIFY - NAME CONFLICT
    // =========================
    @Test
    void modifier_conflictName_throws() {

        Hopital existing = new Hopital();
        existing.setIdHopital(2);
        existing.setNom("Old Name");

        Hopital other = new Hopital();
        other.setIdHopital(99);
        other.setNom("New Name");

        when(hopitalRepository.rechercherhopitalParId(2L)).thenReturn(existing);
        when(hopitalRepository.rechercherParNom("New Name")).thenReturn(other);

        Hopital toUpdate = new Hopital();
        toUpdate.setIdHopital(2);
        toUpdate.setNom("New Name");

        assertThrows(IllegalStateException.class,
                () -> hopitalService.modifier(toUpdate));

        verify(hopitalRepository, never()).modifier(any());
    }

    // =========================
    // MODIFY SUCCESS
    // =========================
    @Test
    void modifier_success_callsRepository() {

        Hopital existing = new Hopital();
        existing.setIdHopital(2);
        existing.setNom("Old Name");

        when(hopitalRepository.rechercherhopitalParId(2L)).thenReturn(existing);
        when(hopitalRepository.rechercherParNom("Old Name")).thenReturn(existing);

        Hopital toUpdate = new Hopital();
        toUpdate.setIdHopital(2);
        toUpdate.setNom("Old Name");
        toUpdate.setEmail("admin@hopital.test");

        hopitalService.modifier(toUpdate);

        verify(hopitalRepository).modifier(hopitalCaptor.capture());

        Hopital saved = hopitalCaptor.getValue();

        assertEquals(2, saved.getIdHopital());
        assertNotNull(saved.getDateModification());
    }

    // =========================
    // DELETE NOT FOUND
    // =========================
    @Test
    void supprimer_notFound_throws() {

        when(hopitalRepository.rechercherhopitalParId(5L)).thenReturn(null);

        assertThrows(ResourceNotFoundException.class,
                () -> hopitalService.supprimer(5));

        verify(hopitalRepository, never()).supprimer(any());
    }
}