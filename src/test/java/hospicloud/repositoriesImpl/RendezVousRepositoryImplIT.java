package hospicloud.repositoriesImpl;

import hospicloud.events.EventProducer;
import hospicloud.exceptions.rendezvous.RendezVousConflictException;
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

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@JdbcTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(RendezVousRepositoryImpl.class)
class RendezVousRepositoryImplIT {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private RendezVousRepositoryImpl repository;

    @MockBean
    private EventProducer eventProducer;

    @BeforeEach
    void setup() {
        // Initialisation du contexte Multi-Tenant pour le thread de test
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

    // =====================================================
    // CREATION & RECHERCHE
    // =====================================================
    @Test
    void creerEtListerParMedecin() {
        RendezVous rdv = new RendezVous();
        rdv.setIdPatient(10);
        rdv.setIdMedecin(100);
        rdv.setDateHeureRdv(LocalDateTime.now().plusHours(2));
        rdv.setDureeEstimee(30);
        rdv.setMotifVisite("Consultation");

        repository.creer(rdv);

        assertNotNull(rdv.getIdRdv());

        List<RendezVous> result = repository.listerParMedecinEtDate(100, LocalDate.now());

        assertEquals(1, result.size());
        assertEquals("Consultation", result.get(0).getMotifVisite());
    }

    // =====================================================
    // VÉRIFICATION CRÉNEAU
    // =====================================================
    @Test
    void estCreneauLibre() {
        RendezVous rdv = new RendezVous();
        rdv.setIdPatient(10);
        rdv.setIdMedecin(100);
        rdv.setDateHeureRdv(LocalDateTime.of(2030, 1, 1, 10, 0));
        rdv.setDureeEstimee(60);

        repository.creer(rdv);

        LocalDateTime storedDate = jdbcTemplate.queryForObject(
                "SELECT date_heure_rdv FROM rendez_vous01 WHERE id_rdv = ?",
                LocalDateTime.class,
                rdv.getIdRdv());
        boolean libre = repository.estCreneauLibre(100, storedDate);

        assertFalse(libre);
    }

    // =====================================================
    // GESTION DES CONFLITS
    // =====================================================
    @Test
    void creerConflitDoitLeverException() {
        LocalDateTime heure = LocalDateTime.of(2030, 1, 1, 10, 0);

        RendezVous rdv1 = new RendezVous();
        rdv1.setIdPatient(1);
        rdv1.setIdMedecin(10);
        rdv1.setDateHeureRdv(heure);
        rdv1.setDureeEstimee(60);

        repository.creer(rdv1);

        LocalDateTime storedHeure = jdbcTemplate.queryForObject(
                "SELECT date_heure_rdv FROM rendez_vous01 WHERE id_rdv = ?",
                LocalDateTime.class,
                rdv1.getIdRdv());

        RendezVous rdv2 = new RendezVous();
        rdv2.setIdPatient(2);
        rdv2.setIdMedecin(10);
        rdv2.setDateHeureRdv(storedHeure); // Même heure stockée, même médecin -> Conflit !
        rdv2.setDureeEstimee(60);

        assertThrows(
                RendezVousConflictException.class,
                () -> repository.creer(rdv2)
        );
    }

    // =====================================================
    // RÉCUPÉRATION DU JOUR (AVEC FILTRE MÉDECIN EXIGÉ)
    // =====================================================
    @Test
    void listerRendezVousDuJour() {
        RendezVous rdv = new RendezVous();
        rdv.setIdPatient(5);
        rdv.setIdMedecin(10);
        rdv.setDateHeureRdv(LocalDateTime.now().plusHours(1));
        rdv.setDureeEstimee(20);

        repository.creer(rdv);

        // Correction apportée ici pour correspondre à l'évolution de la signature (idMedecin = 10)
        List<RendezVous> result = repository.listerRendezVousDuJourParMedecin(10);

        assertEquals(1, result.size());
    }

    // =====================================================
    // CONFIRMATION DE PRÉSENCE
    // =====================================================
    @Test
    void confirmerPresence() {
        RendezVous rdv = new RendezVous();
        rdv.setIdPatient(5);
        rdv.setIdMedecin(10);
        rdv.setDateHeureRdv(LocalDateTime.now().plusHours(1));
        rdv.setDureeEstimee(20);

        repository.creer(rdv);

        repository.confirmerPresence(rdv.getIdRdv());

        String statut = jdbcTemplate.queryForObject(
                "SELECT statut_rdv FROM rendez_vous01 WHERE id_rdv=?",
                String.class,
                rdv.getIdRdv());

        assertEquals("CONFIRME", statut);
    }

    // =====================================================
    // MODIFICATION DE RENDEZ-VOUS
    // =====================================================
    @Test
    void modifierRendezVous() {
        RendezVous rdv = new RendezVous();
        rdv.setIdPatient(5);
        rdv.setIdMedecin(10);
        rdv.setDateHeureRdv(LocalDateTime.of(2030, 1, 1, 10, 0));
        rdv.setDureeEstimee(20);
        rdv.setMotifVisite("Ancien");
        rdv.setStatutRdv("PROGRAMME");

        repository.creer(rdv);

        // Mise à jour de l'objet managé
        rdv.setMotifVisite("Nouveau");

        repository.modifierRendezVous(rdv);

        String motif = jdbcTemplate.queryForObject(
                "SELECT motif_visite FROM rendez_vous01 WHERE id_rdv=?",
                String.class,
                rdv.getIdRdv());

        assertEquals("Nouveau", motif);
    }

    // =====================================================
    // MARQUAGE ABSENT
    // =====================================================
    @Test
    void marquerCommeAbsent() {
        RendezVous rdv = new RendezVous();
        rdv.setIdPatient(5);
        rdv.setIdMedecin(10);
        rdv.setDateHeureRdv(LocalDateTime.now().plusHours(1));
        rdv.setDureeEstimee(20);

        repository.creer(rdv);

        repository.marquerCommeAbsent(rdv.getIdRdv());

        String statut = jdbcTemplate.queryForObject(
                "SELECT statut_rdv FROM rendez_vous01 WHERE id_rdv=?",
                String.class,
                rdv.getIdRdv());

        assertEquals("ABSENT", statut);
    }

    // =====================================================
    // LISTE VIDE
    // =====================================================
    @Test
    void aucunRendezVousPourMedecin() {
        List<RendezVous> result = repository.listerParMedecinEtDate(999, LocalDate.now());
        assertTrue(result.isEmpty());
    }
}