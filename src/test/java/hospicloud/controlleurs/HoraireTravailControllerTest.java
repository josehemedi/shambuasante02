package hospicloud.controlleurs;

import com.fasterxml.jackson.databind.ObjectMapper;
import hospicloud.model.HoraireTravail;
import hospicloud.services.HoraireTravailService;
import hospicloud.security.CurrentUserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(HoraireTravailController.class)
@AutoConfigureMockMvc(addFilters = false)
class HoraireTravailControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private HoraireTravailService horaireService;

    @MockBean
    private CurrentUserService currentUserService;

    @Autowired
    private ObjectMapper objectMapper;

    // =========================
    // CREATE - 201
    // =========================
    @Test
    void creerHoraire_shouldReturn201() throws Exception {

        HoraireTravail input = new HoraireTravail();
        input.setMedecinId(1);
        input.setJourSemaine("Lundi");
        input.setHeureDebut(LocalTime.of(8, 0));
        input.setHeureFin(LocalTime.of(12, 0));

        HoraireTravail saved = new HoraireTravail();
        saved.setId(1L);
        saved.setMedecinId(1);

        when(horaireService.creerHoraire(any(HoraireTravail.class)))
                .thenReturn(saved);

        mockMvc.perform(post("/api/v1/horaires-travail")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(input)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1));
    }

    // =========================
    // GET BY ID - 200
    // =========================
    @Test
    void obtenirParId_shouldReturn200() throws Exception {

        HoraireTravail h = new HoraireTravail();
        h.setId(1L);

        when(horaireService.obtenirParId(1L))
                .thenReturn(Optional.of(h));

        mockMvc.perform(get("/api/v1/horaires-travail/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    // =========================
    // GET BY ID - 404
    // =========================
    @Test
    void obtenirParId_shouldReturn404() throws Exception {

        when(horaireService.obtenirParId(999L))
                .thenReturn(Optional.empty());

        mockMvc.perform(get("/api/v1/horaires-travail/999"))
                .andExpect(status().isNotFound());
    }

    // =========================
    // GET BY MEDECIN - 200
    // =========================
    @Test
    void obtenirParMedecin_shouldReturn200() throws Exception {

        when(currentUserService.getCurrentMedecinId())
                .thenReturn(1);
        when(horaireService.obtenirParMedecin(1))
                .thenReturn(List.of(new HoraireTravail()));

        mockMvc.perform(get("/api/v1/horaires-travail/medecin"))
                .andExpect(status().isOk());
    }

    // =========================
    // RECHERCHE - SANS HOPITAL
    // =========================
    @Test
    void rechercherSansHopital_shouldReturn200() throws Exception {

        when(currentUserService.getCurrentMedecinId())
                .thenReturn(1);
        when(horaireService.obtenirParMedecinEtJour(1, "Lundi"))
                .thenReturn(List.of());

        mockMvc.perform(get("/api/v1/horaires-travail/recherche")
                        .param("jourSemaine", "Lundi"))
                .andExpect(status().isOk());
    }

    // =========================
    // RECHERCHE - AVEC HOPITAL
    // =========================
    @Test
    void rechercherAvecHopital_shouldReturn200() throws Exception {

        when(currentUserService.getCurrentMedecinId())
                .thenReturn(1);
        when(horaireService.obtenirParMedecinJourEtHopital(1, "Lundi"))
                .thenReturn(List.of());

        mockMvc.perform(get("/api/v1/horaires-travail/recherche")
                        .param("jourSemaine", "Lundi"))
                .andExpect(status().isOk());
    }

    // =========================
    // DELETE - SUCCESS
    // =========================
    @Test
    void supprimerHoraire_success_shouldReturn200() throws Exception {

        when(horaireService.supprimerHoraire(1L))
                .thenReturn(true);

        mockMvc.perform(delete("/api/v1/horaires-travail/1"))
                .andExpect(status().isOk());
    }

    // =========================
    // DELETE - NOT FOUND
    // =========================
    @Test
    void supprimerHoraire_notFound_shouldReturn404() throws Exception {

        when(horaireService.supprimerHoraire(999L))
                .thenReturn(false);

        mockMvc.perform(delete("/api/v1/horaires-travail/999"))
                .andExpect(status().isNotFound());
    }
}