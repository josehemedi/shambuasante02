package hospicloud.controlleurs;

import com.fasterxml.jackson.databind.ObjectMapper;
import hospicloud.dtos.SocieteDTO;
import hospicloud.model.Societe;
import hospicloud.services.SocieteService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = SocieteController.class)
@AutoConfigureMockMvc(addFilters = false)
class SocieteControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private SocieteService societeService;

    @Autowired
    private ObjectMapper objectMapper;

    // =========================
    // CREATE - SUCCESS (201)
    // =========================
    @Test
    void creerSociete_shouldReturn201() throws Exception {

        SocieteDTO dto = new SocieteDTO();
        dto.setNomSociete("TestCo");
        dto.setTauxCouverture(10.0);

        Mockito.doNothing().when(societeService).creerSociete(any(Societe.class));

        mockMvc.perform(post("/api/societes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated());
    }

    // =========================
    // UPDATE - SUCCESS (200)
    // =========================
    @Test
    void modifierSociete_shouldReturn200() throws Exception {

        SocieteDTO dto = new SocieteDTO();
        dto.setNomSociete("UpdateCo");

        Mockito.doNothing().when(societeService).mettreAJourSociete(any(Societe.class));

        mockMvc.perform(put("/api/societes/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk());
    }

    // =========================
    // DELETE - SUCCESS (204)
    // =========================
    @Test
    void supprimerSociete_shouldReturn204() throws Exception {

        Mockito.doNothing().when(societeService).supprimerSociete(1L);

        mockMvc.perform(delete("/api/societes/1"))
                .andExpect(status().isNoContent());
    }

    // =========================
    // GET BY ID - 404
    // =========================
    @Test
    void trouverParId_notFound_shouldReturn404() throws Exception {

        Mockito.when(societeService.recupererParId(99L))
                .thenReturn(Optional.empty());

        mockMvc.perform(get("/api/societes/99"))
                .andExpect(status().isNotFound());
    }

    // =========================
    // GET BY ID - 200
    // =========================
    @Test
    void trouverParId_shouldReturn200() throws Exception {

        Societe s = new Societe();
        s.setIdSociete(1L);
        s.setNomSociete("TestCo");

        Mockito.when(societeService.recupererParId(1L))
                .thenReturn(Optional.of(s));

        mockMvc.perform(get("/api/societes/1"))
                .andExpect(status().isOk());
    }

    // =========================
    // LIST BY HOSPITAL
    // =========================
    @Test
    void listerParHopital_shouldReturn200() throws Exception {

        Mockito.when(societeService.listerParHopital())
                .thenReturn(List.of());

        mockMvc.perform(get("/api/societes"))
                .andExpect(status().isOk());
    }

    // =========================
    // SEARCH - NOT FOUND
    // =========================
    @Test
    void rechercher_notFound_shouldReturn404() throws Exception {

        Mockito.when(societeService.trouverParNom("Nope"))
                .thenReturn(Optional.empty());

        mockMvc.perform(get("/api/societes/search")
                        .param("nom", "Nope"))
                .andExpect(status().isNotFound());
    }

    // =========================
    // SEARCH - FOUND
    // =========================
    @Test
    void rechercher_found_shouldReturn200() throws Exception {

        Societe s = new Societe();
        s.setIdSociete(1L);
        s.setNomSociete("TestCo");

        Mockito.when(societeService.trouverParNom("TestCo"))
                .thenReturn(Optional.of(s));

        mockMvc.perform(get("/api/societes/search")
                        .param("nom", "TestCo"))
                .andExpect(status().isOk());
    }
}