package hospicloud.repositoriesImpl;

import hospicloud.events.EventProducer;
import hospicloud.exceptions.rendezvous.RendezVousNotFoundException;
import hospicloud.model.RendezVous;
import hospicloud.security.TenantContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

@JdbcTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(RendezVousRepositoryImpl.class)
public class RendezVousMultiTenantIT {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private RendezVousRepositoryImpl repository;

    @MockBean
    private EventProducer eventProducer;

    @BeforeEach
    void setup() {
        TenantContext.setHopitalId(1);
        jdbcTemplate.execute("DROP TABLE IF EXISTS rendez_vous01");

        jdbcTemplate.execute("""
            CREATE TABLE rendez_vous01(
                id_rdv INT AUTO_INCREMENT PRIMARY KEY,
                id_hopital INT NOT NULL,
                id_patient INT NOT NULL,
                id_medecin INT NOT NULL,
                date_heure_rdv DATETIME NOT NULL,
                duree_estimee INT DEFAULT 30,
                motif_visite VARCHAR(255),
                canal VARCHAR(20) DEFAULT 'PHYSIQUE',
                statut_rdv VARCHAR(20) DEFAULT 'PROGRAMME',
                date_creation TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                cree_par INT
            )
        """);
    }

    @Test
    void confirmer_n_affecte_pas_les_autres_hopitaux() {
        TenantContext.setHopitalId(1);
        RendezVous rdv1 = new RendezVous();
        rdv1.setIdHopital(1);
        rdv1.setIdPatient(10);
        rdv1.setIdMedecin(100);
        rdv1.setDateHeureRdv(LocalDateTime.now().plusHours(1));
        repository.creer(rdv1);

        TenantContext.setHopitalId(2);
        RendezVous rdv2 = new RendezVous();
        rdv2.setIdHopital(2);
        rdv2.setIdPatient(11);
        rdv2.setIdMedecin(100);
        rdv2.setDateHeureRdv(LocalDateTime.now().plusHours(2));
        repository.creer(rdv2);

        TenantContext.setHopitalId(1);
        repository.confirmerPresence(rdv1.getIdRdv());

        String statut1 = jdbcTemplate.queryForObject("SELECT statut_rdv FROM rendez_vous01 WHERE id_rdv=?", String.class, rdv1.getIdRdv());
        String statut2 = jdbcTemplate.queryForObject("SELECT statut_rdv FROM rendez_vous01 WHERE id_rdv=?", String.class, rdv2.getIdRdv());

        assertEquals("CONFIRME", statut1);
        assertEquals("PROGRAMME", statut2);
    }

    @Test
    void marquer_absent_n_affecte_pas_les_autres_hopitaux() {
        TenantContext.setHopitalId(5);
        RendezVous rdv1 = new RendezVous();
        rdv1.setIdHopital(5);
        rdv1.setIdPatient(20);
        rdv1.setIdMedecin(200);
        rdv1.setDateHeureRdv(LocalDateTime.now().plusHours(1));
        repository.creer(rdv1);

        TenantContext.setHopitalId(6);
        RendezVous rdv2 = new RendezVous();
        rdv2.setIdHopital(6);
        rdv2.setIdPatient(21);
        rdv2.setIdMedecin(200);
        rdv2.setDateHeureRdv(LocalDateTime.now().plusHours(2));
        repository.creer(rdv2);

        TenantContext.setHopitalId(6);
        repository.marquerCommeAbsent(rdv2.getIdRdv());

        String statut1 = jdbcTemplate.queryForObject("SELECT statut_rdv FROM rendez_vous01 WHERE id_rdv=?", String.class, rdv1.getIdRdv());
        String statut2 = jdbcTemplate.queryForObject("SELECT statut_rdv FROM rendez_vous01 WHERE id_rdv=?", String.class, rdv2.getIdRdv());

        assertEquals("PROGRAMME", statut1);
        assertEquals("ABSENT", statut2);
    }

    @Test
    void modifier_avec_mauvais_hopital_doit_lever_exception() {
        TenantContext.setHopitalId(10);
        RendezVous rdv = new RendezVous();
        rdv.setIdHopital(10);
        rdv.setIdPatient(30);
        rdv.setIdMedecin(300);
        rdv.setDateHeureRdv(LocalDateTime.now().plusHours(1));
        rdv.setMotifVisite("Initial");

        repository.creer(rdv);

        // Simule un appel depuis un hôpital différent
        TenantContext.setHopitalId(9999);
        rdv.setMotifVisite("Modifie");

        assertThrows(RendezVousNotFoundException.class, () -> repository.modifierRendezVous(rdv));

        String motif = jdbcTemplate.queryForObject("SELECT motif_visite FROM rendez_vous01 WHERE id_rdv=?", String.class, rdv.getIdRdv());
        assertEquals("Initial", motif);
    }
}
