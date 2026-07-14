package hospicloud.controlleurs;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;
import java.util.Optional;

import com.fasterxml.jackson.databind.ObjectMapper;

import hospicloud.model.Patient;
import hospicloud.services.PatientService;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(PatientController.class)
@AutoConfigureMockMvc(addFilters = false)
class PatientControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PatientService patientService;

    @Autowired
    private ObjectMapper objectMapper;

    // =========================
    // GET BY ID - 404
    // =========================
    @Test
    void get_nonExistingPatient_shouldReturn404() throws Exception {

        when(patientService.trouverPatientParId(9999L))
                .thenReturn(Optional.empty());

        mockMvc.perform(get("/api/patients/9999"))
                .andExpect(status().isNotFound());
    }

    // =========================
    // GET BY ID - 200
    // =========================
    @Test
    void get_existingPatient_shouldReturn200() throws Exception {

        Patient patient = new Patient();
        patient.setIdPatient(1L);

        when(patientService.trouverPatientParId(1L))
                .thenReturn(Optional.of(patient));

        mockMvc.perform(get("/api/patients/1"))
                .andExpect(status().isOk());
    }

    // =========================
    // GET BY CODE - 404
    // =========================
    @Test
    void getByCode_nonExisting_shouldReturn404() throws Exception {

        when(patientService.trouverPatientParNumero("P999"))
                .thenReturn(Optional.empty());

        mockMvc.perform(get("/api/patients/by-code/P999"))
                .andExpect(status().isNotFound());
    }

    // =========================
    // GET BY CODE - 200
    // =========================
    @Test
    void getByCode_existing_shouldReturn200() throws Exception {

        Patient patient = new Patient();
        patient.setIdPatient(1L);

        when(patientService.trouverPatientParNumero("P001"))
                .thenReturn(Optional.of(patient));

        mockMvc.perform(get("/api/patients/by-code/P001"))
                .andExpect(status().isOk());
    }

    // =========================
    // GET ALL
    // =========================
    @Test
    void getAllPatients_shouldReturn200() throws Exception {

        when(patientService.trouverTousLesPatients())
                .thenReturn(List.of());

        mockMvc.perform(get("/api/patients"))
                .andExpect(status().isOk());
    }

    // =========================
    // SEARCH
    // =========================
    @Test
    void searchPatients_shouldReturn200() throws Exception {

        when(patientService.rechercherParNomEtPrenom("John", "Doe"))
                .thenReturn(List.of());

        mockMvc.perform(
                get("/api/patients/search")
                        .param("nom", "John")
                        .param("prenom", "Doe"))
                .andExpect(status().isOk());
    }

    // =========================
    // CREATE
    // =========================
    @Test
    void createPatient_shouldReturn201() throws Exception {

        Patient patient = new Patient();
        patient.setIdPatient(1L);

        doNothing().when(patientService).enregisterPatient(any(Patient.class));

        mockMvc.perform(
                post("/api/patients")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(patient)))
                .andExpect(status().isCreated());
    }

    // =========================
    // UPDATE
    // =========================
    @Test
    void updatePatient_shouldReturn204() throws Exception {

        doNothing().when(patientService).modifierPatient(any(Patient.class));

        mockMvc.perform(
                put("/api/patients/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new Patient())))
                .andExpect(status().isNoContent());
    }

    // =========================
    // DELETE
    // =========================
    @Test
    void deletePatient_shouldReturn204() throws Exception {

        doNothing().when(patientService).supprimerPatient(1L);

        mockMvc.perform(delete("/api/patients/1"))
                .andExpect(status().isNoContent());
    }
}