package hospicloud.controlleurs;

import com.fasterxml.jackson.databind.ObjectMapper;
import hospicloud.dtos.HopitalDto;
import hospicloud.dtos.mappers.HopitalMapper;
import hospicloud.exceptions.ResourceNotFoundException;
import hospicloud.model.Hopital;
import hospicloud.services.HospitalService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(HopitalController.class)
@AutoConfigureMockMvc(addFilters = false)
public class HopitalControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private HospitalService hospitalService;

    @MockBean
    private HopitalMapper hopitalMapper;

    @Autowired
    private ObjectMapper objectMapper;

    // =========================
    // POST INVALID BODY
    // =========================
    @Test
    void post_invalidBody_shouldReturn400() throws Exception {

        mockMvc.perform(post("/api/hopitaux")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());
    }

    // =========================
    // GET NOT FOUND
    // =========================
    @Test
    void get_nonExisting_shouldReturn404() throws Exception {

        when(hospitalService.rechercherhopitalParId(9999L))
                .thenReturn(null);

        mockMvc.perform(get("/api/hopitaux/9999"))
                .andExpect(status().isNotFound());
    }

    // =========================
    // DELETE NOT FOUND
    // =========================
    @Test
    void delete_nonExisting_shouldReturn404() throws Exception {

        doThrow(new ResourceNotFoundException("Hopital introuvable"))
                .when(hospitalService)
                .supprimer(9999);

        mockMvc.perform(delete("/api/hopitaux/9999"))
                .andExpect(status().isNotFound());
    }

    // =========================
    // PUT NOT FOUND
    // =========================
    @Test
    void put_nonExisting_shouldReturn404() throws Exception {

        HopitalDto dto = new HopitalDto();
        Hopital entity = new Hopital();

        when(hopitalMapper.toEntity(any())).thenReturn(entity);

        doThrow(new ResourceNotFoundException("Hopital introuvable"))
                .when(hospitalService)
                .modifier(any(Hopital.class));

        mockMvc.perform(put("/api/hopitaux/9999")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isNotFound());
    }

    // =========================
    // SEARCH BY NAME NOT FOUND
    // =========================
    @Test
    void rechercherParNom_inexistant_shouldReturn404() throws Exception {

        when(hospitalService.rechercherParNom("ABC"))
                .thenReturn(null);

        mockMvc.perform(get("/api/hopitaux/chercher")
                        .param("nom", "ABC"))
                .andExpect(status().isNotFound());
    }

    // =========================
    // SEARCH EMPTY PARAM
    // =========================
    @Test
    void rechercherParNom_vide_shouldReturn400() throws Exception {

        mockMvc.perform(get("/api/hopitaux/chercher")
                        .param("nom", " "))
                .andExpect(status().isBadRequest());
    }
}