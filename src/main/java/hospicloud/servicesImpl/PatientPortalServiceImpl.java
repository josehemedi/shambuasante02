package hospicloud.servicesImpl;

import hospicloud.dtos.SupportTicketCreateDTO;
import hospicloud.dtos.SupportTicketDTO;
import hospicloud.dtos.patient.*;
import hospicloud.exceptions.BadRequestException;
import hospicloud.exceptions.ForbiddenException;
import hospicloud.exceptions.ResourceNotFoundException;
import hospicloud.model.*;
import hospicloud.repositories.*;
import hospicloud.security.CurrentUserContext;
import hospicloud.security.CurrentUserService;
import hospicloud.security.TenantAuthorization;
import hospicloud.security.TenantContext;
import hospicloud.services.PatientPortalService;
import hospicloud.services.PatientService;
import hospicloud.services.RendezVousService;
import hospicloud.services.SupportTicketService;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.*;

@Service
public class PatientPortalServiceImpl implements PatientPortalService {

    private final HopitalRepository hopitalRepository;
    private final PatientRepository patientRepository;
    private final UtilisateurRepository utilisateurRepository;
    private final PatientService patientService;
    private final RendezVousService rendezVousService;
    private final RendezVousRepository rendezVousRepository;
    private final FactureRepository factureRepository;
    private final OrdonnanceRepository ordonnanceRepository;
    private final MedecinRepository medecinRepository;
    private final SupportTicketService supportTicketService;
    private final PasswordEncoder passwordEncoder;
    private final CurrentUserService currentUserService;
    private final JdbcTemplate jdbcTemplate;

    public PatientPortalServiceImpl(
            HopitalRepository hopitalRepository,
            PatientRepository patientRepository,
            UtilisateurRepository utilisateurRepository,
            PatientService patientService,
            RendezVousService rendezVousService,
            RendezVousRepository rendezVousRepository,
            FactureRepository factureRepository,
            OrdonnanceRepository ordonnanceRepository,
            MedecinRepository medecinRepository,
            SupportTicketService supportTicketService,
            PasswordEncoder passwordEncoder,
            CurrentUserService currentUserService,
            JdbcTemplate jdbcTemplate) {
        this.hopitalRepository = hopitalRepository;
        this.patientRepository = patientRepository;
        this.utilisateurRepository = utilisateurRepository;
        this.patientService = patientService;
        this.rendezVousService = rendezVousService;
        this.rendezVousRepository = rendezVousRepository;
        this.factureRepository = factureRepository;
        this.ordonnanceRepository = ordonnanceRepository;
        this.medecinRepository = medecinRepository;
        this.supportTicketService = supportTicketService;
        this.passwordEncoder = passwordEncoder;
        this.currentUserService = currentUserService;
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    @Transactional(readOnly = true)
    public List<PublicHospitalDTO> searchHospitals(String query) {
        String q = query != null ? query.trim().toLowerCase(Locale.ROOT) : "";
        List<Hopital> all = hopitalRepository.listerTous();
        List<PublicHospitalDTO> out = new ArrayList<>();
        for (Hopital h : all) {
            if (h == null || !h.isEstActif()) continue;
            String nom = coalesce(h.getNomCommercial(), h.getNom());
            String ville = h.getVille() != null ? h.getVille() : "";
            String pays = h.getPays() != null ? h.getPays() : "";
            String hay = (nom + " " + ville + " " + pays + " " + coalesce(h.getAdresseComplete(), h.getAdresse())).toLowerCase(Locale.ROOT);
            if (!q.isEmpty() && !hay.contains(q)) continue;
            PublicHospitalDTO dto = new PublicHospitalDTO();
            dto.setIdHopital(h.getIdHopital());
            dto.setNom(h.getNom());
            dto.setNomCommercial(nom);
            dto.setVille(h.getVille());
            dto.setPays(h.getPays());
            dto.setAdresse(coalesce(h.getAdresseComplete(), h.getAdresse()));
            dto.setTelephone(h.getTelephone());
            out.add(dto);
        }
        out.sort(Comparator.comparing(d -> d.getNomCommercial() != null ? d.getNomCommercial() : "", String.CASE_INSENSITIVE_ORDER));
        return out;
    }

    @Override
    @Transactional
    public PatientRegistrationResponseDTO register(PatientRegistrationRequestDTO request) {
        if (request.getIdHopital() == null) {
            throw new BadRequestException("Veuillez sélectionner un établissement.");
        }
        Hopital hopital = hopitalRepository.rechercherhopitalParId(request.getIdHopital().longValue());
        if (hopital == null || !hopital.isEstActif()) {
            throw new BadRequestException("Établissement introuvable ou inactif.");
        }

        String email = request.getEmail().trim().toLowerCase(Locale.ROOT);
        if (utilisateurRepository.existsByEmail(email)) {
            throw new BadRequestException("Un compte existe déjà avec cet email.");
        }

        Integer previousTenant = TenantContext.getHopitalId();
        TenantContext.setHopitalId(request.getIdHopital());
        try {
            Patient patient = new Patient();
            patient.setNom(request.getNom().trim());
            patient.setPrenom(request.getPrenom().trim());
            patient.setSexe(normalizeSexe(request.getSexe()));
            patient.setDateNaissance(parseDate(request.getDateNaissance()));
            patient.setEmail(email);
            patient.setTelephone(trimToNull(request.getTelephone()));
            patient.setAdresse(trimToNull(request.getAdresse()));
            patient.setEstActif(true);
            patient.setStatutClinique("AMBULATOIRE");
            patient.setDateEnregistrement(LocalDateTime.now());
            patientService.enregisterPatient(patient);

            if (patient.getIdPatient() == null) {
                throw new BadRequestException("Échec de la création de la fiche patient.");
            }

            Utilisateur user = new Utilisateur();
            user.setIdHopital(request.getIdHopital());
            user.setIdPatient(patient.getIdPatient());
            user.setNom(patient.getNom());
            user.setPrenom(patient.getPrenom());
            user.setEmail(email);
            user.setTelephone(patient.getTelephone());
            user.setMotDePasse(passwordEncoder.encode(request.getPassword()));
            user.setRole(Role.PATIENT);
            user.setEstActif(true);
            user.setDateCreation(LocalDateTime.now());
            utilisateurRepository.insert(user);

            PatientRegistrationResponseDTO response = new PatientRegistrationResponseDTO();
            response.setIdPatient(patient.getIdPatient().intValue());
            response.setCodePatient(patient.getCodePatient());
            response.setIdHopital(request.getIdHopital());
            response.setNomHopital(coalesce(hopital.getNomCommercial(), hopital.getNom()));
            response.setEmail(email);
            response.setMessage("Compte créé dans « " + response.getNomHopital()
                    + " ». Vous pouvez vous connecter avec votre email.");
            return response;
        } finally {
            if (previousTenant != null) {
                TenantContext.setHopitalId(previousTenant);
            } else {
                TenantContext.clear();
            }
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Patient getMyProfile() {
        Integer idPatient = requirePatientId();
        return patientRepository.trouverPatientParId(idPatient.longValue())
                .orElseThrow(() -> new ResourceNotFoundException("Fiche patient introuvable."));
    }

    @Override
    @Transactional
    public Patient updateMyProfile(PatientProfileUpdateDTO request) {
        Patient patient = getMyProfile();
        if (StringUtils.hasText(request.getTelephone())) {
            patient.setTelephone(request.getTelephone().trim());
        }
        if (request.getAdresse() != null) {
            patient.setAdresse(request.getAdresse().trim());
        }
        if (request.getContactUrgence() != null) {
            patient.setContactUrgence(request.getContactUrgence().trim());
        }
        if (request.getProfession() != null) {
            patient.setProfession(request.getProfession().trim());
        }
        if (StringUtils.hasText(request.getEmail())) {
            String email = request.getEmail().trim().toLowerCase(Locale.ROOT);
            patient.setEmail(email);
            Integer userId = currentUserService.getCurrentUtilisateurId();
            if (userId != null) {
                Utilisateur u = utilisateurRepository.findById(userId)
                        .orElse(null);
                if (u != null) {
                    if (utilisateurRepository.existsByEmailExcludingId(email, userId)) {
                        throw new BadRequestException("Cet email est déjà utilisé.");
                    }
                    u.setEmail(email);
                    if (StringUtils.hasText(request.getTelephone())) {
                        u.setTelephone(request.getTelephone().trim());
                    }
                    utilisateurRepository.updateProfile(u);
                }
            }
        } else if (StringUtils.hasText(request.getTelephone())) {
            Integer userId = currentUserService.getCurrentUtilisateurId();
            if (userId != null) {
                utilisateurRepository.findById(userId).ifPresent(u -> {
                    u.setTelephone(request.getTelephone().trim());
                    utilisateurRepository.updateProfile(u);
                });
            }
        }
        patient.setModifiePar(currentUserService.getCurrentUtilisateurId());
        patientRepository.modifierPatient(patient);
        return getMyProfile();
    }

    @Override
    @Transactional(readOnly = true)
    public List<RendezVous> listMyAppointments() {
        return rendezVousRepository.listerParPatient(requirePatientId());
    }

    @Override
    @Transactional
    public RendezVous requestAppointment(PatientAppointmentRequestDTO request) {
        Integer idPatient = requirePatientId();
        LocalDateTime dateHeure = parseDateTime(request.getDateHeureRdv());
        if (dateHeure.isBefore(LocalDateTime.now().plusMinutes(30))) {
            throw new BadRequestException("Le rendez-vous doit être au moins 30 minutes dans le futur.");
        }
        String canal = request.getCanal().trim().toUpperCase(Locale.ROOT);
        if (!"PHYSIQUE".equals(canal) && !"TELECONSULTATION".equals(canal)) {
            throw new BadRequestException("Canal invalide (PHYSIQUE ou TELECONSULTATION).");
        }
        Medecin medecin = medecinRepository.trouverParId(request.getIdMedecin())
                .orElseThrow(() -> new BadRequestException("Médecin introuvable dans votre établissement."));

        if (!rendezVousService.verifierCreneau(medecin.getIdMedecin(), dateHeure)) {
            throw new BadRequestException("Ce créneau n'est plus disponible.");
        }

        RendezVous rdv = new RendezVous();
        rdv.setIdPatient(idPatient);
        rdv.setIdMedecin(medecin.getIdMedecin());
        rdv.setDateHeureRdv(dateHeure);
        rdv.setDureeEstimee(request.getDureeEstimee() != null ? request.getDureeEstimee() : 30);
        rdv.setMotifVisite(request.getMotifVisite().trim());
        rdv.setCanal(canal);
        rdv.setStatutRdv("EN_ATTENTE");
        rdv.setCreePar(currentUserService.getCurrentUtilisateurId());
        return rendezVousService.creerEtPublier(rdv);
    }

    @Override
    @Transactional
    public RendezVous cancelMyAppointment(Integer idRdv, String motif) {
        RendezVous rdv = requireOwnedAppointment(idRdv);
        String statut = rdv.getStatutRdv() != null ? rdv.getStatutRdv().toUpperCase(Locale.ROOT) : "";
        if ("ANNULE".equals(statut) || "TERMINE".equals(statut) || "VALIDE".equals(statut)) {
            throw new BadRequestException("Ce rendez-vous ne peut plus être annulé.");
        }
        if (rdv.getDateHeureRdv() != null && rdv.getDateHeureRdv().isBefore(LocalDateTime.now().plusHours(2))) {
            throw new BadRequestException("Annulation impossible moins de 2 heures avant le rendez-vous.");
        }
        rendezVousRepository.annulerRendezVous(idRdv);
        return rendezVousRepository.trouverParId(idRdv);
    }

    @Override
    @Transactional
    public RendezVous rescheduleMyAppointment(Integer idRdv, String nouvelleDateHeure) {
        RendezVous rdv = requireOwnedAppointment(idRdv);
        String statut = rdv.getStatutRdv() != null ? rdv.getStatutRdv().toUpperCase(Locale.ROOT) : "";
        if ("ANNULE".equals(statut) || "TERMINE".equals(statut)) {
            throw new BadRequestException("Ce rendez-vous ne peut plus être reporté.");
        }
        LocalDateTime nouvelle = parseDateTime(nouvelleDateHeure);
        if (nouvelle.isBefore(LocalDateTime.now().plusMinutes(30))) {
            throw new BadRequestException("La nouvelle date doit être au moins 30 minutes dans le futur.");
        }
        if (!rendezVousRepository.estCreneauLibre(rdv.getIdMedecin(), nouvelle)) {
            throw new BadRequestException("Ce créneau n'est plus disponible.");
        }
        rendezVousRepository.reporterRendezVous(idRdv, nouvelle);
        return rendezVousRepository.trouverParId(idRdv);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Facture> listMyInvoices() {
        return factureRepository.findByIdPatient(requirePatientId());
    }

    @Override
    @Transactional(readOnly = true)
    public List<Ordonnance> listMyPrescriptions() {
        return ordonnanceRepository.listerParPatient(requirePatientId());
    }

    @Override
    @Transactional(readOnly = true)
    public List<Map<String, Object>> listMyLabResults() {
        Integer idPatient = requirePatientId();
        Integer hopitalId = TenantContext.getRequiredHopitalId();
        try {
            return jdbcTemplate.query(
                    """
                    SELECT a.id_analyse,
                           COALESCE(ta.nom_analyse, 'Analyse') AS type_analyse,
                           a.statut, a.resultat_texte AS resultat,
                           a.date_demande, a.date_resultat, a.interpretation, a.valeurs_reference
                    FROM analyses_laboratoire a
                    LEFT JOIN types_analyses ta ON a.id_type_analyse = ta.id_type_analyse AND ta.id_hopital = a.id_hopital
                    WHERE a.id_hopital = ? AND a.id_patient = ?
                      AND UPPER(COALESCE(a.statut, '')) IN ('TERMINE', 'VALIDE', 'VALIDATED', 'COMPLETED')
                    ORDER BY COALESCE(a.date_resultat, a.date_demande) DESC
                    """,
                    (rs, rowNum) -> {
                        Map<String, Object> m = new LinkedHashMap<>();
                        m.put("idAnalyse", rs.getObject("id_analyse"));
                        m.put("typeAnalyse", rs.getString("type_analyse"));
                        m.put("statut", rs.getString("statut"));
                        m.put("resultat", rs.getString("resultat"));
                        m.put("interpretation", rs.getString("interpretation"));
                        m.put("valeursReference", rs.getString("valeurs_reference"));
                        m.put("dateDemande", rs.getTimestamp("date_demande"));
                        m.put("dateResultat", rs.getTimestamp("date_resultat"));
                        return m;
                    },
                    hopitalId, idPatient
            );
        } catch (Exception ex) {
            return List.of();
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<Map<String, Object>> listMyDocuments() {
        Integer idPatient = requirePatientId();
        Integer hopitalId = TenantContext.getRequiredHopitalId();
        List<Map<String, Object>> docs = new ArrayList<>();

        docs.add(Map.of(
                "id", "dossier-pdf",
                "type", "DOSSIER",
                "titre", "Dossier médical (PDF)",
                "downloadPath", "/api/v1/patients/me/dossier/pdf",
                "autorise", true
        ));

        try {
            List<Map<String, Object>> files = jdbcTemplate.query(
                    """
                    SELECT pd.id_document, pd.nom_fichier, pd.type_document, pd.date_envoi, pd.date_upload,
                           pd.contenu_resume, pd.url_fichier
                    FROM patients_documents pd
                    WHERE pd.id_hopital = ? AND pd.id_patient = ?
                      AND COALESCE(pd.partage_patient, 0) = 1
                    ORDER BY COALESCE(pd.date_envoi, pd.date_upload) DESC
                    """,
                    (rs, rowNum) -> {
                        Map<String, Object> m = new LinkedHashMap<>();
                        m.put("id", rs.getObject("id_document"));
                        m.put("type", coalesce(rs.getString("type_document"), "DOCUMENT"));
                        m.put("titre", coalesce(rs.getString("nom_fichier"), "Document"));
                        m.put("resume", rs.getString("contenu_resume"));
                        m.put("downloadPath", rs.getString("url_fichier") != null
                                ? "/api/v1/patients/me/documents/" + rs.getObject("id_document") + "/download"
                                : null);
                        m.put("autorise", true);
                        m.put("dateAjout", rs.getTimestamp("date_envoi") != null
                                ? rs.getTimestamp("date_envoi")
                                : rs.getTimestamp("date_upload"));
                        return m;
                    },
                    hopitalId, idPatient
            );
            docs.addAll(files);
        } catch (Exception ignored) {
            // table optionnelle
        }
        return docs;
    }

    @Override
    @Transactional(readOnly = true)
    public Map<String, Object> downloadMyDocument(Integer idDocument) {
        Integer idPatient = requirePatientId();
        Integer hopitalId = TenantContext.getRequiredHopitalId();
        if (idDocument == null) {
            throw new BadRequestException("Identifiant document manquant.");
        }
        List<Map<String, Object>> rows = jdbcTemplate.query(
                """
                SELECT pd.nom_fichier, pd.url_fichier, pd.type_document
                FROM patients_documents pd
                WHERE pd.id_document = ? AND pd.id_hopital = ? AND pd.id_patient = ?
                  AND COALESCE(pd.partage_patient, 0) = 1
                """,
                (rs, rowNum) -> {
                    Map<String, Object> m = new LinkedHashMap<>();
                    m.put("nomFichier", rs.getString("nom_fichier"));
                    m.put("urlFichier", rs.getString("url_fichier"));
                    m.put("typeDocument", rs.getString("type_document"));
                    return m;
                },
                idDocument, hopitalId, idPatient
        );
        if (rows.isEmpty()) {
            throw new ResourceNotFoundException("Document introuvable ou non partagé avec vous.");
        }
        Map<String, Object> meta = rows.get(0);
        String url = (String) meta.get("urlFichier");
        if (!StringUtils.hasText(url)) {
            throw new ResourceNotFoundException("Fichier non disponible pour ce document.");
        }
        try {
            Path path = Path.of(url);
            if (!Files.isRegularFile(path)) {
                throw new ResourceNotFoundException("Fichier introuvable sur le serveur.");
            }
            byte[] bytes = Files.readAllBytes(path);
            String nom = (String) meta.get("nomFichier");
            if (!StringUtils.hasText(nom)) {
                nom = path.getFileName().toString();
            }
            String contentType = Files.probeContentType(path);
            if (!StringUtils.hasText(contentType)) {
                contentType = "application/octet-stream";
            }
            Map<String, Object> out = new LinkedHashMap<>();
            out.put("bytes", bytes);
            out.put("fileName", nom);
            out.put("contentType", contentType);
            return out;
        } catch (ResourceNotFoundException e) {
            throw e;
        } catch (IOException e) {
            throw new IllegalStateException("Impossible de lire le document partagé.", e);
        }
    }

    @Override
    @Transactional
    public SupportTicketDTO requestAssistance(PatientAssistanceRequestDTO request) {
        requirePatientId();
        SupportTicketCreateDTO ticket = new SupportTicketCreateDTO();
        ticket.setSubject(request.getSubject().trim());
        ticket.setDescription(request.getDescription().trim());
        ticket.setModule("PATIENT_PORTAL");
        ticket.setPriority(StringUtils.hasText(request.getPriority()) ? request.getPriority() : "NORMAL");
        return supportTicketService.create(ticket);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Map<String, Object>> listDoctorsForBooking(String specialite) {
        Integer hopitalId = TenantContext.getRequiredHopitalId();
        List<Medecin> medecins = medecinRepository.listerParHopital(hopitalId);
        String filtre = specialite != null ? specialite.trim().toLowerCase(Locale.ROOT) : "";
        List<Map<String, Object>> out = new ArrayList<>();
        for (Medecin m : medecins) {
            if (m == null) continue;
            String spec = m.getSpecialite() != null ? m.getSpecialite() : "";
            if (!filtre.isEmpty() && !spec.toLowerCase(Locale.ROOT).contains(filtre)) continue;
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("idMedecin", m.getIdMedecin());
            String prenom = m.getPrenom() != null ? m.getPrenom().trim() : "";
            String nom = m.getNom() != null ? m.getNom().trim() : "";
            String nomComplet = (prenom + " " + nom).trim();
            row.put("prenom", prenom);
            row.put("nom", nom);
            row.put("nomComplet", nomComplet.isEmpty() ? "Médecin" : nomComplet);
            row.put("specialite", spec.isEmpty() ? "Médecine générale" : spec);
            row.put("email", m.getEmail());
            row.put("telephone", m.getTelephonePro());
            row.put("disponible", m.getDisponibiliteStatus() == null || Boolean.TRUE.equals(m.getDisponibiliteStatus()));
            String initials = "";
            if (!prenom.isEmpty()) initials += Character.toUpperCase(prenom.charAt(0));
            if (!nom.isEmpty()) initials += Character.toUpperCase(nom.charAt(0));
            row.put("initiales", initials.isEmpty() ? "DR" : initials);
            out.add(row);
        }
        return out;
    }

    private RendezVous requireOwnedAppointment(Integer idRdv) {
        Integer idPatient = requirePatientId();
        RendezVous rdv = rendezVousRepository.trouverParId(idRdv);
        if (rdv == null) {
            throw new ResourceNotFoundException("Rendez-vous introuvable.");
        }
        TenantAuthorization.assertPatientOwns(rdv.getIdPatient());
        if (rdv.getIdPatient() == null || !idPatient.equals(rdv.getIdPatient())) {
            throw new ForbiddenException("Ce rendez-vous ne vous appartient pas.");
        }
        return rdv;
    }

    private Integer requirePatientId() {
        Integer id = CurrentUserContext.getPatientId();
        if (id == null) {
            throw new ForbiddenException("Compte patient non lié à une fiche médicale.");
        }
        return id;
    }

    private static String normalizeSexe(String sexe) {
        if (!StringUtils.hasText(sexe)) {
            throw new BadRequestException("Le sexe est obligatoire.");
        }
        String s = sexe.trim().toUpperCase(Locale.ROOT);
        if (s.startsWith("F")) return "F";
        if (s.startsWith("M")) return "M";
        throw new BadRequestException("Sexe invalide (M ou F).");
    }

    private static LocalDate parseDate(String raw) {
        try {
            return LocalDate.parse(raw.trim());
        } catch (DateTimeParseException ex) {
            throw new BadRequestException("Date de naissance invalide (attendu AAAA-MM-JJ).");
        }
    }

    private static LocalDateTime parseDateTime(String raw) {
        try {
            String v = raw.trim().replace(" ", "T");
            if (v.length() == 16) v = v + ":00";
            return LocalDateTime.parse(v);
        } catch (DateTimeParseException ex) {
            throw new BadRequestException("Date/heure invalide.");
        }
    }

    private static String coalesce(String a, String b) {
        if (StringUtils.hasText(a)) return a;
        return b != null ? b : "";
    }

    private static String trimToNull(String v) {
        if (!StringUtils.hasText(v)) return null;
        return v.trim();
    }
}
