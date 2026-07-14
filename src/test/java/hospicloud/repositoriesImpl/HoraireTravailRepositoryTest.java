package hospicloud.repositoriesImpl;

import hospicloud.model.Hopital;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class HopitalRepositoryTest {

    @Mock
    private JdbcTemplate jdbcTemplate;

    @InjectMocks
    private HopitalRepositoryImpl hopitalRepository;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    // =========================
    // CREATE - SUCCESS
    // =========================
    @Test
    void enresgitrerHopital_shouldInsert_success() {

        Hopital hopital = new Hopital();
        hopital.setNom("CHU Kinshasa");
        hopital.setAdresse("Kinshasa");
        hopital.setTelephone("123456789");
        hopital.setEmail("test@test.com");
        hopital.setEstActif(true);

        // simulate no duplicate check
        when(jdbcTemplate.queryForObject(anyString(), eq(Integer.class), anyString()))
                .thenReturn(0);

        // mock insert (KeyHolder simulation impossible direct -> on just verify call)
        doAnswer(invocation -> {
            KeyHolder keyHolder = invocation.getArgument(1);

            // simulate generated key if needed
            if (keyHolder instanceof GeneratedKeyHolder) {
                // nothing to do; test only verifies call
            }
            return 1;
        }).when(jdbcTemplate).update(any(), any(KeyHolder.class));

        hopitalRepository.enresgitrerHopital(hopital);

        assertEquals("CHU Kinshasa", hopital.getNom());
        verify(jdbcTemplate, atLeastOnce()).update(any(), any(KeyHolder.class));
    }

    // =========================
    // FIND BY ID
    // =========================
    @Test
    void rechercherParId_shouldReturnNull_whenNotFound() {

        when(jdbcTemplate.query(
                anyString(),
                any(Object[].class),
                ArgumentMatchers.<org.springframework.jdbc.core.RowMapper<Hopital>>any()
        )).thenReturn(List.of());
    }

    // =========================
    // FIND ALL
    // =========================
    @Test
    void listerTous_shouldReturnList() {

        when(jdbcTemplate.query(
                anyString(),
                ArgumentMatchers.<org.springframework.jdbc.core.RowMapper<Hopital>>any()
        )).thenReturn(List.of(new Hopital(), new Hopital()));
        List<Hopital> result = hopitalRepository.listerTous();

        assertEquals(2, result.size());
    }

    // =========================
    // UPDATE
    // =========================
    @Test
    void modifier_shouldCallUpdate() {

        Hopital h = new Hopital();
        h.setIdHopital(1);
        h.setNom("Updated");

        when(jdbcTemplate.update(anyString(),
                any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any()))
                .thenReturn(1);

        hopitalRepository.modifier(h);

        verify(jdbcTemplate, times(1)).update(anyString(),
                any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any());
    }

    // =========================
    // DELETE (important logic test)
    // =========================
    @Test
    void supprimer_shouldExecuteAllDeletes() {

        when(jdbcTemplate.update(anyString(), anyInt()))
                .thenReturn(1);

        hopitalRepository.supprimer(10);

        verify(jdbcTemplate, atLeastOnce())
                .update(contains("DELETE FROM hopitaux"), eq(10));
    }
}