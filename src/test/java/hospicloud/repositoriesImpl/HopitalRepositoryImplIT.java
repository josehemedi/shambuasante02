package hospicloud.repositoriesImpl;

import hospicloud.model.Hopital;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@JdbcTest
@Import(HopitalRepositoryImpl.class)
public class HopitalRepositoryImplIT {
	
	 @Autowired
	    private JdbcTemplate jdbcTemplate;

	    @Autowired
	    private HopitalRepositoryImpl repository;

	    @BeforeEach
	    void setup() {

	        jdbcTemplate.execute("DROP TABLE IF EXISTS hopitaux");

	        jdbcTemplate.execute("""
	            CREATE TABLE hopitaux (
	                id_hopital INT AUTO_INCREMENT PRIMARY KEY,
	                nom VARCHAR(255),
	                adresse VARCHAR(255),
	                telephone VARCHAR(50),
	                email VARCHAR(100),
	                logo_url VARCHAR(255),
	                ville VARCHAR(100),
	                pays VARCHAR(100),
	                type VARCHAR(100),
	                date_creation TIMESTAMP,
	                date_modification TIMESTAMP,
	                est_actif BOOLEAN
	            )
	        """);

	        jdbcTemplate.execute("""
	            CREATE TABLE IF NOT EXISTS societes (
	                id_societe INT AUTO_INCREMENT PRIMARY KEY,
	                hospital_id INT
	            )
	        """);

	        jdbcTemplate.execute("""
	            CREATE TABLE IF NOT EXISTS rendez_vous (
	                id_rdv INT AUTO_INCREMENT PRIMARY KEY,
	                id_hopital INT NOT NULL
	            )
	        """);

	        jdbcTemplate.execute("""
	            CREATE TABLE IF NOT EXISTS antecedents (
	                id_antecedent INT AUTO_INCREMENT PRIMARY KEY,
	                id_hopital INT
	            )
	        """);

	        jdbcTemplate.execute("""
	            CREATE TABLE IF NOT EXISTS patients (
	                id_patient INT AUTO_INCREMENT PRIMARY KEY,
	                id_hopital INT
	            )
	        """);
	    }

	    @Test
	    void enregistrerHopital() {

	        Hopital hopital = new Hopital();
	        hopital.setNom("Clinique Ngaliema");
	        hopital.setVille("Kinshasa");
	        hopital.setPays("RDC");
	        hopital.setEstActif(true);

	        repository.enresgitrerHopital(hopital);

	        assertNotNull(hopital.getIdHopital());

	        Integer count = jdbcTemplate.queryForObject(
	                "SELECT COUNT(*) FROM hopitaux",
	                Integer.class);

	        assertEquals(1, count);
	    }

	    @Test
	    void enregistrerDoublonNeDoitPasInserer() {

	        Hopital h1 = new Hopital();
	        h1.setNom("Hopital General");

	        Hopital h2 = new Hopital();
	        h2.setNom("   HOPITAL GENERAL   ");

	        repository.enresgitrerHopital(h1);
	        repository.enresgitrerHopital(h2);

	        Integer count = jdbcTemplate.queryForObject(
	                "SELECT COUNT(*) FROM hopitaux",
	                Integer.class);

	        assertEquals(1, count);
	    }

	    @Test
	    void rechercherParId() {

	        Hopital hopital = new Hopital();
	        hopital.setNom("CMK");

	        repository.enresgitrerHopital(hopital);

	        Hopital trouve =
	                repository.rechercherhopitalParId(
	                        hopital.getIdHopital().longValue());

	        assertNotNull(trouve);
	        assertEquals("CMK", trouve.getNom());
	    }

	    @Test
	    void rechercherParNom() {

	        Hopital hopital = new Hopital();
	        hopital.setNom("Mon Hopital");

	        repository.enresgitrerHopital(hopital);

	        Hopital trouve =
	                repository.rechercherParNom("Mon Hopital");

	        assertNotNull(trouve);
	        assertEquals("Mon Hopital", trouve.getNom());
	    }

	    @Test
	    void listerTous() {

	        Hopital h1 = new Hopital();
	        h1.setNom("Hopital A");

	        Hopital h2 = new Hopital();
	        h2.setNom("Hopital B");

	        repository.enresgitrerHopital(h1);
	        repository.enresgitrerHopital(h2);

	        List<Hopital> result =
	                repository.listerTous();

	        assertEquals(2, result.size());
	    }

	    @Test
	    void modifierHopital() {

	        Hopital hopital = new Hopital();
	        hopital.setNom("Ancien Nom");

	        repository.enresgitrerHopital(hopital);

	        hopital.setNom("Nouveau Nom");

	        repository.modifier(hopital);

	        String nom =
	                jdbcTemplate.queryForObject(
	                        "SELECT nom FROM hopitaux WHERE id_hopital=?",
	                        String.class,
	                        hopital.getIdHopital());

	        assertEquals("Nouveau Nom", nom);
	    }

	    @Test
	    void supprimerHopital() {

	        Hopital hopital = new Hopital();
	        hopital.setNom("A supprimer");

	        repository.enresgitrerHopital(hopital);

	        Integer id = hopital.getIdHopital();

	        jdbcTemplate.update(
	                "INSERT INTO patients(id_hopital) VALUES (?)",
	                id);

	        jdbcTemplate.update(
	                "INSERT INTO antecedents(id_hopital) VALUES (?)",
	                id);

	        jdbcTemplate.update(
	                "INSERT INTO rendez_vous(id_hopital) VALUES (?)",
	                id);

	        jdbcTemplate.update(
	                "INSERT INTO societes(hospital_id) VALUES (?)",
	                id);

	        repository.supprimer(id);

	        Integer hopitaux =
	                jdbcTemplate.queryForObject(
	                        "SELECT COUNT(*) FROM hopitaux",
	                        Integer.class);

	        Integer patients =
	                jdbcTemplate.queryForObject(
	                        "SELECT COUNT(*) FROM patients",
	                        Integer.class);

	        Integer antecedents =
	                jdbcTemplate.queryForObject(
	                        "SELECT COUNT(*) FROM antecedents",
	                        Integer.class);

	        Integer rdv =
	                jdbcTemplate.queryForObject(
	                        "SELECT COUNT(*) FROM rendez_vous",
	                        Integer.class);

	        Integer societes =
	                jdbcTemplate.queryForObject(
	                        "SELECT COUNT(*) FROM societes",
	                        Integer.class);

	        assertEquals(0, hopitaux);
	        assertEquals(0, patients);
	        assertEquals(0, antecedents);
	        assertEquals(0, rdv);
	        assertEquals(0, societes);
	    }

	    @Test
	    void rechercherIdInexistantRetourneNull() {

	        Hopital hopital =
	                repository.rechercherhopitalParId(999L);

	        assertNull(hopital);
	    }

	    @Test
	    void rechercherNomInexistantRetourneNull() {

	        Hopital hopital =
	                repository.rechercherParNom("Inexistant");

	        assertNull(hopital);
	    }
}