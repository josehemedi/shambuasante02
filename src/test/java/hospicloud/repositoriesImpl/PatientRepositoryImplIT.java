package hospicloud.repositoriesImpl;

import com.fasterxml.jackson.databind.ObjectMapper;
import hospicloud.model.Patient;
import hospicloud.security.TenantContext;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@JdbcTest
@Import({
        PatientRepositoryImpl.class,
        ObjectMapper.class
})
public class PatientRepositoryImplIT {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private PatientRepositoryImpl repository;

    @BeforeEach
    void setup() {

        jdbcTemplate.execute("DROP TABLE IF EXISTS patients");
        jdbcTemplate.execute("DROP TABLE IF EXISTS sequences");

        jdbcTemplate.execute("""
            CREATE TABLE sequences(
                seq_name VARCHAR(100) PRIMARY KEY,
                next_val BIGINT
            )
        """);

        jdbcTemplate.execute("""
            CREATE TABLE patients(
                id_patient INT AUTO_INCREMENT PRIMARY KEY,
                id_hopital INT,
                code_patient VARCHAR(100) UNIQUE,
                nom VARCHAR(100),
                prenom VARCHAR(100),
                sexe VARCHAR(20),
                date_naissance DATE,
                groupe_sanguin VARCHAR(10),
                adresse VARCHAR(255),
                telephone VARCHAR(50),
                email VARCHAR(100),
                profession VARCHAR(100),
                est_actif BOOLEAN,
                date_enregistrement TIMESTAMP
            )
        """);
    }

    // =========================
    // CREATE + FIND BY ID
    // =========================
    @Test
    void enregistrerEtTrouverParId() {

        try (MockedStatic<TenantContext> mocked =
                     Mockito.mockStatic(TenantContext.class)) {

            mocked.when(TenantContext::getRequiredHopitalId).thenReturn(1);

            Patient patient = new Patient();
            patient.setNom("EMEDI");
            patient.setPrenom("SIKU");
            patient.setSexe("M");
            patient.setDateNaissance(LocalDate.of(2000, 1, 1));
            patient.setEstActif(true);

            repository.enregistrerPatient(patient);

            assertNotNull(patient.getIdPatient());

            Optional<Patient> found =
                    repository.trouverPatientParId(patient.getIdPatient());

            assertTrue(found.isPresent());
            assertEquals("EMEDI", found.get().getNom());
        }
    }    // =========================
    // FIND BY CODE
    // =========================
    @Test
    void trouverPatientParNumero() {

        try (MockedStatic<TenantContext> mocked =
                     Mockito.mockStatic(TenantContext.class)) {

            mocked.when(TenantContext::getRequiredHopitalId).thenReturn(1);

            Patient patient = new Patient();
            patient.setNom("John");
            patient.setPrenom("Doe");
            patient.setCodePatient("PAT-001");
            patient.setEstActif(true);

            repository.enregistrerPatient(patient);

            Optional<Patient> found =
                    repository.trouverPatientParNumero("PAT-001");

            assertTrue(found.isPresent());
            assertEquals("John", found.get().getNom());
        }
    }
    // =========================
    // UPDATE
    // =========================
    @Test
    void modifierPatient() {

        try (MockedStatic<TenantContext> mocked =
                     Mockito.mockStatic(TenantContext.class)) {

            mocked.when(TenantContext::getRequiredHopitalId).thenReturn(1);

            Patient patient = new Patient();
            patient.setNom("Ancien");
            patient.setCodePatient("PAT-002");
            patient.setEstActif(true);

            repository.enregistrerPatient(patient);

            patient.setNom("Nouveau");

            repository.modifierPatient(patient);

            Optional<Patient> found =
                    repository.trouverPatientParId(patient.getIdPatient());

            assertEquals("Nouveau", found.get().getNom());
        }
    }

    // =========================
    // DELETE
    // =========================
    @Test
    void supprimerPatient() {

        try (MockedStatic<TenantContext> mocked =
                     Mockito.mockStatic(TenantContext.class)) {

            mocked.when(TenantContext::getRequiredHopitalId).thenReturn(1);

            Patient patient = new Patient();
            patient.setNom("Delete");
            patient.setCodePatient("PAT-003");
            patient.setEstActif(true);

            repository.enregistrerPatient(patient);

            Long id = patient.getIdPatient();

            repository.supprimerPatient(id);

            Optional<Patient> found =
                    repository.trouverPatientParId(id);

            assertTrue(found.isEmpty());
        }
    }

    // =========================
    // FIND ALL
    // =========================
    @Test
    void trouverTousLesPatients() {

        try (MockedStatic<TenantContext> mocked =
                     Mockito.mockStatic(TenantContext.class)) {

            mocked.when(TenantContext::getRequiredHopitalId).thenReturn(1);

            Patient p1 = new Patient();
            p1.setCodePatient("PAT-004");
            p1.setNom("A");

            Patient p2 = new Patient();
            p2.setCodePatient("PAT-005");
            p2.setNom("B");

            repository.enregistrerPatient(p1);
            repository.enregistrerPatient(p2);

            assertEquals(2, repository.trouverTousLesPatients().size());
        }
    }

    // =========================
    // SEARCH NAME + PRENAME
    // =========================
    @Test
    void rechercherParNomEtPrenom() {

        try (MockedStatic<TenantContext> mocked =
                     Mockito.mockStatic(TenantContext.class)) {

            mocked.when(TenantContext::getRequiredHopitalId).thenReturn(1);

            Patient patient = new Patient();
            patient.setCodePatient("PAT-006");
            patient.setNom("SIKU");
            patient.setPrenom("EMEDI");

            repository.enregistrerPatient(patient);

            assertEquals(
                    1,
                    repository.rechercherParNomEtPrenom("SIKU", "EMEDI").size()
            );
        }
    }

    // =========================
    // FIND BY HOSPITAL
    // =========================
    @Test
    void trouverPatientParHopital() {

        try (MockedStatic<TenantContext> mocked =
                     Mockito.mockStatic(TenantContext.class)) {

            mocked.when(TenantContext::getRequiredHopitalId).thenReturn(10);

            Patient patient = new Patient();
            patient.setCodePatient("PAT-007");
            patient.setNom("Hopital");

            repository.enregistrerPatient(patient);

            assertEquals(
                    1,
                    repository.trouverTousLesPatients().size()
            );
        }
    }

    // =========================
    // AUTO CODE GENERATION
    // =========================
    @Test
    void generatePatientCodeAutomatically() {

        try (MockedStatic<TenantContext> mocked =
                     Mockito.mockStatic(TenantContext.class)) {

            mocked.when(TenantContext::getRequiredHopitalId).thenReturn(5);

            Patient patient = new Patient();
            patient.setNom("AutoCode");

            repository.enregistrerPatient(patient);

            assertNotNull(patient.getCodePatient());
            assertTrue(patient.getCodePatient().startsWith("PAT-"));
        }
    }
}