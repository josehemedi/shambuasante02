package hospicloud.controlleurs;

import com.fasterxml.jackson.databind.ObjectMapper;
import hospicloud.model.RendezVous;
import hospicloud.services.RendezVousService;
import hospicloud.security.CurrentUserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = RendezVousController.class)
@AutoConfigureMockMvc(addFilters = false)
class RendezVousControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private RendezVousService rendezVousService;

    @MockBean
    private CurrentUserService currentUserService;

    @Autowired
    private ObjectMapper objectMapper;

    // =========================
    // CREATE - SUCCESS (201)
    // =========================
    @Test
    void creerRendezVous_shouldReturn201() throws Exception {

        RendezVous input = new RendezVous();
        input.setIdRdv(1);
        input.setIdPatient(10);
        input.setIdMedecin(20);
        input.setDateHeureRdv(LocalDateTime.now().plusDays(1));
        input.setMotifVisite("Consultation générale");
        input.setCreePar(1);

        RendezVous saved = new RendezVous();
        saved.setIdRdv(1);

        when(rendezVousService.creerEtPublier(any(RendezVous.class)))
                .thenReturn(saved);

        mockMvc.perform(post("/api/rendezvous")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(input)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.idRdv").value(1));
    }
}