package hospicloud.servicesImpl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import hospicloud.dtos.MedecinRequest;
import hospicloud.dtos.MedecinResponse;
import hospicloud.dtos.StatistiqueMedecinDTO;
import hospicloud.model.Medecin;
import hospicloud.repositories.MedecinRepository;
import hospicloud.security.TenantContext;
import hospicloud.services.MedecinService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class MedecinServiceImpl implements MedecinService {

    private static final Logger logger = LoggerFactory.getLogger(MedecinServiceImpl.class);
    private final MedecinRepository repository;
    private final JedisPool jedisPool;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public MedecinServiceImpl(MedecinRepository repository,
                              @Autowired(required = false) JedisPool jedisPool) {
        this.repository = repository;
        this.jedisPool = jedisPool;
    }
    
    
    

    // =========================
    // CRÉATION
    // =========================
    @Override
    public void creer(MedecinRequest request) {
        Medecin m = new Medecin();
        m.setNom(request.getNom());
        m.setPrenom(request.getPrenom());
        m.setEmail(request.getEmail());
        m.setSpecialite(request.getSpecialite());
        m.setNumeroOrdre(request.getNumeroOrdre());
        m.setTelephonePro(request.getTelephonePro());
        m.setDisponibiliteStatus(request.getDisponibiliteStatus());

        repository.creer(m);
        invalidateCache();
    }

    // =========================
    // LECTURE AVEC CACHE
    // =========================
    @Override
    public Optional<MedecinResponse> trouverParId(Integer idMedecin) {
        Integer hopitalId = TenantContext.getRequiredHopitalId();
        String key = "medecin:" + idMedecin + ":hopital:" + hopitalId;

        if (jedisPool != null) {
            try (Jedis jedis = jedisPool.getResource()) {
                String cached = jedis.get(key);
                if (cached != null) {
                    return Optional.of(objectMapper.readValue(cached, MedecinResponse.class));
                }
            } catch (Exception e) {
                logger.error("Erreur lecture cache Redis", e);
            }
        }

        Optional<Medecin> medecin = repository.trouverParId(idMedecin);
        if (medecin.isEmpty()) return Optional.empty();

        MedecinResponse response = mapToResponse(medecin.get());
        saveToCache(response);
        return Optional.of(response);
    }

    @Override
    public List<MedecinResponse> listerParHopital() {
        Integer hopitalId = TenantContext.getRequiredHopitalId();
        String key = "medecins:hopital:" + hopitalId;

        if (jedisPool != null) {
            try (Jedis jedis = jedisPool.getResource()) {
                String cached = jedis.get(key);
                if (cached != null) {
                    return objectMapper.readValue(cached, new TypeReference<List<MedecinResponse>>() {});
                }
            } catch (Exception e) {
                logger.error("Erreur lecture liste cache Redis", e);
            }
        }

        List<MedecinResponse> list = repository.listerParHopital(hopitalId)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());

        // Sauvegarde auto
        saveListToCache(key, list);
        return list;
    }

    // =========================
    // MODIFICATION
    // =========================
    @Override
    public MedecinResponse mettreAJour(Integer idMedecin, MedecinRequest request) {
        Medecin m = repository.trouverParId(idMedecin)
                .orElseThrow(() -> new RuntimeException("Médecin introuvable"));

        m.setNom(request.getNom());
        m.setPrenom(request.getPrenom());
        m.setEmail(request.getEmail());
        m.setSpecialite(request.getSpecialite());
        m.setNumeroOrdre(request.getNumeroOrdre());
        m.setTelephonePro(request.getTelephonePro());
        m.setDisponibiliteStatus(request.getDisponibiliteStatus());

        repository.mettreAJour(m);
        invalidateCache();
        return mapToResponse(m);
    }

    @Override
    public void changerDisponibilite(Integer idMedecin, Boolean status) {
        repository.changerDisponibilite(idMedecin, status);
        invalidateCache();
    }

    // =====================================================
    // STATISTIQUES (Délégation au Repository)
    // =====================================================
    @Override
    public StatistiqueMedecinDTO getDashboardStats(Integer medecinId) {
        if (medecinId == null) {
            throw new IllegalArgumentException("L'ID du médecin est requis");
        }
        Integer hopitalId = TenantContext.getRequiredHopitalId();
        return repository.getDashboardStats(medecinId, hopitalId);
    }

    @Override
    public long getNombrePatients(Integer medecinId, Integer hopitalId) {
        return repository.getNombrePatients(medecinId, hopitalId);
    }

    @Override
    public long getConsultationsAujourdhui(Integer medecinId, Integer hopitalId) {
        return repository.getConsultationsAujourdhui(medecinId, hopitalId);
    }

    @Override
    public long getRendezVousAujourdhui(Integer medecinId, Integer hopitalId) {
        return repository.getRendezVousAujourdhui(medecinId, hopitalId);
    }

    @Override
    public long getHospitalisationsEncours(Integer medecinId, Integer hopitalId) {
        return repository.getHospitalisationsEncours(medecinId, hopitalId);
    }

    @Override
    public long getExamensEnAttente(Integer medecinId, Integer hopitalId) {
        return repository.getExamensEnAttente(medecinId, hopitalId);
    }

    @Override
    public long getNotificationsNonLues(Integer medecinId, Integer hopitalId) {
        return repository.getNotificationsNonLues(medecinId, hopitalId);
    }

    // =========================
    // HELPERS PRIVÉS
    // =========================
    private MedecinResponse mapToResponse(Medecin m) {
        return new MedecinResponse(m.getIdMedecin(), m.getNom(), m.getPrenom(), m.getEmail(), 
                                   m.getSpecialite(), m.getNumeroOrdre(), m.getTelephonePro(), m.getDisponibiliteStatus());
    }

    private void saveToCache(MedecinResponse m) {
        if (jedisPool == null) return;
        Integer hopitalId = TenantContext.getRequiredHopitalId();
        String key = "medecin:" + m.getIdMedecin() + ":hopital:" + hopitalId;
        try (Jedis jedis = jedisPool.getResource()) {
            jedis.setex(key, 1800, objectMapper.writeValueAsString(m));
        } catch (Exception e) { logger.error("Erreur écriture cache", e); }
    }

    private void saveListToCache(String key, List<MedecinResponse> list) {
        if (jedisPool == null) return;
        try (Jedis jedis = jedisPool.getResource()) {
            jedis.setex(key, 1800, objectMapper.writeValueAsString(list));
        } catch (Exception e) { logger.error("Erreur écriture liste cache", e); }
    }

    private void invalidateCache() {
        if (jedisPool == null) return;
        Integer hopitalId = TenantContext.getRequiredHopitalId();
        try (Jedis jedis = jedisPool.getResource()) {
            jedis.del("medecins:hopital:" + hopitalId);
        } catch (Exception e) { logger.error("Erreur invalidation cache", e); }
    }
}