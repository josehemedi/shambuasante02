package hospicloud.repositoriesImpl;

import hospicloud.model.Societe;
import hospicloud.repositoriesImpl.SocieteRepositoryImpl;
import hospicloud.security.TenantContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(SpringExtension.class)
@JdbcTest
@Import({SocieteRepositoryImpl.class, com.fasterxml.jackson.databind.ObjectMapper.class})
public class SocieteRepositoryImplIT {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private SocieteRepositoryImpl repo;

    @BeforeEach
    void initDb() {

        jdbcTemplate.execute("DROP TABLE IF EXISTS societes");
        jdbcTemplate.execute("""
            CREATE TABLE societes (
                id_societe BIGINT AUTO_INCREMENT PRIMARY KEY,
                hospital_id INT,
                nom_societe VARCHAR(200),
                adresse_facturation VARCHAR(255),
                telephone_contact VARCHAR(50),
                email_contact VARCHAR(100),
                taux_couverture DOUBLE
            )
        """);

        jdbcTemplate.execute("DROP TABLE IF EXISTS hopitaux");
        jdbcTemplate.execute("""
            CREATE TABLE hopitaux (
                id_hopital INT AUTO_INCREMENT PRIMARY KEY,
                nom VARCHAR(255)
            )
        """);
    }

    @Test
    void testEnregistrerEtTrouver() {

        try (MockedStatic<TenantContext> mocked =
                     Mockito.mockStatic(TenantContext.class)) {

            // ✅ Simule le multi-tenant obligatoire
            mocked.when(TenantContext::getRequiredHopitalId).thenReturn(7);

            Societe s = new Societe();
            s.setNomSociete("ITCo");
            s.setTauxCouverture(50.0);

            int result = repo.enregistrerSociete(s);

            assertEquals(1, result);
            assertNotNull(s.getIdSociete());

            // ✅ FIX 1 : méthode réelle du repository
            Optional<Societe> found =
                    repo.trouverParNom("ITCo");

            assertTrue(found.isPresent());
            assertEquals("ITCo", found.get().getNomSociete());

            // ✅ FIX 2 : méthode réelle du repository
            boolean exists =
                    repo.existeParId(s.getIdSociete());

            assertTrue(exists);
        }
    }
}