package hospicloud.servicesImpl;

import hospicloud.dtos.CreateTenantUserRequest;
import hospicloud.dtos.TenantUserResponse;
import hospicloud.dtos.UpdateTenantUserRequest;
import hospicloud.exceptions.BadRequestException;
import hospicloud.exceptions.ConflictException;
import hospicloud.exceptions.ForbiddenException;
import hospicloud.exceptions.ResourceNotFoundException;
import hospicloud.model.Hopital;
import hospicloud.model.Medecin;
import hospicloud.model.Role;
import hospicloud.model.Utilisateur;
import hospicloud.repositories.HopitalRepository;
import hospicloud.repositories.MedecinRepository;
import hospicloud.repositories.UtilisateurRepository;
import hospicloud.security.RoleRequestParser;
import hospicloud.security.TenantContext;
import hospicloud.security.UserPresenceService;
import hospicloud.security.UtilisateurPrincipal;
import hospicloud.services.TenantUserService;
import hospicloud.services.SaasPlanService;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class TenantUserServiceImpl implements TenantUserService {

    private final UtilisateurRepository utilisateurRepository;
    private final HopitalRepository hopitalRepository;
    private final MedecinRepository medecinRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserPresenceService userPresenceService;
    private final SaasPlanService saasPlanService;

    public TenantUserServiceImpl(UtilisateurRepository utilisateurRepository,
                                 HopitalRepository hopitalRepository,
                                 MedecinRepository medecinRepository,
                                 PasswordEncoder passwordEncoder,
                                 UserPresenceService userPresenceService,
                                 SaasPlanService saasPlanService) {
        this.utilisateurRepository = utilisateurRepository;
        this.hopitalRepository = hopitalRepository;
        this.medecinRepository = medecinRepository;
        this.passwordEncoder = passwordEncoder;
        this.userPresenceService = userPresenceService;
        this.saasPlanService = saasPlanService;
    }

    @Override
    public TenantUserResponse createUser(CreateTenantUserRequest request) {
        AdminContext admin = requireTenantAdmin();
        Role role = RoleRequestParser.parseTenantAssignableRole(request.getRole());
        if (role != Role.PATIENT) {
            saasPlanService.assertCanAddStaffUser(admin.hopitalId());
        }

        String email = normalizeEmail(request.getEmail());

        if (utilisateurRepository.existsByEmail(email)) {
            throw new ConflictException("Un utilisateur avec cet email existe déjà");
        }

        Utilisateur utilisateur = new Utilisateur();
        utilisateur.setPrenom(request.getFirstName().trim());
        utilisateur.setNom(request.getLastName().trim());
        utilisateur.setEmail(email);
        utilisateur.setTelephone(request.getTelephone());
        utilisateur.setMotDePasse(passwordEncoder.encode(request.getPassword()));
        utilisateur.setRole(role);
        utilisateur.setIdHopital(admin.hopitalId());
        utilisateur.setEstActif(true);

        if (role == Role.MEDECIN) {
            Integer medecinId = ensureMedecinProfile(
                    admin.hopitalId(),
                    utilisateur.getPrenom(),
                    utilisateur.getNom(),
                    email,
                    utilisateur.getTelephone(),
                    request.getSpecialite()
            );
            utilisateur.setIdMedecin(medecinId);
        }

        Utilisateur saved = utilisateurRepository.insert(utilisateur);
        return toResponse(saved, resolveTenantName(admin.hopitalId()));
    }

    @Override
    @Transactional(readOnly = true)
    public List<TenantUserResponse> listUsers() {
        AdminContext admin = requireTenantAdmin();
        String tenantName = resolveTenantName(admin.hopitalId());

        return utilisateurRepository.findAllByHopitalIdIncludingInactive(admin.hopitalId()).stream()
                .map(user -> toResponse(user, tenantName))
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public TenantUserResponse getUser(Integer id) {
        AdminContext admin = requireTenantAdmin();
        Utilisateur user = findUserInTenantOrThrow(id, admin.hopitalId());
        return toResponse(user, resolveTenantName(admin.hopitalId()));
    }

    @Override
    public TenantUserResponse updateUser(Integer id, UpdateTenantUserRequest request) {
        AdminContext admin = requireTenantAdmin();
        Utilisateur existing = findUserInTenantOrThrow(id, admin.hopitalId());
        Role previousRole = existing.getRole();

        String email = normalizeEmail(request.getEmail());
        if (utilisateurRepository.existsByEmailExcludingId(email, id)) {
            throw new ConflictException("Un utilisateur avec cet email existe déjà");
        }

        existing.setPrenom(request.getFirstName().trim());
        existing.setNom(request.getLastName().trim());
        existing.setEmail(email);
        existing.setTelephone(request.getTelephone());

        if (request.getRole() != null && !request.getRole().isBlank()) {
            Role newRole = RoleRequestParser.parseTenantAssignableRole(request.getRole());
            existing.setRole(newRole);
        }

        if (existing.getRole() == Role.MEDECIN && existing.getIdMedecin() == null) {
            Integer medecinId = ensureMedecinProfile(
                    admin.hopitalId(),
                    existing.getPrenom(),
                    existing.getNom(),
                    existing.getEmail(),
                    existing.getTelephone(),
                    null
            );
            existing.setIdMedecin(medecinId);
        }

        if (previousRole == Role.MEDECIN && existing.getRole() != Role.MEDECIN) {
            // Le compte n'est plus médecin : on retire le lien, le profil medecin reste historique.
            existing.setIdMedecin(null);
        }

        utilisateurRepository.updateProfile(existing);
        return toResponse(existing, resolveTenantName(admin.hopitalId()));
    }

    @Override
    public TenantUserResponse disableUser(Integer id) {
        return setUserActive(id, false);
    }

    @Override
    public TenantUserResponse enableUser(Integer id) {
        return setUserActive(id, true);
    }

    /**
     * Crée (ou réutilise) une fiche dans {@code medecin} pour l'hôpital connecté.
     */
    private Integer ensureMedecinProfile(Integer hopitalId,
                                         String prenom,
                                         String nom,
                                         String email,
                                         String telephone,
                                         String specialite) {
        Integer previousTenant = TenantContext.getHopitalId();
        try {
            TenantContext.setHopitalId(hopitalId);

            return medecinRepository.trouverParEmail(email)
                    .map(Medecin::getIdMedecin)
                    .orElseGet(() -> {
                        Medecin medecin = new Medecin();
                        medecin.setIdHopital(hopitalId);
                        medecin.setPrenom(prenom);
                        medecin.setNom(nom);
                        medecin.setEmail(email);
                        medecin.setTelephonePro(telephone);
                        medecin.setSpecialite(
                                specialite != null && !specialite.isBlank()
                                        ? specialite.trim()
                                        : "Médecine générale"
                        );
                        medecin.setDisponibiliteStatus(true);
                        Integer id = medecinRepository.creerEtRetournerId(medecin);
                        if (id == null) {
                            throw new IllegalStateException("Impossible de créer le profil médecin");
                        }
                        return id;
                    });
        } finally {
            if (previousTenant != null) {
                TenantContext.setHopitalId(previousTenant);
            } else {
                TenantContext.clear();
            }
        }
    }

    private TenantUserResponse setUserActive(Integer id, boolean active) {
        AdminContext admin = requireTenantAdmin();

        if (!active && admin.userId() != null && admin.userId().equals(id)) {
            throw new BadRequestException("Vous ne pouvez pas désactiver votre propre compte");
        }

        Utilisateur existing = findUserInTenantOrThrow(id, admin.hopitalId());

        if (existing.getRole() == Role.TENANT_ADMIN && !active) {
            throw new ForbiddenException("Vous ne pouvez pas désactiver un autre administrateur d'hôpital");
        }

        if (active && existing.getRole() != Role.PATIENT) {
            saasPlanService.assertCanAddStaffUser(admin.hopitalId());
        }

        boolean updated = utilisateurRepository.setActive(id, admin.hopitalId(), active);
        if (!updated) {
            throw new ResourceNotFoundException("Utilisateur introuvable dans votre établissement");
        }

        existing.setEstActif(active);
        return toResponse(existing, resolveTenantName(admin.hopitalId()));
    }

    private Utilisateur findUserInTenantOrThrow(Integer id, Integer hopitalId) {
        return utilisateurRepository.findByIdAndHopitalId(id, hopitalId)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur introuvable dans votre établissement"));
    }

    private AdminContext requireTenantAdmin() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof UtilisateurPrincipal principal)) {
            throw new AccessDeniedException("Authentification requise");
        }

        if (principal.getAppRole() != Role.TENANT_ADMIN) {
            throw new ForbiddenException("Accès réservé aux administrateurs d'hôpital");
        }

        Integer hopitalId = principal.getIdHopital();
        if (hopitalId == null) {
            throw new ForbiddenException("Aucun établissement associé à votre compte");
        }

        return new AdminContext(principal.getIdUtilisateur(), hopitalId);
    }

    private String resolveTenantName(Integer hopitalId) {
        Hopital hopital = hopitalRepository.rechercherhopitalParId(hopitalId.longValue());
        return hopital != null ? hopital.getNom() : null;
    }

    private TenantUserResponse toResponse(Utilisateur user, String tenantName) {
        TenantUserResponse response = new TenantUserResponse();
        response.setId(user.getIdUtilisateur());
        response.setFirstName(user.getPrenom());
        response.setLastName(user.getNom());
        response.setEmail(user.getEmail());
        response.setRole(RoleRequestParser.toApiRole(user.getRole()));
        response.setAccountEnabled(user.isEstActif());
        response.setActive(userPresenceService.isPresent(user.getIdUtilisateur()));
        response.setTenantId(user.getIdHopital());
        response.setTenantName(tenantName);
        response.setTelephone(user.getTelephone());
        userPresenceService.getLastSeen(user.getIdUtilisateur())
                .ifPresent(instant -> response.setLastSeenAt(instant.toString()));
        return response;
    }

    private String normalizeEmail(String email) {
        if (email == null || email.isBlank()) {
            throw new BadRequestException("L'email est obligatoire");
        }
        return email.trim().toLowerCase();
    }

    private record AdminContext(Integer userId, Integer hopitalId) {}
}
