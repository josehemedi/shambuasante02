package hospicloud.controlleurs;

import com.fasterxml.jackson.databind.ObjectMapper;
import hospicloud.model.Antecedent;
import hospicloud.services.AntecedentService;


import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = AntecedentController.class)
@Disabled
public class AntecedentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AntecedentService antecedentService;

    //@MockBean
    //private AntecedentMapper mapper;

    @Autowired
    private ObjectMapper objectMapper;

    // =========================
    // POST INVALID
    // =========================
    @Test
    public void post_invalidBody_shouldReturn400() throws Exception {

        String json = "{}";

     doNothing().when(antecedentService).ajouterAntecedent(any(Antecedent.class));
        mockMvc.perform(post("/api/antecedents")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest());
    }

    // =========================
    // GET NOT FOUND
    // =========================
    @Test
    public void get_nonExisting_shouldReturn404() throws Exception {

        when(antecedentService.trouverParId(9999))
                .thenReturn(Optional.empty());

        mockMvc.perform(get("/api/antecedents/9999"))
                .andExpect(status().isNotFound());
    }
}