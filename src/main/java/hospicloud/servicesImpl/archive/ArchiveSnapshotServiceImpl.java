package hospicloud.servicesImpl.archive;

import com.fasterxml.jackson.databind.ObjectMapper;
import hospicloud.dtos.ConsultationResponseDTO;
import hospicloud.dtos.PatientDossierDTO;
import hospicloud.dtos.archive.ArchiveContenuSnapshotDto;
import hospicloud.exceptions.ForbiddenException;
import hospicloud.model.Antecedent;
import hospicloud.model.BonSortie;
import hospicloud.model.Ordonnance;
import hospicloud.model.Patient;
import hospicloud.model.RendezVous;
import hospicloud.model.archive.ArchiveDossier;
import hospicloud.repositories.BonSortieRepository;
import hospicloud.repositories.OrdonnanceRepository;
import hospicloud.repositories.archive.ArchiveDossierRepository;
import hospicloud.security.TenantAuthorization;
import hospicloud.security.TenantContext;
import hospicloud.services.PatientService;
import hospicloud.services.archive.ArchiveSnapshotService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Service
public class ArchiveSnapshotServiceImpl implements ArchiveSnapshotService {

    private static final Logger log = LoggerFactory.getLogger(ArchiveSnapshotServiceImpl.class);

    private final PatientService patientService;
    private final BonSortieRepository bonSortieRepository;
    private final OrdonnanceRepository ordonnanceRepository;
    private final ArchiveDossierRepository archiveRepository;
    private final ObjectMapper objectMapper;
    private final JdbcTemplate jdbcTemplate;

    public ArchiveSnapshotServiceImpl(PatientService patientService,
                                      BonSortieRepository bonSortieRepository,
                                      OrdonnanceRepository ordonnanceRepository,
                                      ArchiveDossierRepository archiveRepository,
                                      ObjectMapper objectMapper,
                                      JdbcTemplate jdbcTemplate) {
        this.patientService = patientService;
        this.bonSortieRepository = bonSortieRepository;
        this.ordonnanceRepository = ordonnanceRepository;
        this.archiveRepository = archiveRepository;
        this.objectMapper = objectMapper;
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    @Transactional
    public ArchiveContenuSnapshotDto capturerEtPersister(ArchiveDossier archive) {
        if (archive == null || archive.getId() == null || archive.getPatientId() == null) {
            return null;
        }
        assertArchiveTenant(archive);

        ArchiveContenuSnapshotDto snapshot = construire(archive);
        try {
            String json = objectMapper.writeValueAsString(snapshot);
            String nomFige = snapshot.getPatient() != null
                    ? String.valueOf(snapshot.getPatient().getOrDefault("nomComplet",
                    snapshot.getPatient().getOrDefault("nom", "")))
                    : archive.getNomPatient();
            String numeroFige = snapshot.getPatient() != null
                    ? String.valueOf(snapshot.getPatient().getOrDefault("codePatient",
                    "PT-" + archive.getPatientId()))
                    : archive.getNumeroDossier();

            archiveRepository.saveContenuSnapshot(
                    archive.getHopitalId(),
                    archive.getId(),
                    json,
                    snapshot.getCaptureAt(),
                    nomFige,
                    numeroFige);
            return snapshot;
        } catch (Exception e) {
            log.error("Échec snapshot archive {}: {}", archive.getId(), e.getMessage());
            throw new IllegalStateException("Impossible de figer le dossier patient pour l'archivage.", e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public ArchiveContenuSnapshotDto lire(Integer hopitalId, Long archiveId) {
        Integer tenant = TenantContext.getRequiredHopitalId();
        if (!Objects.equals(tenant, hopitalId)) {
            throw new ForbiddenException("Violation multi-tenant à la lecture du snapshot.");
        }
        String json = archiveRepository.findContenuSnapshot(hopitalId, archiveId);
        if (json == null || json.isBlank()) {
            return null;
        }
        try {
            return objectMapper.readValue(json, ArchiveContenuSnapshotDto.class);
        } catch (Exception e) {
            log.warn("Snapshot illisible pour archive {}: {}", archiveId, e.getMessage());
            return null;
        }
    }

    private ArchiveContenuSnapshotDto construire(ArchiveDossier archive) {
        PatientDossierDTO dossier = patientService.obtenirDossierComplet(archive.getPatientId());
        Patient patient = dossier != null ? dossier.getPatient() : null;
        if (patient == null || patient.getIdHopital() == null
                || !Objects.equals(patient.getIdHopital(), archive.getHopitalId())) {
            throw new ForbiddenException("Patient hors périmètre de l'établissement de l'archive.");
        }
        TenantAuthorization.assertSameTenant(patient.getIdHopital());

        Integer patientIdInt = archive.getPatientId().intValue();
        Integer hopitalId = archive.getHopitalId();

        // Isolation stricte : exclure toute ressource hors hôpital (y compris hopital_id NULL).
        List<BonSortie> bons = safeList(() -> bonSortieRepository.findByPatientId(patientIdInt)).stream()
                .filter(b -> Objects.equals(b.getIdHopital(), hopitalId))
                .toList();
        List<Ordonnance> ordo = safeList(() -> ordonnanceRepository.listerParPatient(patientIdInt)).stream()
                .filter(o -> Objects.equals(o.getHospitalId(), hopitalId))
                .toList();
        List<Map<String, Object>> admissions = loadAdmissions(patientIdInt, hopitalId);

        ArchiveContenuSnapshotDto snap = new ArchiveContenuSnapshotDto();
        snap.setVersion("1.1");
        snap.setCaptureAt(LocalDateTime.now());
        snap.setArchiveId(archive.getId());
        snap.setHopitalId(hopitalId);
        snap.setTypeEpisode(archive.getTypeEpisode() != null ? archive.getTypeEpisode().name() : null);
        snap.setEpisodeId(archive.getEpisodeId());
        snap.setPatient(mapPatient(patient));
        snap.setRendezVous(safeStream(dossier.getRendezVous()).map(this::mapRendezVous).toList());
        snap.setConsultations(safeStream(dossier.getConsultations()).map(this::mapConsultation).toList());
        snap.setAntecedents(safeStream(dossier.getAntecedents()).map(this::mapAntecedent).toList());
        snap.setBonsSortie(bons.stream().map(this::mapBonSortie).toList());
        snap.setOrdonnances(ordo.stream().map(this::mapOrdonnance).toList());
        snap.setAdmissions(admissions);
        return snap;
    }

    private List<Map<String, Object>> loadAdmissions(Integer patientId, Integer hopitalId) {
        try {
            return jdbcTemplate.query("""
                    SELECT id_admission, id_patient, id_medecin, id_hopital, temps_arrivee,
                           niveau_priorite, statut, salle, numero_passage
                      FROM admission
                     WHERE id_patient = ? AND id_hopital = ?
                     ORDER BY temps_arrivee DESC
                    """,
                    (rs, i) -> {
                        Map<String, Object> m = new LinkedHashMap<>();
                        m.put("idAdmission", rs.getInt("id_admission"));
                        m.put("idPatient", rs.getInt("id_patient"));
                        m.put("idHopital", rs.getInt("id_hopital"));
                        int med = rs.getInt("id_medecin");
                        m.put("idMedecin", rs.wasNull() ? null : med);
                        var ts = rs.getTimestamp("temps_arrivee");
                        m.put("tempsArrivee", ts != null ? ts.toLocalDateTime() : null);
                        m.put("niveauPriorite", rs.getObject("niveau_priorite"));
                        m.put("statut", rs.getString("statut"));
                        m.put("salle", rs.getString("salle"));
                        m.put("numeroPassage", rs.getObject("numero_passage"));
                        return m;
                    },
                    patientId, hopitalId);
        } catch (Exception e) {
            log.warn("Admissions snapshot ignorées: {}", e.getMessage());
            return new ArrayList<>();
        }
    }

    private void assertArchiveTenant(ArchiveDossier archive) {
        Integer tenant = TenantContext.getRequiredHopitalId();
        if (!Objects.equals(tenant, archive.getHopitalId())) {
            throw new ForbiddenException("Violation multi-tenant : archive hors établissement courant.");
        }
        TenantAuthorization.assertSameTenant(archive.getHopitalId());
    }

    private <T> java.util.stream.Stream<T> safeStream(List<T> list) {
        return list != null ? list.stream() : java.util.stream.Stream.empty();
    }

    private Map<String, Object> mapPatient(Patient p) {
        Map<String, Object> m = new LinkedHashMap<>();
        if (p == null) return m;
        m.put("idPatient", p.getIdPatient());
        m.put("idHopital", p.getIdHopital());
        m.put("codePatient", p.getCodePatient());
        m.put("nom", p.getNom());
        m.put("prenom", p.getPrenom());
        m.put("nomComplet", ((p.getPrenom() != null ? p.getPrenom() + " " : "")
                + (p.getNom() != null ? p.getNom() : "")).trim());
        m.put("sexe", p.getSexe());
        m.put("dateNaissance", p.getDateNaissance());
        m.put("groupeSanguin", p.getGroupeSanguin());
        m.put("adresse", p.getAdresse());
        m.put("telephone", p.getTelephone());
        m.put("email", p.getEmail());
        m.put("profession", p.getProfession());
        m.put("estActif", p.isEstActif());
        m.put("dateEnregistrement", p.getDateEnregistrement());
        m.put("idSociete", p.getIdSociete());
        m.put("numeroMatricule", p.getNumeroMatricule());
        m.put("contactUrgence", p.getContactUrgence());
        m.put("statutClinique", p.getStatutClinique());
        return m;
    }

    private Map<String, Object> mapRendezVous(RendezVous r) {
        Map<String, Object> m = new LinkedHashMap<>();
        if (r == null) return m;
        m.put("idRendezVous", r.getIdRdv());
        m.put("dateHeure", r.getDateHeureRdv());
        m.put("statut", r.getStatutRdv());
        m.put("motif", r.getMotifVisite());
        m.put("idMedecin", r.getIdMedecin());
        m.put("idHopital", r.getIdHopital());
        return m;
    }

    private Map<String, Object> mapConsultation(ConsultationResponseDTO c) {
        Map<String, Object> m = new LinkedHashMap<>();
        if (c == null) return m;
        m.put("idConsultation", c.getIdConsultation());
        m.put("dateConsultation", c.getDateConsultation());
        m.put("motifVisite", c.getMotifVisite());
        m.put("motif", c.getMotifVisite());
        m.put("diagnostic", c.getDiagnostic());
        m.put("observations", c.getObservations());
        m.put("tensionArterielle", c.getTensionArterielle());
        m.put("frequenceCardiaque", c.getFrequenceCardiaque());
        m.put("temperature", c.getTemperature());
        m.put("poids", c.getPoids());
        m.put("taille", c.getTaille());
        m.put("statut", c.getStatut());
        m.put("nomMedecin", c.getNomMedecin());
        m.put("dateSignature", c.getDateSignature());
        m.put("referenceSignature", c.getReferenceSignature());
        m.put("idHopital", c.getIdHopital());
        return m;
    }

    private Map<String, Object> mapAntecedent(Antecedent a) {
        Map<String, Object> m = new LinkedHashMap<>();
        if (a == null) return m;
        m.put("id", a.getIdAntecendent());
        m.put("type", a.getTypeAntecedent());
        m.put("libelle", a.getLibelle());
        m.put("description", a.getDescription());
        m.put("date", a.getDateDiagnostic());
        m.put("critique", a.isEst_critique());
        m.put("statut", a.getStatut() != null ? a.getStatut().name() : null);
        m.put("idHopital", a.getIdHopital());
        return m;
    }

    private Map<String, Object> mapBonSortie(BonSortie b) {
        Map<String, Object> m = new LinkedHashMap<>();
        if (b == null) return m;
        m.put("idBonSortie", b.getIdBonSortie());
        m.put("idHopital", b.getIdHopital());
        m.put("numeroBon", b.getNumeroBon());
        m.put("dateSortie", b.getDateSortie());
        m.put("diagnosticFinal", b.getDiagnosticFinal());
        m.put("etatSortie", b.getEtatSortie());
        m.put("recommandations", b.getRecommandationsPostHospitalisation());
        m.put("statutWorkflow", b.getStatutWorkflow());
        m.put("idConsultation", b.getIdConsultation());
        m.put("idAdmission", b.getIdAdmission());
        return m;
    }

    private Map<String, Object> mapOrdonnance(Ordonnance o) {
        Map<String, Object> m = new LinkedHashMap<>();
        if (o == null) return m;
        m.put("idOrdonnance", o.getIdOrdonnance());
        m.put("idHopital", o.getHospitalId());
        m.put("numeroOrdonnance", o.getNumeroOrdonnance());
        m.put("dateOrdonnance", o.getDatePrescription());
        m.put("diagnostic", o.getDiagnostic());
        m.put("contenuOrdonnance", o.getContenuOrdonnance());
        m.put("observations", o.getObservations());
        m.put("idPatient", o.getIdPatient());
        m.put("idMedecin", o.getIdMedecin());
        m.put("statut", o.getStatut());
        return m;
    }

    private <T> List<T> safeList(java.util.concurrent.Callable<List<T>> supplier) {
        try {
            List<T> list = supplier.call();
            return list != null ? list : new ArrayList<>();
        } catch (Exception e) {
            log.warn("Lecture optionnelle snapshot ignorée: {}", e.getMessage());
            return new ArrayList<>();
        }
    }
}
